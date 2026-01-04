package ronsijm.templater.cli

import ronsijm.templater.services.SystemOperationsService

class CliSystemOperationsService : SystemOperationsService {

    override fun prompt(
        promptText: String,
        defaultValue: String?,
        multiLine: Boolean,
        password: Boolean
    ): String? {
        print("$promptText${if (defaultValue != null) " [$defaultValue]" else ""}: ")
        val input = if (multiLine) {
            val lines = mutableListOf<String>()
            println("(Enter empty line to finish)")
            while (true) {
                val line = readlnOrNull() ?: break
                if (line.isEmpty()) break
                lines.add(line)
            }
            lines.joinToString("\n")
        } else {
            readlnOrNull() ?: ""
        }

        return if (input.isEmpty() && defaultValue != null) defaultValue else input
    }

    override fun suggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): Any? {
        if (placeholder.isNotEmpty()) println(placeholder)
        textItems.forEachIndexed { index, item ->
            println("  ${index + 1}. $item")
        }
        print("Enter number (1-${textItems.size}): ")

        val input = readlnOrNull()?.toIntOrNull()
        return if (input != null && input in 1..values.size) {
            values[input - 1]
        } else {
            null
        }
    }

    override fun multiSuggester(
        textItems: List<*>,
        values: List<*>,
        throwOnCancel: Boolean,
        placeholder: String,
        limit: Int?
    ): List<*>? {
        if (placeholder.isNotEmpty()) println(placeholder)
        textItems.forEachIndexed { index, item ->
            println("  ${index + 1}. $item")
        }
        print("Enter numbers (e.g., 1,3,5): ")

        val input = readlnOrNull() ?: return null
        val indices = input.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..values.size }
            .map { it - 1 }

        return if (indices.isNotEmpty()) {
            indices.map { values[it] }
        } else {
            null
        }
    }
}
