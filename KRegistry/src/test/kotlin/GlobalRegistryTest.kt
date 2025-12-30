package gg.aquatic.kregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class GlobalRegistryTest {

    private val TEST_KEY = RegistryKey<String, String>(RegistryId("test","test2"))

    @Test
    fun `test global registry updates`() {
        // Initialize the registry in the graph
        Registry.update {
            registerRegistry(TEST_KEY, FrozenRegistry(emptyMap()))
        }

        // Update specific registry within the graph
        Registry.update {
            replaceRegistry(TEST_KEY) {
                register("hello", "world")
            }
        }

        val frozen = Registry.get(TEST_KEY)
        assertEquals("world", frozen["hello"])
    }
}
