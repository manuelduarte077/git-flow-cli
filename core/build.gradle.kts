plugins {
    kotlin("jvm")
}

dependencies {
    implementation("org.tomlj:tomlj:1.1.1")
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

tasks.processResources {
    from(rootProject.layout.projectDirectory.file("git-flow-cli.example.toml"))
    filesMatching("**/git-flow-cli-version.properties") {
        filter { line -> line.replace("@version@", project.version.toString()) }
    }
}

tasks.withType<Jar>().configureEach {
    manifest {
        attributes(
            "Implementation-Version" to project.version,
            "Implementation-Title" to "git-flow-cli-core",
        )
    }
}

tasks.test {
    useJUnitPlatform()
}
