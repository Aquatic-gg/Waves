package gg.aquatic.waves.blockbench.reader

import gg.aquatic.waves.blockbench.template.BBPartTemplate
import gg.aquatic.waves.blockbench.animation.LoopMode
import gg.aquatic.waves.blockbench.animation.impl.BBTimeline
import gg.aquatic.waves.blockbench.animation.impl.PositionKeyframe
import gg.aquatic.waves.blockbench.animation.impl.RotationKeyframe
import gg.aquatic.waves.blockbench.animation.impl.ScaleKeyframe
import gg.aquatic.waves.blockbench.reader.data.*
import gg.aquatic.waves.blockbench.template.BBAnimationTemplate
import gg.aquatic.waves.blockbench.template.BBBoneTemplate
import gg.aquatic.waves.blockbench.template.BBTemplate
import gg.aquatic.waves.blockbench.timeline.InterpolatedTimeline
import gg.aquatic.waves.blockbench.timeline.InterpolationType
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import java.io.File
import java.io.StringReader
import java.util.*
import kotlin.math.abs

object BlockBenchReader {

    fun read(file: File): BBTemplate? {
        val model = BlockBenchParser.readModel(file) ?: return null
        return read(model)
    }

    fun read(bbmodel: BBModel): BBTemplate? {
        val outliner = bbmodel.outliner ?: return null
        val parentBones: MutableList<BBBoneTemplate> = ArrayList()
        for (obj in outliner) {
            if (obj is BBChildren.BBBoneChildren) {
                val readbone = readBone(obj, bbmodel)
                parentBones.add(readbone)
            }
        }

        val animations: MutableMap<String, BBAnimationTemplate> = HashMap()
        for (animation in bbmodel.animations) {
            val templateAnimation: BBAnimationTemplate = readAnimation(animation, bbmodel.groups)
            if (templateAnimation != null) {
                animations[templateAnimation.name] = templateAnimation
            }
        }

        return BBTemplate(bbmodel.name, parentBones, animations)
    }

    private fun readAnimation(bbAnimation: BBAnimation, outliner: Array<BBBone>): BBAnimationTemplate {
        val name: String = bbAnimation.name
        val length: Double = bbAnimation.length
        val loopMode: LoopMode = LoopMode.valueOf(bbAnimation.loop.uppercase())
        val timelines: MutableMap<String, BBTimeline> = HashMap()
        val scripts = HashMap<Int,BBAnimationTemplate.ParticleScript>()
        for ((uuid, bbAnimator) in bbAnimation.animators.entries) {
            if (uuid == "effects") {
                for (keyframe in bbAnimator.keyframes) {
                    val datapoint = keyframe.data_points.firstOrNull() ?: continue
                    val scriptYml = parseBBModelStringToFileConfig(datapoint.script ?: continue)
                }
                continue
            }
            val bone: BBBone = findBone(UUID.fromString(uuid), outliner) ?: continue
            val timeline: BBTimeline = readTimeline(bbAnimator)
            timelines[bone.name] = timeline
        }
        return BBAnimationTemplate(name, length, loopMode, timelines, scripts)
    }

    private fun findBone(uuid: UUID, outliner: Array<BBBone>): BBBone? {
        for (item in outliner) {
            val bone = findBone(uuid, item)
            if (bone != null) {
                return bone
            }
        }
        return null
    }

    private fun parseBBModelStringToFileConfig(bbModelString: String): FileConfiguration {
        val yamlConfig = YamlConfiguration()

        val stringReader = StringReader(bbModelString)
        yamlConfig.load(stringReader)

        return yamlConfig
    }

    private fun findBone(uuid: UUID, bone: BBBone): BBBone? {
        if (bone.uuid == uuid) {
            return bone
        }
        for (child in bone.children) {
            if (child is BBBone) {
                val b = findBone(uuid, child)
                if (b != null) {
                    return b
                }
            }
        }
        return null
    }

