package gg.aquatic.waves.blockbenchfuture.raw

import com.google.gson.JsonDeserializer
import gg.aquatic.waves.blockbenchfuture.blueprint.BlueprintChildren

interface ModelChildren {

    companion object {
        val PARSER = JsonDeserializer<ModelChildren> { json, _, context ->
            if (json.isJsonPrimitive) return@JsonDeserializer ModelUUID(json.asString)
            else if (json.isJsonObject) return@JsonDeserializer context.deserialize(json, ModelGroup::class.java)
            else throw IllegalArgumentException("Invalid ModelChildren type")
        }
    }

    fun toBlueprint(elements: Map<String, ModelElement>): BlueprintChildren

    class ModelUUID(val uuid: String) : ModelChildren {
        override fun toBlueprint(elements: Map<String, ModelElement>): BlueprintChildren {
            return BlueprintChildren.Element(elements[uuid]!!)
        }
    }

    class ModelGroup(
        val name: String,
        val origin: Float3?,
        val rotation: Float3?,
        val uuid: String,
        val children: List<ModelChildren>,
        val visibility: Boolean?,
    ) : ModelChildren {

        fun origin(): Float3 {
            return origin ?: Float3(0f, 0f, 0f)
        }

        fun rotation(): Float3 {
            return rotation ?: Float3(0f, 0f, 0f)
        }

        fun visibility(): Boolean {
            return visibility ?: true
        }

        override fun toBlueprint(elements: Map<String, ModelElement>): BlueprintChildren {
            val child = children.map { it.toBlueprint(elements) }
            val filtered = child.filterIsInstance<BlueprintChildren.Element>()
            return BlueprintChildren.Group(
                name,
                origin(),
                rotation(),
                child,
                if (filtered.isEmpty()) visibility() else filtered.any { it.element.visible() }
            )
        }
    }

}