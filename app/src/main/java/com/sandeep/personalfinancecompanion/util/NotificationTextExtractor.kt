package com.sandeep.personalfinancecompanion.util

import android.os.Bundle

data class NotificationTextSnapshot(
    val title: String = "",
    val text: String = "",
    val bigText: String = "",
    val titleBig: String = "",
    val textLines: List<String> = emptyList(),
    val messageTexts: List<String> = emptyList(),
    val tickerText: String = ""
)

object NotificationTextExtractor {
    fun fromSnapshot(snapshot: NotificationTextSnapshot): String {
        return buildList {
            addIfNotBlank(snapshot.title)
            addIfNotBlank(snapshot.text)
            addIfNotBlank(snapshot.bigText)
            addIfNotBlank(snapshot.titleBig)
            addIfNotBlank(snapshot.tickerText)
            snapshot.textLines.forEach { addIfNotBlank(it) }
            snapshot.messageTexts.forEach { addIfNotBlank(it) }
        }
            .joinToString(" ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun candidateTextsFromSnapshot(snapshot: NotificationTextSnapshot): List<String> {
        return buildList {
            addIfNotBlank(snapshot.title)
            addIfNotBlank(snapshot.text)
            addIfNotBlank(snapshot.bigText)
            addIfNotBlank(snapshot.titleBig)
            addIfNotBlank(snapshot.tickerText)
            snapshot.textLines.forEach { addIfNotBlank(it) }
            snapshot.messageTexts.forEach { addIfNotBlank(it) }
        }.distinctBy { normalizeCandidateText(it) }
    }

    fun fromExtras(extras: Bundle): NotificationTextSnapshot {
        val textLines = extras.getCharSequenceArray("android.textLines")
            ?.mapNotNull { it?.toString()?.trim()?.takeIf { value -> value.isNotBlank() } }
            .orEmpty()
        val messageTexts = extractMessageTexts(extras)

        return NotificationTextSnapshot(
            title = extras.getCharSequence("android.title")?.toString().orEmpty().trim(),
            text = extras.getCharSequence("android.text")?.toString().orEmpty().trim(),
            bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty().trim(),
            titleBig = extras.getCharSequence("android.title.big")?.toString().orEmpty().trim(),
            textLines = textLines,
            messageTexts = messageTexts,
            tickerText = extras.getCharSequence("tickerText")?.toString().orEmpty().trim()
        )
    }

    private fun extractMessageTexts(extras: Bundle): List<String> {
        val rawMessages = extras.get("android.messages") ?: return emptyList()
        val candidates = when (rawMessages) {
            is Array<*> -> rawMessages.asList()
            is List<*> -> rawMessages
            else -> emptyList<Any?>()
        }

        return candidates.mapNotNull { candidate ->
            when (candidate) {
                is Bundle -> candidate.getCharSequence("text")?.toString()
                else -> candidate?.toString()
            }?.trim()?.takeIf { value -> value.isNotBlank() }
        }
    }

    private fun MutableList<String>.addIfNotBlank(value: String) {
        if (value.isNotBlank()) add(value)
    }

    private fun normalizeCandidateText(value: String): String {
        return value
            .replace(Regex("\\s+"), " ")
            .trim()
            .lowercase()
    }
}
