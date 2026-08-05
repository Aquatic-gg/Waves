package gg.aquatic.klocale.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.LocaleSerializer
import gg.aquatic.klocale.message.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResourceLocaleProvider<M : Message<M>>(
    /**
     * The path to the file inside your resources folder (e.g., "lang/en.yml")
     */
    val path: String,
    val serializer: LocaleSerializer<ByteArray, M>,
    /**
     * The classloader to use. Defaults to the one that loaded this class.
     */
    private val classLoader: ClassLoader = ResourceLocaleProvider::class.java.classLoader
) : LocaleProvider<M> {

    override suspend fun fetch(): Map<String, Map<String, M>> = withContext(Dispatchers.IO) {
        val stream = classLoader.getResourceAsStream(path) ?: return@withContext emptyMap()

        val content = try {
            stream.readAllBytes()
        } catch (e: Exception) {
            return@withContext emptyMap()
        }

        if (content.isEmpty()) return@withContext emptyMap()

        serializer.parse(content) ?: return@withContext emptyMap()
    }
}
