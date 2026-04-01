package com.sandeep.personalfinancecompanion.data.remote

import com.sandeep.personalfinancecompanion.data.remote.dto.TransactionDto
import com.sandeep.personalfinancecompanion.data.remote.dto.TransactionListResponse
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

/**
 * Fake backend engine that intercepts all Ktor HTTP calls.
 * Simulates network latency and maintains an in-memory data store.
 * Demonstrates how the app would work with a real REST API.
 */
object FakeMockEngine {

    private val json = Json { ignoreUnknownKeys = true }

    // In-memory data store acting as our "server database"
    private val transactionsStore = mutableListOf(
        TransactionDto("1", 55000.0, "INCOME", "SALARY", 1743465600000, "March salary"),
        TransactionDto("2", 1200.0, "EXPENSE", "FOOD", 1743552000000, "Groceries at BigBasket"),
        TransactionDto("3", 500.0, "EXPENSE", "TRANSPORT", 1743638400000, "Uber to office"),
        TransactionDto("4", 2500.0, "EXPENSE", "SHOPPING", 1743724800000, "Amazon order"),
        TransactionDto("5", 800.0, "EXPENSE", "ENTERTAINMENT", 1743811200000, "Movie night"),
        TransactionDto("6", 3000.0, "EXPENSE", "BILLS", 1743897600000, "Electricity bill"),
        TransactionDto("7", 15000.0, "INCOME", "FREELANCE", 1743984000000, "Client project payment"),
        TransactionDto("8", 450.0, "EXPENSE", "FOOD", 1744070400000, "Dinner at restaurant"),
        TransactionDto("9", 1500.0, "EXPENSE", "HEALTH", 1744156800000, "Medicine and consultation"),
        TransactionDto("10", 5000.0, "INCOME", "INVESTMENT", 1744243200000, "Dividend income"),
    )

    private var nextId = 11

    val engine = MockEngine { request ->
        // Simulate network latency (300-600ms)
        delay((300L..600L).random())

        val path = request.url.encodedPath

        when {
            // GET /api/transactions
            path == "/api/transactions" && request.method == HttpMethod.Get -> {
                val responseBody = json.encodeToString(
                    TransactionListResponse.serializer(),
                    TransactionListResponse(transactionsStore.toList())
                )
                respond(
                    content = responseBody,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }

            // POST /api/transactions
            path == "/api/transactions" && request.method == HttpMethod.Post -> {
                val body = request.body.toByteArray().decodeToString()
                val dto = json.decodeFromString(TransactionDto.serializer(), body)
                val newDto = dto.copy(id = (nextId++).toString())
                transactionsStore.add(0, newDto)
                respond(
                    content = json.encodeToString(TransactionDto.serializer(), newDto),
                    status = HttpStatusCode.Created,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }

            // PUT /api/transactions/{id}
            path.startsWith("/api/transactions/") && request.method == HttpMethod.Put -> {
                val body = request.body.toByteArray().decodeToString()
                val dto = json.decodeFromString(TransactionDto.serializer(), body)
                val index = transactionsStore.indexOfFirst { it.id == dto.id }
                if (index != -1) {
                    transactionsStore[index] = dto
                }
                respond(
                    content = json.encodeToString(TransactionDto.serializer(), dto),
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }

            // DELETE /api/transactions/{id}
            path.startsWith("/api/transactions/") && request.method == HttpMethod.Delete -> {
                val id = path.substringAfterLast("/")
                transactionsStore.removeAll { it.id == id }
                respond(
                    content = "",
                    status = HttpStatusCode.NoContent,
                    headers = headersOf()
                )
            }

            else -> {
                respond(
                    content = """{"error": "Not Found"}""",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }
    }
}
