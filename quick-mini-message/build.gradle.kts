plugins {
    id("me.champeau.jmh")
}

dependencies {
    compileOnly(libs.paper.api)

    jmhImplementation(libs.paper.api)
    jmhImplementation(libs.adventure.text.minimessage)
    jmhImplementation(libs.jackson.databind)
    jmhImplementation(libs.xchart)

    // Testing
    testImplementation(libs.mockk)
    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
    testImplementation(libs.adventure.text.serializer.gson)
    testImplementation(libs.adventure.text.minimessage)
}

val jmhResultsFile = layout.buildDirectory.file("reports/jmh/results.json")
val jmhGraphsDir = layout.projectDirectory.dir("docs/benchmarks")

jmh {
    jmhVersion.set(libs.versions.jmh.asProvider())
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
