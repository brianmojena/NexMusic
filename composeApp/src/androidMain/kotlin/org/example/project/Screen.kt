package org.example.project

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Enum para manejar las pantallas de la aplicación.
 */
enum class Screen(val title: String, val icon: ImageVector, val route: String) {
    Home("Inicio", Icons.Default.Home, "home"),
    MyMusic("Mi Música", Icons.Default.LibraryMusic, "myMusic"),
    Playlists("Playlists", Icons.Default.QueueMusic, "playlists"),
    Account("Cuenta", Icons.Default.Person, "account"),
    NowPlaying("Reproducir", Icons.Default.PlayCircle, "nowPlaying")
}