package gg.aquatic.klocale.benchmark

import gg.aquatic.klocale.impl.paper.replaceWith
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.NamedTextColor
import org.openjdk.jmh.annotations.*
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput) // Operations per second
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 2)
@Measurement(iterations = 5, time = 2)
@Fork(1)
open class ReplacementBenchmark {

    private lateinit var baseComponent: Component
    private val placeholder = "%player%"
    private val replacementText = "AquaticPlayer"
    private val replacementComponent = Component.text("AquaticPlayer", NamedTextColor.GOLD)

    @Setup
    fun setup() {
        // Create a realistic complex component: 
        // A main string with children and nested styles
        baseComponent = Component.text()
            .append(Component.text("Welcome, ", NamedTextColor.GRAY))
            .append(Component.text(placeholder, NamedTextColor.AQUA))
            .append(Component.text("! Enjoy your stay on the server.", NamedTextColor.GRAY))
            .build()
    }

    @Benchmark
    fun benchmarkKyoriNative(): Component {
        // Native Kyori replacement
        return baseComponent.replaceText(
            TextReplacementConfig.builder()
                .matchLiteral(placeholder)
                .replacement(replacementComponent)
                .build()
        )
    }

    @Benchmark
    fun benchmarkKLocaleReplaceWith(): Component {
        // Your custom recursive replacement
        return baseComponent.replaceWith(mapOf(
            placeholder to { replacementComponent }
        ))
    }
}
