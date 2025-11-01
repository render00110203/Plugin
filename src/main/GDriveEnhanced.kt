package com.bollywoodfever

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import kotlinx.coroutines.delay

class GDriveEnhanced : MainAPI() {
    override var mainUrl = "https://bollywoodfever2310.onrender.com"
    override var name = "GDrive Enhanced"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)
    override val lang = "hi"

    // Search endpoint from your Node.js backend
    override suspend fun search(query: String): List<SearchResponse> {
        val response = app.get("$mainUrl/api/search?query=${query.encodeUri()}")
        val data = response.parsedSafe<GDriveResponse>() ?: return emptyList()

        return data.results.map {
            MovieSearchResponse(
                name = it.name,
                url = it.id,
                apiName = name,
                type = TvType.Movie,
                posterUrl = it.thumbnail,
                year = null
            )
        }
    }

    // Load streams
    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val res = app.get("$mainUrl/api/stream/${data}")
        val json = res.parsedSafe<StreamResponse>() ?: return false

        json.streams.forEach {
            callback(
                ExtractorLink(
                    source = "GDrive",
                    name = it.name,
                    url = it.url,
                    referer = mainUrl,
                    quality = Qualities.Unknown,
                    isM3u8 = it.url.contains(".m3u8")
                )
            )
        }

        // Subtitles if provided
        json.subtitles?.forEach {
            subtitleCallback(SubtitleFile(it.lang, it.url))
        }

        return true
    }

    // Optional: fetch movie details
    override suspend fun load(url: String): LoadResponse? {
        return MovieLoadResponse(
            name = "GDrive Movie",
            url = url,
            apiName = name,
            type = TvType.Movie,
            dataUrl = url,
            posterUrl = null,
            year = null,
            plot = "Stream powered by GDrive Enhanced",
            rating = null
        )
    }

    // Data models
    data class GDriveResponse(val results: List<GDriveItem>)
    data class GDriveItem(val id: String, val name: String, val thumbnail: String?)
    data class StreamResponse(val streams: List<StreamItem>, val subtitles: List<SubtitleItem>?)
    data class StreamItem(val name: String, val url: String)
    data class SubtitleItem(val lang: String, val url: String)
}
