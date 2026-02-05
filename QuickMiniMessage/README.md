# QuickMiniMessage

QuickMiniMessage is a fast, allocation-conscious MiniMessage parser for Adventure Components. It focuses on the common
tag set and lightweight extensibility so you can parse chat-like templates at high throughput.

## What It Is For

- Fast MiniMessage-style parsing in hot paths (chat formatting, scoreboard lines, etc..).
- Drop-in alternative when full MiniMessage flexibility is not required.
- Small, direct API for custom tags and formatting helpers.

## Features

- Core MiniMessage tags: colors, decorations, gradients/rainbows, click/hover, insertion, font, key, NBT, selector,
  score, and more.
- Custom tags via `MMTagResolver`.
- Format helpers via `MMFormatter` (numbers, dates, choice, joining).
- Translatable component handling via `MMTranslator`.

## Installation

If you publish to the configured Maven repository, depend on the published coordinates:

```kotlin
repositories {
    maven("https://repo.nekroplex.com/releases")
}

dependencies {
    implementation("gg.aquatic:QuickMiniMessage:<version>")
}
```

## Usage

Basic parsing:

```kotlin
val component = MMParser.deserialize("<red>Hello</red> <bold>world</bold>")
```

Custom tag resolver:

```kotlin
val resolver = MMTagResolver.resolver("shout") { args, context ->
    val message = args.firstOrNull() ?: return@resolver null
    MMTag.inserting(context.deserialize(message.uppercase()))
}

val component = MMParser.deserialize("<shout:hello>", resolver)
```

Formatter helpers:

```kotlin
val resolver = MMTagResolver.resolver(
    MMFormatter.number("amount", 12345),
    MMFormatter.date("today", java.time.LocalDate.now())
)

val component = MMParser.deserialize("Paid <amount> on <today:yyyy-MM-dd>", resolver)
```

## Benchmarks

JMH results below come from `build/reports/jmh/results.json` on this machine (JDK 25.0.1, 1 fork, 3 warmup x 10s,
5 measurement x 1s, mode = throughput). Results vary by machine and input complexity.

### Parsing Scenarios

| Scenario     | QuickMiniMessage     | MiniMessage      | Speedup |
|--------------|----------------------|------------------|---------|
| plain        | 266,551,008.97 ops/s | 694,679.73 ops/s | 383.7x  |
| simple       | 4,852,675.34 ops/s   | 254,844.58 ops/s | 19.0x   |
| nested       | 1,055,032.85 ops/s   | 252,931.94 ops/s | 4.2x    |
| gradient     | 37,984.31 ops/s      | 18,735.17 ops/s  | 2.0x    |
| rainbow      | 37,757.89 ops/s      | 38,652.76 ops/s  | 1.0x    |
| hover        | 800,271.07 ops/s     | 136,987.02 ops/s | 5.8x    |
| nbt          | 1,561,248.44 ops/s   | 271,604.62 ops/s | 5.7x    |
| translatable | 1,346,849.98 ops/s   | 88,216.11 ops/s  | 15.3x   |

![Parse benchmark](docs/benchmarks/mmparserbenchmark.png)

### Resolver Scenarios

| Scenario | QuickMiniMessage   | MiniMessage      | Speedup |
|----------|--------------------|------------------|---------|
| parsed   | 3,933,961.11 ops/s | 417,781.49 ops/s | 9.4x    |
| unparsed | 8,283,153.03 ops/s | 744,363.75 ops/s | 11.1x   |
| styling  | 5,324,275.95 ops/s | 642,034.98 ops/s | 8.3x    |
| number   | 1,943,916.79 ops/s | 244,943.60 ops/s | 7.9x    |
| joining  | 1,173,354.57 ops/s | 82,949.85 ops/s  | 14.1x   |

![Resolver benchmark](docs/benchmarks/mmparserresolverbenchmark.png)

### Run Benchmarks

```bash
./gradlew jmh
```

That command runs JMH and then generates PNG charts automatically.

### Outputs

- JMH JSON results: `build/reports/jmh/results.json`
- PNG charts: `docs/benchmarks/mmparserbenchmark.png`, `docs/benchmarks/mmparserresolverbenchmark.png`

Benchmark run settings (forks/iterations/time) are configured in the `jmh {}` block in `build.gradle.kts`.

### Regenerate Charts Only

If you already have `build/reports/jmh/results.json`, you can regenerate charts without re-running JMH:

```bash
./gradlew jmhGraphs
```
