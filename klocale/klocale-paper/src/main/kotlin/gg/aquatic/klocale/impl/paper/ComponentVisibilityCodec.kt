package gg.aquatic.klocale.impl.paper

import java.util.Base64

data class ComponentVisibilityPayload(
    val id: String,
    val args: Map<String, String?> = emptyMap(),
)

object ComponentVisibilityCodec {
    private val encoder = Base64.getUrlEncoder().withoutPadding()
    private val decoder = Base64.getUrlDecoder()

    fun encode(payloads: List<ComponentVisibilityPayload>): String {
        return payloads.joinToString(";") { payload ->
            val encodedId = encodePart(payload.id)
            val encodedArgs = payload.args.entries.joinToString("&") { (key, value) ->
                "${encodePart(key)}=${value?.let(::encodePart) ?: "~"}"
            }
            if (encodedArgs.isEmpty()) encodedId else "$encodedId?$encodedArgs"
        }
    }

    fun decode(raw: String): List<ComponentVisibilityPayload> {
        if (raw.isBlank()) return emptyList()
        return raw.split(';').mapNotNull { token ->
            if (token.isBlank()) return@mapNotNull null
            val parts = token.split('?', limit = 2)
            val id = decodePart(parts[0]) ?: return@mapNotNull null
            val args = if (parts.size < 2 || parts[1].isBlank()) {
                emptyMap()
            } else {
                parts[1].split('&').mapNotNull { pair ->
                    if (pair.isBlank()) return@mapNotNull null
                    val kv = pair.split('=', limit = 2)
                    val key = decodePart(kv[0]) ?: return@mapNotNull null
                    val value = kv.getOrNull(1)?.takeUnless { it == "~" }?.let(::decodePart)
                    key to value
                }.toMap()
            }
            ComponentVisibilityPayload(id, args)
        }
    }

    private fun encodePart(value: String): String {
        return encoder.encodeToString(value.toByteArray(Charsets.UTF_8))
    }

    private fun decodePart(value: String): String? {
        return runCatching { String(decoder.decode(value), Charsets.UTF_8) }.getOrNull()
    }
}
