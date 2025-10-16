package gg.aquatic.waves.blockbench.handle

import gg.aquatic.waves.blockbench.template.BBBoneTemplate
import gg.aquatic.waves.blockbench.Quaternion
import org.bukkit.Location
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class BBBone(
    val template: BBBoneTemplate,
    val children: MutableList<BBBone>,
    val parts: MutableList<BBPart>,
) {

    fun spawn(
        location: Location,
        bbTemplateHandle: BBTemplateHandle,
        parentOrigin: Vector?,
        parentPivot: Vector?,
        parentAngle: EulerAngle?,
        parentScale: Vector,
    ) {
        val currentParentOrigin = parentOrigin ?: Vector()
        val currentParentPivot = parentPivot ?: Vector()
        val currentParentAngle = parentAngle ?: EulerAngle.ZERO

        val rotation: EulerAngle = getFinalAngle(currentParentAngle, bbTemplateHandle)

        val scale = parentScale.clone().multiply(getFinalScale(bbTemplateHandle))
        val pivot: Vector = getFinalPivot(currentParentOrigin, currentParentPivot, currentParentAngle, bbTemplateHandle)
        for (part in parts) {
            part.spawn(
                this,
                bbTemplateHandle,
                location.clone(),
                template.origin.clone(),
                pivot.clone(),
                rotation,
                scale
            )
        }
        for (child in children) {
            child.spawn(location.clone(), bbTemplateHandle, template.origin.clone(), pivot.clone(), rotation, scale)
        }
    }

    fun getFinalPivot(
        parentOrigin: Vector,
        parentPivot: Vector,
        parentRotation: EulerAngle,
        BBTemplateHandle: BBTemplateHandle,
    ): Vector {
        var pivot: Vector = template.origin.clone()
        val animationPivot: Vector = BBTemplateHandle.animationHandler.getPosition(template.name)

        pivot.add(animationPivot)
        if (!(parentRotation.x == 0.0 && parentRotation.y == 0.0 && parentRotation.z == 0.0)) {
            pivot = parentOrigin.clone().subtract(pivot)
            pivot.rotateAroundX(parentRotation.x)
            pivot.rotateAroundY(-parentRotation.y)
            pivot.rotateAroundZ(-parentRotation.z)

            pivot = parentPivot.clone().subtract(pivot)
        }
        return pivot
    }

    fun getFinalScale(
        BBTemplateHandle: BBTemplateHandle,
    ): Vector {
        val animationScale: Vector = BBTemplateHandle.animationHandler.getScale(template.name)
        return animationScale
    }

    private fun getFinalAngle(parentAngle: EulerAngle, BBTemplateHandle: BBTemplateHandle): EulerAngle {
        var rotation = template.rotation
        val animationRotation = BBTemplateHandle.animationHandler.getRotation(template.name)

        if (animationRotation.x == 0.0 && animationRotation.y == 0.0 && animationRotation.z == 0.0) {
            if (template.name != "bone2") {
                //Bukkit.broadcastMessage("Animation degrees are 0")
            }
        }

        rotation = rotation.add(animationRotation.x, animationRotation.y, animationRotation.z)
        if (parentAngle !== EulerAngle.ZERO) {
            val startQuat = Quaternion(parentAngle)
            val rotationQuat = Quaternion(rotation)

            val resultQuat: Quaternion = rotationQuat.mul(startQuat)
            rotation = resultQuat.getEulerAnglesXYZ()
        }

        if (rotation.x == 0.0 && rotation.y == 0.0 && rotation.z == 0.0) {
            if (template.name != "bone2") {
                //Bukkit.broadcastMessage("Result rotation is 0")
            }
        }
        return rotation
    }

}