package gg.aquatic.replace

import gg.aquatic.replace.placeholder.Placeholder
import gg.aquatic.replace.placeholder.Placeholders
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PlaceholderTest {

    @Test
    fun `test literal placeholder replacement`() {
        // Use a simple String as the target instead of a Player object
        val binder = "Larkyy"

        val namePlaceholder = Placeholder.Literal<String>("name") { target, _ ->
            target
        }

        val context = Placeholders.resolverFor(String::class.java, 5)
        context.addPlaceholder(namePlaceholder)

        val item = context.createItem(binder, "Hello %name%!")

        assertEquals("Hello Larkyy!", item.latestState.value)
    }

    @Test
    fun `test context transformation`() {
        data class User(val name: String)
        val binder = User("Aquatic")

        val namePlaceholder = Placeholder.Literal<String>("name") { target, _ -> target }
        Placeholders.register(String::class.java, namePlaceholder)

        val userContext = Placeholders.resolverFor<User>(
            maxUpdateInterval = 5,
            transforms = arrayOf(Placeholders.Transform { it.name })
        )

        val item = userContext.createItem(binder, "Welcome %name%")
        assertEquals("Welcome Aquatic", item.latestState.value)
    }

    @Test
    fun `test nested multi-transformation`() {
        data class Stats(val kills: Int)
        data class Profile(val username: String, val stats: Stats)

        val binder = Profile("Larkyy", Stats(42))

        // 1. Register placeholders for different types
        val killsPlaceholder = Placeholder.Literal<Stats>("kills") { s, _ -> s.kills.toString() }
        val userPlaceholder = Placeholder.Literal<String>("user") { s, _ -> s }

        Placeholders.register(Stats::class.java, killsPlaceholder)
        Placeholders.register(String::class.java, userPlaceholder)

        // 2. Resolve context for Profile with two transforms
        val profileContext = Placeholders.resolverFor<Profile>(
            maxUpdateInterval = 0, // Instant update
            transforms = arrayOf(
                Placeholders.Transform { it.username },
                Placeholders.Transform { it.stats }
            )
        )

        // 3. Verify it collects placeholders from both transformed types
        val item = profileContext.createItem(binder, "User: %user%, Kills: %kills%")
        assertEquals("User: Larkyy, Kills: 42", item.latestState.value)
    }

    @Test
    fun `test transform with kyori components`() {
        data class Game(val name: String)
        val binder = Game("Survival")

        // Identifier with underscore works now!
        val componentPlaceholder = Placeholder.Component<String>("styled_name") { name, _ ->
            net.kyori.adventure.text.Component.text(name).color(net.kyori.adventure.text.format.NamedTextColor.GOLD)
        }
        Placeholders.register(String::class.java, componentPlaceholder)

        val gameContext = Placeholders.resolverFor<Game>(
            transforms = arrayOf(Placeholders.Transform { it.name })
        )

        val item = gameContext.createItem(binder, net.kyori.adventure.text.Component.text("Playing %styled_name%"))

        assertEquals("Playing Survival", item.latestState.value.toPlain())
    }

    @Test
    fun `test placeholder with arguments`() {
        // Use a unique data class for this test
        data class ArgumentUser(val name: String)
        val binder = ArgumentUser("Larkyy")

        val statPlaceholder = Placeholder.Literal<String>("stat") { _, fullMatch ->
            val arg = fullMatch.substringAfter("_")
            if (arg == "coins") "500" else "0"
        }
        Placeholders.register(String::class.java, statPlaceholder)

        val context = Placeholders.resolverFor<ArgumentUser>(
            transforms = arrayOf(Placeholders.Transform { it.name })
        )
        val item = context.createItem(binder, "You have %stat_coins% coins")

        assertEquals("You have 500 coins", item.latestState.value)
    }
}
