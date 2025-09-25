package gg.aquatic.waves.blockbench.blueprint

import gg.aquatic.waves.blockbench.raw.ModelResolution

data class ModelBlueprint(
    val name: String,
    val resolution: ModelResolution,
    val textures: List<BlueprintTexture>,
    val group: List<BlueprintChildren>,
    val animations: Map<String, BlueprintAnimation>
) {
}