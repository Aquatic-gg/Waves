package gg.aquatic.waves.blockbench.raw

import gg.aquatic.waves.blockbench.blueprint.BlueprintTexture
import java.io.ByteArrayInputStream
import java.util.Base64
import javax.imageio.ImageIO

class ModelTexture(
    val name: String,
    val source: String,
    val width: Int,
    val height: Int,
    val uv_width: Int,
    val uv_height: Int
) {

    fun toBlueprint(): BlueprintTexture {
        val img = ByteArrayInputStream(Base64.getDecoder().decode(source.split(",")[1])).use { ImageIO.read(it) }
        return BlueprintTexture(name.split("\\.")[0], img, uv_width, uv_height)
    }
}