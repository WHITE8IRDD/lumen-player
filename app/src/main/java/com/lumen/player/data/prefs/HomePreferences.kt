package com.lumen.player.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.homeDataStore: DataStore<Preferences> by preferencesDataStore(name = "home_prefs")

@Singleton
class HomePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val RECENTLY_ADDED_VISIBLE = booleanPreferencesKey("recently_added_visible")
        val YOUR_FOLDERS_VISIBLE = booleanPreferencesKey("your_folders_visible")
    }

    val recentlyAddedVisible: Flow<Boolean> = context.homeDataStore.data
        .map { it[Keys.RECENTLY_ADDED_VISIBLE] ?: true }

    val yourFoldersVisible: Flow<Boolean> = context.homeDataStore.data
        .map { it[Keys.YOUR_FOLDERS_VISIBLE] ?: true }

    suspend fun setRecentlyAddedVisible(visible: Boolean) {
        context.homeDataStore.edit { it[Keys.RECENTLY_ADDED_VISIBLE] = visible }
    }

    suspend fun setYourFoldersVisible(visible: Boolean) {
        context.homeDataStore.edit { it[Keys.YOUR_FOLDERS_VISIBLE] = visible }
    }
}
