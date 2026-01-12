package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message
import gg.aquatic.klocale.message.MessageCache
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.util.*

class LocaleManagerTest {

    class TestMessage(val content: String) : Message<TestMessage> {
        override fun replace(placeholder: String, replacement: String) = TestMessage(content.replace(placeholder, replacement))
        override fun replace(updater: (String) -> String) = TestMessage(updater(content))
    }

    @Test
    fun `test locale fallback logic`() = runTest {
        val cache = MessageCache.MapCache<TestMessage>()
        val provider = LocaleProvider<TestMessage> {
            mapOf(
                "en" to mapOf("welcome" to TestMessage("Welcome!")),
                "fr" to mapOf("welcome" to TestMessage("Bienvenue!"))
            )
        }

        val manager = BaseLocaleManager(
            cache = cache,
            defaultLanguage = "en",
            provider = provider
        )

        manager.invalidate()

        assertEquals("Bienvenue!", manager.getOrDefault(Locale.FRENCH, "welcome").content)
        assertEquals("Welcome!", manager.getOrDefault(Locale.GERMAN, "welcome").content)
    }

    @Test
    fun `test missing key handler`() = runBlocking {
        val manager = BaseLocaleManager(
            defaultLanguage = "en",
            provider = { emptyMap() },
            missingKeyHandler = object : MissingKeyHandler<TestMessage> {
                override fun handle(key: String, language: String): TestMessage {
                    return TestMessage("MISSING: $key")
                }
            }
        )

        val result = manager.getOrDefault(Locale.ENGLISH, "non-existent")
        assertEquals("MISSING: non-existent", result.content)
    }

    @Test
    fun `test throwing handler`() {
        val manager = BaseLocaleManager<TestMessage>(
            defaultLanguage = "en",
            provider = { emptyMap() }
        )

        assertThrows(IllegalArgumentException::class.java) {
            manager.getOrDefault(Locale.ENGLISH, "invalid")
        }
    }
}
