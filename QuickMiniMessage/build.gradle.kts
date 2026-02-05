plugins {
    kotlin("jvm") version "2.3.0"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
    id("me.champeau.jmh") version "0.7.2"
    `maven-publish`
}

group = "gg.aquatic"
version = "26.0.1"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    jmhImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    jmhImplementation("net.kyori:adventure-text-minimessage:4.25.0")
    jmhImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    jmhImplementation("org.knowm.xchart:xchart:3.8.7")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
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

val mavenUsername = if (env.isPresent("MAVEN_USERNAME")) env.fetch("MAVEN_USERNAME") else ""
val mavenPassword = if (env.isPresent("MAVEN_PASSWORD")) env.fetch("MAVEN_PASSWORD") else ""

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri("https://repo.nekroplex.com/releases")

            credentials {
                username = mavenUsername
                password = mavenPassword
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "gg.aquatic"
            artifactId = "QuickMiniMessage"
            version = "${project.version}"

            from(components["java"])
        }
    }
}
