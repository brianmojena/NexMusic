package org.example.musicplayer

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import android.os.Handler
import android.os.Looper

class App(private val context: Context) {

    private val handler = Handler(Looper.getMainLooper())
    private val progressRunnable = object : Runnable {
        override fun run() {
            updateProgress()
            handler.postDelayed(this, 1000)
        }
    }


    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen

    private val _nowPlaying = MutableStateFlow<Song?>(null)
    val nowPlaying: StateFlow<Song?> = _nowPlaying

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress

    private val _userSongs = MutableStateFlow<List<Song>>(emptyList())
    val userSongs: StateFlow<List<Song>> = _userSongs

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed

    private val _recommendations = MutableStateFlow<List<Song>>(emptyList())
    val recommendations: StateFlow<List<Song>> = _recommendations

    private val _trending = MutableStateFlow<List<Song>>(emptyList())
    val trending: StateFlow<List<Song>> = _trending

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue

    private val _isShuffleOn = MutableStateFlow(false)
    val isShuffleOn: StateFlow<Boolean> = _isShuffleOn

    var mediaPlayer: MediaPlayer? = null

    init {
        loadMockData()
    }

    suspend fun loadLocalMusic() {
        withContext(Dispatchers.IO) {
            val songs = mutableListOf<Song>()
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            )
            val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
            val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val title = cursor.getString(titleColumn)
                    val artist = cursor.getString(artistColumn)
                    val albumId = cursor.getLong(albumIdColumn)
                    val duration = cursor.getLong(durationColumn)

                    if (duration >= 60_000) {
                        val albumArtUri = getAlbumArtUri(albumId)
                        val contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())

                        songs.add(Song(id.toString(), title, artist, albumArtUri, contentUri.toString()))
                    }
                }
            }
            _userSongs.value = songs
            _queue.value = songs
        }
    }

    private fun getAlbumArtUri(albumId: Long): String {
        return "content://media/external/audio/albumart/$albumId"
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun playSong(song: Song) {
        _nowPlaying.value = song
        _isPlaying.value = true
        addToRecentlyPlayed(song)

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(context, Uri.parse(song.contentUri))
            prepare()
            start()
            setOnCompletionListener {
                playNextSong()
            }
        }

        startProgressUpdates()
    }

    fun togglePlayPause() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressUpdates()
            } else {
                player.start()
                _isPlaying.value = true
                startProgressUpdates()
            }
        }
    }

    fun seekTo(position: Float) {
        mediaPlayer?.let { player ->
            val newPosition = (position * player.duration).toInt()
            player.seekTo(newPosition)
            _progress.value = position
        }
    }

    fun playNextSong() {
        val currentQueue = _queue.value
        val currentSongIndex = currentQueue.indexOfFirst { it.id == _nowPlaying.value?.id }
        if (currentSongIndex != -1 && currentSongIndex < currentQueue.size - 1) {
            playSong(currentQueue[currentSongIndex + 1])
        } else if (currentQueue.isNotEmpty()) {
            playSong(currentQueue[0])
        }
    }

    fun playPreviousSong() {
        val currentQueue = _queue.value
        val currentSongIndex = currentQueue.indexOfFirst { it.id == _nowPlaying.value?.id }
        if (currentSongIndex > 0) {
            playSong(currentQueue[currentSongIndex - 1])
        } else if (currentQueue.isNotEmpty()) {
            playSong(currentQueue.last())
        }
    }

    fun toggleShuffle() {
        _isShuffleOn.value = !_isShuffleOn.value
        if (_isShuffleOn.value) {
            _queue.value = _queue.value.shuffled()
        } else {
            _queue.value = _userSongs.value
        }
    }

    private fun startProgressUpdates() {
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
    }

    private fun updateProgress() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                val currentPosition = player.currentPosition.toFloat()
                val duration = player.duration.toFloat()
                _progress.value = currentPosition / duration
            }
        }
    }

    private fun addToRecentlyPlayed(song: Song) {
        val currentList = _recentlyPlayed.value.toMutableList()
        currentList.remove(song)
        currentList.add(0, song)
        _recentlyPlayed.value = currentList.take(5)
    }

    private fun loadMockData() {
        _recommendations.value = List(10) { index ->
            Song("rec$index", "Recomendación $index", "Artista $index", "https://picsum.photos/200/300?random=$index", "")
        }
        _trending.value = List(10) { index ->
            Song("trend$index", "Tendencia $index", "Artista Trending $index", "https://picsum.photos/200/300?random=${index + 100}", "")
        }
    }
}

enum class Screen {
    Home,
    MyMusic,
    Playlists,
    Account,
    NowPlaying
}

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val albumCover: String,
    val contentUri: String
)

