package gg.aquatic.kregistry

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RegistryTest {

    @Test
    fun `test basic mutable registry operations`() {
        val registry = MutableRegistry<String, Int>()
        registry.register("one", 1)
        registry.register("two", 2)

        assertEquals(1, registry["one"])
        assertEquals(2, registry.getAll().size)
        
        registry.unregister("one")
        assertNull(registry["one"])
    }

    @Test
    fun `test registry freezing and immutability`() {
        val mutable = MutableRegistry<String, String>()
        mutable.register("key", "value")
        
        val frozen = mutable.freeze()
        
        // Ensure data is there
        assertEquals("value", frozen["key"])
        
        // Modifying original mutable shouldn't affect frozen if implemented via toMap()
        mutable.register("new", "new_value")
        assertNull(frozen["new"])
    }

    @Test
    fun `test updateRegistry logic`() {
        val initial = FrozenRegistry(mapOf("a" to 1))
        val updated = initial.updateRegistry {
            register("b", 2)
        }

        assertEquals(1, updated["a"])
        assertEquals(2, updated["b"])
        // Ensure original is untouched
        assertNull(initial["b"])
    }
}
