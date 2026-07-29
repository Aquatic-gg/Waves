package gg.aquatic.pakket.api.nms.profile

import java.util.*

class UserProfile(
    var uuid: UUID,
    var name: String,
    var textureProperties: MutableCollection<TextureProperty>
) {

    class TextureProperty(
        val name: String,
        val value: String,
        val signature: String
    )

}