package gg.aquatic.waves.blockbench.template

import gg.aquatic.waves.blockbench.handle.BBBone
import gg.aquatic.waves.blockbench.handle.BBPart
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class BBPartTemplate(
    val name: String,
    //val particle: Particle,
    val origin: Vector,
    val translation: Vector,
    val rotation: EulerAngle
) {

    fun create(bone: BBBone): BBPart {
        return BBPart(this, bone)
    }
}