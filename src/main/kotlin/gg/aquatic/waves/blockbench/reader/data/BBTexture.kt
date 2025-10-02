package gg.aquatic.waves.blockbench.reader.data

import java.util.UUID

class BBTexture(
    val path: String,
    val name: String,
    val folder: String,
    val namespace: String,
    val id: String,
    val particle: Boolean,
    val render_mode: String,
    val render_sides: String,
    val frame_time: Int,
    val frame_order_type: String,
    val frame_order: String,
    val frame_interpolate: Boolean,
    val visible: Boolean,
    val mode: String,
    val saved: Boolean,
    val uuid: UUID,
    val source: String,
    val relative_path: String
) {
}