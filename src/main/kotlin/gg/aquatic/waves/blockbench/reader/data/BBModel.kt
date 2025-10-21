package gg.aquatic.waves.blockbench.reader.data

class BBModel(
    val meta: BBMeta,
    val name: String,
    val model_identifier: String,
    val visible_box: Array<Int> = arrayOf(),
    val variable_placeholders: String,
    val resolution: BBResolution,
    val elements: Array<BBElement> = arrayOf(),
    val groups: Array<BBBone> = arrayOf(),
    val outliner: Array<BBChildren> = arrayOf(),
    val textures: Array<BBTexture>,
    val animations: Array<BBAnimation>
) {
}