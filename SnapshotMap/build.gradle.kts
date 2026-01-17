plugins {
    kotlin("jvm") version "2.3.0"
    id("me.champeau.jmh") version "0.7.2"
}

group = "gg.aquatic.shardedmap"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.knowm.xchart:xchart:3.8.8")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("generateCharts") {
    dependsOn("jmh")
    group = "benchmark"
    description = "Runs benchmarks and generates PNG charts"

    mainClass.set("gg.aquatic.shardedmap.ChartKt")
    classpath = sourceSets["jmh"].runtimeClasspath

    workingDir = projectDir
}

jmh {
    threads.set(Runtime.getRuntime().availableProcessors())
    resultFormat.set("JSON")
    benchmarkMode.set(listOf("thrpt"))

    jvmArgs.set(listOf(
        "-XX:-RestrictContended"
    ))
}