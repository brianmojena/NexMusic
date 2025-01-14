package org.example.project

import AccountScreen
import HomeScreen
import org.example.project.ui.components.MyMusicScreen
import org.example.project.ui.components.NowPlayingScreen
import PlaylistsScreen
import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.example.musicplayer.App
import org.example.project.model.MusicPlayerAppTheme

class MainActivity : ComponentActivity() {
    private lateinit var app: App
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = App(applicationContext)

        // Configura el lanzador para manejar el resultado de la solicitud de permisos
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions[Manifest.permission.READ_MEDIA_AUDIO] == true ||
                permissions[Manifest.permission.READ_EXTERNAL_STORAGE] == true) {
                // Permiso concedido, cargar música
                lifecycleScope.launch {
                    app.loadLocalMusic()
                }
            } else {
                // Permiso denegado
            }
        }

        // Solicitar permisos
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_MEDIA_AUDIO))
        } else {
            permissionLauncher.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }

        setContent {
            MusicPlayerAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MusicPlayerApp(app)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerApp(app: App) {
    val navController = rememberNavController()
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Screen.values().forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentScreen == screen,
                        onClick = {
                            currentScreen = screen
                            navController.navigate(screen.route)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, app = app)
            }
            composable(Screen.MyMusic.route) {
                MyMusicScreen(navController, app)
            }
            composable(Screen.Playlists.route) {
                PlaylistsScreen(navController)
            }
            composable(Screen.Account.route) {
                AccountScreen(navController)
            }
            composable(Screen.NowPlaying.route) {
                NowPlayingScreen(navController, app)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MusicPlayerAppTheme {
        MusicPlayerApp(app = App(android.content.ContextWrapper(null)))
    }
}