package com.sandeep.personalfinancecompanion.data.remote

import com.sandeep.personalfinancecompanion.data.remote.dto.TransactionDto
import com.sandeep.personalfinancecompanion.data.remote.dto.TransactionListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class TransactionApiService @Inject constructor(
    private val client: HttpClient
) {

    suspend fun getAllTransactions(): List<TransactionDto> {
        val response: TransactionListResponse = client.get("/api/transactions").body()
        return response.transactions
    }

    suspend fun addTransaction(dto: TransactionDto): TransactionDto {
        return client.post("/api/transactions") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    suspend fun updateTransaction(dto: TransactionDto): TransactionDto {
        return client.put("/api/transactions/${dto.id}") {
            contentType(ContentType.Application.Json)
            setBody(dto)
        }.body()
    }

    suspend fun deleteTransaction(id: String) {
        client.delete("/api/transactions/$id")
    }
}
