package gg.aquatic.quickminimessage.benchmark

import gg.aquatic.quickminimessage.MMFormatter
import gg.aquatic.quickminimessage.MMParser
import gg.aquatic.quickminimessage.MMPlaceholder
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Formatter
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole

@State(Scope.Thread)
open class MMParserResolverBenchmark {
    @Param(
        "parsed",
        "unparsed",
        "styling",
        "number",
        "joining"
    )
    lateinit var scenario: String

    private val miniMessage = MiniMessage.miniMessage()
    private lateinit var input: String
    private lateinit var miniResolver: TagResolver
    private lateinit var mmResolver: MMTagResolver

    @Setup
    fun setup() {
        when (scenario) {
            "parsed" -> {
                input = "Hello <name>!"
                miniResolver = Placeholder.parsed("name", "<red>Bob</red>")
                mmResolver = MMPlaceholder.parsed("name", "<red>Bob</red>")
            }
            "unparsed" -> {
                input = "Hello <name>!"
                miniResolver = Placeholder.unparsed("name", "<red>Bob</red>")
                mmResolver = MMPlaceholder.unparsed("name", "<red>Bob</red>")
            }
            "styling" -> {
                input = "<fancy>Hello</fancy> world"
                miniResolver = Placeholder.styling("fancy", NamedTextColor.RED, TextDecoration.BOLD)
                mmResolver = MMPlaceholder.styling("fancy", NamedTextColor.RED, TextDecoration.BOLD)
            }
            "number" -> {
                input = "Balance: <no:'en-US':'#.00'>"
                miniResolver = Formatter.number("no", 250.25)
                mmResolver = MMFormatter.number("no", 250.25)
            }
            "joining" -> {
                input = "Items: <items:'<gray>, </gray>':'<gray> and </gray>'>"
                miniResolver = Formatter.joining(
                    "items",
                    Component.text("A"),
                    Component.text("B"),
                    Component.text("C")
                )
                mmResolver = MMFormatter.joining(
                    "items",
                    Component.text("A"),
                    Component.text("B"),
                    Component.text("C")
                )
            }
            else -> error("Unknown scenario: $scenario")
        }
    }

    @Benchmark
    fun resolveMiniMessage(blackhole: Blackhole) {
        blackhole.consume(miniMessage.deserialize(input, miniResolver))
    }

    @Benchmark
    fun resolveQuickMiniMessage(blackhole: Blackhole) {
        blackhole.consume(MMParser.deserialize(input, mmResolver))
    }
}
