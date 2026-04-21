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
        modelName = "gemini-2.0-flash",
        apiKey = apiKey,
        systemInstruction = content {
            text("""
                You are a "Finance Data Extractor". Your job is to extract financial transactions from user text and return a JSON ARRAY of objects.
                
                RULES:
                1. Always output a valid JSON ARRAY containing objects with: amount (Double), category (String), note (String), type (String), peer_name (String or null).
                2. Transaction 'type' must be one of: INCOME, EXPENSE, BORROWED, LENT.
                3. Evaluate the transaction 'type' specifically for EACH detected amount:
                   - BORROWED: Use when user "took" money from someone or says "udhar liye".
                   - LENT: Use when user "gave" money to someone or says "udhar diye".
                   - INCOME/EXPENSE: Standard for other transactions.
                4. Extract 'peer_name' if a person's name is mentioned (e.g., "Rahul", "Papa", "Sandeep").
                5. Extract the Category intelligently (e.g., Petrol -> TRANSPORT, Lunch -> FOOD).
                6. USE ONLY THESE CATEGORIES: FOOD, TRANSPORT, SHOPPING, ENTERTAINMENT, BILLS, HEALTH, EDUCATION, SALARY, INVESTMENT, GIFT, OTHER.
                7. Do NOT include any other text in your response. Just the JSON ARRAY.
                
                EXAMPLES:
                - Input: "500 udhar diye Rahul ko petrol ke liye"
                  Output: [{"amount": 500.0, "category": "TRANSPORT", "note": "Udhar", "type": "LENT", "peer_name": "Rahul"}]
                
                - Input: "Received 50000 salary and borrowed 2000 from Rohit"
                  Output: [{"amount": 50000.0, "category": "SALARY", "note": "Salary", "type": "INCOME", "peer_name": null}, {"amount": 2000.0, "category": "OTHER", "note": "Borrowed", "type": "BORROWED", "peer_name": "Rohit"}]
            """.trimIndent())
        }
    )

    suspend fun extractTransactionData(text: String): List<VoiceAgentResult> = withContext(Dispatchers.IO) {
        try {
            val response = model.generateContent(text)
            val rawText = response.text ?: return@withContext emptyList()
            
            Log.d("VoiceAgent", "Gemini Raw Response: $rawText")
            
            return@withContext parseJsonResponse(rawText)
        } catch (e: Exception) {
            Log.e("VoiceAgent", "Gemini API Error", e)
            emptyList()
        }
    }

    private fun parseJsonResponse(text: String): List<VoiceAgentResult> {
        val results = mutableListOf<VoiceAgentResult>()
        try {
            val startIndex = text.indexOf('[')
            val endIndex = text.lastIndexOf(']')
            
            if (startIndex == -1 || endIndex == -1) {
                // Try parsing as a single object if array markers are missing
                val singleObjStart = text.indexOf('{')
                val singleObjEnd = text.lastIndexOf('}')
                if (singleObjStart != -1 && singleObjEnd != -1) {
                    val jsonPart = text.substring(singleObjStart, singleObjEnd + 1)
                    parseSingleObject(JSONObject(jsonPart))?.let { results.add(it) }
                }
                return results
            }
            
            val jsonPart = text.substring(startIndex, endIndex + 1)
            val jsonArray = org.json.JSONArray(jsonPart)
            
            for (i in 0 until jsonArray.length()) {
                val json = jsonArray.getJSONObject(i)
                parseSingleObject(json)?.let { results.add(it) }
            }
        } catch (e: Exception) {
            Log.e("VoiceAgent", "Parsing failed", e)
        }
        return results
    }

    private fun parseSingleObject(json: JSONObject): VoiceAgentResult? {
        return try {
            val categoryStr = json.getString("category").uppercase()
            val category = try {
                Category.valueOf(categoryStr)
            } catch (e: Exception) {
                Category.OTHER
            }

            val typeStr = json.getString("type").uppercase()
            val type = try {
                TransactionType.valueOf(typeStr)
            } catch (e: Exception) {
                TransactionType.EXPENSE
            }

            VoiceAgentResult(
                amount = json.getDouble("amount"),
                category = category,
                type = type,
                notes = json.getString("note"),
                peerName = if (json.has("peer_name") && !json.isNull("peer_name")) json.getString("peer_name") else null,
                confidence = 0.95f,
                isReadyToSave = true
            )
        } catch (e: Exception) {
            null
        }
    }
}
