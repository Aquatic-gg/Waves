plugins {
    kotlin("jvm") version "2.3.0"
    id("me.champeau.jmh") version "0.7.2"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    `maven-publish`
}

group = "gg.aquatic.shardedmap"
version = "26.0.1"

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

val maven_username = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val maven_password = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri("https://repo.nekroplex.com/releases")

            credentials {
                username = maven_username
                password = maven_password
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "shardedmap"
            version = "${project.version}"
            from(components["java"])
        }
    }
}