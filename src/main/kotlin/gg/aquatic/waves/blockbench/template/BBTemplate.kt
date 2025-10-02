package gg.aquatic.waves.blockbench.template

import gg.aquatic.waves.blockbench.handle.BBBone
import gg.aquatic.waves.blockbench.handle.BBPart
import gg.aquatic.waves.blockbench.handle.BBTemplateHandle
import org.bukkit.Location

class BBTemplate(
    val name: String,
    val parentBones: List<BBBoneTemplate>,
    val animations: Map<String, BBAnimationTemplate>
) {

    fun spawn(location: Location, onTickBone: (BBBone) -> Unit, onTickPart: (BBPart) -> Unit): BBTemplateHandle {
        val parentBones: MutableList<BBBone> = ArrayList()
        for (bone in this.parentBones) {
            parentBones.add(bone.create())
        }
        return BBTemplateHandle(this, location, parentBones, onTickBone, onTickPart)
    }
}