    private fun readTimeline(bbAnimator: BBAnimator): BBTimeline {
        val rotationTimeline: InterpolatedTimeline<RotationKeyframe> = InterpolatedTimeline()
        val positionTimeline: InterpolatedTimeline<PositionKeyframe> = InterpolatedTimeline()
        val scaleTimeline: InterpolatedTimeline<ScaleKeyframe> = InterpolatedTimeline()
        for (keyframe in bbAnimator.keyframes) {
            val datapoints = keyframe.data_points[0]
            val vector = Vector(datapoints.x, datapoints.y, datapoints.z)
            val time: Double = keyframe.time
            val interpolation: String = keyframe.interpolation
            val interpolationType: InterpolationType = when (interpolation.uppercase(Locale.getDefault())) {
                "CATMULLROM" -> InterpolationType.SMOOTH
                "STEP" -> InterpolationType.STEP
                else -> InterpolationType.LINEAR
            }
            if (keyframe.channel.equals("rotation",true)) {
                val kf = RotationKeyframe(
                    Vector(
                        Math.toRadians(-vector.x),
                        Math.toRadians(vector.y),
                        Math.toRadians(vector.z)
                    )
                )
                kf.interpolationType = interpolationType
                rotationTimeline.addFrame(time, kf)
                Bukkit.broadcastMessage("Loaded rotation kf")
            } else if (keyframe.channel.equals("position",true)) {
                val kf = PositionKeyframe(Vector(vector.x / 16, vector.y / 16, vector.z / 16))
                kf.interpolationType = interpolationType
                positionTimeline.addFrame(time, kf)
                Bukkit.broadcastMessage("Loaded position kf")
            } else if (keyframe.channel.equals("scale",true)) {
                val kf = ScaleKeyframe(Vector(vector.x, vector.y, vector.z))
                kf.interpolationType = interpolationType
                scaleTimeline.addFrame(time, kf)
                Bukkit.broadcastMessage("Loaded scale kf")
            }
        }
        return BBTimeline(positionTimeline, rotationTimeline,scaleTimeline)
    }


    private fun readBone(boneChildren: BBChildren.BBBoneChildren, bbModel: BBModel): BBBoneTemplate {
        val bone = bbModel.groups.find { it.uuid == boneChildren.uuid }!!

        val bbOrigin = bone.origin
        val bbRotation = bone.rotation
        val rotation = readRotation(bbRotation)
        val origin: Vector = if (bbOrigin == null || bbOrigin.size < 3) {
            Vector()
        } else {
            Vector(bbOrigin[0] / 16, bbOrigin[1] / 16, bbOrigin[2] / 16)
        }
        val parts: MutableList<BBPartTemplate> = ArrayList<BBPartTemplate>()
        val children: MutableList<BBBoneTemplate> = ArrayList()

        Bukkit.broadcastMessage("Children: " + bone.children.size)
        for (child in boneChildren.children) {
            if (child is BBChildren.BBBoneChildren) {
                val childBone = readBone(child, bbModel)
                if (childBone != null) {
                    children.add(childBone)
                }
            } else if (child is BBChildren.BBElementChildren) {
                for (element in bbModel.elements) {
                    if (element.uuid == child.uuid) {
                        val part: BBPartTemplate = readPart(element)
                        if (part != null) {
                            parts.add(part)
                        }
                        break
                    }
                }
            }
        }
        Bukkit.broadcastMessage("Loaded bone (${bone.name}):\n " +
                "Origin: ${origin.x} ${origin.y} ${origin.z}\n " +
                "Rotation: ${rotation.x} ${rotation.y} ${rotation.z}\n " +
                "Children: ${children.size}")
        for (child in children) {
            Bukkit.broadcastMessage("Child: ${child.name}")
        }
        return BBBoneTemplate(bone.name, origin, rotation, parts, children)
    }

    private fun readPart(element: BBElement): BBPartTemplate {
        val from = element.from
        val to = element.to

        val bbRotation = element.rotation
        val bbOrigin = element.origin
        val rotation = readRotation(bbRotation)
        val origin: Vector = if (bbOrigin == null || bbOrigin.size < 3) {
            Vector()
        } else {
            Vector(bbOrigin[0] / 16, bbOrigin[1] / 16, bbOrigin[2] / 16)
        }

        var x: Double = (from[0] + to[0]) / 2
        var y: Double = (from[1] + to[1]) / 2
        var z: Double = (from[2] + to[2]) / 2
        if (abs(x) < 0.001) {
            x = 0.0
        }
        if (abs(y) < 0.001) {
            y = 0.0
        }
        if (abs(z) < 0.001) {
            z = 0.0
        }

        return BBPartTemplate(
            element.name,
            origin,
            Vector(x / 16, y / 16, z / 16),
            rotation
        )
    }

    private fun readRotation(doubles: Array<Double>?): EulerAngle {
        val rotation = if (doubles == null || doubles.size < 3) {
            EulerAngle(0.0, 0.0, 0.0)
        } else {
            EulerAngle(
                -Math.toRadians(doubles[0]), -Math.toRadians(doubles[1]), Math.toRadians(
                    doubles[2]
                )
            )
        }
        return rotation
    }

}