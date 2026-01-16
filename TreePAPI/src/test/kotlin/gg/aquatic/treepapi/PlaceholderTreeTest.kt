package gg.aquatic.treepapi

import io.mockk.mockk
import org.bukkit.OfflinePlayer
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderTreeTest {

    @Test
    fun `test deep tree resolution`() {
        val player = mockk<OfflinePlayer>()
        val root = PlaceholderNode()

        root.apply {
            "stats" {
                "kills" {
                    handle { "10" }
                }
                "deaths" {
                    handle { "5" }
                }
            }
        }

        assertEquals("10", root.resolve(player, listOf("stats", "kills"), emptyMap()))
        assertEquals("5", root.resolve(player, listOf("stats", "deaths"), emptyMap()))
    }

    @Test
    fun `test arguments in tree`() {
        val player = mockk<OfflinePlayer>()
        val root = PlaceholderNode()

        root.apply {
            "eco" {
                stringArgument("currency") {
                    handle {
                        val type = string("currency")
                        if (type == "gems") "50" else "1000"
                    }
                }
            }
        }

        assertEquals("50", root.resolve(player, listOf("eco", "gems"), emptyMap()))
        assertEquals("1000", root.resolve(player, listOf("eco", "money"), emptyMap()))
    }

    @Test
    fun `test optional trailing arguments fallback`() {
        val player = mockk<OfflinePlayer>()
        val root = PlaceholderNode()

        root.apply {
            "profile" {
                handle { "main_profile" }
                "settings" {
                    handle { "settings_profile" }
                }
            }
        }

        // Exact match
        assertEquals("settings_profile", root.resolve(player, listOf("profile", "settings"), emptyMap()))
        // Fallback to "profile" handler if tokens remain but no children match
        assertEquals("main_profile", root.resolve(player, listOf("profile", "extra", "tokens"), emptyMap()))
    }

    @Test
    fun `test optional argument logic`() {
        val player = mockk<OfflinePlayer>()
        val root = PlaceholderNode()

        root.apply {
            "balance" {
                // This handler triggers if NO argument is provided
                handle { "Total: 100" }

                stringArgument("currency") {
                    // This handler triggers if an argument IS provided
                    handle {
                        val currency = string("currency")
                        if (currency == "gems") "5 Gems" else "100 Coins"
                    }
                }
            }
        }

        // Case 1: No argument (optional logic)
        assertEquals("Total: 100", root.resolve(player, listOf("balance"), emptyMap()))

        // Case 2: Argument provided
        assertEquals("5 Gems", root.resolve(player, listOf("balance", "gems"), emptyMap()))
        assertEquals("100 Coins", root.resolve(player, listOf("balance", "money"), emptyMap()))
    }
}
