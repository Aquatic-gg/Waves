package gg.aquatic.kregistry.core

import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kregistry.bootstrap.ContributionBuilder
import gg.aquatic.kregistry.bootstrap.RegistryContributionBuilder
import gg.aquatic.kregistry.bootstrap.RegistryHolder
import gg.aquatic.kregistry.core.GroupedRegistry
import gg.aquatic.kregistry.core.TypedCollectionRegistry
import gg.aquatic.kregistry.core.TypedRegistry
import gg.aquatic.kregistry.core.addGrouped
import gg.aquatic.kregistry.core.addTyped
import gg.aquatic.kregistry.core.getAllHierarchical
import gg.aquatic.kregistry.core.getAllHierarchicalEntries
import gg.aquatic.kregistry.core.getHierarchical
import gg.aquatic.kregistry.core.getHierarchicalEntries
import gg.aquatic.kregistry.core.getTyped
import gg.aquatic.kregistry.core.getTypedEntries
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypedRegistryTest {

    interface Animal
    open class Mammal(val name: String) : Animal
    class Dog(name: String) : Mammal(name)

    data class AnimalEntry<T : Animal>(
        override val value: T
    ) : ValueTyped<T>

    private fun <K, V> registry(id: String, data: Map<K, V>): Registry<K, V> {
        return Registry(RegistryKey(RegistryId("test", id)), data, emptyMap())
    }

    @Test
    fun `typed registry supports explicit and reified lookups`() {
        val dog = Dog("fido")
        val mammal = Mammal("mammal")

        val typedKey = RegistryKey<Class<out Animal>, Registry<String, Animal>>(RegistryId("test", "typed-reg"))
        val dogRegistryKey = RegistryKey<String, Animal>(RegistryId("test", "dog-reg"))
        val mammalRegistryKey = RegistryKey<String, Animal>(RegistryId("test", "mammal-reg"))
        val contribution: ContributionBuilder.() -> Unit = {
            registry(typedKey) {
                addGrouped(Dog::class.java, dogRegistryKey) {
                    add("dog", dog)
                }
                addGrouped(Mammal::class.java, mammalRegistryKey) {
                    add("mammal", mammal)
                }
            }
        }

        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()
        testHolder.registryBootstrap(testBootstrap, contribution)
        build()

        val typedRegistry: TypedRegistry<String, Animal> =
            testBootstrap[typedKey as RegistryKey<Class<*>, Registry<String, out Animal>>]

        val reified: Dog? = typedRegistry.getTypedByClass("dog", Dog::class.java)
        assertEquals(dog, reified)

        val explicit = typedRegistry.getTypedByClass("dog", Dog::class.java)
        assertEquals(dog, explicit)

        val hierarchicalReified: Animal? = typedRegistry.getHierarchicalByClass("dog", Animal::class.java)
        assertEquals(dog, hierarchicalReified)

        val hierarchicalExplicit = typedRegistry.getHierarchicalByClass("mammal", Animal::class.java)
        assertEquals(mammal, hierarchicalExplicit)

        val allHier: Map<String, Animal> = typedRegistry.getAllHierarchicalByClass(Animal::class.java)
        assertEquals(setOf("dog", "mammal"), allHier.keys)

        val exactWrongType: Animal? = typedRegistry.getTypedByClass("mammal", Animal::class.java)
        assertEquals(null, exactWrongType)
    }

    interface Action<B>
    class Player(val name: String)
    data class SendMessage(val text: String) : Action<Player>
    data class PlaySound(val sound: String) : Action<Player>

    @Test
    fun `typed registry supports grouped lookups by binder type`() {
        val actionsKey = RegistryKey.grouped<String, Player, Action<Player>>(RegistryId("test", "actions"))
        val playerActionsKey = RegistryKey<String, Action<Player>>(RegistryId("test", "player-actions"))
        val contribution: ContributionBuilder.() -> Unit = {
            registry(actionsKey) {
                addGrouped(Player::class.java, playerActionsKey) {
                    add("message", SendMessage("hi"))
                    add("sound", PlaySound("ping"))
                }
            }
        }

        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()
        testHolder.registryBootstrap(testBootstrap, contribution)
        build()

        val grouped: GroupedRegistry<String, Player, Action<Player>> = testBootstrap[actionsKey]

        val message: Action<Player>? = grouped.getTypedByClass("message", Player::class.java)
        assertNotNull(message)
        assertTrue(message is SendMessage)
    }

    @Test
    fun `grouped registry merges contributions per binder and rebuilds`() {
        val actionsKey = RegistryKey.grouped<String, Player, Action<Player>>(RegistryId("test", "actions-merge"))
        val playerActionsKey = RegistryKey<String, Action<Player>>(RegistryId("test", "player-actions-merge"))
        val testBootstrap = object : BootstrapHolder {}
        val holderOne = object : RegistryHolder {}
        val holderTwo = object : RegistryHolder {}

        val build = testBootstrap.inject()

        holderOne.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                addGrouped(Player::class.java, playerActionsKey) {
                    add("message", SendMessage("hi"))
                }
            }
        }

        holderTwo.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                addGrouped(Player::class.java, playerActionsKey) {
                    add("sound", PlaySound("ping"))
                }
            }
        }

        build()

        val grouped: GroupedRegistry<String, Player, Action<Player>> = testBootstrap[actionsKey]
        assertTrue(grouped.getTypedByClass("message", Player::class.java) is SendMessage)
        assertTrue(grouped.getTypedByClass("sound", Player::class.java) is PlaySound)

        holderOne.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                addGrouped(Player::class.java, playerActionsKey) {
                    add("message", SendMessage("hello"))
                }
            }
        }

        testBootstrap.rebuildRegistries(holderOne)

        val rebuilt: GroupedRegistry<String, Player, Action<Player>> = testBootstrap[actionsKey]
        val updated = rebuilt.getTypedByClass("message", Player::class.java) as? SendMessage
        assertEquals("hello", updated?.text)
        assertTrue(rebuilt.getTypedByClass("sound", Player::class.java) is PlaySound)
    }

    @Test
    fun `typed collection registry supports explicit and reified lookups`() {
        val dog = Dog("fido")
        val mammal = Mammal("mammal")

        val builder = RegistryContributionBuilder<Class<out Animal>, List<GenericTyped<out Animal>>>()
        builder.addTyped(AnimalEntry(dog))
        builder.addTyped(AnimalEntry(mammal))

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

    @Test
    fun `contribution builder supports typed collections`() {
        val dog = Dog("fido")
        val registryKey = RegistryKey.typedCollection<Animal>(RegistryId("test", "typed-collection"))

        val contributionBuilder: ContributionBuilder.() -> Unit = {
            registry(registryKey) {
                addTyped(AnimalEntry(dog))
            }
        }

        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()
        testHolder.registryBootstrap(testBootstrap, contributionBuilder)
        build()

        val typedCollection = testBootstrap[registryKey]
        val dogs = typedCollection.getTypedEntries<Dog>()
        assertEquals(1, dogs.size)
    }
}
