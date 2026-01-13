package gg.aquatic.klocale.impl.paper.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.LocaleSerializer
import gg.aquatic.klocale.impl.paper.PaperMessage
import gg.aquatic.klocale.impl.paper.toMMComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.collections.set

class YamlLocaleProvider(
    val file: File,
    val serializer: LocaleSerializer<YamlConfiguration, PaperMessage>
) : LocaleProvider<PaperMessage> {
    override suspend fun fetch(): Map<String, Map<String, PaperMessage>> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyMap()
        val config = YamlConfiguration.loadConfiguration(file)
        return@withContext serializer.parse(config) ?: emptyMap()
    }

    object DefaultSerializer : LocaleSerializer<YamlConfiguration, PaperMessage> {
        override fun parse(data: YamlConfiguration): Map<String, Map<String, PaperMessage>> {
            val result = mutableMapOf<String, MutableMap<String, PaperMessage>>()
            for (string in data.getKeys(false)) {
                val map = HashMap<String, PaperMessage>()
                val lang = data.getConfigurationSection(string) ?: continue
                for (str in lang.getKeys(false)) {
                    map[str] = parseMessage(lang, str) ?: continue
                }
                result[string] = map
            }
            return result
        }

        fun parseMessage(section: ConfigurationSection, key: String): PaperMessage? {
            if (section.isList(key)) return PaperMessage.of(section.getStringList(key).map { it.toMMComponent() })
            else {
                return PaperMessage.of(section.getString(key)?.toMMComponent() ?: return null)
            }
        }
    }
}