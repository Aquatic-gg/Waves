import me.champeau.jmh.JmhBytecodeGeneratorTask

plugins {
    id("me.champeau.jmh")
    id("io.morethan.jmhreport")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.adventure.text.serializer.plain)

    implementation(project(":klocale:klocale-common"))
    api(project(":aquatic-common"))
    api(libs.kotlinx.coroutines.core)

    jmh(libs.jmh.core)
    jmh(libs.jmh.generator.annprocess)

    jmhImplementation(libs.paper.api)
    jmhImplementation(libs.adventure.text.serializer.plain)

    testImplementation(kotlin("test"))
    testImplementation(libs.paper.api)
    testImplementation(libs.adventure.text.serializer.plain)
}

tasks.named<JmhBytecodeGeneratorTask>("jmhRunBytecodeGenerator") {
}

tasks.named<Jar>("jmhJar") {
    isZip64 = true
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

jmh {
    resultFormat = "JSON"
    includes.set(listOf("ReplacementBenchmark"))
    forceGC = true
    duplicateClassesStrategy = DuplicatesStrategy.EXCLUDE
    resultsFile = layout.buildDirectory.file("reports/jmh/results.json")
}

jmhReport {
    jmhResultPath = layout.buildDirectory.file("reports/jmh/results.json").get().asFile.absolutePath
    jmhReportOutput = layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}
