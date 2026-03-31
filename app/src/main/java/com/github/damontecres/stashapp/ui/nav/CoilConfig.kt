package com.github.damontecres.stashapp.ui.nav

import androidx.compose.runtime.Composable
import coil3.ImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.github.damontecres.stashapp.proto.StashPreferences
import com.github.damontecres.stashapp.views.models.ServerViewModel
import okhttp3.Call
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class, ExperimentalCoilApi::class)
@Composable
fun CoilConfig(
    serverViewModel: ServerViewModel,
    preferences: StashPreferences,
) {
    setSingletonImageLoaderFactory { ctx ->
        val cacheLogging = preferences.cachePreferences.logCacheHits
        val diskCacheSize =
            preferences.cachePreferences.imageDiskCacheSize
                .coerceAtLeast(10)
        // When cacheExpirationTime == 0 the user wants no caching, so respect HTTP
        // cache-control headers from the server. Otherwise, ignore those headers and
        // always use Coil's own disk cache so thumbnails survive across scrolls.
        val alwaysCache = preferences.cachePreferences.cacheExpirationTime > 0

        val callFactory =
            Call.Factory { request ->
                // TODO this seems hacky?
                serverViewModel.requireServer().okHttpClient.newCall(request)
            }

        ImageLoader
            .Builder(ctx)
            .memoryCache(
                MemoryCache
                    .Builder()
                    .maxSizePercent(ctx, 0.25)
                    .build(),
            ).diskCache(
                DiskCache
                    .Builder()
                    .directory(ctx.cacheDir.resolve("coil3_image_cache"))
                    .maxSizeBytes(diskCacheSize)
                    .build(),
            ).crossfade(true)
            .logger(if (cacheLogging) DebugLogger() else null)
            .components {
                if (alwaysCache) {
                    // No cache-control strategy → Coil always writes to / reads from
                    // its own disk cache, regardless of HTTP response headers.
                    add(OkHttpNetworkFetcherFactory(callFactory = { callFactory }))
                } else {
                    // User disabled caching: respect whatever the server sends.
                    add(
                        OkHttpNetworkFetcherFactory(
                            cacheStrategy = { CacheControlCacheStrategy() },
                            callFactory = { callFactory },
                        ),
                    )
                }
            }.build()
    }
}
