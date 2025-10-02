package gg.aquatic.waves.blockbench.handle

import gg.aquatic.waves.blockbench.template.BBPartTemplate
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector

class BBPart(
    val template: BBPartTemplate,
    val parent: BBBone
) {
    var lastCachedLocation: Location? = null

    fun spawn(bone: BBBone, templateHandle: BBTemplateHandle, location: Location, parentOrigin: Vector, parentPivot: Vector, parentAngle: EulerAngle) {
        val translation = template.translation.clone()

        val newLocation = location.clone() //.add(particlePartTemplate.getTranslation());
        val rotation = template.rotation
        val origin = template.origin.clone()

        // Cube rotation
        val vector = translation.clone().subtract(template.origin)
        vector.rotateAroundX(rotation.x)
        vector.rotateAroundY(-rotation.y)
        vector.rotateAroundZ(-rotation.z)

        vector.add(origin.clone().subtract(parentOrigin))

        vector.rotateAroundX(parentAngle.x)
        vector.rotateAroundY(-parentAngle.y)
        vector.rotateAroundZ(-parentAngle.z)

        vector.add(parentPivot)

        newLocation.add(vector)
        lastCachedLocation = newLocation

        templateHandle.onTickPart(this)
    }

}