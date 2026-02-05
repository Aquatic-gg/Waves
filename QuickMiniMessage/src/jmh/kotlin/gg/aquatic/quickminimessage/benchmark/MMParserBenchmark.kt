package gg.aquatic.quickminimessage.benchmark

import gg.aquatic.quickminimessage.MMParser
import net.kyori.adventure.text.minimessage.MiniMessage
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Thread)
open class MMParserBenchmark {
    @Param(
        "plain",
        "simple",
        "nested",
        "gradient",
        "rainbow",
        "hover",
        "nbt",
        "translatable"
    )
    lateinit var scenario: String

    private val miniMessage = MiniMessage.miniMessage()
    private lateinit var input: String

    @Benchmark
    fun parseQuickMiniMessage(blackhole: Blackhole) {
        blackhole.consume(MMParser.deserialize(input))
    }

    @Benchmark
    fun parseMiniMessage(blackhole: Blackhole) {
        blackhole.consume(miniMessage.deserialize(input))
    }

    @Setup
    fun setup() {
        input = when (scenario) {
            "plain" -> "Hello world, this is a plain string with no tags."
            "simple" -> "<red>Hello</red> world"
            "nested" -> "<bold><blue>Hello</blue> <italic>world</italic></bold>"
            "gradient" -> "<gradient:#ff0000:#00ff00>${longText()}</gradient>"
            "rainbow" -> "<rainbow>${longText()}</rainbow>"
            "hover" -> "<hover:show_text:'<green>Hover</green>'>Hover me</hover>"
            "nbt" -> "Value: <nbt:storage:minecraft:foo:bar>"
            "translatable" -> "<lang:chat.type.text:'<green>A</green>':'<blue>B</blue>'>"
            else -> error("Unknown scenario: $scenario")
        }
    }

    private fun longText(): String {
        return "This is a longer benchmark string to exercise parsing. ".repeat(8)
    }
}
