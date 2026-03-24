import org.gradle.api.tasks.bundling.Compression

plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(project(":core"))
    implementation("com.github.ajalt.clikt:clikt:4.2.0")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("dev.donmanuel.cli.MainKt")
    applicationName = "git-flow-cli"
}

distributions {
    main {
        distributionBaseName.set("git-flow-cli")
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Title" to "git-flow-cli",
        )
    }
}

tasks.named<Tar>("distTar") {
    compression = Compression.GZIP
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}
