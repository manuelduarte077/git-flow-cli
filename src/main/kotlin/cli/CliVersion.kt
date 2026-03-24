package dev.donmanuel.cli

import java.io.File
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.Properties
import java.util.jar.Attributes
import java.util.jar.JarFile

object CliVersion {

    const val REPO_URL = "https://github.com/manuelduarte077/git-flow-cli"

    fun current(): String {
        readFromResource()?.let { return it }
        readFromJarManifest()?.let { return it }
        return "dev"
    }

    /** JAR que contiene esta clase (instalación empacada); null en `./gradlew run` u otros modos sin JAR. */
    fun mainJarFileOrNull(): File? = codeSourceJarFile()

    private fun readFromResource(): String? {
        val stream = CliVersion::class.java.getResourceAsStream("/git-flow-cli-version.properties")
            ?: return null
        return Properties().apply { load(stream) }.getProperty("version")?.trim()
            ?.takeIf { it.isNotEmpty() && it != "@version@" }
    }

    private fun readFromJarManifest(): String? {
        val pkg = CliVersion::class.java.`package`
        pkg?.implementationVersion?.takeIf { it.isNotEmpty() }?.let { return it }

        val jarFile = codeSourceJarFile() ?: return null
        return JarFile(jarFile).use { jar ->
            jar.manifest?.mainAttributes?.getValue(Attributes.Name.IMPLEMENTATION_VERSION.toString())
        }
    }

    private fun codeSourceJarFile(): File? {
        val url = CliVersion::class.java.getResource(
            CliVersion::class.java.name.replace('.', '/') + ".class",
        ) ?: return null
        if (url.protocol != "jar") return null
        val path = url.path
        val separator = path.indexOf("!/")
        if (separator < 0) return null
        val jarPath = path.substring(0, separator)
        val filePath = URLDecoder.decode(jarPath.removePrefix("file:"), StandardCharsets.UTF_8)
        return File(filePath).takeIf { it.isFile }
    }
}
