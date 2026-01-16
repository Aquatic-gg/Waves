package gg.aquatic.replace

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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
            Component.text(name).color(NamedTextColor.GOLD)
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

    @Test
    fun `test dsl placeholder registration and resolution`() {
        val player = "Larkyy"

        val placeholder = dslPlaceholder<String>("test") {
            "rank" {
                handle { "Admin" }
            }
            "level" {
                intArgument("val") {
                    handle {
                        val level = arg<Int>("val") ?: 0
                        "Level: $level"
                    }
                }
            }
        }

        val context = Placeholders.resolverFor(String::class.java)
        context.addPlaceholder(placeholder)

        val item1 = context.createItem(player, "Rank: %test_rank%")
        assertEquals("Rank: Admin", item1.latestState.value)

        val item2 = context.createItem(player, "Level: %test_level_50%")
        assertEquals("Level: Level: 50", item2.latestState.value)
    }

    @Test
    fun `test optional dsl arguments`() {
        val binder = "User"

        val placeholder = dslPlaceholder<String>("stats") {
            "balance" {
                handle { "Total: 500" }

                stringArgument("currency") {
                    handle {
                        val currency = string("currency")
                        if (currency == "gems") "10 Gems" else "500 Coins"
                    }
                }
            }
        }

        val context = Placeholders.resolverFor(String::class.java)
        context.addPlaceholder(placeholder)

        // Case 1: No argument (Optional logic)
        val item1 = context.createItem(binder, "%stats_balance%")
        assertEquals("Total: 500", item1.latestState.value)

        // Case 2: Argument provided
        val item2 = context.createItem(binder, "%stats_balance_gems%")
        assertEquals("10 Gems", item2.latestState.value)

        val item3 = context.createItem(binder, "%stats_balance_money%")
        assertEquals("500 Coins", item3.latestState.value)
    }

    @Test
    fun `test single handler for optional argument`() {
        val binder = "Larkyy"

        val placeholder = dslPlaceholder<String>("stat") {
            "wins" {
                // This single handler will be called for %stat_wins%
                // AND %stat_wins_Other% because the argument node has no handler
                handle {
                    val target = string("player") ?: binder
                    if (target == "Larkyy") "10" else "0"
                }

                stringArgument("player") {
                    // No handle block here! It falls back to the parent.
                }
            }
        }

        val context = Placeholders.resolverFor(String::class.java)
        context.addPlaceholder(placeholder)

        // Case 1: %stat_wins% -> tokens empty -> current node handler called -> target is binder -> 10
        val item1 = context.createItem(binder, "%stat_wins%")
        assertEquals("10", item1.latestState.value)

        // Case 2: %stat_wins_Other% -> "Other" parsed as "player" -> child resolve() called
        // -> child has no tokens and no handler -> child returns null
        // -> parent resolve() falls back to its own handler -> target is "Other" -> 0
        val item2 = context.createItem(binder, "%stat_wins_Other%")
        assertEquals("0", item2.latestState.value)
    }
}
