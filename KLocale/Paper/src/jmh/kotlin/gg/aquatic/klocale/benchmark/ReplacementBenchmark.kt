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
    private val p1 = "%player%"
    private val p2 = "%rank%"
    private val p3 = "%server%"

    private val r1 = Component.text("AquaticPlayer", NamedTextColor.GOLD)
    private val r2 = Component.text("Admin", NamedTextColor.RED)
    private val r3 = Component.text("Survival-01", NamedTextColor.GREEN)


    @Setup
    fun setup() {
        // Complex nested component to stress the recursive logic
        baseComponent = Component.text()
            .append(Component.text("[", NamedTextColor.GRAY))
            .append(Component.text(p2, NamedTextColor.WHITE)) // Placeholder in child
            .append(Component.text("] ", NamedTextColor.GRAY))
            .append(Component.text(p1, NamedTextColor.AQUA))  // Placeholder in child
            .append(Component.text(": Hello! Welcome to ", NamedTextColor.YELLOW))
            .append(Component.text(p3, NamedTextColor.BLUE))  // Placeholder in child
            .build()
    }

    @Benchmark
    fun benchmarkKyoriNativeMulti(): Component {
        // Native Kyori requires either multiple calls or a regex-based approach.
        // Calling it 3 times is the most common "naive" way.
        return baseComponent
            .replaceText(TextReplacementConfig.builder().matchLiteral(p1).replacement(r1).build())
            .replaceText(TextReplacementConfig.builder().matchLiteral(p2).replacement(r2).build())
            .replaceText(TextReplacementConfig.builder().matchLiteral(p3).replacement(r3).build())
    }

    @Benchmark
    fun benchmarkKLocaleReplaceWithMulti(): Component {
        // Your custom replacement handles the whole map in one recursive pass
        return baseComponent.replaceWith(mapOf(
            p1 to { r1 },
            p2 to { r2 },
            p3 to { r3 }
        ))
    }
}
