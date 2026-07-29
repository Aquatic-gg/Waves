package gg.aquatic.quickminimessage.parser

internal data class TagToken(
    val rawStart: Int,
    val rawEnd: Int,
    val name: String,
    val args: List<String>,
    val isClosing: Boolean,
    val isSelfClosing: Boolean,
    val endIndex: Int
)

