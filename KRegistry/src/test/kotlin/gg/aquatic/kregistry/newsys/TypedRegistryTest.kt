package gg.aquatic.kregistry.newsys

import gg.aquatic.kregistry.GenericTyped
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryContributionBuilder
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import gg.aquatic.kregistry.TypedCollectionRegistry
import gg.aquatic.kregistry.TypedRegistry
import gg.aquatic.kregistry.addTyped
import gg.aquatic.kregistry.getAllHierarchical
import gg.aquatic.kregistry.getAllHierarchicalEntries
import gg.aquatic.kregistry.getHierarchical
import gg.aquatic.kregistry.getHierarchicalEntries
import gg.aquatic.kregistry.getTyped
import gg.aquatic.kregistry.getTypedEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TypedRegistryTest {

    interface Animal
    open class Mammal(val name: String) : Animal
    class Dog(name: String) : Mammal(name)

    data class AnimalEntry<T : Animal>(
        override val type: Class<out T>,
        val value: T
    ) : GenericTyped<T>

    private fun <K, V> registry(id: String, data: Map<K, V>): Registry<K, V> {
        return Registry(RegistryKey(RegistryId("test", id)), data, emptyMap())
    }

    @Test
    fun `typed registry supports explicit and reified lookups`() {
        val dog = Dog("fido")
        val mammal = Mammal("mammal")

        val dogRegistry: Registry<String, Animal> = registry("dog-reg", mapOf("dog" to dog))
        val mammalRegistry: Registry<String, Animal> = registry("mammal-reg", mapOf("mammal" to mammal))

        val typedRegistry: TypedRegistry<String, Animal> =
            Registry(
                RegistryKey(RegistryId("test", "typed-reg")),
                mapOf<Class<*>, Registry<String, Animal>>(
                    Dog::class.java to dogRegistry,
                    Mammal::class.java to mammalRegistry
                ),
                emptyMap()
            )

        val reified = typedRegistry.getTyped<Dog>("dog")
        assertEquals(dog, reified)

        val explicit = typedRegistry.getTyped<String, Dog, Animal>("dog", Dog::class.java)
        assertEquals(dog, explicit)

        val hierarchicalReified = typedRegistry.getHierarchical<Animal>("dog")
        assertEquals(dog, hierarchicalReified)

        val hierarchicalExplicit = typedRegistry.getHierarchical<String, Animal, Animal>("mammal", Animal::class.java)
        assertEquals(mammal, hierarchicalExplicit)

        val allHier = typedRegistry.getAllHierarchical<Animal>()
        assertEquals(setOf("dog", "mammal"), allHier.keys)
    }

    @Test
    fun `typed collection registry supports explicit and reified lookups`() {
        val dog = Dog("fido")
        val mammal = Mammal("mammal")

        val builder = RegistryContributionBuilder<Class<out Animal>, List<GenericTyped<out Animal>>>()
        builder.addTyped(AnimalEntry(Dog::class.java, dog))
        builder.addTyped(AnimalEntry(Mammal::class.java, mammal))

        val typedCollection: TypedCollectionRegistry<Animal> =
            Registry(
                RegistryKey(RegistryId("test", "typed-collection")),
                builder.data,
                emptyMap()
            )

        val explicit = typedCollection.getTypedEntries(Dog::class.java)
        assertEquals(1, explicit.size)

        val reified = typedCollection.getTypedEntries<Dog>()
        assertEquals(1, reified.size)

        val hierarchicalReified = typedCollection.getAllHierarchicalEntries<Animal>()
        assertEquals(2, hierarchicalReified.size)

        val hierarchicalExplicit = typedCollection.getAllHierarchicalEntries(Animal::class.java)
        assertEquals(2, hierarchicalExplicit.size)

        assertNotNull(typedCollection.getHierarchicalEntries<Dog>())
    }
}
