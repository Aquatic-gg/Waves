plugins {
    id("me.champeau.jmh")
}

dependencies {
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    jmh("org.knowm.xchart:xchart:3.8.8")
    jmh("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")

    testImplementation(kotlin("test"))
}

tasks.register<JavaExec>("generateCharts") {
    dependsOn("jmh")
    group = "benchmark"
    description = "Runs benchmarks and generates PNG charts"

    mainClass.set("gg.aquatic.snapshotmap.ChartKt")
    classpath = sourceSets["jmh"].runtimeClasspath

    workingDir = projectDir
}

jmh {
    threads.set(Runtime.getRuntime().availableProcessors())
    resultFormat.set("JSON")
    benchmarkMode.set(listOf("thrpt"))

    jvmArgs.set(
        listOf(
            "-XX:-RestrictContended",
            "-XX:+UseParallelGC",
            "-XX:MaxInlineLevel=20"
        )
    )
}
