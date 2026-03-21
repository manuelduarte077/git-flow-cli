plugins {
    kotlin("jvm") version "2.3.10"
    application
}

group = "dev.donmanuel"
version = "2.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.github.ajalt.clikt:clikt:4.2.0")
    implementation("org.tomlj:tomlj:1.1.1")

}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.donmanuel.cli.MainKt")
    applicationName = "git-bn-cli"
}

distributions {
    main {
        distributionBaseName.set("git-bn-cli")
    }
}

tasks.named<Tar>("distTar") {
    compression = Compression.GZIP
}

tasks.test {
    useJUnitPlatform()
}
