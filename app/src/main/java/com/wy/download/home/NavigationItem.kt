package com.wy.download.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(var route: String, var icon: ImageVector, var title: String) {
    data object Home : NavigationItem("home", Icons.Outlined.Home, "Home")
    data object Movies : NavigationItem("Movies", Icons.Outlined.PlayArrow, "Movies")
    data object Music : NavigationItem("Music", Icons.Outlined.FavoriteBorder, "Music")
    data object Ebook : NavigationItem("Ebook", Icons.Outlined.DateRange, "Ebook")
    data object Settings : NavigationItem("settings", Icons.Outlined.Settings, "Settings")
}