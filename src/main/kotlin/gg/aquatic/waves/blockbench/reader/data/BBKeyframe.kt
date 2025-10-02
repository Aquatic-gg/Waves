package gg.aquatic.waves.blockbench.reader.data

import java.util.UUID

class BBKeyframe(
    val channel: String,
    val data_points: Array<BBDatapoints>,
    val uuid: UUID,
    val time: Double,
    val color: Int,
    val interpolation: String
) {
}