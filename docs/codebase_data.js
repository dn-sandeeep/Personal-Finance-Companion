const codebase_data = {
    "name": "PersonalFinanceCompanion",
    "type": "root",
    "children": [
        {
            "name": "personalfinancecompanion",
            "type": "directory",
            "children": [
                {
                    "name": "FinanceApplication.kt",
                    "type": "file",
                    "size": 561,
                    "layer": "Other",
                    "extension": ".kt",
                    "snippet": "package com.sandeep.personalfinancecompanion\n\nimport android.app.Application\nimport androidx.hilt.work.HiltWorkerFactory\nimport androidx.work.Configuration\nimport dagger.hilt.android.HiltAndroidApp\nimport javax.inject.Inject\n\n@HiltAndroidApp\nclass FinanceApplication : Application(), Configuration.Provider {\n    \n    @Inject\n    lateinit var workerFactory: HiltWorkerFactory\n\n    override val workManagerConfiguration: Configuration\n        get() = Configuration.Builder()\n            .setWorkerFactory(workerFactory)\n            .build()\n}\n",
                    "concept": "Hilt / DI",
                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                    "is_compose": false
                },
                {
                    "name": "MainActivity.kt",
                    "type": "file",
                    "size": 9479,
                    "layer": "Other",
                    "extension": ".kt",
                    "snippet": "package com.sandeep.personalfinancecompanion\n\nimport android.os.Bundle\nimport androidx.activity.ComponentActivity\nimport androidx.activity.compose.setContent\nimport androidx.activity.enableEdgeToEdge\nimport androidx.compose.animation.AnimatedVisibility\nimport androidx.compose.animation.slideInVertically\nimport androidx.compose.animation.slideOutVertically\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.automirrored.filled.ArrowBack\nimport androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.Person\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.FloatingActionButton\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.IconButton\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.NavigationBar\nimport androidx.compose.material3.NavigationBarItem\nimport androidx.compose.material3.NavigationBarItemDefaults\nimport androidx.compose.material3.Scaffold\nimport androidx.compose.material3.SnackbarHost\nimport androidx.compose.material3.SnackbarHostState\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.TopAppBar\nimport androidx.compose.material3.TopAppBarDefaults\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.remember\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport androidx.navigation.NavDestination.Companion.hierarchy\nimport androidx.navigation.compose.currentBackStackEntryAsState\nimport com.sandeep.personalfinancecompanion.presentation.navigation.AppNavigation\nimport com.sandeep.personalfinancecompanion.presentation.navigation.Screen\nimport com.sandeep.personalfinancecompanion.presentation.navigation.bottomNavItems\nimport com.sandeep.personalfinancecompanion.ui.theme.PersonalFinanceCompanionTheme\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent\nimport dagger.hilt.android.AndroidEntryPoint\nimport androidx.activity.viewModels\nimport androidx.navigation.compose.rememberNavController\n\n@AndroidEntryPoint\nclass MainActivity : ComponentActivity() {\n    private val mainViewModel: MainViewModel by viewModels()\n\n    override fun onCreate(savedInstanceState: Bundle?) {\n        super.onCreate(savedInstanceState)\n        // Triggering mainViewModel initialization to start observing and scheduling\n        mainViewModel\n        enableEdgeToEdge()\n        setContent {\n            PersonalFinanceCompanionTheme {\n                FinanceApp()\n            }\n        }\n    }\n}\n\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun FinanceApp() {\n    val navController = rememberNavController()\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    val snackbarHostState = remember { SnackbarHostState() }\n    val navBackStackEntry by navController.currentBackStackEntryAsState()\n    val currentRoute = navBackStackEntry?.destination?.route\n\n    // Show bottom bar only on main tabs\n    val showBottomBar = currentRoute in bottomNavItems.map { it.route }\n\n    val topBarTitle = when {\n        currentRoute == Screen.Home.route -> \"Home\"\n        currentRoute == Screen.Transactions.route -> \"History\"\n        currentRoute == Screen.Goals.route -> \"Goals\"\n        currentRoute == Screen.Insights.route -> \"Insights\"\n        currentRoute?.startsWith(\"add_transaction\") == true -> \"Add Transaction\"\n        else -> \"Finance Companion\"\n    }\n\n    val canNavigateBack = currentRoute?.startsWith(\"add_transaction\") == true\n\n    Scaffold(\n        modifier = Modifier.fillMaxSize(),\n        topBar = {\n            TopAppBar(\n                title = {\n                    Text(",
                    "concept": "Compose State",
                    "explanation": "Jetpack Compose is 'declarative'. 'remember' tells Compose to keep a value across recompositions. 'by' is a Kotlin delegate that makes accessing the state value easier.",
                    "is_compose": true
                },
                {
                    "name": "MainViewModel.kt",
                    "type": "file",
                    "size": 1568,
                    "layer": "Other",
                    "extension": ".kt",
                    "snippet": "package com.sandeep.personalfinancecompanion\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport com.sandeep.personalfinancecompanion.util.WorkManagerScheduler\nimport dagger.hilt.android.lifecycle.HiltViewModel\nimport kotlinx.coroutines.flow.collectLatest\nimport kotlinx.coroutines.launch\nimport javax.inject.Inject\n\n@HiltViewModel\nclass MainViewModel @Inject constructor(\n    private val preferencesRepository: UserPreferencesRepository,\n    private val scheduler: WorkManagerScheduler\n) : ViewModel() {\n\n    init {\n        observeReminders()\n    }\n\n    private fun observeReminders() {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.dailyReminderEnabledFlow.collectLatest { enabled ->\n                if (enabled) {\n                    val time = \"20:00\" // For now daily reminder is 8:00 PM\n                    val parts = time.split(\":\").map { it.toInt() }\n                    scheduler.scheduleDailyReminder(parts[0], parts[1])\n                } else {\n                    scheduler.cancelDailyReminder()\n                }\n            }\n        }\n\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.goalRemindersEnabledFlow.collectLatest { enabled ->\n                if (enabled) {\n                    scheduler.scheduleGoalReminders()\n                } else {\n                    scheduler.cancelGoalReminders()\n                }\n            }\n        }\n    }\n}\n",
                    "concept": "Hilt / DI",
                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                    "is_compose": false
                },
                {
                    "name": "app",
                    "type": "directory",
                    "children": [
                        {
                            "name": "src",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "main",
                                    "type": "directory",
                                    "children": [
                                        {
                                            "name": "java",
                                            "type": "directory",
                                            "children": [
                                                {
                                                    "name": "com",
                                                    "type": "directory",
                                                    "children": [
                                                        {
                                                            "name": "sandeep",
                                                            "type": "directory",
                                                            "children": [
                                                                {
                                                                    "name": "personalfinancecompanion",
                                                                    "type": "directory",
                                                                    "children": [
                                                                        {
                                                                            "name": "domain",
                                                                            "type": "directory",
                                                                            "children": [
                                                                                {
                                                                                    "name": "model",
                                                                                    "type": "directory",
                                                                                    "children": [],
                                                                                    "layer": "Domain"
                                                                                }
                                                                            ],
                                                                            "layer": "Other"
                                                                        }
                                                                    ],
                                                                    "layer": "Other"
                                                                }
                                                            ],
                                                            "layer": "Other"
                                                        }
                                                    ],
                                                    "layer": "Other"
                                                }
                                            ],
                                            "layer": "Other"
                                        }
                                    ],
                                    "layer": "Other"
                                }
                            ],
                            "layer": "Other"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "data",
                    "type": "directory",
                    "children": [
                        {
                            "name": "local",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "AppDatabase.kt",
                                    "type": "file",
                                    "size": 800,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.local\n\nimport androidx.room.Database\nimport androidx.room.RoomDatabase\nimport com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao\nimport com.sandeep.personalfinancecompanion.data.local.dao.GoalDao\nimport com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity\n\n@Database(\n    entities = [TransactionEntity::class, GoalEntity::class, GoalContributionEntity::class],\n    version = 3,\n    exportSchema = false\n)\nabstract class AppDatabase : RoomDatabase() {\n\n    abstract val transactionDao: TransactionDao\n    abstract val goalDao: GoalDao\n}\n",
                                    "concept": "Persistence (Room)",
                                    "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                    "is_compose": false
                                },
                                {
                                    "name": "dao",
                                    "type": "directory",
                                    "children": [
                                        {
                                            "name": "GoalDao.kt",
                                            "type": "file",
                                            "size": 1685,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.dao\n\nimport androidx.room.*\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalWithContributions\nimport kotlinx.coroutines.flow.Flow\n\n    // ROOM: This interface defines how we talk to the SQLite database.\n@Dao\ninterface GoalDao {\n\n    @Transaction\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"SELECT * FROM goals\")\n    fun getAllGoals(): Flow<List<GoalWithContributions>>\n\n    @Transaction\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"SELECT * FROM goals WHERE id = :id\")\n    suspend fun getGoalWithContributionsById(id: String): GoalWithContributions?\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertGoal(goal: GoalEntity)\n\n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    suspend fun insertContribution(contribution: GoalContributionEntity)\n\n    @Update\n    suspend fun updateGoal(goal: GoalEntity)\n\n    @Transaction\n    suspend fun addContributionAndUpdateGoal(contribution: GoalContributionEntity, updatedGoal: GoalEntity) {\n        insertContribution(contribution)\n        updateGoal(updatedGoal)\n    }\n\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"DELETE FROM goals WHERE id = :id\")\n    suspend fun deleteGoal(id: String)\n\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"DELETE FROM goal_contributions WHERE id = :contributionId\")\n    suspend fun deleteContribution(contributionId: String)\n\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"UPDATE goals SET targetAmount = targetAmount * :factor, savedAmount = savedAmount * :factor\")\n    suspend fun convertAllGoals(factor: Double)\n\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"UPDATE goal_contributions SET amount = amount * :factor\")\n    suspend fun convertAllContributions(factor: Double)\n}\n",
                                            "concept": "Persistence (Room)",
                                            "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                            "is_compose": false
                                        },
                                        {
                                            "name": "TransactionDao.kt",
                                            "type": "file",
                                            "size": 1044,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.dao\n\nimport androidx.room.Dao\nimport androidx.room.Insert\nimport androidx.room.OnConflictStrategy\nimport androidx.room.Query\nimport androidx.room.Update\nimport com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity\nimport kotlinx.coroutines.flow.Flow\n\n    // ROOM: This interface defines how we talk to the SQLite database.\n@Dao\ninterface TransactionDao {\n    \n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"SELECT * FROM transactions ORDER BY date DESC\")\n    fun getAllTransactions(): Flow<List<TransactionEntity>>\n    \n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"SELECT * FROM transactions WHERE id = :id\")\n    fun getTransactionById(id: String): TransactionEntity?\n    \n    @Insert(onConflict = OnConflictStrategy.REPLACE)\n    fun insertTransaction(transaction: TransactionEntity)\n    \n    @Update\n    fun updateTransaction(transaction: TransactionEntity)\n    \n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"DELETE FROM transactions WHERE id = :id\")\n    fun deleteTransaction(id: String)\n\n    // ROOM: A SQL query to fetch data from the database.\n    @Query(\"UPDATE transactions SET amount = amount * :factor\")\n    suspend fun convertAllTransactions(factor: Double)\n}\n",
                                            "concept": "Persistence (Room)",
                                            "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                            "is_compose": false
                                        }
                                    ],
                                    "layer": "Data"
                                },
                                {
                                    "name": "entity",
                                    "type": "directory",
                                    "children": [
                                        {
                                            "name": "GoalContributionEntity.kt",
                                            "type": "file",
                                            "size": 556,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.entity\n\nimport androidx.room.*\n\n@Entity(\n    tableName = \"goal_contributions\",\n    foreignKeys = [\n        ForeignKey(\n            entity = GoalEntity::class,\n            parentColumns = [\"id\"],\n            childColumns = [\"goalId\"],\n            onDelete = ForeignKey.CASCADE\n        )\n    ],\n    indices = [Index(value = [\"goalId\"])]\n)\ndata class GoalContributionEntity(\n    @PrimaryKey\n    val id: String,\n    val goalId: String,\n    val amount: Double,\n    val date: Long\n)\n",
                                            "concept": "Persistence (Room)",
                                            "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                            "is_compose": false
                                        },
                                        {
                                            "name": "GoalEntity.kt",
                                            "type": "file",
                                            "size": 398,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.entity\n\nimport androidx.room.Entity\nimport androidx.room.PrimaryKey\n\n@Entity(tableName = \"goals\")\ndata class GoalEntity(\n    @PrimaryKey\n    val id: String,\n    val title: String,\n    val targetAmount: Double,\n    val savedAmount: Double,\n    val iconName: String,\n    val colorHex: String,\n    val targetDate: Long? = null\n)\n",
                                            "concept": "Persistence (Room)",
                                            "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                            "is_compose": false
                                        },
                                        {
                                            "name": "GoalWithContributions.kt",
                                            "type": "file",
                                            "size": 344,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.entity\n\nimport androidx.room.Embedded\nimport androidx.room.Relation\n\ndata class GoalWithContributions(\n    @Embedded val goal: GoalEntity,\n    @Relation(\n        parentColumn = \"id\",\n        entityColumn = \"goalId\"\n    )\n    val contributions: List<GoalContributionEntity>\n)\n",
                                            "concept": null,
                                            "explanation": "",
                                            "is_compose": false
                                        },
                                        {
                                            "name": "TransactionEntity.kt",
                                            "type": "file",
                                            "size": 358,
                                            "layer": "Data",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.data.local.entity\n\nimport androidx.room.Entity\nimport androidx.room.PrimaryKey\n\n@Entity(tableName = \"transactions\")\ndata class TransactionEntity(\n    @PrimaryKey\n    val id: String,\n    val amount: Double,\n    val type: String,\n    val category: String,\n    val date: Long,\n    val notes: String\n)\n",
                                            "concept": "Persistence (Room)",
                                            "explanation": "Room is an abstraction over SQLite. It allows you to save data locally on the device so the app works offline. DAOs (Data Access Objects) define how you interact with the data.",
                                            "is_compose": false
                                        }
                                    ],
                                    "layer": "Data"
                                }
                            ],
                            "layer": "Data"
                        },
                        {
                            "name": "mapper",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "GoalMapper.kt",
                                    "type": "file",
                                    "size": 1603,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.mapper\n\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalEntity\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalWithContributions\nimport com.sandeep.personalfinancecompanion.domain.model.Goal\nimport com.sandeep.personalfinancecompanion.domain.model.GoalContribution\nimport java.util.UUID\n\nfun GoalContributionEntity.toDomainModel(): GoalContribution {\n    return GoalContribution(\n        id = id,\n        amount = amount,\n        date = date\n    )\n}\n\nfun GoalContribution.toEntity(goalId: String): GoalContributionEntity {\n    return GoalContributionEntity(\n        id = id.ifEmpty { UUID.randomUUID().toString() },\n        goalId = goalId,\n        amount = amount,\n        date = date\n    )\n}\n\nfun GoalWithContributions.toDomainModel(): Goal {\n    return Goal(\n        id = goal.id,\n        title = goal.title,\n        targetAmount = goal.targetAmount,\n        savedAmount = goal.savedAmount,\n        iconName = goal.iconName,\n        colorHex = goal.colorHex,\n        contributions = contributions.map { it.toDomainModel() },\n        targetDate = goal.targetDate\n    )\n}\n\nfun Goal.toEntity(): GoalEntity {\n    return GoalEntity(\n        id = id.ifEmpty { UUID.randomUUID().toString() },\n        title = title,\n        targetAmount = targetAmount,\n        savedAmount = savedAmount,\n        iconName = iconName,\n        colorHex = colorHex,\n        targetDate = targetDate\n    )\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "TransactionMapper.kt",
                                    "type": "file",
                                    "size": 1088,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.mapper\n\nimport com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\n\nfun TransactionEntity.toDomain(): Transaction {\n    return Transaction(\n        id = id,\n        amount = amount,\n        type = try {\n            TransactionType.valueOf(type)\n        } catch (e: IllegalArgumentException) {\n            TransactionType.EXPENSE\n        },\n        category = try {\n            Category.valueOf(category)\n        } catch (e: IllegalArgumentException) {\n            Category.OTHER\n        },\n        date = date,\n        notes = notes\n    )\n}\n\nfun Transaction.toEntity(): TransactionEntity {\n    return TransactionEntity(\n        id = id,\n        amount = amount,\n        type = type.name,\n        category = category.name,\n        date = date,\n        notes = notes\n    )\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Data"
                        },
                        {
                            "name": "repository",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "GoalRepositoryImpl.kt",
                                    "type": "file",
                                    "size": 2735,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.repository\n\nimport com.sandeep.personalfinancecompanion.data.local.dao.GoalDao\nimport com.sandeep.personalfinancecompanion.data.local.entity.GoalContributionEntity\nimport com.sandeep.personalfinancecompanion.data.mapper.toDomainModel\nimport com.sandeep.personalfinancecompanion.data.mapper.toEntity\nimport com.sandeep.personalfinancecompanion.domain.model.Goal\nimport com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\nimport kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.map\nimport java.util.UUID\n\nclass GoalRepositoryImpl(\n    private val dao: GoalDao\n) : GoalRepository {\n\n    override fun getAllGoals(): Flow<List<Goal>> {\n        return dao.getAllGoals().map { list ->\n            list.map { it.toDomainModel() }\n        }\n    }\n\n    override suspend fun insertGoal(goal: Goal) {\n        dao.insertGoal(goal.toEntity())\n    }\n\n    override suspend fun addContribution(goalId: String, amount: Double) {\n        val goalWithContributions = dao.getGoalWithContributionsById(goalId) \n        if (goalWithContributions != null) {\n            val contribution = GoalContributionEntity(\n                id = UUID.randomUUID().toString(),\n                goalId = goalId,\n                amount = amount,\n                date = System.currentTimeMillis()\n            )\n            val updatedGoal = goalWithContributions.goal.copy(\n                savedAmount = goalWithContributions.goal.savedAmount + amount\n            )\n            dao.addContributionAndUpdateGoal(contribution, updatedGoal)\n        }\n    }\n\n    override suspend fun updateGoalTargetDate(goalId: String, targetDate: Long?) {\n        val goalWithContributions = dao.getGoalWithContributionsById(goalId)\n        if (goalWithContributions != null) {\n            val updatedGoal = goalWithContributions.goal.copy(\n                targetDate = targetDate\n            )\n            dao.updateGoal(updatedGoal)\n        }\n    }\n\n    override suspend fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?) {\n        val goalWithContributions = dao.getGoalWithContributionsById(goalId)\n        if (goalWithContributions != null) {\n            val updatedGoal = goalWithContributions.goal.copy(\n                targetAmount = targetAmount,\n                targetDate = targetDate\n            )\n            dao.updateGoal(updatedGoal)\n        }\n    }\n\n    override suspend fun deleteGoal(id: String) {\n        dao.deleteGoal(id)\n    }\n\n    override suspend fun convertAllGoalsAndContributions(factor: Double) {\n        dao.convertAllGoals(factor)\n        dao.convertAllContributions(factor)\n    }\n}\n",
                                    "concept": "Coroutines",
                                    "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                                    "is_compose": false
                                },
                                {
                                    "name": "TransactionRepositoryImpl.kt",
                                    "type": "file",
                                    "size": 1860,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.repository\n\nimport com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao\nimport com.sandeep.personalfinancecompanion.data.mapper.toDomain\nimport com.sandeep.personalfinancecompanion.data.mapper.toEntity\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.map\nimport kotlinx.coroutines.withContext\nimport javax.inject.Inject\nimport javax.inject.Singleton\n\n    // HILT: Only ONE instance of this object will exist in the whole app.\n@Singleton\nclass TransactionRepositoryImpl @Inject constructor(\n    private val dao: TransactionDao\n) : TransactionRepository {\n\n    override fun getAllTransactions(): Flow<List<Transaction>> {\n        return dao.getAllTransactions().map { entities ->\n            entities.map { it.toDomain() }\n        }\n    }\n\n    override suspend fun getTransactionById(id: String): Transaction? {\n        return withContext(Dispatchers.IO) {\n            dao.getTransactionById(id)?.toDomain()\n        }\n    }\n\n    override suspend fun addTransaction(transaction: Transaction) {\n        withContext(Dispatchers.IO) {\n            dao.insertTransaction(transaction.toEntity())\n        }\n    }\n\n    override suspend fun updateTransaction(transaction: Transaction) {\n        withContext(Dispatchers.IO) {\n            dao.updateTransaction(transaction.toEntity())\n        }\n    }\n\n    override suspend fun deleteTransaction(id: String) {\n        withContext(Dispatchers.IO) {\n            dao.deleteTransaction(id)\n        }\n    }\n\n    override suspend fun convertAllTransactions(factor: Double) {\n        withContext(Dispatchers.IO) {\n            dao.convertAllTransactions(factor)\n        }\n    }\n}\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                },
                                {
                                    "name": "UserPreferencesRepositoryImpl.kt",
                                    "type": "file",
                                    "size": 4140,
                                    "layer": "Data",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.data.repository\n\nimport androidx.datastore.core.DataStore\nimport androidx.datastore.preferences.core.Preferences\nimport androidx.datastore.preferences.core.booleanPreferencesKey\nimport androidx.datastore.preferences.core.doublePreferencesKey\nimport androidx.datastore.preferences.core.edit\nimport androidx.datastore.preferences.core.intPreferencesKey\nimport androidx.datastore.preferences.core.stringPreferencesKey\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.map\nimport javax.inject.Inject\nimport javax.inject.Singleton\n\n    // HILT: Only ONE instance of this object will exist in the whole app.\n@Singleton\nclass UserPreferencesRepositoryImpl @Inject constructor(\n    private val dataStore: DataStore<Preferences>\n) : UserPreferencesRepository {\n\n    private object PreferencesKeys {\n        val BUDGET_LIMIT = doublePreferencesKey(\"budget_limit\")\n        val CURRENCY_CODE = stringPreferencesKey(\"currency_code\")\n        val DAILY_REMINDER_ENABLED = booleanPreferencesKey(\"daily_reminder_enabled\")\n        val REMINDER_TIME = stringPreferencesKey(\"reminder_time\")\n        val BUDGET_ALERTS_ENABLED = booleanPreferencesKey(\"budget_alerts_enabled\")\n        val GOAL_REMINDERS_ENABLED = booleanPreferencesKey(\"goal_reminders_enabled\")\n        val NO_SPEND_TARGET = intPreferencesKey(\"no_spend_target\")\n    }\n\n    override val budgetLimitFlow: Flow<Double> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.BUDGET_LIMIT] ?: 0.0\n        }\n\n    override val currencyFlow: Flow<Currency> = dataStore.data\n        .map { preferences ->\n            Currency.fromCode(preferences[PreferencesKeys.CURRENCY_CODE])\n        }\n\n    override val dailyReminderEnabledFlow: Flow<Boolean> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] ?: false\n        }\n\n    override val reminderTimeFlow: Flow<String> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.REMINDER_TIME] ?: \"20:00\"\n        }\n\n    override val budgetAlertsEnabledFlow: Flow<Boolean> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] ?: true\n        }\n\n    override val goalRemindersEnabledFlow: Flow<Boolean> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.GOAL_REMINDERS_ENABLED] ?: true\n        }\n\n    override val noSpendTargetFlow: Flow<Int> = dataStore.data\n        .map { preferences ->\n            preferences[PreferencesKeys.NO_SPEND_TARGET] ?: 30\n        }\n\n    override suspend fun updateBudgetLimit(limit: Double) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.BUDGET_LIMIT] = limit\n        }\n    }\n\n    override suspend fun updateCurrency(currency: Currency) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.CURRENCY_CODE] = currency.code\n        }\n    }\n\n    override suspend fun updateDailyReminderEnabled(enabled: Boolean) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.DAILY_REMINDER_ENABLED] = enabled\n        }\n    }\n\n    override suspend fun updateReminderTime(time: String) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.REMINDER_TIME] = time\n        }\n    }\n\n    override suspend fun updateBudgetAlertsEnabled(enabled: Boolean) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.BUDGET_ALERTS_ENABLED] = enabled\n        }\n    }\n\n    override suspend fun updateGoalRemindersEnabled(enabled: Boolean) {\n        dataStore.edit { preferences ->\n            preferences[PreferencesKeys.GOAL_REMINDERS_ENABLED] = enabled",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Data"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "di",
                    "type": "directory",
                    "children": [
                        {
                            "name": "AppModule.kt",
                            "type": "file",
                            "size": 2699,
                            "layer": "DI",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.di\n\nimport android.app.Application\nimport android.content.Context\nimport androidx.datastore.core.DataStore\nimport androidx.datastore.preferences.core.Preferences\nimport androidx.datastore.preferences.preferencesDataStore\nimport androidx.room.Room\nimport com.sandeep.personalfinancecompanion.data.local.AppDatabase\nimport com.sandeep.personalfinancecompanion.data.local.dao.TransactionDao\nimport com.sandeep.personalfinancecompanion.data.local.dao.GoalDao\nimport com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl\nimport com.sandeep.personalfinancecompanion.data.repository.GoalRepositoryImpl\nimport com.sandeep.personalfinancecompanion.data.repository.UserPreferencesRepositoryImpl\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport dagger.Module\nimport dagger.Provides\nimport dagger.hilt.InstallIn\nimport dagger.hilt.components.SingletonComponent\nimport javax.inject.Provider\nimport javax.inject.Singleton\n\n    // HILT: This class tells Hilt HOW to provide certain dependencies.\n@Module\n@InstallIn(SingletonComponent::class)\nobject AppModule {\n\n    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = \"user_settings\")\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideAppDatabase(\n        app: Application\n    ): AppDatabase {\n        return Room.databaseBuilder(\n            app,\n            AppDatabase::class.java,\n            \"finance_db\"\n        )\n        .fallbackToDestructiveMigration()\n        .build()\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideTransactionDao(appDatabase: AppDatabase): TransactionDao {\n        return appDatabase.transactionDao\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideGoalDao(appDatabase: AppDatabase): GoalDao {\n        return appDatabase.goalDao\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideTransactionRepository(\n        dao: TransactionDao\n    ): TransactionRepository {\n        return TransactionRepositoryImpl(dao)\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideGoalRepository(\n        dao: GoalDao\n    ): GoalRepository {\n        return GoalRepositoryImpl(dao)\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideDataStore(app: Application): DataStore<Preferences> {\n        return app.dataStore\n    }\n\n    // HILT: This function creates and provides a specific object whenever needed.\n    @Provides\n    // HILT: Only ONE instance of this object will exist in the whole app.\n    @Singleton\n    fun provideUserPreferencesRepository(\n        dataStore: DataStore<Preferences>\n    ): UserPreferencesRepository {\n        return UserPreferencesRepositoryImpl(dataStore)\n    }",
                            "concept": "Hilt / DI",
                            "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "domain",
                    "type": "directory",
                    "children": [
                        {
                            "name": "model",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "Category.kt",
                                    "type": "file",
                                    "size": 809,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\nenum class Category(val displayName: String, val emoji: String) {\n    FOOD(\"Food\", \"\ud83c\udf54\"),\n    TRANSPORT(\"Transport\", \"\ud83d\ude97\"),\n    SHOPPING(\"Shopping\", \"\ud83d\udecd\ufe0f\"),\n    ENTERTAINMENT(\"Entertainment\", \"\ud83c\udfac\"),\n    BILLS(\"Bills\", \"\ud83d\udcc4\"),\n    HEALTH(\"Health\", \"\ud83d\udc8a\"),\n    EDUCATION(\"Education\", \"\ud83d\udcda\"),\n    SALARY(\"Salary\", \"\ud83d\udcb0\"),\n    FREELANCE(\"Freelance\", \"\ud83d\udcbc\"),\n    INVESTMENT(\"Investment\", \"\ud83d\udcc8\"),\n    GIFT(\"Gift\", \"\ud83c\udf81\"),\n    OTHER(\"Other\", \"\ud83d\udccc\");\n\n    companion object {\n        fun expenseCategories(): List<Category> =\n            listOf(FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTH, EDUCATION, OTHER)\n\n        fun incomeCategories(): List<Category> =\n            listOf(SALARY, FREELANCE, INVESTMENT, GIFT, OTHER)\n    }\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "Currency.kt",
                                    "type": "file",
                                    "size": 1173,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\nenum class Currency(\n    val code: String,\n    val symbol: String,\n    val label: String,\n    val flag: String,\n    val rateInINR: Double \n) {\n    INR(\"INR\", \"\u20b9\", \"Indian Rupee\", \"\ud83c\uddee\ud83c\uddf3\", 1.0),\n    USD(\"USD\", \"$\", \"US Dollar\", \"\ud83c\uddfa\ud83c\uddf8\", 91.0),\n    EUR(\"EUR\", \"\u20ac\", \"Euro\", \"\ud83c\uddea\ud83c\uddfa\", 105.0),\n    GBP(\"GBP\", \"\u00a3\", \"British Pound\", \"\ud83c\uddec\ud83c\udde7\", 120.0),\n    JPY(\"JPY\", \"\u00a5\", \"Japanese Yen\", \"\ud83c\uddef\ud83c\uddf5\", 0.55),\n    AUD(\"AUD\", \"A$\", \"Australian Dollar\", \"\ud83c\udde6\ud83c\uddfa\", 60.0),\n    CAD(\"CAD\", \"C$\", \"Canadian Dollar\", \"\ud83c\udde8\ud83c\udde6\", 61.0),\n    CHF(\"CHF\", \"Fr\", \"Swiss Franc\", \"\ud83c\udde8\ud83c\udded\", 110.0),\n    CNY(\"CNY\", \"\u00a5\", \"Chinese Yuan\", \"\ud83c\udde8\ud83c\uddf3\", 11.5),\n    AED(\"AED\", \"\u062f.\u0625\", \"UAE Dirham\", \"\ud83c\udde6\ud83c\uddea\", 22.6);\n\n    companion object {\n        fun fromCode(code: String?): Currency {\n            return entries.find { it.code == code } ?: INR\n        }\n\n        fun convert(amount: Double, from: Currency, to: Currency): Double {\n            if (from == to) return amount\n            val amountInINR = amount * from.rateInINR\n            return amountInINR / to.rateInINR\n        }\n    }\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "Goal.kt",
                                    "type": "file",
                                    "size": 798,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\ndata class Goal(\n    val id: String,\n    val title: String,\n    val targetAmount: Double,\n    val savedAmount: Double,\n    val iconName: String,\n    val colorHex: String,\n    val contributions: List<GoalContribution>,\n    val targetDate: Long? = null\n) {\n    val progress: Float\n        get() = if (targetAmount > 0) (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f\n\n    val isOverdue: Boolean\n        get() = targetDate != null && System.currentTimeMillis() > targetDate && savedAmount < targetAmount\n\n    val daysRemaining: Int?\n        get() = targetDate?.let {\n            val diff = it - System.currentTimeMillis()\n            (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)\n        }\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "GoalContribution.kt",
                                    "type": "file",
                                    "size": 188,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\ndata class GoalContribution(\n    val id: String,\n    val amount: Double,\n    val date: Long // Unix timestamp in millis\n)\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "NoSpendStreak.kt",
                                    "type": "file",
                                    "size": 411,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\ndata class NoSpendStreak(\n    val currentStreak: Int,\n    val message: String,\n    val isHealthy: Boolean = true,\n    val targetDays: Int = 30,\n    val bestStreak: Int = 0,\n    val potentialSavings: Double = 0.0,\n    val isCompleted: Boolean = false,\n    val hasSpentToday: Boolean = false,\n    val noSpendDays: List<Long> = emptyList()\n)\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "Transaction.kt",
                                    "type": "file",
                                    "size": 258,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\ndata class Transaction(\n    val id: String,\n    val amount: Double,\n    val type: TransactionType,\n    val category: Category,\n    val date: Long, // Unix timestamp in millis\n    val notes: String\n)\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "TransactionType.kt",
                                    "type": "file",
                                    "size": 114,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.model\n\nenum class TransactionType {\n    INCOME,\n    EXPENSE\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Domain"
                        },
                        {
                            "name": "repository",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "GoalRepository.kt",
                                    "type": "file",
                                    "size": 691,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.repository\n\nimport com.sandeep.personalfinancecompanion.domain.model.Goal\nimport com.sandeep.personalfinancecompanion.domain.model.GoalContribution\nimport kotlinx.coroutines.flow.Flow\n\ninterface GoalRepository {\n    fun getAllGoals(): Flow<List<Goal>>\n    suspend fun insertGoal(goal: Goal)\n    suspend fun addContribution(goalId: String, amount: Double)\n    suspend fun updateGoalTargetDate(goalId: String, targetDate: Long?)\n    suspend fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?)\n    suspend fun deleteGoal(id: String)\n    suspend fun convertAllGoalsAndContributions(factor: Double)\n}\n",
                                    "concept": "Coroutines",
                                    "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                                    "is_compose": false
                                },
                                {
                                    "name": "TransactionRepository.kt",
                                    "type": "file",
                                    "size": 552,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.repository\n\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport kotlinx.coroutines.flow.Flow\n\ninterface TransactionRepository {\n    fun getAllTransactions(): Flow<List<Transaction>>\n    suspend fun getTransactionById(id: String): Transaction?\n    suspend fun addTransaction(transaction: Transaction)\n    suspend fun updateTransaction(transaction: Transaction)\n    suspend fun deleteTransaction(id: String)\n    suspend fun convertAllTransactions(factor: Double)\n}\n",
                                    "concept": "Coroutines",
                                    "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                                    "is_compose": false
                                },
                                {
                                    "name": "UserPreferencesRepository.kt",
                                    "type": "file",
                                    "size": 902,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.repository\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport kotlinx.coroutines.flow.Flow\n\ninterface UserPreferencesRepository {\n    val budgetLimitFlow: Flow<Double>\n    val currencyFlow: Flow<Currency>\n    val dailyReminderEnabledFlow: Flow<Boolean>\n    val reminderTimeFlow: Flow<String>\n    val budgetAlertsEnabledFlow: Flow<Boolean>\n    val goalRemindersEnabledFlow: Flow<Boolean>\n    val noSpendTargetFlow: Flow<Int>\n\n    suspend fun updateBudgetLimit(limit: Double)\n    suspend fun updateCurrency(currency: Currency)\n    suspend fun updateDailyReminderEnabled(enabled: Boolean)\n    suspend fun updateReminderTime(time: String)\n    suspend fun updateBudgetAlertsEnabled(enabled: Boolean)\n    suspend fun updateGoalRemindersEnabled(enabled: Boolean)\n    suspend fun updateNoSpendTarget(days: Int)\n}\n",
                                    "concept": "Coroutines",
                                    "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Domain"
                        },
                        {
                            "name": "usecase",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "CalculateBalanceUseCase.kt",
                                    "type": "file",
                                    "size": 930,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.usecase\n\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport javax.inject.Inject\n\ndata class BalanceSummary(\n    val totalIncome: Double,\n    val totalExpense: Double,\n    val currentBalance: Double\n)\n\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nclass CalculateBalanceUseCase @Inject constructor() {\n\n    operator fun invoke(transactions: List<Transaction>): BalanceSummary {\n        val totalIncome = transactions\n            .filter { it.type == TransactionType.INCOME }\n            .sumOf { it.amount }\n\n        val totalExpense = transactions\n            .filter { it.type == TransactionType.EXPENSE }\n            .sumOf { it.amount }\n\n        return BalanceSummary(\n            totalIncome = totalIncome,\n            totalExpense = totalExpense,\n            currentBalance = totalIncome - totalExpense\n        )\n    }\n}\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                },
                                {
                                    "name": "ChangeCurrencyUseCase.kt",
                                    "type": "file",
                                    "size": 1444,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.usecase\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport kotlinx.coroutines.flow.first\nimport javax.inject.Inject\n\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nclass ChangeCurrencyUseCase @Inject constructor(\n    private val preferencesRepository: UserPreferencesRepository,\n    private val transactionRepository: TransactionRepository,\n    private val goalRepository: GoalRepository\n) {\n    suspend operator fun invoke(newCurrency: Currency) {\n        val oldCurrency = preferencesRepository.currencyFlow.first()\n        if (oldCurrency == newCurrency) return\n\n        val factor = Currency.convert(1.0, oldCurrency, newCurrency)\n\n        // 1. Update global currency\n        preferencesRepository.updateCurrency(newCurrency)\n\n        // 2. Update budget limit\n        val currentBudget = preferencesRepository.budgetLimitFlow.first()\n        preferencesRepository.updateBudgetLimit(currentBudget * factor)\n\n        // 3. Update all transactions\n        transactionRepository.convertAllTransactions(factor)\n\n        // 4. Update all goals and contributions\n        goalRepository.convertAllGoalsAndContributions(factor)\n    }\n}\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                },
                                {
                                    "name": "GetNoSpendStreakUseCase.kt",
                                    "type": "file",
                                    "size": 5860,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.usecase\n\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport kotlinx.coroutines.flow.Flow\nimport kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.map\nimport java.util.Calendar\nimport javax.inject.Inject\n\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nclass GetNoSpendStreakUseCase @Inject constructor(\n    private val repository: TransactionRepository,\n    private val preferencesRepository: UserPreferencesRepository\n) {\n    private val NON_ESSENTIAL_CATEGORIES = listOf(\n        Category.FOOD,\n        Category.SHOPPING,\n        Category.ENTERTAINMENT,\n        Category.GIFT,\n        Category.OTHER\n    )\n\n    operator fun invoke(): Flow<NoSpendStreak> {\n        return combine(\n            repository.getAllTransactions(),\n            preferencesRepository.noSpendTargetFlow,\n            preferencesRepository.currencyFlow\n        ) { transactions, target, currency ->\n            val nonEssentialTransactions = transactions\n                .filter { it.type == TransactionType.EXPENSE }\n                .filter { it.category in NON_ESSENTIAL_CATEGORIES }\n\n            val nonEssentialSpendDates = nonEssentialTransactions\n                .map { getStartOfDay(it.date) }\n                .toSet()\n\n            val today = getStartOfDay(System.currentTimeMillis())\n            val oneDayMillis = 24 * 60 * 60 * 1000L\n\n            // 1. Calculate Current Streak\n            var currentStreak = 0\n            val hasSpentToday = today in nonEssentialSpendDates\n            \n            if (!hasSpentToday) {\n                var checkDay = today - oneDayMillis\n                while (checkDay !in nonEssentialSpendDates && currentStreak < 365) {\n                    currentStreak++\n                    checkDay -= oneDayMillis\n                }\n            }\n\n            // 2. Calculate Best Streak\n            var bestStreak = currentStreak\n            if (nonEssentialSpendDates.isNotEmpty()) {\n                val sortedDates = nonEssentialSpendDates.sortedDescending()\n                \n                val allDates = transactions.map { getStartOfDay(it.date) }.distinct().sorted()\n                if (allDates.isNotEmpty()) {\n                    var currentMax = 0\n                    var runningStreak = 0\n                    var datePtr = allDates.first()\n                    val lastPossibleDate = if (hasSpentToday) today - oneDayMillis else today\n                    \n                    while (datePtr <= lastPossibleDate) {\n                        if (datePtr !in nonEssentialSpendDates) {\n                            runningStreak++\n                        } else {\n                            currentMax = maxOf(currentMax, runningStreak)\n                            runningStreak = 0\n                        }\n                        datePtr += oneDayMillis\n                    }\n                    bestStreak = maxOf(currentMax, runningStreak, currentStreak)\n                }\n            }\n\n            // 3. Potential Savings (Estimate)\n            // Avg non-essential spend per day when spending occurs\n            val totalNonEssentialAmount = nonEssentialTransactions.sumOf { it.amount }\n            val totalDaysWithNonEssentialSpend = nonEssentialSpendDates.size.toDouble()\n            val avgSpendPerDay = if (totalDaysWithNonEssentialSpend > 0) {\n                totalNonEssentialAmount / totalDaysWithNonEssentialSpend\n            } else {\n                // Treated as INR 500 fallback and converted\n                val fallbackInINR = 500.0\n                com.sandeep.personalfinancecompanion.domain.model.Currency.convert(\n                    fallbackInINR,\n                    com.sandeep.personalfinancecompanion.domain.model.Currency.INR,\n                    currency\n                )\n            }\n\n            val potentialSavings = currentStreak * avgSpendPerDay\n\n            // 4. Calendar Days (Last 30 days)\n            val noSpendDaysList = mutableListOf<Long>()",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                },
                                {
                                    "name": "GetTransactionsUseCase.kt",
                                    "type": "file",
                                    "size": 490,
                                    "layer": "Domain",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.domain.usecase\n\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport kotlinx.coroutines.flow.Flow\nimport javax.inject.Inject\n\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nclass GetTransactionsUseCase @Inject constructor(\n    private val repository: TransactionRepository\n) {\n    operator fun invoke(): Flow<List<Transaction>> {\n        return repository.getAllTransactions()\n    }\n}\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Domain"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "presentation",
                    "type": "directory",
                    "children": [
                        {
                            "name": "components",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "BudgetRing.kt",
                                    "type": "file",
                                    "size": 4135,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.tween\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableFloatStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.StrokeCap\nimport androidx.compose.ui.graphics.drawscope.Stroke\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.Dp\nimport androidx.compose.ui.unit.dp\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetCaution\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetDanger\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetSafe\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter.formatAmount\n\n@Composable\nfun BudgetRing(\n    spent: Double,\n    limit: Double,\n    currency: Currency,\n    targetAmount: Double,\n    modifier: Modifier = Modifier,\n    size: Dp = 180.dp,\n    strokeWidth: Dp = 14.dp\n) {\n    val percentage = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1.2f) else 0f\n    val displayPercentage = (percentage * 100).coerceAtMost(100f)\n\n    val ringColor = when {\n        percentage < 0.5f -> BudgetSafe\n        percentage < 0.8f -> BudgetCaution\n        else -> BudgetDanger\n    }\n\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var animationTarget by remember { mutableFloatStateOf(0f) }\n    val animatedPercentage by animateFloatAsState(\n        targetValue = animationTarget,\n        animationSpec = tween(durationMillis = 1200),\n        label = \"budget_ring_animation\"\n    )\n\n    LaunchedEffect(percentage) {\n        animationTarget = percentage.coerceAtMost(1f)\n    }\n\n    val trackColor = MaterialTheme.colorScheme.surfaceVariant\n    val remaining = (limit - spent).coerceAtLeast(0.0)\n\n    Box(\n        modifier = modifier.size(size),\n        contentAlignment = Alignment.Center\n    ) {\n        Canvas(modifier = Modifier.size(size)) {\n            val stroke = Stroke(\n                width = strokeWidth.toPx(),\n                cap = StrokeCap.Round\n            )\n\n            // Track (background ring)\n            drawArc(\n                color = trackColor,\n                startAngle = -90f,\n                sweepAngle = 360f,\n                useCenter = false,\n                style = stroke\n            )\n\n            // Progress ring\n            drawArc(\n                color = ringColor,\n                startAngle = -90f,\n                sweepAngle = animatedPercentage * 360f,\n                useCenter = false,\n                style = stroke\n            )\n        }\n\n        // Center text\n        Column(horizontalAlignment = Alignment.CenterHorizontally) {\n            Text(\n                text = formatAmount(remaining, currency),\n                style = MaterialTheme.typography.headlineMedium,\n                fontWeight = FontWeight.Bold,",
                                    "concept": "Compose State",
                                    "explanation": "Jetpack Compose is 'declarative'. 'remember' tells Compose to keep a value across recompositions. 'by' is a Kotlin delegate that makes accessing the state value easier.",
                                    "is_compose": true
                                },
                                {
                                    "name": "EmptyState.kt",
                                    "type": "file",
                                    "size": 1708,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.unit.dp\n\n@Composable\nfun EmptyState(\n    emoji: String = \"\ud83d\udcca\",\n    title: String = \"No data yet\",\n    subtitle: String = \"Add your first transaction to get started!\",\n    modifier: Modifier = Modifier\n) {\n    Column(\n        modifier = modifier\n            .fillMaxWidth()\n            .padding(48.dp),\n        horizontalAlignment = Alignment.CenterHorizontally,\n        verticalArrangement = Arrangement.Center\n    ) {\n        Text(\n            text = emoji,\n            style = MaterialTheme.typography.displayLarge\n        )\n\n        Spacer(modifier = Modifier.height(16.dp))\n\n        Text(\n            text = title,\n            style = MaterialTheme.typography.headlineMedium,\n            textAlign = TextAlign.Center\n        )\n\n        Spacer(modifier = Modifier.height(8.dp))\n\n        Text(\n            text = subtitle,\n            style = MaterialTheme.typography.bodyMedium,\n            color = MaterialTheme.colorScheme.onSurfaceVariant,\n            textAlign = TextAlign.Center\n        )\n    }\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": true
                                },
                                {
                                    "name": "PieChart.kt",
                                    "type": "file",
                                    "size": 5353,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.tween\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Surface\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableFloatStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.StrokeCap\nimport androidx.compose.ui.graphics.drawscope.Stroke\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.ui.theme.ChartColors\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter.formatAmount\n\ndata class PieChartEntry(\n    val label: String,\n    val value: Double,\n    val color: Color\n)\n\n@Composable\nfun PieChart(\n    entries: List<PieChartEntry>,\n    currency: Currency,\n    modifier: Modifier = Modifier\n) {\n    if (entries.isEmpty()) return\n\n    val total = entries.sumOf { it.value }\n    if (total == 0.0) return\n\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var animationTarget by remember { mutableFloatStateOf(0f) }\n    val animatedSweep by animateFloatAsState(\n        targetValue = animationTarget,\n        animationSpec = tween(durationMillis = 1000),\n        label = \"pie_chart_animation\"\n    )\n\n    LaunchedEffect(entries) {\n        animationTarget = 1f\n    }\n\n    Column(modifier = modifier.fillMaxWidth()) {\n        // The Chart\n        Box(\n            modifier = Modifier\n                .size(200.dp)\n                .align(Alignment.CenterHorizontally)\n        ) {\n            Canvas(modifier = Modifier.size(200.dp)) {\n                var startAngle = -90f\n                entries.forEach { entry ->\n                    val sweepAngle = ((entry.value / total) * 360f * animatedSweep).toFloat()\n                    drawArc(\n                        color = entry.color,\n                        startAngle = startAngle,\n                        sweepAngle = sweepAngle,\n                        useCenter = false,\n                        style = Stroke(width = 40.dp.toPx(), cap = StrokeCap.Butt)\n                    )\n                    startAngle += sweepAngle\n                }\n            }\n\n            // Center text\n            Column(\n                modifier = Modifier.align(Alignment.Center),\n                horizontalAlignment = Alignment.CenterHorizontally\n            ) {\n                Text(\n                    text = formatAmount(total, currency),\n                    style = MaterialTheme.typography.titleLarge,\n                    fontWeight = FontWeight.Bold\n                )\n                Text(\n                    text = \"Total\",",
                                    "concept": "Compose State",
                                    "explanation": "Jetpack Compose is 'declarative'. 'remember' tells Compose to keep a value across recompositions. 'by' is a Kotlin delegate that makes accessing the state value easier.",
                                    "is_compose": true
                                },
                                {
                                    "name": "SummaryCard.kt",
                                    "type": "file",
                                    "size": 1948,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryDark\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryMedium\n\n@Composable\nfun SummaryCard(\n    title: String,\n    value: String,\n    modifier: Modifier = Modifier,\n    gradientColors: List<Color> = listOf(PrimaryMedium, PrimaryDark)\n) {\n    Box(\n        modifier = modifier\n            .fillMaxWidth()\n            .clip(RoundedCornerShape(20.dp))\n            .background(\n                brush = Brush.linearGradient(colors = gradientColors)\n            )\n            .padding(20.dp)\n    ) {\n        Column {\n            Text(\n                text = title,\n                style = MaterialTheme.typography.bodyMedium,\n                color = Color.White.copy(alpha = 0.7f)\n            )\n            Spacer(modifier = Modifier.height(8.dp))\n            Text(\n                text = value,\n                style = MaterialTheme.typography.displayMedium,\n                color = Color.White,\n                fontWeight = FontWeight.Bold\n            )\n        }\n    }\n}\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": true
                                },
                                {
                                    "name": "TransactionListItem.kt",
                                    "type": "file",
                                    "size": 5737,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextOverflow\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed\nimport com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen\n\n@Composable\nfun TransactionListItem(\n    transaction: Transaction,\n    currency: Currency,\n    modifier: Modifier = Modifier\n) {\n    val colorScheme = MaterialTheme.colorScheme\n    val isIncome = transaction.type == TransactionType.INCOME\n\n    // Assigning specific badge colors based on category\n    val badgeColor = when (transaction.category) {\n        Category.FOOD, Category.BILLS -> colorScheme.errorContainer.copy(alpha = 0.5f)\n        Category.TRANSPORT -> colorScheme.secondaryContainer\n        Category.SALARY, Category.INVESTMENT, Category.FREELANCE -> colorScheme.primaryContainer\n        else -> colorScheme.surfaceVariant\n    }\n\n    //val amountColor = if (isIncome) colorScheme.primary else colorScheme.onSurface\n\n    // Derive Status logic based on type/category for visual matching\n    val statusText = if (isIncome) \"EARNED\" else \"SPEND\"\n    val statusColor = if (isIncome) IncomeGreen else ExpenseRed\n\n    // Title mapping (Fallback to Category if Notes are empty)\n    val title =\n        if (transaction.notes.isNotBlank()) transaction.notes else transaction.category.displayName\n    val subtitle = transaction.category.displayName\n\n    Card(\n        modifier = modifier.fillMaxWidth(),\n        shape = RoundedCornerShape(16.dp),\n        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),\n        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)\n    ) {\n        Row(\n            modifier = Modifier\n                .fillMaxWidth()\n                .padding(8.dp),\n            verticalAlignment = Alignment.CenterVertically\n        ) {\n            // Icon Badge (Rounded Rectangle)\n            Box(\n                modifier = Modifier\n                    .size(48.dp)\n                    .clip(RoundedCornerShape(12.dp))\n                    .background(badgeColor),\n                contentAlignment = Alignment.Center\n            ) {\n                Text(\n                    text = transaction.category.emoji,\n                    style = MaterialTheme.typography.titleMedium\n                )\n            }\n\n            Spacer(modifier = Modifier.width(16.dp))\n\n            // Title and Category\n            Column(\n                modifier = Modifier.weight(1f),\n                verticalArrangement = Arrangement.Center\n            ) {\n                Text(\n                    text = title,\n                    style = MaterialTheme.typography.titleMedium,\n                    fontWeight = FontWeight.Bold,",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": true
                                },
                                {
                                    "name": "WeeklyTrendChart.kt",
                                    "type": "file",
                                    "size": 10322,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.components\n\nimport androidx.compose.animation.core.animateFloatAsState\nimport androidx.compose.animation.core.tween\nimport androidx.compose.foundation.Canvas\nimport androidx.compose.foundation.gestures.detectTapGestures\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableFloatStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.geometry.CornerRadius\nimport androidx.compose.ui.geometry.Offset\nimport androidx.compose.ui.geometry.Size\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.input.pointer.pointerInput\nimport androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.unit.dp\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetCaution\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetDanger\nimport com.sandeep.personalfinancecompanion.ui.theme.BudgetSafe\n\n\ndata class BarEntry(\n    val label: String,\n    val value: Float,\n    val isHighlighted: Boolean = false,\n    val dayOfWeek: Int = -1\n)\n@Composable\nfun WeeklyTrendChart(\n    entries: List<BarEntry>,\n    modifier: Modifier = Modifier,\n    onBarClick: (BarEntry) -> Unit = {},\n    budgetLimit: Double = 50000.0,\n    currency: com.sandeep.personalfinancecompanion.domain.model.Currency = com.sandeep.personalfinancecompanion.domain.model.Currency.INR,\n    barColor: Color = Color(0xFF0D6B58),\n    barColorLight: Color = Color(0xFFB2DFDB),\n    highlightColor: Color = Color(0xFF0D6B58)\n) {\n    if (entries.isEmpty()) return\n\n    val maxEntryValue = entries.maxOfOrNull { it.value } ?: 0f\n    \n    // Dynamic Scale: Default max is \u20b91000 converted to current currency\n    val minDefaultMax = com.sandeep.personalfinancecompanion.domain.model.Currency.convert(\n        1000.0, \n        com.sandeep.personalfinancecompanion.domain.model.Currency.INR, \n        currency\n    ).toFloat()\n\n    // Dynamic Thresholds Based on Daily Budget (Monthly Budget / 30)\n    val dailyBudget = (budgetLimit / 30.0).toFloat()\n    val safeThreshold = dailyBudget * 0.5f  // 50% of daily budget\n    val cautionThreshold = dailyBudget      // 100% of daily budget\n\n    // Calculate a \"nice\" max value for the chart scale\n    val maxValue = when {\n        maxEntryValue <= 0f -> minDefaultMax\n        maxEntryValue <= minDefaultMax -> minDefaultMax\n        maxEntryValue <= minDefaultMax * 5 -> minDefaultMax * 5\n        maxEntryValue <= minDefaultMax * 10 -> minDefaultMax * 10\n        else -> {\n            val magnitude = Math.pow(10.0, Math.floor(Math.log10(maxEntryValue.toDouble()))).toFloat()\n            (Math.ceil((maxEntryValue / magnitude).toDouble()) * magnitude).toFloat()\n        }\n    }\n\n    val yAxisLabels = listOf(\n        maxValue,\n        maxValue * 0.75f,\n        maxValue * 0.5f,\n        maxValue * 0.25f\n    ).map { it.toInt() }\n\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var animTarget by remember { mutableFloatStateOf(0f) }\n    val animProgress by animateFloatAsState(\n        targetValue = animTarget,\n        animationSpec = tween(durationMillis = 800),\n        label = \"bar_animation\"\n    )\n\n    LaunchedEffect(entries) {\n        animTarget = 1f",
                                    "concept": "Compose State",
                                    "explanation": "Jetpack Compose is 'declarative'. 'remember' tells Compose to keep a value across recompositions. 'by' is a Kotlin delegate that makes accessing the state value easier.",
                                    "is_compose": true
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "goal",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "GoalScreen.kt",
                                    "type": "file",
                                    "size": 59566,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.goal\n\nimport android.graphics.Color.parseColor\nimport androidx.compose.foundation.BorderStroke\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.border\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxHeight\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Add\nimport androidx.compose.material.icons.filled.AddCircle\nimport androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.DirectionsCar\nimport androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Edit\nimport androidx.compose.material.icons.filled.FlightTakeoff\nimport androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.LocalFireDepartment\nimport androidx.compose.material.icons.filled.School\nimport androidx.compose.material.icons.filled.Security\nimport androidx.compose.material.icons.filled.ShowChart\nimport androidx.compose.material.icons.filled.Smartphone\nimport androidx.compose.material.icons.filled.Star\nimport androidx.compose.material.icons.filled.TrendingUp\nimport androidx.compose.material3.AlertDialog\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.CircularProgressIndicator\nimport androidx.compose.material3.DatePicker\nimport androidx.compose.material3.DatePickerDialog\nimport androidx.compose.material3.Divider\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.HorizontalDivider\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.IconButton\nimport androidx.compose.material3.LinearProgressIndicator\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.ModalBottomSheet\nimport androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.RadioButton\nimport androidx.compose.material3.Surface\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.TextButton\nimport androidx.compose.material3.rememberDatePickerState\nimport androidx.compose.material3.rememberModalBottomSheetState\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.draw.drawBehind\nimport androidx.compose.ui.geometry.CornerRadius\nimport androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.StrokeCap\nimport androidx.compose.ui.graphics.drawscope.Stroke\nimport androidx.compose.ui.graphics.vector.ImageVector\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.model.Goal\nimport com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak\nimport com.sandeep.personalfinancecompanion.presentation.components.EmptyState\nimport com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter\nimport java.util.Calendar\nimport java.util.Locale\n\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun GoalScreen(\n    viewModel: GoalViewModel = hiltViewModel()\n) {\n    val colorScheme = MaterialTheme.colorScheme\n    val uiState by viewModel.uiState.collectAsStateWithLifecycle()\n\n    when (val state = uiState) {",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "GoalViewModel.kt",
                                    "type": "file",
                                    "size": 3808,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.goal\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.domain.model.Goal\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport dagger.hilt.android.lifecycle.HiltViewModel\nimport com.sandeep.personalfinancecompanion.domain.model.NoSpendStreak\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.GetNoSpendStreakUseCase\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\nimport kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.SharingStarted\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.catch\nimport kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.stateIn\nimport kotlinx.coroutines.launch\nimport java.util.UUID\nimport javax.inject.Inject\n\nsealed interface GoalUiState {\n    data object Loading : GoalUiState\n    data class Success(\n        val goals: List<Goal>,\n        val currency: Currency,\n        val noSpendStreak: NoSpendStreak\n    ) : GoalUiState\n    data class Error(val message: String) : GoalUiState\n}\n\n@HiltViewModel\nclass GoalViewModel @Inject constructor(\n    private val repository: GoalRepository,\n    private val preferencesRepository: UserPreferencesRepository,\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val getNoSpendStreakUseCase: GetNoSpendStreakUseCase\n) : ViewModel() {\n    \n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _uiState = MutableStateFlow<GoalUiState>(GoalUiState.Loading)\n    val uiState: StateFlow<GoalUiState> = combine(\n        repository.getAllGoals(),\n        preferencesRepository.currencyFlow,\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n        getNoSpendStreakUseCase()\n    ) { goals, currency, streak ->\n        GoalUiState.Success(goals, currency, streak)\n    }.catch { e ->\n        _uiState.value = GoalUiState.Error(e.message ?: \"An unexpected error occurred\")\n    }.stateIn(\n        scope = viewModelScope,\n        started = SharingStarted.WhileSubscribed(5000),\n        initialValue = GoalUiState.Loading\n    )\n    \n    fun createNewGoal(title: String, targetAmount: Double, iconName: String, colorHex: String, targetDate: Long? = null) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            try {\n                val newGoal = Goal(\n                    id = UUID.randomUUID().toString(),\n                    title = title,\n                    targetAmount = targetAmount,\n                    savedAmount = 0.0,\n                    iconName = iconName,\n                    colorHex = colorHex,\n                    contributions = emptyList(),\n                    targetDate = targetDate\n                )\n                repository.insertGoal(newGoal)\n            } catch (e: Exception) {\n                // Handle error if needed, maybe via a one-time event/SideEffect\n            }\n        }\n    }\n\n    fun updateGoalSettings(goalId: String, targetAmount: Double, targetDate: Long?) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            repository.updateGoalSettings(goalId, targetAmount, targetDate)\n        }\n    }\n\n    fun deleteGoal(goalId: String) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            repository.deleteGoal(goalId)\n        }\n    }\n\n    fun addSavings(goalId: String, amount: Double) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            repository.addContribution(goalId, amount)\n        }\n    }\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "home",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "HomeScreen.kt",
                                    "type": "file",
                                    "size": 34157,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.home\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter\nimport androidx.compose.animation.Crossfade\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxHeight\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.ArrowDownward\nimport androidx.compose.material.icons.filled.ArrowUpward\nimport androidx.compose.material.icons.filled.Notifications\nimport androidx.compose.material.icons.filled.Person\nimport androidx.compose.material.icons.filled.Edit\nimport androidx.compose.foundation.clickable\nimport androidx.compose.material3.AlertDialog\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.CircularProgressIndicator\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.IconButton\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.TextButton\nimport androidx.compose.material3.OutlinedTextField\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.presentation.components.BarEntry\nimport com.sandeep.personalfinancecompanion.presentation.components.BudgetRing\nimport com.sandeep.personalfinancecompanion.presentation.components.EmptyState\nimport com.sandeep.personalfinancecompanion.presentation.components.WeeklyTrendChart\n\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.ModalBottomSheet\nimport androidx.compose.material3.rememberModalBottomSheetState\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.rememberCoroutineScope\nimport androidx.compose.ui.text.style.TextAlign\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.presentation.components.TransactionListItem\nimport kotlinx.coroutines.launch\nimport java.text.SimpleDateFormat\nimport java.util.Date\nimport java.util.Locale\n\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun HomeScreen(\n    onNavigateToTransactions: () -> Unit,\n    onAddIncome: () -> Unit,\n    onAddExpense: () -> Unit,\n    viewModel: HomeViewModel = hiltViewModel()\n) {\n    val uiState by viewModel.uiState.collectAsStateWithLifecycle()\n    val sheetState = rememberModalBottomSheetState()\n\n    when (val state = uiState) {\n        is HomeUiState.Loading -> {\n            Box(\n                modifier = Modifier.fillMaxSize(),\n                contentAlignment = Alignment.Center\n            ) {\n                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)\n            }\n        }\n\n        is HomeUiState.Error -> {\n            Column(",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "HomeViewModel.kt",
                                    "type": "file",
                                    "size": 11730,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.home\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport com.sandeep.personalfinancecompanion.domain.usecase.BalanceSummary\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.CalculateBalanceUseCase\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase\nimport com.sandeep.personalfinancecompanion.presentation.components.BarEntry\nimport dagger.hilt.android.lifecycle.HiltViewModel\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\nimport kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.launch\nimport java.util.Calendar\nimport javax.inject.Inject\n\ndata class CategoryStats(\n    val category: Category,\n    val amount: Double,\n    val transactionCount: Int,\n    val percentage: Float\n)\n\nsealed interface HomeUiState {\n    data object Loading : HomeUiState\n    data class Success(\n        val balance: BalanceSummary,\n        val recentTransactions: List<Transaction>,\n        val budgetLimit: Double,\n        val totalExpense: Double,\n        val selectedCurrency: Currency,\n        val weeklyTrend: List<BarEntry>,\n        val categoryExpenses: List<CategoryStats>,\n        val selectedDayTransactions: List<Transaction>? = null,\n        val selectedDayLabel: String? = null,\n        val selectedCategoryTransactions: List<Transaction>? = null,\n        val selectedCategoryLabel: String? = null,\n        val goalTargetAmount: Double = 0.0\n    ) : HomeUiState\n    data class Error(val message: String) : HomeUiState\n}\n\n@HiltViewModel\nclass HomeViewModel @Inject constructor(\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val getTransactionsUseCase: GetTransactionsUseCase,\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val calculateBalanceUseCase: CalculateBalanceUseCase,\n    private val repository: TransactionRepository,\n    private val preferencesRepository: UserPreferencesRepository,\n    private val goalRepository: com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\n) : ViewModel() {\n\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)\n    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()\n\n    private var allTransactions: List<Transaction> = emptyList()\n    private var currentBudgetLimit: Double = 0.0\n    private var currentCurrency: Currency = Currency.INR\n\n    init {\n        loadDashboard()\n    }\n\n    private fun loadDashboard() {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            try {\n                _uiState.value = HomeUiState.Loading\n\n                // Collect preferences first to get budget and currency\n                launch {\n                    preferencesRepository.budgetLimitFlow.collect { limit ->\n                        currentBudgetLimit = limit\n                        val currentState = _uiState.value\n                        if (currentState is HomeUiState.Success) {\n                            _uiState.value = currentState.copy(budgetLimit = limit)\n                        }\n                    }\n                }\n\n                launch {\n                    preferencesRepository.currencyFlow.collect { currency ->\n                        currentCurrency = currency\n                        val currentState = _uiState.value\n                        if (currentState is HomeUiState.Success) {\n                            _uiState.value = currentState.copy(selectedCurrency = currency)\n                        }\n                    }\n                }",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "insights",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "InsightsScreen.kt",
                                    "type": "file",
                                    "size": 8698,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.insights\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter\nimport androidx.compose.animation.Crossfade\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.CircularProgressIndicator\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.unit.dp\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.sandeep.personalfinancecompanion.presentation.components.EmptyState\nimport com.sandeep.personalfinancecompanion.presentation.components.PieChart\nimport com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed\nimport com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen\n\n@Composable\nfun InsightsScreen(\n    viewModel: InsightsViewModel = hiltViewModel()\n) {\n    val state by viewModel.state.collectAsStateWithLifecycle()\n    val currency by viewModel.currency.collectAsStateWithLifecycle()\n    val colorScheme = MaterialTheme.colorScheme\n\n    when {\n        state.isLoading -> {\n            Box(\n                modifier = Modifier.fillMaxSize(),\n                contentAlignment = Alignment.Center\n            ) {\n                CircularProgressIndicator(color = colorScheme.primary)\n            }\n        }\n\n        state.error != null -> {\n            Column(\n                modifier = Modifier\n                    .fillMaxSize()\n                    .padding(32.dp),\n                horizontalAlignment = Alignment.CenterHorizontally,\n                verticalArrangement = Arrangement.Center\n            ) {\n                Text(text = \"\ud83c\udfe5\", style = MaterialTheme.typography.displayLarge)\n                Spacer(modifier = Modifier.height(16.dp))\n                Text(\n                    text = state.error ?: \"Analytics failed to load\",\n                    style = MaterialTheme.typography.bodyLarge,\n                    color = colorScheme.error,\n                    textAlign = TextAlign.Center\n                )\n            }\n        }\n\n        state.categoryBreakdown.isEmpty() -> {\n            EmptyState(\n                emoji = \"\ud83d\udcca\",\n                title = \"No insights yet\",\n                subtitle = \"Add some transactions to see your spending patterns!\"\n            )\n        }\n\n        else -> {\n            InsightsContent(state = state, currency = currency)\n        }\n    }\n}\n\n@Composable\nprivate fun InsightsContent(state: InsightsState, currency: Currency) {\n    val colorScheme = MaterialTheme.colorScheme\n    Column(\n        modifier = Modifier\n            .fillMaxSize()\n            .verticalScroll(rememberScrollState())\n            .padding(horizontal = 10.dp)\n    ) {\n        Spacer(modifier = Modifier.height(10.dp))\n\n        // Quick Stats Row\n        Row(",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "InsightsViewModel.kt",
                                    "type": "file",
                                    "size": 4307,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.insights\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.data.repository.TransactionRepositoryImpl\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.usecase.BalanceSummary\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.CalculateBalanceUseCase\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase\nimport com.sandeep.personalfinancecompanion.presentation.components.PieChartEntry\nimport com.sandeep.personalfinancecompanion.ui.theme.ChartColors\nimport dagger.hilt.android.lifecycle.HiltViewModel\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\nimport kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.asStateFlow\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport kotlinx.coroutines.flow.SharingStarted\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.stateIn\nimport kotlinx.coroutines.launch\nimport javax.inject.Inject\n\ndata class InsightsState(\n    val isLoading: Boolean = true,\n    val categoryBreakdown: List<PieChartEntry> = emptyList(),\n    val balanceSummary: BalanceSummary = BalanceSummary(0.0, 0.0, 0.0),\n    val topCategory: String = \"\",\n    val totalTransactions: Int = 0,\n    val error: String? = null\n)\n\n@HiltViewModel\nclass InsightsViewModel @Inject constructor(\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val getTransactionsUseCase: GetTransactionsUseCase,\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val calculateBalanceUseCase: CalculateBalanceUseCase,\n    private val repository: TransactionRepository,\n    private val preferencesRepository: UserPreferencesRepository\n) : ViewModel() {\n    \n    val currency: StateFlow<Currency> = preferencesRepository.currencyFlow\n        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Currency.INR)\n\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _state = MutableStateFlow(InsightsState())\n    val state: StateFlow<InsightsState> = _state.asStateFlow()\n\n    init {\n        loadInsights()\n    }\n\n    private fun loadInsights() {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            try {\n\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n                getTransactionsUseCase().collect { transactions ->\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n                    val balance = calculateBalanceUseCase(transactions)\n\n                    // Category breakdown for expenses only\n                    val expensesByCategory = transactions\n                        .filter { it.type == TransactionType.EXPENSE }\n                        .groupBy { it.category }\n                        .mapValues { entry -> entry.value.sumOf { it.amount } }\n                        .toList()\n                        .sortedByDescending { it.second }\n\n                    val chartEntries = expensesByCategory.mapIndexed { index, (category, amount) ->\n                        PieChartEntry(\n                            label = \"${category.emoji} ${category.displayName}\",\n                            value = amount,\n                            color = ChartColors[index % ChartColors.size]\n                        )\n                    }\n\n                    val topCategory = if (expensesByCategory.isNotEmpty()) {\n                        val top = expensesByCategory.first()\n                        \"${top.first.emoji} ${top.first.displayName}\"\n                    } else \"N/A\"\n\n                    _state.value = InsightsState(\n                        isLoading = false,\n                        categoryBreakdown = chartEntries,\n                        balanceSummary = balance,\n                        topCategory = topCategory,\n                        totalTransactions = transactions.size,\n                        error = null\n                    )\n                }\n            } catch (e: Exception) {\n                _state.value = InsightsState(",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "navigation",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "AppNavigation.kt",
                                    "type": "file",
                                    "size": 7814,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.navigation\n\nimport androidx.compose.animation.AnimatedContentTransitionScope\nimport androidx.compose.animation.core.tween\nimport androidx.compose.animation.fadeIn\nimport androidx.compose.animation.fadeOut\nimport androidx.compose.material3.SnackbarHostState\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Modifier\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.navigation.NavHostController\nimport androidx.navigation.NavType\nimport androidx.navigation.compose.NavHost\nimport androidx.navigation.compose.composable\nimport androidx.navigation.navArgument\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.presentation.goal.GoalScreen\nimport com.sandeep.personalfinancecompanion.presentation.home.HomeScreen\nimport com.sandeep.personalfinancecompanion.presentation.insights.InsightsScreen\nimport com.sandeep.personalfinancecompanion.presentation.profile.ProfileScreen\nimport com.sandeep.personalfinancecompanion.presentation.transactions.AddEditTransactionScreen\nimport com.sandeep.personalfinancecompanion.presentation.transactions.TransactionListScreen\nimport com.sandeep.personalfinancecompanion.presentation.transactions.TransactionViewModel\n\n@Composable\nfun AppNavigation(\n    navController: NavHostController,\n    snackbarHostState: SnackbarHostState,\n    modifier: Modifier = Modifier\n) {\n    val transactionViewModel: TransactionViewModel = hiltViewModel()\n\n    NavHost(\n        navController = navController,\n        startDestination = Screen.Home.route,\n        modifier = modifier,\n        enterTransition = {\n            fadeIn(animationSpec = tween(500)) + \n            slideIntoContainer(\n                AnimatedContentTransitionScope.SlideDirection.Start, \n                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)\n            )\n        },\n        exitTransition = {\n            fadeOut(animationSpec = tween(500)) + \n            slideOutOfContainer(\n                AnimatedContentTransitionScope.SlideDirection.Start, \n                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)\n            )\n        },\n        popEnterTransition = {\n            fadeIn(animationSpec = tween(500)) + \n            slideIntoContainer(\n                AnimatedContentTransitionScope.SlideDirection.End, \n                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)\n            )\n        },\n        popExitTransition = {\n            fadeOut(animationSpec = tween(500)) + \n            slideOutOfContainer(\n                AnimatedContentTransitionScope.SlideDirection.End, \n                animationSpec = tween(500, easing = androidx.compose.animation.core.FastOutSlowInEasing)\n            )\n        }\n    ) {\n        composable(Screen.Home.route) {\n            HomeScreen(\n                onNavigateToTransactions = {\n                    navController.navigate(Screen.Transactions.route) {\n                        popUpTo(Screen.Home.route) { saveState = true }\n                        launchSingleTop = true\n                        restoreState = true\n                    }\n                },\n                onAddIncome = {\n                    navController.navigate(Screen.AddTransaction.createRoute(\"INCOME\"))\n                },\n                onAddExpense = {\n                    navController.navigate(Screen.AddTransaction.createRoute(\"EXPENSE\"))\n                }\n            )\n        }\n\n        composable(Screen.Transactions.route) {\n            TransactionListScreen(\n                snackbarHostState = snackbarHostState,\n                onAddTransaction = {\n                    navController.navigate(Screen.AddTransaction.createRoute())\n                },\n                onEditTransaction = { transactionId ->\n                    navController.navigate(Screen.EditTransaction.createRoute(transactionId))\n                },\n                viewModel = transactionViewModel\n            )\n        }\n\n        composable(Screen.Goals.route) {\n            GoalScreen()\n        }\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": true
                                },
                                {
                                    "name": "Screen.kt",
                                    "type": "file",
                                    "size": 2168,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.navigation\n\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.BarChart\nimport androidx.compose.material.icons.filled.FlagCircle\nimport androidx.compose.material.icons.filled.Home\nimport androidx.compose.material.icons.filled.History\nimport androidx.compose.material.icons.outlined.BarChart\nimport androidx.compose.material.icons.outlined.FlagCircle\nimport androidx.compose.material.icons.outlined.Home\nimport androidx.compose.material.icons.outlined.History\nimport androidx.compose.ui.graphics.vector.ImageVector\n\nsealed class Screen(val route: String) {\n    data object Home : Screen(\"home\")\n    data object Transactions : Screen(\"transactions\")\n    data object Goals : Screen(\"goals\")\n    data object Insights : Screen(\"insights\")\n    data object AddTransaction : Screen(\"add_transaction/{type}\") {\n        fun createRoute(type: String = \"EXPENSE\") = \"add_transaction/$type\"\n    }\n    data object EditTransaction : Screen(\"edit_transaction/{transactionId}\") {\n        fun createRoute(transactionId: String) = \"edit_transaction/$transactionId\"\n    }\n    data object Profile : Screen(\"profile\")\n}\n\ndata class BottomNavItem(\n    val label: String,\n    val route: String,\n    val selectedIcon: ImageVector,\n    val unselectedIcon: ImageVector\n)\n\nval bottomNavItems = listOf(\n    BottomNavItem(\n        label = \"Home\",\n        route = Screen.Home.route,\n        selectedIcon = Icons.Filled.Home,\n        unselectedIcon = Icons.Outlined.Home\n    ),\n    BottomNavItem(\n        label = \"History\",\n        route = Screen.Transactions.route,\n        selectedIcon = Icons.Filled.History,\n        unselectedIcon = Icons.Outlined.History\n    ),\n    BottomNavItem(\n        label = \"Goals\",\n        route = Screen.Goals.route,\n        selectedIcon = Icons.Filled.FlagCircle,\n        unselectedIcon = Icons.Outlined.FlagCircle\n    ),\n    BottomNavItem(\n        label = \"Insights\",\n        route = Screen.Insights.route,\n        selectedIcon = Icons.Filled.BarChart,\n        unselectedIcon = Icons.Outlined.BarChart\n    )\n)\n",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "profile",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "ProfileScreen.kt",
                                    "type": "file",
                                    "size": 13439,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.profile\n\nimport android.Manifest\nimport android.os.Build\nimport androidx.activity.compose.rememberLauncherForActivityResult\nimport androidx.activity.result.contract.ActivityResultContracts\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.ColumnScope\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.layout.size\nimport androidx.compose.foundation.layout.width\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.CircleShape\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.AccountBalanceWallet\nimport androidx.compose.material.icons.filled.ChevronRight\nimport androidx.compose.material.icons.filled.CurrencyExchange\nimport androidx.compose.material.icons.filled.Download\nimport androidx.compose.material.icons.filled.Flag\nimport androidx.compose.material.icons.filled.NotificationsActive\nimport androidx.compose.material.icons.filled.Person\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\nimport androidx.compose.material3.Card\nimport androidx.compose.material3.CardDefaults\nimport androidx.compose.material3.Divider\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.Switch\nimport androidx.compose.material3.SwitchDefaults\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.collectAsState\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Brush\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.vector.ImageVector\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport com.sandeep.personalfinancecompanion.presentation.profile.components.CurrencySelectionDialog\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent\n\n@Composable\nfun ProfileScreen(\n    onBack: () -> Unit,\n    viewModel: ProfileViewModel = hiltViewModel()\n) {\n    val state by viewModel.state.collectAsState()\n    val scrollState = rememberScrollState()\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var showCurrencyDialog by remember { mutableStateOf(false) }\n\n    // Permission launcher for Android 13+\n    val permissionLauncher = rememberLauncherForActivityResult(\n        contract = ActivityResultContracts.RequestPermission()\n    ) { isGranted ->\n        if (isGranted) {\n            viewModel.toggleDailyReminder(true)\n        }\n    }\n\n    Column(\n        modifier = Modifier\n            .fillMaxSize()\n            .background(MaterialTheme.colorScheme.background)\n            .verticalScroll(scrollState)\n    ) {\n        ProfileHeader()\n\n        Column(\n            modifier = Modifier\n                .padding(16.dp)\n                .fillMaxWidth()\n        ) {\n            Text(\n                text = \"Notifications & Alerts\",\n                style = MaterialTheme.typography.titleMedium,\n                fontWeight = FontWeight.Bold,\n                color = MaterialTheme.colorScheme.onBackground,\n                modifier = Modifier.padding(bottom = 12.dp)\n            )\n",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "ProfileViewModel.kt",
                                    "type": "file",
                                    "size": 2758,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.profile\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\nimport dagger.hilt.android.lifecycle.HiltViewModel\nimport kotlinx.coroutines.flow.SharingStarted\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.stateIn\nimport kotlinx.coroutines.launch\nimport javax.inject.Inject\n\ndata class ProfileState(\n    val dailyReminderEnabled: Boolean = false,\n    val reminderTime: String = \"20:00\",\n    val budgetAlertsEnabled: Boolean = true,\n    val goalRemindersEnabled: Boolean = true,\n    val selectedCurrency: Currency = Currency.INR,\n    val isLoading: Boolean = true\n)\n\n@HiltViewModel\nclass ProfileViewModel @Inject constructor(\n    private val preferencesRepository: UserPreferencesRepository,\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val changeCurrencyUseCase: com.sandeep.personalfinancecompanion.domain.usecase.ChangeCurrencyUseCase\n) : ViewModel() {\n\n    val state: StateFlow<ProfileState> = combine(\n        preferencesRepository.dailyReminderEnabledFlow,\n        preferencesRepository.reminderTimeFlow,\n        preferencesRepository.budgetAlertsEnabledFlow,\n        preferencesRepository.goalRemindersEnabledFlow,\n        preferencesRepository.currencyFlow\n    ) { daily, time, budget, goals, currency ->\n        ProfileState(\n            dailyReminderEnabled = daily,\n            reminderTime = time,\n            budgetAlertsEnabled = budget,\n            goalRemindersEnabled = goals,\n            selectedCurrency = currency,\n            isLoading = false\n        )\n    }.stateIn(\n        scope = viewModelScope,\n        started = SharingStarted.WhileSubscribed(5000),\n        initialValue = ProfileState()\n    )\n\n    fun toggleDailyReminder(enabled: Boolean) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.updateDailyReminderEnabled(enabled)\n        }\n    }\n\n    fun updateReminderTime(time: String) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.updateReminderTime(time)\n        }\n    }\n\n    fun toggleBudgetAlerts(enabled: Boolean) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.updateBudgetAlertsEnabled(enabled)\n        }\n    }\n\n    fun toggleGoalReminders(enabled: Boolean) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n            preferencesRepository.updateGoalRemindersEnabled(enabled)\n        }\n    }\n\n    fun updateCurrency(currency: Currency) {\n    // COROUTINES: Starting a background task that lives as long as the screen (ViewModel).\n        viewModelScope.launch {\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n            changeCurrencyUseCase(currency)\n        }\n    }\n}\n",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                },
                                {
                                    "name": "components",
                                    "type": "directory",
                                    "children": [
                                        {
                                            "name": "CurrencySelectionDialog.kt",
                                            "type": "file",
                                            "size": 5238,
                                            "layer": "Presentation",
                                            "extension": ".kt",
                                            "snippet": "package com.sandeep.personalfinancecompanion.presentation.profile.components\n\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Close\nimport androidx.compose.material3.*\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.compose.ui.window.Dialog\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.ui.theme.PrimaryAccent\n\n@Composable\nfun CurrencySelectionDialog(\n    selectedCurrency: Currency,\n    onCurrencySelected: (Currency) -> Unit,\n    onDismiss: () -> Unit\n) {\n    Dialog(onDismissRequest = onDismiss) {\n        Card(\n            modifier = Modifier\n                .fillMaxWidth()\n                .padding(vertical = 24.dp),\n            shape = RoundedCornerShape(24.dp),\n            colors = CardDefaults.cardColors(\n                containerColor = MaterialTheme.colorScheme.surface\n            ),\n            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)\n        ) {\n            Column(\n                modifier = Modifier\n                    .fillMaxWidth()\n                    .padding(20.dp)\n            ) {\n                Row(\n                    modifier = Modifier.fillMaxWidth(),\n                    horizontalArrangement = Arrangement.SpaceBetween,\n                    verticalAlignment = Alignment.CenterVertically\n                ) {\n                    Text(\n                        text = \"Select Currency\",\n                        style = MaterialTheme.typography.titleLarge,\n                        fontWeight = FontWeight.Bold,\n                        color = MaterialTheme.colorScheme.onSurface\n                    )\n                    IconButton(onClick = onDismiss) {\n                        Icon(imageVector = Icons.Default.Close, contentDescription = \"Close\")\n                    }\n                }\n\n                Spacer(modifier = Modifier.height(16.dp))\n\n                LazyColumn(\n                    modifier = Modifier\n                        .fillMaxWidth()\n                        .heightIn(max = 400.dp),\n                    verticalArrangement = Arrangement.spacedBy(8.dp)\n                ) {\n                    items(Currency.entries) { currency ->\n                        CurrencyItem(\n                            currency = currency,\n                            isSelected = currency == selectedCurrency,\n                            onClick = {\n                                onCurrencySelected(currency)\n                                onDismiss()\n                            }\n                        )\n                    }\n                }\n            }\n        }\n    }\n}\n\n@Composable\nfun CurrencyItem(\n    currency: Currency,\n    isSelected: Boolean,\n    onClick: () -> Unit\n) {\n    Surface(\n        modifier = Modifier\n            .fillMaxWidth()\n            .clip(RoundedCornerShape(12.dp))\n            .clickable(onClick = onClick),\n        color = if (isSelected) PrimaryAccent.copy(alpha = 0.1f) else Color.Transparent,\n        shape = RoundedCornerShape(12.dp),\n        border = if (isSelected) {",
                                            "concept": null,
                                            "explanation": "",
                                            "is_compose": true
                                        }
                                    ],
                                    "layer": "Presentation"
                                }
                            ],
                            "layer": "Presentation"
                        },
                        {
                            "name": "transactions",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "AddEditTransactionScreen.kt",
                                    "type": "file",
                                    "size": 11182,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.transactions\n\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.rememberScrollState\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.foundation.text.KeyboardOptions\nimport androidx.compose.foundation.verticalScroll\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.ButtonDefaults\nimport androidx.compose.material3.DropdownMenuItem\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.ExposedDropdownMenuBox\nimport androidx.compose.material3.ExposedDropdownMenuDefaults\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.MenuAnchorType\nimport androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.SegmentedButton\nimport androidx.compose.material3.SegmentedButtonDefaults\nimport androidx.compose.material3.SingleChoiceSegmentedButtonRow\nimport androidx.compose.material3.Text\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.mutableLongStateOf\nimport androidx.compose.runtime.mutableStateOf\nimport androidx.compose.runtime.remember\nimport androidx.compose.runtime.setValue\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.input.KeyboardType\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Close\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.IconButton\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.ui.Alignment\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\n\n@OptIn(ExperimentalMaterial3Api::class)\n@Composable\nfun AddEditTransactionScreen(\n    transactionId: String? = null,\n    initialType: TransactionType = TransactionType.EXPENSE,\n    onSave: (Transaction) -> Unit,\n    onBack: () -> Unit,\n    viewModel: TransactionViewModel = hiltViewModel()\n) {\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var amount by remember { mutableStateOf(\"\") }\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var type by remember { mutableStateOf(initialType) }\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var selectedCategory by remember {\n        mutableStateOf(\n            if (initialType == TransactionType.INCOME) Category.SALARY else Category.FOOD\n        )\n    }\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var notes by remember { mutableStateOf(\"\") }\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var date by remember { mutableLongStateOf(System.currentTimeMillis()) }\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var expanded by remember { mutableStateOf(false) }\n\n    // Validation\n    // COMPOSE: Telling the UI to 'remember' this piece of data during refresh.\n    // COMPOSE: Using property delegation for easier state access.\n    var amountError by remember { mutableStateOf<String?>(null) }\n\n    val listState by viewModel.listState.collectAsStateWithLifecycle()\n    val currency = listState.selectedCurrency\n\n    LaunchedEffect(transactionId) {\n        if (transactionId != null) {\n            val transaction = viewModel.getTransactionById(transactionId)\n            if (transaction != null) {\n                amount = transaction.amount.toString()\n                type = transaction.type\n                selectedCategory = transaction.category\n                notes = transaction.notes\n                date = transaction.date",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "TransactionListScreen.kt",
                                    "type": "file",
                                    "size": 16600,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.transactions\n\nimport androidx.compose.animation.animateColorAsState\nimport androidx.compose.animation.animateContentSize\nimport androidx.compose.foundation.background\nimport androidx.compose.foundation.clickable\nimport androidx.compose.foundation.layout.Arrangement\nimport androidx.compose.foundation.layout.Box\nimport androidx.compose.foundation.layout.Column\nimport androidx.compose.foundation.layout.ExperimentalLayoutApi\nimport androidx.compose.foundation.layout.Row\nimport androidx.compose.foundation.layout.Spacer\nimport androidx.compose.foundation.layout.fillMaxSize\nimport androidx.compose.foundation.layout.fillMaxWidth\nimport androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.padding\nimport androidx.compose.foundation.lazy.LazyColumn\nimport androidx.compose.foundation.lazy.items\nimport androidx.compose.foundation.shape.RoundedCornerShape\nimport androidx.compose.material.icons.Icons\nimport androidx.compose.material.icons.filled.Delete\nimport androidx.compose.material.icons.filled.Edit\nimport androidx.compose.material.icons.filled.Search\nimport androidx.compose.material3.Button\nimport androidx.compose.material3.CircularProgressIndicator\nimport androidx.compose.material3.ExperimentalMaterial3Api\nimport androidx.compose.material3.Icon\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.OutlinedTextField\nimport androidx.compose.material3.OutlinedTextFieldDefaults\nimport androidx.compose.material3.SnackbarDuration\nimport androidx.compose.material3.SnackbarHostState\nimport androidx.compose.material3.SnackbarResult\nimport androidx.compose.material3.SwipeToDismissBox\nimport androidx.compose.material3.SwipeToDismissBoxValue\nimport androidx.compose.material3.Text\nimport androidx.compose.material3.rememberSwipeToDismissBoxState\nimport androidx.compose.runtime.Composable\nimport androidx.compose.runtime.getValue\nimport androidx.compose.runtime.rememberCoroutineScope\nimport androidx.compose.ui.Alignment\nimport androidx.compose.ui.Modifier\nimport androidx.compose.ui.draw.clip\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.text.style.TextAlign\nimport androidx.compose.ui.unit.dp\nimport androidx.compose.ui.unit.sp\nimport androidx.hilt.navigation.compose.hiltViewModel\nimport androidx.lifecycle.compose.collectAsStateWithLifecycle\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.presentation.components.EmptyState\nimport com.sandeep.personalfinancecompanion.presentation.components.TransactionListItem\nimport com.sandeep.personalfinancecompanion.ui.theme.ExpenseRed\nimport com.sandeep.personalfinancecompanion.ui.theme.IncomeGreen\nimport com.sandeep.personalfinancecompanion.util.CurrencyFormatter\nimport kotlinx.coroutines.launch\nimport java.text.SimpleDateFormat\nimport java.util.Calendar\nimport java.util.Date\nimport java.util.Locale\n\n@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)\n@Composable\nfun TransactionListScreen(\n    snackbarHostState: SnackbarHostState,\n    onAddTransaction: () -> Unit,\n    onEditTransaction: (String) -> Unit,\n    viewModel: TransactionViewModel = hiltViewModel()\n) {\n    val listState by viewModel.listState.collectAsStateWithLifecycle()\n    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()\n    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()\n    val scope = rememberCoroutineScope()\n    val colorScheme = MaterialTheme.colorScheme\n\n    Column(\n        modifier = Modifier\n            .fillMaxSize()\n            .background(colorScheme.background)\n    ) {\n\n        Spacer(modifier = Modifier.height(8.dp))\n\n        // \u2500\u2500\u2500\u2500 Headers \u2500\u2500\u2500\u2500\n        Column(modifier = Modifier.padding(horizontal = 20.dp)) {\n            Text(\n                text = \"CASH FLOW ARCHIVE\",\n                style = MaterialTheme.typography.labelSmall,\n                color = colorScheme.primary,\n                fontWeight = FontWeight.Bold,\n                letterSpacing = 2.sp\n            )\n        }\n\n        Spacer(modifier = Modifier.height(16.dp))\n\n        // \u2500\u2500\u2500\u2500 Search & Filters \u2500\u2500\u2500\u2500",
                                    "concept": "Flow / StateFlow",
                                    "explanation": "Flow is a reactive stream of data. StateFlow is a special Flow that 'holds' the current state. When the data in a StateFlow changes, the UI automatically updates (observes) it.",
                                    "is_compose": true
                                },
                                {
                                    "name": "TransactionViewModel.kt",
                                    "type": "file",
                                    "size": 6267,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.presentation.transactions\n\nimport androidx.lifecycle.ViewModel\nimport androidx.lifecycle.viewModelScope\nimport com.sandeep.personalfinancecompanion.domain.model.Category\nimport com.sandeep.personalfinancecompanion.domain.model.Transaction\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport com.sandeep.personalfinancecompanion.domain.model.TransactionType\nimport com.sandeep.personalfinancecompanion.domain.repository.TransactionRepository\nimport com.sandeep.personalfinancecompanion.domain.repository.UserPreferencesRepository\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\nimport com.sandeep.personalfinancecompanion.domain.usecase.GetTransactionsUseCase\nimport com.sandeep.personalfinancecompanion.util.NotificationHelper\nimport dagger.hilt.android.lifecycle.HiltViewModel\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\nimport kotlinx.coroutines.flow.MutableStateFlow\nimport kotlinx.coroutines.flow.SharingStarted\nimport kotlinx.coroutines.flow.StateFlow\nimport kotlinx.coroutines.flow.asStateFlow\nimport kotlinx.coroutines.flow.combine\nimport kotlinx.coroutines.flow.first\nimport kotlinx.coroutines.flow.stateIn\nimport kotlinx.coroutines.launch\nimport java.util.Calendar\nimport java.util.UUID\nimport javax.inject.Inject\n\ndata class TransactionListState(\n    val transactions: List<Transaction> = emptyList(),\n    val selectedCurrency: Currency = Currency.INR,\n    val isLoading: Boolean = true,\n    val error: String? = null\n)\n\n@HiltViewModel\nclass TransactionViewModel @Inject constructor(\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n    private val getTransactionsUseCase: GetTransactionsUseCase,\n    private val repository: TransactionRepository,\n    private val preferencesRepository: UserPreferencesRepository,\n    private val notificationHelper: NotificationHelper\n) : ViewModel() {\n\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _searchQuery = MutableStateFlow(\"\")\n    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()\n\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)\n    val selectedFilter: StateFlow<TransactionType?> = _selectedFilter.asStateFlow()\n\n    // FLOW: A stream that 'holds' data and updates the UI when it changes.\n    private val _isLoading = MutableStateFlow(false)\n\n    val listState: StateFlow<TransactionListState> = combine(\n    // CLEAN ARCH: This class handles only ONE specific job or business rule.\n        getTransactionsUseCase(),\n        preferencesRepository.currencyFlow,\n        _searchQuery,\n        _selectedFilter,\n        _isLoading\n    ) { transactions, currency, query, filter, loading ->\n        val filtered = transactions\n            .filter { transaction ->\n                if (filter != null) transaction.type == filter else true\n            }\n            .filter { transaction ->\n                if (query.isNotBlank()) {\n                    transaction.notes.contains(query, ignoreCase = true) ||\n                            transaction.category.displayName.contains(query, ignoreCase = true)\n                } else true\n            }\n\n        TransactionListState(\n            transactions = filtered,\n            selectedCurrency = currency,\n            isLoading = loading,\n            error = null\n        )\n    }.stateIn(\n        scope = viewModelScope,\n        started = SharingStarted.WhileSubscribed(5000),\n        initialValue = TransactionListState(isLoading = true)\n    )\n\n    fun onSearchQueryChanged(query: String) {\n        _searchQuery.value = query\n    }\n\n    fun onFilterChanged(filter: TransactionType?) {\n        _selectedFilter.value = filter\n    }\n\n    fun addTransaction(\n        amount: Double,\n        type: TransactionType,\n        category: Category,\n        notes: String,\n        date: Long\n    ) {",
                                    "concept": "Hilt / DI",
                                    "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "ui",
                    "type": "directory",
                    "children": [
                        {
                            "name": "theme",
                            "type": "directory",
                            "children": [
                                {
                                    "name": "Color.kt",
                                    "type": "file",
                                    "size": 1563,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.ui.theme\n\nimport androidx.compose.ui.graphics.Color\n\n// Primary Palette - Deep Teal / Emerald\nval PrimaryDark = Color(0xFF0D1B2A)       // Very dark navy\nval PrimaryMedium = Color(0xFF1B2838)     // Dark surface\nval PrimarySurface = Color(0xFF243447)    // Card surfaces\nval PrimaryAccent = Color(0xFF00BFA6)     // Vibrant teal accent\nval PrimaryAccentLight = Color(0xFF64FFDA) // Light teal\n\n// Semantic Colors\nval IncomeGreen = Color(0xFF4CAF50)\nval IncomeGreenLight = Color(0xFF81C784)\nval ExpenseRed = Color(0xFFEF5350)\nval ExpenseRedLight = Color(0xFFE57373)\n\n// Budget Ring Colors\nval BudgetSafe = Color(0xFF4CAF50)         // Green - Safe\nval BudgetCaution = Color(0xFFFFA726)      // Orange - Caution\nval BudgetDanger = Color(0xFFEF5350)       // Red - Danger\n\n// Chart Colors\nval ChartColor1 = Color(0xFF00BFA6)\nval ChartColor2 = Color(0xFF7C4DFF)\nval ChartColor3 = Color(0xFFFF7043)\nval ChartColor4 = Color(0xFF42A5F5)\nval ChartColor5 = Color(0xFFAB47BC)\nval ChartColor6 = Color(0xFFFFCA28)\nval ChartColor7 = Color(0xFF26A69A)\nval ChartColor8 = Color(0xFFEC407A)\n\nval ChartColors = listOf(\n    ChartColor1, ChartColor2, ChartColor3, ChartColor4,\n    ChartColor5, ChartColor6, ChartColor7, ChartColor8\n)\n\n// Light Theme overrides\nval LightBackground = Color(0xFFF5F7FA)\nval LightSurface = Color(0xFFFFFFFF)\nval LightOnSurface = Color(0xFF1C1B1F)\nval LightOnBackground = Color(0xFF1C1B1F)\n\n// Text Colors\nval TextWhite = Color(0xFFFFFFFF)\nval TextGrey = Color(0xFFB0BEC5)\nval TextLightGrey = Color(0xFF78909C)",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                },
                                {
                                    "name": "Theme.kt",
                                    "type": "file",
                                    "size": 2675,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.ui.theme\n\nimport android.os.Build\nimport androidx.compose.foundation.isSystemInDarkTheme\nimport androidx.compose.material3.MaterialTheme\nimport androidx.compose.material3.darkColorScheme\nimport androidx.compose.material3.dynamicDarkColorScheme\nimport androidx.compose.material3.dynamicLightColorScheme\nimport androidx.compose.material3.lightColorScheme\nimport androidx.compose.runtime.Composable\nimport androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.platform.LocalContext\n\nprivate val DarkColorScheme = darkColorScheme(\n    primary = PrimaryAccent,\n    onPrimary = PrimaryDark,\n    primaryContainer = PrimarySurface,\n    onPrimaryContainer = PrimaryAccentLight,\n    secondary = PrimaryAccentLight,\n    onSecondary = PrimaryDark,\n    secondaryContainer = PrimarySurface,\n    onSecondaryContainer = TextWhite,\n    tertiary = ChartColor2,\n    onTertiary = Color.White,\n    background = PrimaryDark,\n    onBackground = TextWhite,\n    surface = PrimaryMedium,\n    onSurface = TextWhite,\n    surfaceVariant = PrimarySurface,\n    onSurfaceVariant = TextGrey,\n    outline = TextLightGrey,\n    error = ExpenseRed,\n    onError = Color.White\n)\n\nprivate val LightColorScheme = lightColorScheme(\n    primary = PrimaryAccent,\n    onPrimary = Color.White,\n    primaryContainer = Color(0xFFE0F2F1), // Very light teal\n    onPrimaryContainer = Color(0xFF004D40), // Dark teal\n    secondary = Color(0xFF26A69A),\n    onSecondary = Color.White,\n    secondaryContainer = Color(0xFFB2DFDB),\n    onSecondaryContainer = Color(0xFF002521),\n    tertiary = ChartColor2,\n    onTertiary = Color.White,\n    background = LightBackground,\n    onBackground = LightOnBackground,\n    surface = LightSurface,\n    onSurface = LightOnSurface,\n    surfaceVariant = Color(0xFFF1F4F9),\n    onSurfaceVariant = Color(0xFF5E6E7E),\n    outline = Color(0xFF8E9EAD),\n    error = ExpenseRed,\n    onError = Color.White\n)\n\n@Composable\nfun PersonalFinanceCompanionTheme(\n    darkTheme: Boolean = isSystemInDarkTheme(),\n    dynamicColor: Boolean = false, // Disabled to keep our custom palette\n    content: @Composable () -> Unit\n) {\n    val colorScheme = when {\n        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {\n            val context = LocalContext.current\n            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)\n        }\n        darkTheme -> DarkColorScheme\n        else -> LightColorScheme\n    }\n\n    MaterialTheme(\n        colorScheme = colorScheme,\n        typography = Typography,\n        content = content\n    )\n}",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": true
                                },
                                {
                                    "name": "Type.kt",
                                    "type": "file",
                                    "size": 2741,
                                    "layer": "Presentation",
                                    "extension": ".kt",
                                    "snippet": "package com.sandeep.personalfinancecompanion.ui.theme\n\nimport androidx.compose.material3.Typography\nimport androidx.compose.ui.text.TextStyle\nimport androidx.compose.ui.text.font.FontFamily\nimport androidx.compose.ui.text.font.FontWeight\nimport androidx.compose.ui.unit.sp\n\nval Typography = Typography(\n    displayLarge = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Bold,\n        fontSize = 34.sp,\n        lineHeight = 40.sp,\n        letterSpacing = 0.sp\n    ),\n    displayMedium = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Bold,\n        fontSize = 28.sp,\n        lineHeight = 36.sp,\n        letterSpacing = 0.sp\n    ),\n    headlineLarge = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Bold,\n        fontSize = 24.sp,\n        lineHeight = 32.sp,\n        letterSpacing = 0.sp\n    ),\n    headlineMedium = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.SemiBold,\n        fontSize = 20.sp,\n        lineHeight = 28.sp,\n        letterSpacing = 0.sp\n    ),\n    titleLarge = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.SemiBold,\n        fontSize = 18.sp,\n        lineHeight = 26.sp,\n        letterSpacing = 0.sp\n    ),\n    titleMedium = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Medium,\n        fontSize = 16.sp,\n        lineHeight = 24.sp,\n        letterSpacing = 0.15.sp\n    ),\n    bodyLarge = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Normal,\n        fontSize = 16.sp,\n        lineHeight = 24.sp,\n        letterSpacing = 0.5.sp\n    ),\n    bodyMedium = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Normal,\n        fontSize = 14.sp,\n        lineHeight = 20.sp,\n        letterSpacing = 0.25.sp\n    ),\n    bodySmall = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Normal,\n        fontSize = 12.sp,\n        lineHeight = 16.sp,\n        letterSpacing = 0.4.sp\n    ),\n    labelLarge = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Medium,\n        fontSize = 14.sp,\n        lineHeight = 20.sp,\n        letterSpacing = 0.1.sp\n    ),\n    labelMedium = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Medium,\n        fontSize = 12.sp,\n        lineHeight = 16.sp,\n        letterSpacing = 0.5.sp\n    ),\n    labelSmall = TextStyle(\n        fontFamily = FontFamily.SansSerif,\n        fontWeight = FontWeight.Medium,\n        fontSize = 10.sp,\n        lineHeight = 14.sp,\n        letterSpacing = 0.5.sp\n    )\n)",
                                    "concept": null,
                                    "explanation": "",
                                    "is_compose": false
                                }
                            ],
                            "layer": "Presentation"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "util",
                    "type": "directory",
                    "children": [
                        {
                            "name": "CurrencyFormatter.kt",
                            "type": "file",
                            "size": 1917,
                            "layer": "Other",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.util\n\nimport com.sandeep.personalfinancecompanion.domain.model.Currency\nimport java.text.NumberFormat\nimport java.util.Locale\n\nobject CurrencyFormatter {\n    \n    fun formatAmount(amount: Double, currency: Currency): String {\n        val locale = when (currency) {\n            Currency.INR -> Locale(\"en\", \"IN\")\n            Currency.USD -> Locale.US\n            Currency.EUR -> Locale.GERMANY\n            Currency.GBP -> Locale.UK\n            Currency.JPY -> Locale.JAPAN\n            Currency.CAD -> Locale.CANADA\n            Currency.AUD -> Locale(\"en\", \"AU\")\n            Currency.CHF -> Locale(\"de\", \"CH\")\n            Currency.CNY -> Locale.CHINA\n            Currency.AED -> Locale(\"ar\", \"AE\")\n        }\n\n        val format = NumberFormat.getCurrencyInstance(locale)\n        // Ensure the symbol from our enum is used if the locale one differs\n        // Or just let NumberFormat handle it. Usually, it's better to let NumberFormat handle it \n        // to get correct spacing and placement (e.g. symbol before or after).\n        return format.format(amount)\n    }\n\n    fun formatWithoutSymbol(amount: Double, currency: Currency): String {\n        val locale = when (currency) {\n            Currency.INR -> Locale(\"en\", \"IN\")\n            Currency.USD -> Locale.US\n            Currency.EUR -> Locale.GERMANY\n            Currency.GBP -> Locale.UK\n            Currency.JPY -> Locale.JAPAN\n            Currency.CAD -> Locale.CANADA\n            Currency.AUD -> Locale(\"en\", \"AU\")\n            Currency.CHF -> Locale(\"de\", \"CH\")\n            Currency.CNY -> Locale.CHINA\n            Currency.AED -> Locale(\"ar\", \"AE\")\n        }\n\n        val format = NumberFormat.getNumberInstance(locale).apply {\n            minimumFractionDigits = 2\n            maximumFractionDigits = 2\n        }\n        return format.format(amount)\n    }\n}\n",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "NotificationHelper.kt",
                            "type": "file",
                            "size": 5518,
                            "layer": "Other",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.util\n\nimport android.Manifest\nimport android.app.NotificationChannel\nimport android.app.NotificationManager\nimport android.app.PendingIntent\nimport android.content.Context\nimport android.content.Intent\nimport android.content.pm.PackageManager\nimport android.os.Build\nimport androidx.core.app.ActivityCompat\nimport androidx.core.app.NotificationCompat\nimport androidx.core.app.NotificationManagerCompat\nimport com.sandeep.personalfinancecompanion.MainActivity\nimport com.sandeep.personalfinancecompanion.R\nimport dagger.hilt.android.qualifiers.ApplicationContext\nimport javax.inject.Inject\nimport javax.inject.Singleton\n\n    // HILT: Only ONE instance of this object will exist in the whole app.\n@Singleton\nclass NotificationHelper @Inject constructor(\n    @ApplicationContext private val context: Context\n) {\n    private val notificationManager = NotificationManagerCompat.from(context)\n\n    companion object {\n        const val REMINDERS_CHANNEL_ID = \"reminders_channel\"\n        const val BUDGET_ALERTS_CHANNEL_ID = \"budget_alerts_channel\"\n        const val GOAL_ALERTS_CHANNEL_ID = \"goal_alerts_channel\"\n        \n        const val DAILY_REMINDER_NOTIFICATION_ID = 1001\n        const val BUDGET_ALERT_NOTIFICATION_ID = 1002\n        const val GOAL_REMINDER_NOTIFICATION_ID = 1003\n    }\n\n    init {\n        createNotificationChannels()\n    }\n\n    private fun createNotificationChannels() {\n        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {\n            val remindersChannel = NotificationChannel(\n                REMINDERS_CHANNEL_ID,\n                \"Reminders\",\n                NotificationManager.IMPORTANCE_DEFAULT\n            ).apply {\n                description = \"Daily reminders to log your expenses\"\n            }\n\n            val budgetChannel = NotificationChannel(\n                BUDGET_ALERTS_CHANNEL_ID,\n                \"Budget Alerts\",\n                NotificationManager.IMPORTANCE_HIGH\n            ).apply {\n                description = \"Alerts when you approach or exceed your budget\"\n            }\n\n            val goalsChannel = NotificationChannel(\n                GOAL_ALERTS_CHANNEL_ID,\n                \"Goal Reminders\",\n                NotificationManager.IMPORTANCE_DEFAULT\n            ).apply {\n                description = \"Updates on your savings goals\"\n            }\n\n            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager\n            manager.createNotificationChannel(remindersChannel)\n            manager.createNotificationChannel(budgetChannel)\n            manager.createNotificationChannel(goalsChannel)\n        }\n    }\n\n    fun showDailyReminder() {\n        val intent = Intent(context, MainActivity::class.java).apply {\n            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK\n        }\n        val pendingIntent = PendingIntent.getActivity(\n            context, 0, intent,\n            PendingIntent.FLAG_IMMUTABLE\n        )\n\n        val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)\n            .setSmallIcon(android.R.drawable.ic_popup_reminder) // Replace with app icon later\n            .setContentTitle(\"Finance Reminder\")\n            .setContentText(\"Don't forget to log your expenses for today!\")\n            .setPriority(NotificationCompat.PRIORITY_DEFAULT)\n            .setContentIntent(pendingIntent)\n            .setAutoCancel(true)\n            .build()\n\n        showNotification(DAILY_REMINDER_NOTIFICATION_ID, notification)\n    }\n\n    fun showBudgetAlert(title: String, message: String) {\n        val intent = Intent(context, MainActivity::class.java)\n        val pendingIntent = PendingIntent.getActivity(\n            context, 1, intent,\n            PendingIntent.FLAG_IMMUTABLE\n        )",
                            "concept": "Hilt / DI",
                            "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                            "is_compose": false
                        },
                        {
                            "name": "WorkManagerScheduler.kt",
                            "type": "file",
                            "size": 2032,
                            "layer": "Other",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.util\n\nimport android.content.Context\nimport androidx.work.*\nimport com.sandeep.personalfinancecompanion.worker.DailyReminderWorker\nimport com.sandeep.personalfinancecompanion.worker.GoalReminderWorker\nimport dagger.hilt.android.qualifiers.ApplicationContext\nimport java.util.*\nimport java.util.concurrent.TimeUnit\nimport javax.inject.Inject\nimport javax.inject.Singleton\n\n    // HILT: Only ONE instance of this object will exist in the whole app.\n@Singleton\nclass WorkManagerScheduler @Inject constructor(\n    @ApplicationContext private val context: Context\n) {\n    private val workManager = WorkManager.getInstance(context)\n\n    fun scheduleDailyReminder(hour: Int, minute: Int) {\n        val calendar = Calendar.getInstance()\n        val now = calendar.timeInMillis\n        \n        calendar.set(Calendar.HOUR_OF_DAY, hour)\n        calendar.set(Calendar.MINUTE, minute)\n        calendar.set(Calendar.SECOND, 0)\n        \n        if (calendar.timeInMillis <= now) {\n            calendar.add(Calendar.DAY_OF_YEAR, 1)\n        }\n        \n        val initialDelay = calendar.timeInMillis - now\n\n        val request = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)\n            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)\n            .addTag(\"daily_reminder\")\n            .build()\n\n        workManager.enqueueUniquePeriodicWork(\n            \"daily_reminder\",\n            ExistingPeriodicWorkPolicy.UPDATE,\n            request\n        )\n    }\n\n    fun cancelDailyReminder() {\n        workManager.cancelUniqueWork(\"daily_reminder\")\n    }\n\n    fun scheduleGoalReminders() {\n        val request = PeriodicWorkRequestBuilder<GoalReminderWorker>(7, TimeUnit.DAYS)\n            .addTag(\"goal_reminders\")\n            .build()\n\n        workManager.enqueueUniquePeriodicWork(\n            \"goal_reminders\",\n            ExistingPeriodicWorkPolicy.KEEP,\n            request\n        )\n    }\n\n    fun cancelGoalReminders() {\n        workManager.cancelUniqueWork(\"goal_reminders\")\n    }\n}\n",
                            "concept": "Hilt / DI",
                            "explanation": "Dependency Injection (Hilt) helps keep the code 'decoupled'. Instead of a class creating its own dependencies, Hilt 'provides' them. This makes testing and maintenance much easier.",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "worker",
                    "type": "directory",
                    "children": [
                        {
                            "name": "DailyReminderWorker.kt",
                            "type": "file",
                            "size": 671,
                            "layer": "Worker",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.worker\n\nimport android.content.Context\nimport androidx.work.CoroutineWorker\nimport androidx.work.WorkerParameters\nimport com.sandeep.personalfinancecompanion.util.NotificationHelper\nimport dagger.assisted.Assisted\nimport dagger.assisted.AssistedInject\n\nclass DailyReminderWorker @AssistedInject constructor(\n    @Assisted context: Context,\n    @Assisted params: WorkerParameters,\n    private val notificationHelper: NotificationHelper\n) : CoroutineWorker(context, params) {\n\n    // WORKMANAGER: The code that runs in the background starts here.\n    override suspend fun doWork(): Result {\n        notificationHelper.showDailyReminder()\n        return Result.success()\n    }\n}\n",
                            "concept": "Coroutines",
                            "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                            "is_compose": false
                        },
                        {
                            "name": "GoalReminderWorker.kt",
                            "type": "file",
                            "size": 1672,
                            "layer": "Worker",
                            "extension": ".kt",
                            "snippet": "package com.sandeep.personalfinancecompanion.worker\n\nimport android.content.Context\nimport androidx.work.CoroutineWorker\nimport androidx.work.WorkerParameters\nimport com.sandeep.personalfinancecompanion.domain.repository.GoalRepository\nimport com.sandeep.personalfinancecompanion.util.NotificationHelper\nimport dagger.assisted.Assisted\nimport dagger.assisted.AssistedInject\nimport kotlinx.coroutines.flow.first\nimport kotlin.math.roundToInt\n\nclass GoalReminderWorker @AssistedInject constructor(\n    @Assisted context: Context,\n    @Assisted params: WorkerParameters,\n    private val goalRepository: GoalRepository,\n    private val notificationHelper: NotificationHelper\n) : CoroutineWorker(context, params) {\n\n    // WORKMANAGER: The code that runs in the background starts here.\n    override suspend fun doWork(): Result {\n        val goals = goalRepository.getAllGoals().first()\n        val incompleteGoals = goals.filter { it.progress < 1.0f }\n\n        if (incompleteGoals.isNotEmpty()) {\n            // Pick one goal to remind about or send a summary\n            if (incompleteGoals.size == 1) {\n                val goal = incompleteGoals.first()\n                val progressPercent = (goal.progress * 100).roundToInt()\n                notificationHelper.showGoalReminder(\n                    goal.title,\n                    \"You've saved $progressPercent% so far. Keep going!\"\n                )\n            } else {\n                notificationHelper.showGoalReminder(\n                    \"Multiple Goals\",\n                    \"You have ${incompleteGoals.size} active goals. Don't forget to contribute today!\"\n                )\n            }\n        }\n\n        return Result.success()\n    }\n}\n",
                            "concept": "Coroutines",
                            "explanation": "Coroutines allow for 'Asynchronous' programming. They let you run heavy tasks (like DB or Network calls) without freezing the UI (Main Thread).",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                }
            ],
            "layer": "Other"
        },
        {
            "name": "res",
            "type": "directory",
            "children": [
                {
                    "name": "drawable",
                    "type": "directory",
                    "children": [
                        {
                            "name": "app_logo_finmate.png",
                            "type": "file",
                            "size": 586634,
                            "layer": "Other",
                            "extension": ".png"
                        },
                        {
                            "name": "app_logo_finmate_red.png",
                            "type": "file",
                            "size": 504176,
                            "layer": "Other",
                            "extension": ".png"
                        },
                        {
                            "name": "ic_launcher_background.xml",
                            "type": "file",
                            "size": 4941,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<vector\n    android:height=\"108dp\"\n    android:width=\"108dp\"\n    android:viewportHeight=\"108\"\n    android:viewportWidth=\"108\"\n    xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    <path android:fillColor=\"#3DDC84\"\n          android:pathData=\"M0,0h108v108h-108z\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M9,0L9,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,0L19,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M29,0L29,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M39,0L39,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M49,0L49,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M59,0L59,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M69,0L69,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M79,0L79,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M89,0L89,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M99,0L99,108\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,9L108,9\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,19L108,19\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,29L108,29\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,39L108,39\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,49L108,49\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,59L108,59\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,69L108,69\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,79L108,79\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,89L108,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M0,99L108,99\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,29L89,29\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,39L89,39\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,49L89,49\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,59L89,59\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,69L89,69\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M19,79L89,79\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M29,19L29,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M39,19L39,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M49,19L49,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M59,19L59,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M69,19L69,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n    <path android:fillColor=\"#00000000\" android:pathData=\"M79,19L79,89\"\n          android:strokeColor=\"#33FFFFFF\" android:strokeWidth=\"0.8\"/>\n</vector>\n",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "ic_launcher_foreground.xml",
                            "type": "file",
                            "size": 1702,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n    xmlns:aapt=\"http://schemas.android.com/aapt\"\n    android:width=\"108dp\"\n    android:height=\"108dp\"\n    android:viewportWidth=\"108\"\n    android:viewportHeight=\"108\">\n    <path android:pathData=\"M31,63.928c0,0 6.4,-11 12.1,-13.1c7.2,-2.6 26,-1.4 26,-1.4l38.1,38.1L107,108.928l-32,-1L31,63.928z\">\n        <aapt:attr name=\"android:fillColor\">\n            <gradient\n                android:endX=\"85.84757\"\n                android:endY=\"92.4963\"\n                android:startX=\"42.9492\"\n                android:startY=\"49.59793\"\n                android:type=\"linear\">\n                <item\n                    android:color=\"#44000000\"\n                    android:offset=\"0.0\" />\n                <item\n                    android:color=\"#00000000\"\n                    android:offset=\"1.0\" />\n            </gradient>\n        </aapt:attr>\n    </path>\n    <path\n        android:fillColor=\"#FFFFFF\"\n        android:fillType=\"nonZero\"\n        android:pathData=\"M65.3,45.828l3.8,-6.6c0.2,-0.4 0.1,-0.9 -0.3,-1.1c-0.4,-0.2 -0.9,-0.1 -1.1,0.3l-3.9,6.7c-6.3,-2.8 -13.4,-2.8 -19.7,0l-3.9,-6.7c-0.2,-0.4 -0.7,-0.5 -1.1,-0.3C38.8,38.328 38.7,38.828 38.9,39.228l3.8,6.6C36.2,49.428 31.7,56.028 31,63.928h46C76.3,56.028 71.8,49.428 65.3,45.828zM43.4,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2c-0.3,-0.7 -0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C45.3,56.528 44.5,57.328 43.4,57.328L43.4,57.328zM64.6,57.328c-0.8,0 -1.5,-0.5 -1.8,-1.2s-0.1,-1.5 0.4,-2.1c0.5,-0.5 1.4,-0.7 2.1,-0.4c0.7,0.3 1.2,1 1.2,1.8C66.5,56.528 65.6,57.328 64.6,57.328L64.6,57.328z\"\n        android:strokeWidth=\"1\"\n        android:strokeColor=\"#00000000\" />\n</vector>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-anydpi-v26",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.xml",
                            "type": "file",
                            "size": 341,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    <background android:drawable=\"@drawable/ic_launcher_background\"/>\n    <foreground android:drawable=\"@mipmap/ic_launcher_foreground\"/>\n    <monochrome android:drawable=\"@mipmap/ic_launcher_foreground\"/>\n</adaptive-icon>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "ic_launcher_round.xml",
                            "type": "file",
                            "size": 341,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<adaptive-icon xmlns:android=\"http://schemas.android.com/apk/res/android\">\n    <background android:drawable=\"@drawable/ic_launcher_background\"/>\n    <foreground android:drawable=\"@mipmap/ic_launcher_foreground\"/>\n    <monochrome android:drawable=\"@mipmap/ic_launcher_foreground\"/>\n</adaptive-icon>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-hdpi",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.webp",
                            "type": "file",
                            "size": 4704,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_foreground.webp",
                            "type": "file",
                            "size": 18344,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_round.webp",
                            "type": "file",
                            "size": 6042,
                            "layer": "Other",
                            "extension": ".webp"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-mdpi",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.webp",
                            "type": "file",
                            "size": 2626,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_foreground.webp",
                            "type": "file",
                            "size": 8788,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_round.webp",
                            "type": "file",
                            "size": 3414,
                            "layer": "Other",
                            "extension": ".webp"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-xhdpi",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.webp",
                            "type": "file",
                            "size": 7338,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_foreground.webp",
                            "type": "file",
                            "size": 32164,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_round.webp",
                            "type": "file",
                            "size": 9734,
                            "layer": "Other",
                            "extension": ".webp"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-xxhdpi",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.webp",
                            "type": "file",
                            "size": 14082,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_foreground.webp",
                            "type": "file",
                            "size": 74344,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_round.webp",
                            "type": "file",
                            "size": 18090,
                            "layer": "Other",
                            "extension": ".webp"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "mipmap-xxxhdpi",
                    "type": "directory",
                    "children": [
                        {
                            "name": "ic_launcher.webp",
                            "type": "file",
                            "size": 23270,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_foreground.webp",
                            "type": "file",
                            "size": 134746,
                            "layer": "Other",
                            "extension": ".webp"
                        },
                        {
                            "name": "ic_launcher_round.webp",
                            "type": "file",
                            "size": 28656,
                            "layer": "Other",
                            "extension": ".webp"
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "values",
                    "type": "directory",
                    "children": [
                        {
                            "name": "colors.xml",
                            "type": "file",
                            "size": 378,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <color name=\"purple_200\">#FFBB86FC</color>\n    <color name=\"purple_500\">#FF6200EE</color>\n    <color name=\"purple_700\">#FF3700B3</color>\n    <color name=\"teal_200\">#FF03DAC5</color>\n    <color name=\"teal_700\">#FF018786</color>\n    <color name=\"black\">#FF000000</color>\n    <color name=\"white\">#FFFFFFFF</color>\n</resources>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "strings.xml",
                            "type": "file",
                            "size": 72,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<resources>\n    <string name=\"app_name\">Fin mate</string>\n</resources>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "themes.xml",
                            "type": "file",
                            "size": 165,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n    <style name=\"Theme.PersonalFinanceCompanion\" parent=\"android:Theme.Material.Light.NoActionBar\" />\n</resources>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                },
                {
                    "name": "xml",
                    "type": "directory",
                    "children": [
                        {
                            "name": "backup_rules.xml",
                            "type": "file",
                            "size": 478,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?><!--\n   Sample backup rules file; uncomment and customize as necessary.\n   See https://developer.android.com/guide/topics/data/autobackup\n   for details.\n   Note: This file is ignored for devices older than API 31\n   See https://developer.android.com/about/versions/12/backup-restore\n-->\n<full-backup-content>\n    <!--\n   <include domain=\"sharedpref\" path=\".\"/>\n   <exclude domain=\"sharedpref\" path=\"device.xml\"/>\n-->\n</full-backup-content>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        },
                        {
                            "name": "data_extraction_rules.xml",
                            "type": "file",
                            "size": 551,
                            "layer": "Other",
                            "extension": ".xml",
                            "snippet": "<?xml version=\"1.0\" encoding=\"utf-8\"?><!--\n   Sample data extraction rules file; uncomment and customize as necessary.\n   See https://developer.android.com/about/versions/12/backup-restore#xml-changes\n   for details.\n-->\n<data-extraction-rules>\n    <cloud-backup>\n        <!-- TODO: Use <include> and <exclude> to control what is backed up.\n        <include .../>\n        <exclude .../>\n        -->\n    </cloud-backup>\n    <!--\n    <device-transfer>\n        <include .../>\n        <exclude .../>\n    </device-transfer>\n    -->\n</data-extraction-rules>",
                            "concept": null,
                            "explanation": "",
                            "is_compose": false
                        }
                    ],
                    "layer": "Other"
                }
            ],
            "layer": "Other"
        }
    ]
};