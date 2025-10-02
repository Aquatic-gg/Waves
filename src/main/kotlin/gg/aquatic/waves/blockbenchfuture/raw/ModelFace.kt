package gg.aquatic.waves.blockbenchfuture.raw

data class ModelFace(
    val north: ModelUV,
    val south: ModelUV,
    val east: ModelUV,
    val west: ModelUV,
    val up: ModelUV,
    val down: ModelUV,
) {

    fun hasTexture(): Boolean {
        return north.texture != null && south.texture != null && east.texture != null && west.texture != null && up.texture != null && down.texture != null
    }

}