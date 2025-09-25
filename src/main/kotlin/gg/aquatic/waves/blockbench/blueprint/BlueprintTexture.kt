package gg.aquatic.waves.blockbench.blueprint

import gg.aquatic.waves.blockbench.raw.ModelResolution
import java.awt.image.BufferedImage

data class BlueprintTexture(
    val name: String,
    val image: BufferedImage,
    val uvWidth: Int,
    val uvHeight: Int
) {

    val resolution: ModelResolution
        get() {
            return ModelResolution(image.width, image.height)
        }
}