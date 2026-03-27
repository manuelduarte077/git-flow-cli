package dev.donmanuel.desktop.logging

import org.slf4j.LoggerFactory

object DesktopLog {
    private val log = LoggerFactory.getLogger("GitBnFlow.desktop")

    fun warn(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            log.warn("{}: {}", message, throwable.toLogSummary(), throwable)
        } else {
            log.warn(message)
        }
    }

    fun error(message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            log.error("{}: {}", message, throwable.toLogSummary(), throwable)
        } else {
            log.error(message)
        }
    }

    private fun Throwable.toLogSummary(): String =
        "${javaClass.simpleName}${message?.let { ": $it" } ?: ""}"
}
