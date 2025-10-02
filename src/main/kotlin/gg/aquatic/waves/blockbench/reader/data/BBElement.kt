package gg.aquatic.waves.blockbench.reader.data

import java.util.UUID

class BBElement(
    val name: String,
    val box_uv: Boolean,
    val rescale: Boolean,
    val locked: Boolean,
    val from: Array<Double>,
    val to: Array<Double>,
    val autouv: Int,
    val color: Int,
    val inflate: Double,
    val rotation: Array<Double>,
    val origin: Array<Double>,
    val uv_offset: Array<Int>,
    val faces: BBFaces,
    val type: String,
    val uuid: UUID
) {
}