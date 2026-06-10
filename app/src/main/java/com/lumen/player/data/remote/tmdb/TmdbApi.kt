package com.lumen.player.data.remote.tmdb

import com.lumen.player.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

data class TmdbMovieResult(
    val title: String,
    val posterPath: String?,
    val overview: String?,
    val backdropPath: String?
)

@Singleton
class TmdbApi @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun searchMovie(query: String, year: Int?): TmdbMovieResult? {
        return withContext(Dispatchers.IO) {
            try {
                val urlBuilder = HttpUrl.Builder()
                    .scheme("https")
                    .host("api.themoviedb.org")
                    .addPathSegments("3/search/movie")
                    .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                    .addQueryParameter("query", query)
                if (year != null) {
                    urlBuilder.addQueryParameter("year", year.toString())
                }

                val request = Request.Builder()
                    .url(urlBuilder.build())
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return@withContext null
                if (results.length() == 0) return@withContext null

                val first = results.getJSONObject(0)
                TmdbMovieResult(
                    title = first.optString("title", ""),
                    posterPath = first.optString("poster_path", null),
                    overview = first.optString("overview", null),
                    backdropPath = first.optString("backdrop_path", null)
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    suspend fun searchTv(query: String, year: Int?): TmdbMovieResult? {
        return withContext(Dispatchers.IO) {
            try {
                val urlBuilder = HttpUrl.Builder()
                    .scheme("https")
                    .host("api.themoviedb.org")
                    .addPathSegments("3/search/tv")
                    .addQueryParameter("api_key", BuildConfig.TMDB_API_KEY)
                    .addQueryParameter("query", query)
                if (year != null) {
                    urlBuilder.addQueryParameter("first_air_date_year", year.toString())
                }

                val request = Request.Builder()
                    .url(urlBuilder.build())
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val json = JSONObject(body)
                val results = json.optJSONArray("results") ?: return@withContext null
                if (results.length() == 0) return@withContext null

                val first = results.getJSONObject(0)
                TmdbMovieResult(
                    title = first.optString("name", ""),
                    posterPath = first.optString("poster_path", null),
                    overview = first.optString("overview", null),
                    backdropPath = first.optString("backdrop_path", null)
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
