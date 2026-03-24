package dev.donmanuel.desktop.navigation

import kotlinx.serialization.Serializable

@Serializable
data object RouteSelection

@Serializable
data class RoutePendingToml(val root: String)

@Serializable
data class RouteMain(val root: String)
