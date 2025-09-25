package gg.aquatic.waves.blockbench.raw

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import gg.aquatic.waves.blockbench.blueprint.ModelBlueprint

data class ModelData(
    val resolution: ModelResolution,
    val elements: List<ModelElement>,
    val outliner: List<ModelChildren>,
    val textures: List<ModelTexture>,
    val animations: List<ModelAnimation>?,
    @SerializedName("animation_variable_placeholders") val placeholder: ModelPlaceholder?
) {

    companion object {
        val GSON = GsonBuilder()
            .registerTypeAdapter(Float3::class.java, Float3.PARSER)
            .registerTypeAdapter(Float4::class.java, Float4.PARSER)
            .registerTypeAdapter(Datapoint::class.java, Datapoint.PARSER)
            .registerTypeAdapter(ModelChildren::class.java, ModelChildren.PARSER)
            .registerTypeAdapter(ModelPlaceholder::class.java, ModelPlaceholder.PARSER)
            .create()
    }

    fun toBlueprint(name: String) {
        val elementMap = elements.associateBy { it.uuid }
        val group = outliner.map { it.toBlueprint(elementMap) }
        ModelBlueprint(
            name,
            resolution,
            textures.map { it.toBlueprint() },
            group,
            mapOf()
            //animations.map { it.toBlueprint() }
        )
    }
}