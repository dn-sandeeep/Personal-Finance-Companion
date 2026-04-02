package com.sandeep.personalfinancecompanion.data.mapper

import com.sandeep.personalfinancecompanion.data.local.entity.TransactionEntity
import com.sandeep.personalfinancecompanion.domain.model.Category
import com.sandeep.personalfinancecompanion.domain.model.Transaction
import com.sandeep.personalfinancecompanion.domain.model.TransactionType

fun TransactionEntity.toDomain(): Transaction {
    return Transaction(
        id = id,
        amount = amount,
        type = try {
            TransactionType.valueOf(type)
        } catch (e: IllegalArgumentException) {
            TransactionType.EXPENSE
        },
        category = try {
            Category.valueOf(category)
        } catch (e: IllegalArgumentException) {
            Category.OTHER
        },
        date = date,
        notes = notes
    )
}

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        category = category.name,
        date = date,
        notes = notes
    )
}
