package com.sandeep.personalfinancecompanion.voiceagent.data

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.TransactionType
import com.sandeep.personalfinancecompanion.voiceagent.domain.VoiceAgentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Ported from the standalone Agent project.
 * Uses Gemini 2.5 Flash to extract financial data from text.
 */
class GeminiDataSource(apiKey: String) {

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey,
        systemInstruction = content {
            text("""
                You are a "Finance Data Extractor". Your ONLY job is to extract financial data from user text and return a JSON block.
                
                RULES:
                1. Always output a valid JSON block containing: amount (Double), category (String), note (String), isExpense (Boolean).
                2. Extract the Category intelligently (e.g., Petrol -> TRANSPORT, Lunch -> FOOD, Salary -> SALARY).
                3. USE ONLY THESE CATEGORIES: FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTH, EDUCATION, SALARY, INVESTMENT, GIFT, OTHER.
                4. If the user mentions "added" or "income" or "got", set isExpense to false.
                5. Do NOT include any other text in your response. Just the JSON block.
                
                EXAMPLE OUTPUT:
                {"amount": 100.0, "category": "FOOD", "note": "Chai", "isExpense": true}
            """.trimIndent())
        }
    )

    suspend fun extractTransactionData(text: String): VoiceAgentResult? = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(text)
            val rawText = response.text ?: return@withContext null
            
            Log.d("VoiceAgent", "Gemini Raw Response: $rawText")
            
            return@withContext parseJsonResponse(rawText)
        } catch (e: Exception) {
            Log.e("VoiceAgent", "Gemini API Error", e)
            null
        }
    }

    private fun parseJsonResponse(text: String): VoiceAgentResult? {
        return try {
            val startIndex = text.indexOf('{')
            val endIndex = text.lastIndexOf('}')
            
            if (startIndex == -1 || endIndex == -1) return null
            
            val jsonPart = text.substring(startIndex, endIndex + 1)
            val json = JSONObject(jsonPart)
            
            val categoryStr = json.getString("category").uppercase()
            val category = try {
                Category.valueOf(categoryStr)
            } catch (e: Exception) {
                Category.OTHER
            }

            VoiceAgentResult(
                amount = json.getDouble("amount"),
                category = category,
                type = if (json.getBoolean("isExpense")) TransactionType.EXPENSE else TransactionType.INCOME,
                notes = json.getString("note"),
                confidence = 0.95f,
                isReadyToSave = true
            )
        } catch (e: Exception) {
            Log.e("VoiceAgent", "Parsing failed", e)
            null
        }
    }
}
