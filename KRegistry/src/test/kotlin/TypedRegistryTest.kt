package gg.aquatic.kregistry

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class TypedRegistryTest {

    interface Animal
    class Dog(val name: String) : Animal
    class Cat(val name: String) : Animal

    @Test
    fun `test hierarchical lookup`() {
        // Create a TypedRegistry: Map<Class<*>, FrozenRegistry<String, Animal>>
        var registry: TypedRegistry<String, Animal> = FrozenRegistry(emptyMap())

        // Use the extension to register a Dog
        val mutable = MutableRegistry<Class<*>, FrozenRegistry<String, Animal>>()
        mutable.register("fido", Dog("Fido"))
        mutable.register("whiskers", Cat("Whiskers"))
        
        registry = mutable.freeze()

        // Test getTyped (Specific)
        val dog = registry.getTyped<String, Animal, Dog>("fido")
        assertNotNull(dog)
        assertEquals("Fido", dog?.name)

        // Test getAllHierarchical (Should return both Dog and Cat when asking for Animal)
        val allAnimals = registry.getAllHierarchical<String, Animal, Animal>()
        assertEquals(2, allAnimals.size)
        
        // Test getHierarchical
        val foundAsAnimal = registry.getHierarchical<String, Animal, Animal>("fido")
        assertNotNull(foundAsAnimal)
    }
}
