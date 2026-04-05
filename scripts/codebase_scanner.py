import os
import json
import re

# Concept Rules: Map keywords/patterns to educational explanations
CONCEPT_RULES = {
    "Hilt / DI": {
        "patterns": ["@Module", "@Provides", "@InstallIn", "@Inject"],
        "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier."
    },
    "Persistence (Room)": {
        "patterns": ["@Dao", "@Entity", "@Database", "@Query", "@Upsert"],
        "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data."
    },
    "Coroutines": {
        "patterns": ["suspend ", "Dispatchers.IO", "viewModelScope.launch", "withContext"],
        "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread)."
    },
    "Flow / StateFlow": {
        "patterns": ["StateFlow", "MutableStateFlow", "asStateFlow()", ".collect", ".onEach"],
        "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it."
    },
    "Compose State": {
        "patterns": ["remember {", "mutableStateOf", "by remember", "collectAsStateWithLifecycle"],
        "explanation": "Jetpack Compose is 'declarative'. 'remember' tells Compose to keep a value across recompositions. 'by' is a Kotlin delegate that makes accessing the state value easier."
    },
    "WorkManager": {
        "patterns": ["Worker(", "CoroutineWorker", "doWork()", "OneTimeWorkRequestBuilder"],
        "explanation": "WorkManager is for background tasks that *must* run even if the app is closed or the device restarts (like daily reminders)."
    },
    "Clean Architecture": {
        "patterns": ["UseCase", "invoke(", "Repository"],
        "explanation": "Clean Architecture separates 'What' the app does (Domain/UseCases) from 'How' it does it (Data/Presentation). UseCases handle a single piece of business logic."
    }
}

def get_layer(path):
    path_lower = path.lower().replace('\\', '/')
    if '/data/' in path_lower: return 'Data'
    if '/domain/' in path_lower: return 'Domain'
    if '/presentation/' in path_lower or '/ui/' in path_lower: return 'Presentation'
    if '/di/' in path_lower: return 'DI'
    if '/worker/' in path_lower: return 'Worker'
    return 'Other'

def annotate_snippet(snippet, concept):
    """Adds educational comments into the code snippet for the visualizer."""
    lines = snippet.split('\n')
    annotated = []
    
    # Example annotation patterns
    mapping = {
        "@Module": "// HILT: This class tells Hilt HOW to provide certain dependencies.",
        "@Provides": "// HILT: This function creates and provides a specific object whenever needed.",
        "@Singleton": "// HILT: Only ONE instance of this object will exist in the whole app.",
        "@Dao": "// ROOM: This interface defines how we talk to the SQLite database.",
        "@Query": "// ROOM: A SQL query to fetch data from the database.",
        "suspend val": "// COROUTINES: This value can only be accessed within a coroutine.",
        "viewModelScope.launch": "// COROUTINES: Starting a background task that lives as long as the screen (ViewModel).",
        "MutableStateFlow": "// FLOW: A stream that 'holds' data and updates the UI when it changes.",
        "remember {": "// COMPOSE: Telling the UI to 'remember' this piece of data during refresh.",
        "by remember": "// COMPOSE: Using property delegation for easier state access.",
        "UseCase": "// CLEAN ARCH: This class handles only ONE specific job or business rule.",
        "doWork()": "// WORKMANAGER: The code that runs in the background starts here."
    }
    
    for line in lines:
        for keyword, comment in mapping.items():
            if keyword in line and "//" not in line:
                annotated.append(f"    {comment}")
        annotated.append(line)
        
    return "\n".join(annotated[:100]) # Limit to 100 lines

def get_file_info(path):
    snippet = ""
    concept = None
    explanation = ""
    
    ext = os.path.splitext(path)[1].lower()
    if ext in ['.kt', '.kts', '.gradle', '.xml']:
        try:
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read()
                
                # Identify Concept
                for c_name, rules in CONCEPT_RULES.items():
                    for pattern in rules["patterns"]:
                        if pattern in content:
                            concept = c_name
                            explanation = rules["explanation"]
                            break
                    if concept: break
                
                # Get and Annotate Snippet
                lines = content.split('\n')
                raw_snippet = "\n".join(lines[:100])
                snippet = annotate_snippet(raw_snippet, concept)
        except Exception as e:
            snippet = f"// Error reading file: {str(e)}"
            
    return snippet, concept, explanation

def path_to_dict(path, base_path):
    name = os.path.basename(path)
    if name in ["__pycache__", ".git", ".gradle", ".idea", "build"]:
        return None
        
    d = {'name': name}
    if os.path.isdir(path):
        d['type'] = 'directory'
        children = []
        for x in sorted(os.listdir(path)):
            child = path_to_dict(os.path.join(path, x), base_path)
            if child:
                children.append(child)
        d['children'] = children
        d['layer'] = get_layer(path)
    else:
        d['type'] = 'file'
        d['size'] = os.path.getsize(path)
        d['layer'] = get_layer(path)
        d['extension'] = os.path.splitext(path)[1]
        
        snippet, concept, explanation = get_file_info(path)
        if snippet:
            d['snippet'] = snippet
            d['concept'] = concept
            d['explanation'] = explanation
            d['is_compose'] = "@Composable" in snippet
            
    return d

def generate_report():
    base_dir = "d:/Projects/Android/PersonalFinanceCompanion"
    src_dir = os.path.join(base_dir, "app/src/main/java/com/sandeep/personalfinancecompanion")
    res_dir = os.path.join(base_dir, "app/src/main/res")
    
    print(f"Generating Learning Dashboard Data...")
    
    structure = {
        "name": "PersonalFinanceCompanion",
        "type": "root",
        "children": []
    }
    
    if os.path.exists(src_dir):
        structure["children"].append(path_to_dict(src_dir, src_dir))
    
    if os.path.exists(res_dir):
        structure["children"].append(path_to_dict(res_dir, res_dir))
        
    output_path = os.path.join(base_dir, "docs/codebase_data.js")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    
    with open(output_path, "w", encoding='utf-8') as f:
        f.write("const codebase_data = ")
        json.dump(structure, f, indent=4)
        f.write(";")
        
    print(f"Success! Learning data generated at {output_path}")

if __name__ == "__main__":
    generate_report()
