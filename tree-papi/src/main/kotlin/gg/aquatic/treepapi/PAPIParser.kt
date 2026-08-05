package gg.aquatic.treepapi

object PAPIParser {
    fun parse(input: String): List<String> {
        if (input.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        val builder = StringBuilder()
        var inQuotes = false

        for (char in input) {
            when (char) {
                '"' -> inQuotes = !inQuotes
                '_' -> {
                    if (!inQuotes) {
                        if (builder.isNotEmpty()) {
                            result.add(builder.toString())
                            builder.setLength(0)
                        }
                    } else {
                        builder.append(char)
                    }
                }
                else -> builder.append(char)
            }
        }
        if (builder.isNotEmpty()) result.add(builder.toString())
        return result
    }
}