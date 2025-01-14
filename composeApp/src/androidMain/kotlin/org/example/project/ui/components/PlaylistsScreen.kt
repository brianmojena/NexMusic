import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun PlaylistsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            Text(
                "Mis Playlists",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(10) {
            PlaylistItem(
                name = "Playlist ${it + 1}",
                songCount = (it + 1) * 5,
                onClick = { /* Navegar a la lista de canciones de la playlist */ }
            )
        }
    }
}

@Composable
fun PlaylistItem(name: String, songCount: Int, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = { Text("$songCount canciones") },
        leadingContent = {
            Icon(
                Icons.Default.QueueMusic,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

