plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group = "dev.donmanuel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.github.ajalt.clikt:clikt:4.2.0")

}

kotlin {
    jvmToolchain(24)
}

application {
    mainClass.set("dev.donmanuel.cli.MainKt")
}

tasks.test {
    useJUnitPlatform()
}