# Media3 ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * { @dagger.* <fields>; }
-keepclasseswithmembers class * { @javax.inject.* <fields>; }

# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.* class * { *; }

# Coil
-keep class coil.** { *; }

# OkHttp / OkIO
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlinx Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# Compose
-dontwarn androidx.compose.**

# Lumen models (preserve for Room + Compose state)
-keep class com.lumen.player.domain.model.** { *; }
-keep class com.lumen.player.data.local.entity.** { *; }
-keep class com.lumen.player.data.local.dao.** { *; }

# Android
-keepattributes Signature, SourceFile, LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends androidx.work.Worker
-keep public class * extends androidx.work.CoroutineWorker
