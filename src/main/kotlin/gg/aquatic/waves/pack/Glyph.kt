package gg.aquatic.waves.pack

class Glyph(
    val id: String,
    val namespace: String,
    val font: String,
    val character: Char,
    val width: Int,
    val height: Int,
) {

    val iconText = "<!shadow><font:${namespace}:$font>$character</font>"

}