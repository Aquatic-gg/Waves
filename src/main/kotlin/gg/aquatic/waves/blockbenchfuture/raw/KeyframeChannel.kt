package gg.aquatic.waves.blockbenchfuture.raw

import com.google.gson.annotations.SerializedName

enum class KeyframeChannel {

    @SerializedName("position")
    POSITION,
    @SerializedName("rotation")
    ROTATION,
    @SerializedName("scale")
    SCALE,
    @SerializedName("timeline")
    TIMELINE,
    @SerializedName("sound")
    SOUND,
    @SerializedName("particle")
    PARTICLE,
    UNKNOWN
}