package gg.aquatic.kommand

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType
import io.mockk.every
import io.mockk.mockk
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class BrigadierDSLTest {

    data class Crate(val id: String, val chance: Double)

    @Test
    fun `test listArgument mapping and generic get`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockPlayer = mockk<Player>(relaxed = true)
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockPlayer

        val crates = listOf(
            Crate("common", 0.8),
            Crate("legendary", 0.05)
        )

        var capturedCrate: Crate? = null

        dispatcher.command("crate") {
            listArgument("type", values = { crates }, mapper = { it.id }) {
                execute<Player> {
                    capturedCrate = get<Crate>("type")
                    true
                }
            }
        }

        dispatcher.execute("crate legendary", mockSource)
        assertNotNull(capturedCrate, "Crate should have been mapped")
        assertEquals("legendary", capturedCrate.id)
        assertEquals(0.05, capturedCrate.chance)

        capturedCrate = null

        dispatcher.command("crate_null") {
            listArgument("type", values = { crates }, mapper = { it.id }) {
                execute<Player> {
                    capturedCrate = getOrNull<Crate>("type")
                    true
                }
            }
        }
        dispatcher.execute("crate_null invalid", mockSource)
        assertEquals(null, capturedCrate, "Invalid input should map to null via getOrNull")
    }

    @Test
    fun `test deep nesting and inheritance`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockPlayer = mockk<Player>(relaxed = true)
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockPlayer

        var rootCalls = 0
        var sub2Calls = 0

        dispatcher.command("example") {
            execute<Player> {
                rootCalls++
                false
            }
            "sub1" {
                "sub2" {
                    execute<Player> {
                        sub2Calls++
                        true
                    }
                }
            }
        }

        dispatcher.execute("example", mockSource)
        assertEquals(1, rootCalls)
        assertEquals(0, sub2Calls)

        dispatcher.execute("example sub1 sub2", mockSource)
        assertEquals(2, rootCalls, "Root execute should have fired again via inheritance")
        assertEquals(1, sub2Calls, "Sub2 execute should have fired")
    }

    @Test
    fun `test short circuit stops further execution`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockPlayer = mockk<Player>(relaxed = true)
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockPlayer

        var logicCalled = false

        dispatcher.command("cancel") {
            execute<Player> {
                true
            }
            execute<Player> {
                logicCalled = true
                true
            }
        }

        dispatcher.execute("cancel", mockSource)

        assertEquals(false, logicCalled, "Second execute block should not have been called")
    }

    @Test
    fun `test flags parsing`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var capturedFlags = emptySet<String>()

        dispatcher.command("testflags") {
            flagsArgument("options", listOf("-s", "--silent", "-f")) {
                execute<Player> {
                    capturedFlags = flags("options")
                    true
                }
            }
        }

        dispatcher.execute("testflags -f --silent", mockSource)
        assertTrue(capturedFlags.contains("-f"))
        assertTrue(capturedFlags.contains("--silent"))
        assertEquals(2, capturedFlags.size)

        dispatcher.execute("testflags -f --invalid", mockSource)
        assertTrue(capturedFlags.contains("-f"))
        assertEquals(1, capturedFlags.size, "Invalid flag should not be captured")
    }

    @Test
    fun `test named arguments parsing`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var capturedAmount: Int? = null

        dispatcher.command("testnamed") {
            namedArguments("params", mapOf(
                "amount" to IntegerArgumentType.integer(1),
                "radius" to IntegerArgumentType.integer(0)
            )) {
                execute<Player> {
                    capturedAmount = named("params", "amount")
                    true
                }
            }
        }

        dispatcher.execute("testnamed -amount:5 -radius:10", mockSource)
        assertEquals(5, capturedAmount)

        capturedAmount = null
        dispatcher.execute("testnamed -radius:20", mockSource)
        assertEquals(null, capturedAmount, "Missing key should return null")
    }

    @Test
    fun `test optional argument via branching execution`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        val mockSender = mockk<Player>(relaxed = true)

        every { mockSource.sender } returns mockSender

        var capturedVal: String? = null

        dispatcher.command("teleport") {
            execute<Player> {
                capturedVal = getOrNull<String>("target")
                true
            }

            stringArgument("target") {
                execute<Player> {
                    capturedVal = get<String>("target")
                    true
                }
            }
        }

        dispatcher.execute("teleport", mockSource)
        assertEquals(null, capturedVal, "Value should be null when argument is missing")

        dispatcher.execute("teleport MyPlayerName", mockSource)
        assertEquals("MyPlayerName", capturedVal, "Value should be captured when argument is present")
    }

    @Test
    fun `test subcommand inheritance and help by default`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var helpCalls = 0
        var balanceCalls = 0

        dispatcher.command("economy") {
            execute<Player> {
                helpCalls++
                false
            }

            "balance" {
                execute<Player> {
                    balanceCalls++
                    true
                }
            }
        }

        dispatcher.execute("economy", mockSource)
        assertEquals(1, helpCalls, "Help should have fired once")
        assertEquals(0, balanceCalls, "Balance should not have fired")

        dispatcher.execute("economy balance", mockSource)
        assertEquals(2, helpCalls, "Help should have fired again (inherited)")
        assertEquals(1, balanceCalls, "Balance should have fired once")
    }

    @Test
    fun `test non inheritable execute only runs on current node`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var rootCalls = 0
        var subCalls = 0

        dispatcher.command("aqcrates") {
            execute<Player>(inheritToChildren = false) {
                rootCalls++
                true
            }

            "key" {
                "bank" {
                    execute<Player> {
                        subCalls++
                        true
                    }
                }
            }
        }

        dispatcher.execute("aqcrates", mockSource)
        assertEquals(1, rootCalls)
        assertEquals(0, subCalls)

        dispatcher.execute("aqcrates key bank", mockSource)
        assertEquals(1, rootCalls, "Root execute should not run on subcommands when inheritance is disabled")
        assertEquals(1, subCalls, "Subcommand execute should still run")
    }

    @Test
    fun `test trailing execute works for nested optional arguments`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var capturedCrate: String? = null
        var capturedPlayer: String? = null
        var capturedAmount: Int? = null

        dispatcher.command("aqcrates") {
            "key" {
                "give" {
                    listArgument("crate", values = listOf("test")) {
                        stringArgument("player") {
                            intArgument("amount")
                        }

                        execute<Player> {
                            capturedCrate = get<String>("crate")
                            capturedPlayer = getOrNull("player")
                            capturedAmount = getOrNull<Int>("amount") ?: 1
                            true
                        }
                    }
                }
            }
        }

        val playerNode = dispatcher.root
            .getChild("aqcrates")
            .getChild("key")
            .getChild("give")
            .getChild("crate")
            .getChild("player")
        assertNotNull(playerNode.command, "Player node should be executable through inherited execute")
        assertNotNull(
            playerNode.getChild("amount").command,
            "Amount node should be executable through inherited execute"
        )

        dispatcher.execute("aqcrates key give test", mockSource)
        assertEquals("test", capturedCrate)
        assertEquals(null, capturedPlayer)
        assertEquals(1, capturedAmount)

        dispatcher.execute("aqcrates key give test MrLarkyy_", mockSource)
        assertEquals("test", capturedCrate)
        assertEquals("MrLarkyy_", capturedPlayer)
        assertEquals(1, capturedAmount)

        dispatcher.execute("aqcrates key give test MrLarkyy_ 1", mockSource)
        assertEquals("test", capturedCrate)
        assertEquals("MrLarkyy_", capturedPlayer)
        assertEquals(1, capturedAmount)
    }

    @Test
    fun `test conditional suggestions only show for matching prefix`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()

        dispatcher.command("punish") {
            stringArgument("reason", format = StringArgumentFormat.STRING) {
                suggest(
                    suggestions = { listOf(":spam", ":toxicity", ":advertising") },
                    condition = { it.input.startsWith(":") },
                )
            }
        }

        val source = mockk<CommandSourceStack>()
        every { source.sender } returns mockk<Player>(relaxed = true)

        val hiddenSuggestions = dispatcher.getCompletionSuggestions(
            dispatcher.parse("punish normal", source)
        ).get().list

        assertEquals(emptyList(), hiddenSuggestions.map { it.text })

        val shownSuggestions = dispatcher.getCompletionSuggestions(
            dispatcher.parse("punish :t", source)
        ).get().list

        assertEquals(listOf(":toxicity"), shownSuggestions.map { it.text })
    }

    @Test
    fun `test string argument supports greedy format`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var capturedReason: String? = null

        dispatcher.command("punish") {
            stringArgument("reason", format = StringArgumentFormat.GREEDY_STRING) {
                execute<Player> {
                    capturedReason = string("reason")
                    true
                }
            }
        }

        dispatcher.execute("punish repeated chat spam", mockSource)
        assertEquals("repeated chat spam", capturedReason)
    }

    private enum class PunishmentType {
        BAN,
        MUTE
    }

    @Test
    fun `test enum argument maps enum and suggests values`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockk<Player>(relaxed = true)

        var capturedType: PunishmentType? = null

        dispatcher.command("punish") {
            enumArgument<PunishmentType>("type") {
                execute<Player> {
                    capturedType = get("type")
                    true
                }
            }
        }

        dispatcher.execute("punish mute", mockSource)
        assertEquals(PunishmentType.MUTE, capturedType)

        val suggestions = dispatcher.getCompletionSuggestions(
            dispatcher.parse("punish m", mockSource)
        ).get().list

        assertEquals(listOf("mute"), suggestions.map { it.text })
    }

    @Test
    fun `test requires receiver lambda can access sender directly`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val allowedSender = mockk<Player>(relaxed = true)
        val deniedSender = mockk<Player>(relaxed = true)
        val allowedSource = mockk<CommandSourceStack>()
        val deniedSource = mockk<CommandSourceStack>()

        every { allowedSource.sender } returns allowedSender
        every { deniedSource.sender } returns deniedSender
        every { allowedSender.hasPermission("punish.use") } returns true
        every { deniedSender.hasPermission("punish.use") } returns false

        var executions = 0

        dispatcher.command("punish") {
            requires { sender.hasPermission("punish.use") }
            execute<Player> {
                executions++
                true
            }
        }

        dispatcher.execute("punish", allowedSource)
        assertEquals(1, executions)

        runCatching {
            dispatcher.execute("punish", deniedSource)
        }
        assertEquals(1, executions)
    }

    @Test
    fun `test cooldown prevents repeated execution and invokes callback`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val mockPlayer = mockk<Player>(relaxed = true)
        val mockSource = mockk<CommandSourceStack>()
        every { mockSource.sender } returns mockPlayer

        var executions = 0
        var cooldownHits = 0

        dispatcher.command("punish") {
            cooldown(
                duration = java.time.Duration.ofSeconds(30),
                key = { sender },
                onCooldown = { cooldownHits++ }
            )
            execute<Player> {
                executions++
                true
            }
        }

        dispatcher.execute("punish", mockSource)
        dispatcher.execute("punish", mockSource)

        assertEquals(1, executions)
        assertEquals(1, cooldownHits)
    }
}
