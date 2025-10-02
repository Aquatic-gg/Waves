package gg.aquatic.waves.blockbenchfuture.blueprint

import gg.aquatic.waves.blockbenchfuture.raw.ModelResolution

data class ModelBlueprint(
    val name: String,
    val resolution: ModelResolution,
    val textures: List<BlueprintTexture>,
    val group: List<BlueprintChildren>,
    val animations: Map<String, BlueprintAnimation>
) {
}