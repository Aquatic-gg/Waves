package gg.aquatic.waves.util.action.impl.discord

import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.getSectionList
import me.micartey.webhookly.embeds.*
import org.bukkit.configuration.ConfigurationSection
import java.awt.Color

class DiscordEmbedArgument(id: String, defaultValue: List<WavesEmbedObject>?, required: Boolean) :
    AquaticObjectArgument<List<DiscordEmbedArgument.WavesEmbedObject>>(
        id,
        defaultValue, required
    ) {
    override val serializer: AbstractObjectArgumentSerializer<List<WavesEmbedObject>?> = Companion

    override fun load(section: ConfigurationSection): List<WavesEmbedObject>? {
        return serializer.load(section, id) ?: return defaultValue
    }

    companion object : AbstractObjectArgumentSerializer<List<WavesEmbedObject>?>() {
        override fun load(section: ConfigurationSection, id: String): List<WavesEmbedObject> {
            val list = mutableListOf<WavesEmbedObject>()

            for (embedSection in section.getSectionList(id)) {
                val embed = EmbedObject()
                if (embedSection.contains("title")) {
                    embed.title = embedSection.getString("title")
                }
                if (embedSection.contains("description")) {
                    embed.description = embedSection.getString("description")
                }
                if (embedSection.contains("url")) {
                    embed.url = embedSection.getString("url")
                }
                if (embedSection.contains("author")) {
                    val authorName = embedSection.getString("author.name")
                    val authorUrl = embedSection.getString("author.url")
                    val authorIconUrl = embedSection.getString("author.icon-url")
                    embed.author = Author(
                        authorName,
                        authorUrl,
                        authorIconUrl
                    )
                }
                if (embedSection.contains("footer")) {
                    val footer = Footer(
                        embedSection.getString("footer.text"),
                        embedSection.getString("footer.icon-url")
                    )
                    embed.footer = footer
                }
                if (embedSection.contains("color")) {
                    val color = Color(
                        embedSection.getInt("color.red"),
                        embedSection.getInt("color.green"),
                        embedSection.getInt("color.blue")
                    )
                    embed.color = color
                }
                if (embedSection.contains("image")) {
                    val imageUrl = embedSection.getString("image")
                    embed.image = Image(imageUrl)
                }
                if (embedSection.contains("thumbnail")) {
                    val thumbnailUrl = embedSection.getString("thumbnail")
                    embed.thumbnail = Thumbnail(thumbnailUrl)
                }
                list += WavesEmbedObject(embed)
            }
            return list
        }
    }

    class WavesEmbedObject(
        val embedObject: EmbedObject
    ) {

        fun convert(updater: (String) -> String): EmbedObject {
            val new = EmbedObject()

            embedObject.title?.apply {
                new.title = updater(this)
            }
            embedObject.description?.apply {
                new.description = updater(this)
            }
            embedObject.url?.apply {
                new.url = updater(this)
            }
            embedObject.color?.apply {
                new.color = this
            }
            embedObject.timestamp?.apply {
                new.timestamp = this
            }
            embedObject.footer?.apply {
                new.footer = Footer(updater(this.text), this.iconUrl)
            }
            embedObject.thumbnail?.apply {
                new.thumbnail = Thumbnail(updater(this.url))
            }
            embedObject.image?.apply {
                new.image = Image(updater(this.url))
            }
            embedObject.author?.apply {
                new.author = Author(updater(this.name), this.url, this.iconUrl)
            }
            new.fields += embedObject.fields
            return new
        }
    }
}