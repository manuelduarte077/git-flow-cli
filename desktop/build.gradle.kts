import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.10"
    id("org.jetbrains.compose") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.10"
}

dependencies {
    implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.9.0"))
    implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation(project(":core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation("com.mohamedrejeb.calf:calf-file-picker:0.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")
}

compose {
    resources {
        publicResClass = true
        packageOfResClass = "dev.donmanuel.desktop.generated"
    }
}

compose.desktop {
    application {
        mainClass = "dev.donmanuel.desktop.MainKt"
        nativeDistributions {
            packageName = "Git BN Flow"
            description = "Git BN Flow — formularios rama y commits (complemento al CLI)"
            copyright = "See project license"
            vendor = "donmanuel"
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            macOS {
                bundleID = "dev.donmanuel.gitbnflow.desktop"
                iconFile.set(project.file("icons/AppIcon.icns"))
            }
            windows {
                iconFile.set(project.file("icons/AppIcon.ico"))
            }
        }
    }
}

tasks.withType<JavaExec>().configureEach {
    val exec = this
    jvmArgumentProviders.add(
        object : CommandLineArgumentProvider {
            override fun asArguments(): Iterable<String> {
                val launcher = exec.javaLauncher.orNull ?: return emptyList()
                return if (launcher.metadata.languageVersion.asInt() >= 24) {
                    listOf("--enable-native-access=ALL-UNNAMED")
                } else {
                    emptyList()
                }
            }
        },
    )
}
