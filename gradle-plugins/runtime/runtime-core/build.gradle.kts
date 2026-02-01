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
    options.release.set(11)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "runtime-core"
        }
    }
}
