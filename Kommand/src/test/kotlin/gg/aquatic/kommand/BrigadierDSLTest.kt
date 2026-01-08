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
                    // The generic get<T> should use the internal mapper
                    // registered by listArgument
                    capturedCrate = get<Crate>("type")
                    true
                }
            }
        }

        // 1. Test successful mapping
        dispatcher.execute("crate legendary", mockSource)
        assertNotNull(capturedCrate, "Crate should have been mapped")
        // REMOVED SAFE CALLS (?.) because get<Crate> is now non-null
        assertEquals("legendary", capturedCrate.id)
        assertEquals(0.05, capturedCrate.chance)

        // 2. Test invalid input (must use getOrNull in the execute block to avoid Exception)
        capturedCrate = null

        // We redefine the command for the 'invalid' test if we want to test getOrNull specifically
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
                false // Continue to children
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

        // 1. Run root - only root execute should fire
        dispatcher.execute("example", mockSource)
        assertEquals(1, rootCalls)
        assertEquals(0, sub2Calls)

        // 2. Run nested - root execute fires (inherited) then sub2 fires
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
                true // STOP HERE
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

        // 1. Test multiple flags in random order
        dispatcher.execute("testflags -f --silent", mockSource)
        assertTrue(capturedFlags.contains("-f"))
        assertTrue(capturedFlags.contains("--silent"))
        assertEquals(2, capturedFlags.size)

        // 2. Test invalid flags are ignored
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

        // 1. Test valid parsing of named int
        dispatcher.execute("testnamed -amount:5 -radius:10", mockSource)
        assertEquals(5, capturedAmount)

        // 2. Test partial or missing named arguments
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
            // Path 1: /teleport (no args)
            execute<Player> {
                capturedVal = getOrNull<String>("target")
                true
            }

            // Path 2: /teleport <target>
            // Using stringArgument for the test to avoid Vanilla provider issues
            stringArgument("target") {
                execute<Player> {
                    capturedVal = get<String>("target")
                    true
                }
            }
        }

        // 1. Run without argument
        dispatcher.execute("teleport", mockSource)
        assertEquals(null, capturedVal, "Value should be null when argument is missing")

        // 2. Run with argument
        dispatcher.execute("teleport MyPlayerName", mockSource)
        assertEquals("MyPlayerName", capturedVal, "Value should be captured when argument is present")
    }
}