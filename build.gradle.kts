plugins {
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.dokka") version "2.0.0"
    `java-library`
    `maven-publish`
}

group = "org.tribot"
version = System.getenv("VERSION") ?: "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.runelite.net")
}

dependencies {
    api("com.github.TribotRS:automation-sdk:latest.release")
    compileOnly("net.runelite:client:latest.release")
    testImplementation(kotlin("test"))
    testImplementation("io.mockk:mockk:1.13.16")
    testImplementation("net.runelite:client:latest.release")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading")
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
