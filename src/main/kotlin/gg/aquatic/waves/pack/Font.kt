package gg.aquatic.waves.pack

class Font(
    val id: String,
    val spacing: Int,
    val baseHeight: Int,
    val namespace: String,
    val ascent: Int,
) {
    val glyphs = mutableMapOf<Int,MutableMap<Char,MutableList<Pair<Int, Glyph>>>>()
}