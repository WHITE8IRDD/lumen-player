package com.lumen.player.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import javax.inject.Inject
import javax.inject.Singleton

private val Context.searchPrefsDataStore by preferencesDataStore(name = "lumen_search")

@Singleton
class SearchDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val RECENT_SEARCHES = stringPreferencesKey("recent_searches")
    }

    fun observeRecentSearches(): Flow<List<String>> =
        context.searchPrefsDataStore.data.map { prefs ->
            val raw = prefs[Keys.RECENT_SEARCHES] ?: "[]"
            try {
                JSONArray(raw).let { arr ->
                    (0 until arr.length()).map { arr.getString(it) }
                }
            } catch (_: Exception) { emptyList() }
        }

    suspend fun addRecentSearch(query: String) {
        context.searchPrefsDataStore.edit { prefs ->
            val existing = prefs[Keys.RECENT_SEARCHES] ?: "[]"
            val arr = try { JSONArray(existing) } catch (_: Exception) { JSONArray() }
            val deduped = mutableListOf<String>()
            if (arr.length() > 0) {
                for (i in 0 until arr.length()) {
                    val s = arr.getString(i)
                    if (s != query) deduped.add(s)
                }
            }
            deduped.add(0, query)
            val trimmed = deduped.take(10)
            prefs[Keys.RECENT_SEARCHES] = JSONArray(trimmed.toList()).toString()
        }
    }

    suspend fun clearRecentSearches() {
        context.searchPrefsDataStore.edit { it.remove(Keys.RECENT_SEARCHES) }
    }
}
