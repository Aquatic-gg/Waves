package gg.aquatic.waves.blockbench.raw

import com.google.gson.JsonDeserializer

data class ModelPlaceholder(
    val variables: Map<String, String>,
) {

    companion object {
        val EMPTY = ModelPlaceholder(emptyMap())

        val PARSER = JsonDeserializer<ModelPlaceholder> { json, _, _ ->
            val value =
                json.asString.trim().split("\n").map { line -> line.split("=", limit = 2) }.filter { it.size == 2 }
                    .associate { it[0] to it[1] }
            return@JsonDeserializer ModelPlaceholder(value)
        }
    }

    fun parse(expression: String): String {
        var final = expression
        variables.forEach { (key, value) ->
            final = final.replace(key, value)
        }
        return final
    }
}