package gg.aquatic.waves.blockbench.template

import gg.aquatic.waves.blockbench.handle.BBBone
import gg.aquatic.waves.blockbench.handle.BBPart
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class BBBoneTemplate(
    //val particle: Particle,
    val name: String,
    val origin: Vector,
    val rotation: EulerAngle,
    val parts: List<BBPartTemplate>,
    val children: List<BBBoneTemplate>
) {

    fun create(): BBBone {
        val children: MutableList<BBBone> = ArrayList()
        val parts: MutableList<BBPart> = ArrayList()

        val bone = BBBone(this, children, parts)

        for (bone in this.children) {
            children.add(bone.create())
        }
        for (part in this.parts) {
            parts.add(part.create(bone))
        }
        return bone
    }

}