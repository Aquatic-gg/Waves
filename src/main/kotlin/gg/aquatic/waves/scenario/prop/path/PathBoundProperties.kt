package gg.aquatic.waves.scenario.prop.path

class PathBoundProperties(
    val offset: PathPoint,
    val offsetType: OffsetType,
    val affectYawPitch: Boolean
) {

    enum class OffsetType {
        STATIC,
        DYNAMIC
    }

}