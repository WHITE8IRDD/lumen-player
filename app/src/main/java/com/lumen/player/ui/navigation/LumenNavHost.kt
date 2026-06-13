package com.lumen.player.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import com.lumen.player.ui.components.LumenSquircle
import com.lumen.player.ui.components.ShimmerBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lumen.player.ui.equalizer.EqualizerScreen
import com.lumen.player.ui.folders.FolderDetailScreen
import com.lumen.player.ui.home.HomeScaffold
import com.lumen.player.ui.home.HomeScreen
import com.lumen.player.ui.home.HomeViewModel
import com.lumen.player.ui.library.LibraryScreen
import com.lumen.player.ui.player.PlayerScreen
import com.lumen.player.ui.player.QueueScreen
import com.lumen.player.ui.player.VideoPlayerScreen
import com.lumen.player.ui.playlist.PlaylistDetailScreen
import com.lumen.player.ui.player.enterPip
import com.lumen.player.ui.search.SearchScreen
import com.lumen.player.ui.onboarding.OnboardingScreen
import com.lumen.player.ui.settings.SettingsHiddenItemsScreen
import com.lumen.player.ui.settings.SettingsScreen
import com.lumen.player.ui.settings.SettingsViewModel
import com.lumen.player.ui.components.LumenBlobPreview

@UnstableApi
@Composable
fun LumenNavHost(
    hasMediaPermission: Boolean
) {
    val settingsVm: SettingsViewModel = hiltViewModel()
    val isReady by settingsVm.isReady.collectAsStateWithLifecycle()
    val settings by settingsVm.settings.collectAsStateWithLifecycle()

    if (!isReady) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            ShimmerBox(Modifier.size(40.dp), shape = LumenSquircle(12.dp))
        }
        return
    }

    if (!settings.onboardingCompleted) {
        OnboardingScreen()
        return
    }

    val rootNav: NavHostController = rememberNavController()
    val homeVm: HomeViewModel = hiltViewModel()
    val playback by homeVm.playbackState.collectAsStateWithLifecycle()

    var playerExpanded by remember { mutableStateOf(false) }
    var videoFullscreen by remember { mutableStateOf(false) }
    var searchVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    var isGridView by remember { mutableStateOf(true) }

    HomeScaffold(
        rootNav = rootNav,
        playback = playback,
        onPlayPauseMini = homeVm::playPause,
        onExpandPlayer = {
            if (playback.isVideo) videoFullscreen = true else playerExpanded = true
        },
        onDismissMini = homeVm::stopPlayback,
        content = { innerPadding ->
            NavHost(
                navController = rootNav,
                startDestination = Destinations.HOME,
                modifier = Modifier.fillMaxSize().padding(innerPadding)
            ) {
                composable(Destinations.HOME) {
                    HomeScreen(
                        onSearchClick = { searchVisible = true },
                        onItemClick = { item ->
                            android.util.Log.d("HOME", "item ${item.title} isVideo=${item.isVideo}")
                            homeVm.playItem(item)
                            if (item.isVideo) videoFullscreen = true
                            else playerExpanded = true
                        },
                        onFolderClick = { folder ->
                            rootNav.navigate(Destinations.folderDetail(folder.path))
                        },
                        onSeeAll = { filter ->
                            rootNav.navigate(Destinations.LIBRARY) {
                                popUpTo(rootNav.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
                composable(Destinations.LIBRARY) {
                    LibraryScreen(
                        hasPermission = hasMediaPermission,
                        onItemClick = { item ->
                            val route = if (item.isVideo) "VideoPlayerScreen" else "PlayerScreen"
                            android.util.Log.d("ROUTE", "opening $route for ${item.title} (isVideo=${item.isVideo})")
                            if (item.isVideo) videoFullscreen = true
                            else playerExpanded = true
                        },
                        onFolderClick = { path -> rootNav.navigate(Destinations.folderDetail(path)) },
                        onPlaylistClick = { id -> rootNav.navigate(Destinations.playlistDetail(id)) },
                        isGridView = isGridView,
                        onToggleViewMode = { isGridView = !isGridView }
                    )
                }
                composable(
                    Destinations.PLAYLIST_DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.LongType })
                ) {
                    PlaylistDetailScreen(
                        onBack = { rootNav.popBackStack() },
                        onNavigateToPlayer = {
                            if (playback.isVideo) videoFullscreen = true
                            else playerExpanded = true
                        }
                    )
                }
                composable(
                    Destinations.FOLDER_DETAIL,
                    arguments = listOf(navArgument("path") { type = NavType.StringType })
                ) {
                    FolderDetailScreen(
                        onBack = { rootNav.popBackStack() },
                        onVideoClick = { videoFullscreen = true },
                        isGridView = isGridView,
                        onToggleViewMode = { isGridView = !isGridView }
                    )
                }
                composable(Destinations.SETTINGS) {
                    SettingsScreen(
                        onDebugBlob = { rootNav.navigate(Destinations.LUMEN_BLOB_DEBUG) },
                        onOpenEqualizer = { rootNav.navigate(Destinations.EQUALIZER) },
                        onOpenHiddenItems = { rootNav.navigate(Destinations.HIDDEN_ITEMS) }
                    )
                }
                composable(Destinations.HIDDEN_ITEMS) {
                    SettingsHiddenItemsScreen(onBack = { rootNav.popBackStack() })
                }
                composable(Destinations.EQUALIZER) {
                    EqualizerScreen(onBack = { rootNav.popBackStack() })
                }
                composable(Destinations.QUEUE) {
                    QueueScreen(onBack = { rootNav.popBackStack() })
                }
                composable(Destinations.LUMEN_BLOB_DEBUG) {
                    LumenBlobPreview()
                }
            }
        }
    )

    AnimatedVisibility(
        visible = playerExpanded,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        PlayerScreen(
            onCollapse = { playerExpanded = false },
            onOpenEqualizer = {
                playerExpanded = false
                rootNav.navigate(Destinations.EQUALIZER)
            },
            onOpenQueue = {
                playerExpanded = false
                rootNav.navigate(Destinations.QUEUE)
            }
        )
    }

    AnimatedVisibility(
        visible = videoFullscreen,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        Box(Modifier.fillMaxSize()) {
            VideoPlayerScreen(
                onClose = { videoFullscreen = false },
                onEnterPip = { activity?.let { enterPip(it, 16, 9) } }
            )
        }
    }

    AnimatedVisibility(
        visible = searchVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxSize()
    ) {
        SearchScreen(
            onBack = { searchVisible = false },
            onItemClick = { item ->
                searchVisible = false
                if (item.isVideo) videoFullscreen = true
                else playerExpanded = true
            },
            onFolderClick = { path ->
                searchVisible = false
                rootNav.navigate(Destinations.folderDetail(path))
            }
        )
    }
}
