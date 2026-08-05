plugins {
    id("me.champeau.jmh")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    jmhImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    jmhImplementation("net.kyori:adventure-text-minimessage:4.26.1")
    jmhImplementation("com.fasterxml.jackson.core:jackson-databind:2.21.2")
    jmhImplementation("org.knowm.xchart:xchart:3.8.8")

    // Testing
    testImplementation("io.mockk:mockk:1.14.9")
    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-text-serializer-gson:4.26.1")
    testImplementation("net.kyori:adventure-text-minimessage:4.26.1")
}

val jmhResultsFile = layout.buildDirectory.file("reports/jmh/results.json")
val jmhGraphsDir = layout.projectDirectory.dir("docs/benchmarks")

jmh {
    jmhVersion.set("1.37")
    resultFormat.set("JSON")
    resultsFile.set(jmhResultsFile)
    fork.set(1)
    warmupIterations.set(3)
    iterations.set(5)
    timeOnIteration.set("1s")
}

val jmhSourceSet = the<SourceSetContainer>()["jmh"]

tasks.register<JavaExec>("jmhGraphs") {
    group = "verification"
    description = "Generate PNG graphs from JMH JSON results."
    classpath = jmhSourceSet.runtimeClasspath
    mainClass.set("gg.aquatic.quickminimessage.benchmark.JmhChartGenerator")
    args(
        jmhResultsFile.get().asFile.absolutePath,
        jmhGraphsDir.asFile.absolutePath
    )
}

tasks.named("jmh") {
    finalizedBy("jmhGraphs")
}
