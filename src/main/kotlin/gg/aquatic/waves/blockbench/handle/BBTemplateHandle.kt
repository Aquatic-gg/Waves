package gg.aquatic.waves.blockbench.handle

import gg.aquatic.waves.blockbench.animation.AnimationHandler
import gg.aquatic.waves.blockbench.template.BBTemplate
import org.bukkit.Location

class BBTemplateHandle(
    val template: BBTemplate,
    val location: Location,
    val parentBones: MutableList<BBBone>,
    val onTickBone: (bone: BBBone) -> Unit,
    val onTickPart: (part: BBPart) -> Unit,
) {

    val animationHandler = AnimationHandler(this)

    fun tick() {
        animationHandler.update()
        for (parentBone in parentBones) {
            parentBone.spawn(location.clone(),this,null,null,null)
        }
    }

}