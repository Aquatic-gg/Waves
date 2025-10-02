package gg.aquatic.waves.blockbench.reader.data

import java.util.*

class BBBone(
    val name: String,
    val origin: Array<Double>,
    val rotation: Array<Double>,
    val color: Int,
    val boneType: String,
    val uuid: UUID,
    val export: Boolean,
    val mirror_uv: Boolean,
    val isOpen: Boolean,
    val locked: Boolean,
    val visibility: Boolean,
    val autoUv: Int,
    val children: Array<BBChildren> = arrayOf()
): BBChildren {
}