import me.champeau.jmh.JmhBytecodeGeneratorTask

plugins {
    id("me.champeau.jmh") version "0.7.3"
    id("io.morethan.jmhreport") version "0.9.0"
}

group = "gg.aquatic.klocale"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(project(":Common"))
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("net.kyori:adventure-text-serializer-plain:4.25.0")

    jmh("org.openjdk.jmh:jmh-core:1.37")
    jmh("org.openjdk.jmh:jmh-generator-annprocess:1.37")

    jmhImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    jmhImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")

    testImplementation(kotlin("test"))
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.25.0")
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
    resultsFile = project.file("${project.buildDir}/reports/jmh/results.json")
}

jmhReport {
    jmhResultPath = project.file("${project.buildDir}/reports/jmh/results.json").absolutePath
    jmhReportOutput = project.layout.buildDirectory.dir("reports/jmh").get().asFile.absolutePath
}

tasks.jmh {
    finalizedBy(tasks.jmhReport)
}

tasks.test {
    useJUnitPlatform()
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
            artifactId = "KLocale-Paper"
            version = "${project.version}"

            from(components["java"])
            //artifact(tasks.compileJava)
        }
    }
}