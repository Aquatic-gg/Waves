package gg.aquatic.waves.blockbench.reader.data

import java.util.UUID

class BBAnimation(
    val uuid: UUID,
    val name: String,
    val loop: String,
    val override: Boolean,
    val length: Double,
    val snapping: Int,
    val selected: Boolean,
    val anim_time_update: String,
    val blend_weight: String,
    val start_delay: String,
    val loop_delay: String,
    val animators: Map<String, BBAnimator>
) {
}