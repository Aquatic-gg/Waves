package gg.aquatic.kregistry.core

import gg.aquatic.kregistry.bootstrap.BootstrapHolder
import gg.aquatic.kregistry.bootstrap.ContributionBuilder
import gg.aquatic.kregistry.bootstrap.RegistryHolder
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TypedRegistryTest {

    private fun <K, V> registry(id: String, data: Map<K, V>): Registry<K, V> {
        val key = RegistryKey.simple<K, V>(RegistryId("test", id))
        return SimpleRegistry(key, data, emptyMap())
    }

    @Test
    fun `simple registry works`() {
        val key = RegistryKey.simple<String, String>(RegistryId("test", "simple"))
        val contribution: ContributionBuilder.() -> Unit = {
            registry(key) {
                add("hello", "world")
            }
        }

        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()
        testHolder.registryBootstrap(testBootstrap, contribution)
        build()

        val registry = testBootstrap.get(key)
        assertEquals("world", registry["hello"])
    }

    @Test
    fun `simple registry merges multiple bootstrap calls from same holder`() {
        val key = RegistryKey.simple<String, String>(RegistryId("test", "simple-multi"))
        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()

        testHolder.registryBootstrap(testBootstrap) {
            registry(key) {
                add("first", "1")
            }
        }
        testHolder.registryBootstrap(testBootstrap) {
            registry(key) {
                add("second", "2")
            }
        }

        build()

        val registry = testBootstrap.get(key)
        assertEquals("1", registry["first"])
        assertEquals("2", registry["second"])
    }

    interface Action<out B> : GroupedEntry<B>
    open class Entity(val name: String)
    class Player(name: String) : Entity(name)
    data class SendMessage(
        override val binder: Class<out Player>,
        val text: String
    ) : Action<Player>
    data class PlaySound(
        override val binder: Class<out Player>,
        val sound: String
    ) : Action<Player>
    data class Broadcast(
        override val binder: Class<out Entity>,
        val text: String
    ) : Action<Entity>

    @Test
    fun `grouped registry supports grouped lookups by binder type`() {
        val actionsKey = RegistryKey.grouped<String, Entity, Action<Entity>>(
            RegistryId("test", "actions")
        )
        val contribution: ContributionBuilder.() -> Unit = {
            registry(actionsKey) {
                add("broadcast", Broadcast(Entity::class.java, "hi all"))
                add("message", SendMessage(Player::class.java, "hi"))
                add("sound", PlaySound(Player::class.java, "ping"))
            }
        }

        val testBootstrap = object : BootstrapHolder {}
        val testHolder = object : RegistryHolder {}

        val build = testBootstrap.inject()
        testHolder.registryBootstrap(testBootstrap, contribution)
        build()

        val grouped: GroupedRegistry<String, Entity, Action<Entity>> = testBootstrap.get(actionsKey)

        val message: Action<Entity>? = grouped.getTypedByClass("message", Player::class.java)
        assertNotNull(message)
        assertTrue(message is SendMessage)

        val broadcast = grouped.getTypedByClass("broadcast", Entity::class.java)
        assertTrue(broadcast is Broadcast)
    }

    @Test
    fun `grouped registry merges contributions per binder and rebuilds`() {
        val actionsKey = RegistryKey.grouped<String, Entity, Action<Entity>>(
            RegistryId("test", "actions-merge")
        )
        val testBootstrap = object : BootstrapHolder {}
        val holderOne = object : RegistryHolder {}
        val holderTwo = object : RegistryHolder {}

        val build = testBootstrap.inject()

        holderOne.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                add("message", SendMessage(Player::class.java, "hi"))
            }
        }

        holderTwo.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                add("broadcast", Broadcast(Entity::class.java, "ping all"))
                add("sound", PlaySound(Player::class.java, "ping"))
            }
        }

        build()

        val grouped: GroupedRegistry<String, Entity, Action<Entity>> = testBootstrap.get(actionsKey)
        assertTrue(grouped.getTypedByClass("message", Player::class.java) is SendMessage)
        assertTrue(grouped.getTypedByClass("sound", Player::class.java) is PlaySound)
        assertTrue(grouped.getTypedByClass("broadcast", Entity::class.java) is Broadcast)
        assertTrue(grouped.getHierarchicalByClass("broadcast", Player::class.java) is Broadcast)

        val allPlayer = grouped.getAllHierarchicalByClass(Player::class.java).keys
        assertEquals(setOf("message", "sound", "broadcast"), allPlayer)

        holderOne.registryBootstrap(testBootstrap) {
            registry(actionsKey) {
                add("message", SendMessage(Player::class.java, "hello"))
            }
        }

        testBootstrap.rebuildRegistries(holderOne)

        val rebuilt: GroupedRegistry<String, Entity, Action<Entity>> = testBootstrap.get(actionsKey)

        val updated = rebuilt.getTypedByClass("message", Player::class.java) as? SendMessage
        assertEquals("hello", updated?.text)
        assertTrue(rebuilt.getTypedByClass("sound", Player::class.java) is PlaySound)
        assertTrue(rebuilt.getHierarchicalByClass("broadcast", Player::class.java) is Broadcast)
    }
}
