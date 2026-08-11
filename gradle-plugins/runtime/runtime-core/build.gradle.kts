plugins {
    `java-library`
    `maven-publish`
}

description = "Runtime dependency resolution and relocation utilities."

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

publishing {
    repositories {
        maven {
            name = "aquaticRepository"
            url = uri(
                "https://repo.aquatic.gg/" +
                    if (version.toString().endsWith("-SNAPSHOT")) "snapshots" else "releases"
            )

            credentials {
                username = extra["mavenUsername"] as String
                password = extra["mavenPassword"] as String
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "runtime-core"
        }
    }
}
