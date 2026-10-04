package com.adarsh7665.bhoomtv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.net.URI

class BhoomTVProvider : MainAPI() {
    override var mainUrl = "https://bhoomtv.org"
    override var name = "BHOOM TV"
    override var lang = "ml"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Live)

    private val channelPage = "$mainUrl/channel/malayalam/"

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val pageNumber = page.coerceAtLeast(1)
        val url = if (pageNumber == 1) channelPage else "$mainUrl/channel/malayalam/page/$pageNumber/"
        val doc = app.get(url, referer = mainUrl).document
        val items = parseChannelPage(doc)

        val maxPage = doc.select("a[href*='/channel/malayalam/page/']")
            .mapNotNull { link ->
                Regex("""/channel/malayalam/page/(\d+)/?""")
                    .find(link.attr("href"))
                    ?.groupValues
                    ?.getOrNull(1)
                    ?.toIntOrNull()
            }
            .maxOrNull()

        val hasNext = maxPage?.let { pageNumber < it } ?: (items.isNotEmpty() && pageNumber < 4)

        return newHomePageResponse(
            listOf(
                HomePageList(
                    "Malayalam Live TV",
                    items,
                    isHorizontalImages = false
                )
            ),
            hasNext = hasNext
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
        val doc = app.get("$mainUrl/?s=$encodedQuery", referer = mainUrl).document
        return parseChannelPage(doc)
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url, referer = mainUrl).document
        val title = doc.selectFirst("h1")?.text()?.trim()
            ?: doc.selectFirst("meta[property='og:title']")?.attr("content")?.trim()
            ?: "BHOOM TV Channel"

        val poster = doc.selectFirst("meta[property='og:image']")
            ?.attr("content")
            ?.trim()
            ?.ifBlank { null }

        return newLiveStreamLoadResponse(
            name = title,
            url = url,
            dataUrl = url
        ) {
            posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val response = app.get(data, referer = mainUrl)
        val html = response.text
        val doc = response.document

        val streamUrls = linkedSetOf<String>()
        val playerUrls = linkedSetOf<String>()

        fun addCandidate(raw: String?) {
            val resolved = normalizeUrl(raw, data) ?: return
            val clean = resolved
                .replace("\\/", "/")
                .replace("&amp;", "&")

            when {
                clean.contains(".m3u8", ignoreCase = true) ||
                    clean.contains(".mpd", ignoreCase = true) -> streamUrls.add(clean)
                clean.startsWith("http://") || clean.startsWith("https://") -> playerUrls.add(clean)
            }
        }

        // Direct HLS/DASH URLs exposed in the page or inline scripts.
        Regex(
            """(?i)(?:https?:)?//[^"'\s<>\\]+\.(?:m3u8|mpd)(?:\?[^"'\s<>\\]*)?"""
        ).findAll(html).forEach { addCandidate(it.value) }

        // Common player/embed attributes.
        doc.select(
            "iframe[src], iframe[data-src], amp-iframe[src], " +
                "video[src], video source[src], source[src], " +
                "[data-src], [data-url], [data-stream], [data-playlist]"
        ).forEach { element ->
            addCandidate(
                element.attr("src").ifBlank {
                    element.attr("data-src").ifBlank {
                        element.attr("data-url").ifBlank {
                            element.attr("data-stream").ifBlank { element.attr("data-playlist") }
                        }
                    }
                }
            )
        }

        // BHOOM source buttons can keep the actual player URL in uncommon data attributes or inline JavaScript.
        doc.select(
            "[onclick], [data-src], [data-url], [data-source], [data-stream], " +
                "[data-video], [data-video-url], [data-embed], [data-embed-url], " +
                "[data-player], [data-player-url], [data-href], [data-link], " +
                "[data-playlist], [data-file], [data-m3u8], [data-mpd]"
        ).forEach { element ->
            listOf(
                "onclick", "data-src", "data-url", "data-source", "data-stream",
                "data-video", "data-video-url", "data-embed", "data-embed-url",
                "data-player", "data-player-url", "data-href", "data-link",
                "data-playlist", "data-file", "data-m3u8", "data-mpd"
            ).forEach { attr ->
                val value = element.attr(attr)
                if (value.isNotBlank()) {
                    Regex("""(?i)(?:https?:)?//[^\"'\\s<>\\\\]+""")
                        .findAll(value)
                        .forEach { addCandidate(it.value) }
                    addCandidate(value)
                }
            }
        }

        // Common player configuration / JSON fields.
        Regex(
            """(?i)[\"'](?:file|src|source|stream|url|playlist|hls|dash|embed|player|video)[\"']\\s*[:=]\\s*[\"']([^\"']+)[\"']"""
        ).findAll(html).forEach { addCandidate(it.groupValues[1]) }

        // Catch direct media/player URLs anywhere in inline HTML/JS.
        Regex("""(?i)(?:https?:)?//[^\"'\\s<>\\\\]+""").findAll(html).forEach { match ->
            val candidate = match.value
            if (candidate.contains(".m3u8", true) || candidate.contains(".mpd", true) ||
                candidate.contains("/embed/", true) || candidate.contains("/player", true) ||
                candidate.contains("player.", true) || candidate.contains("stream", true)) {
                addCandidate(candidate)
            }
        }

        // Some player URLs are base64-encoded in page data.
        Regex("""(?<![A-Za-z0-9+/])([A-Za-z0-9+/]{80,}={0,2})(?![A-Za-z0-9+/])""").findAll(html).forEach { match ->
            try {
                val decoded = android.util.Base64.decode(match.groupValues[1], android.util.Base64.DEFAULT).toString(Charsets.UTF_8)
                if (decoded.startsWith("http", true) || decoded.contains(".m3u8", true) || decoded.contains(".mpd", true)) {
                    addCandidate(decoded)
                }
            } catch (_: Exception) { }
        }

        var linkCount = 0

        streamUrls.forEach { source ->
            val type = if (source.contains(".mpd", ignoreCase = true)) {
                ExtractorLinkType.DASH
            } else {
                ExtractorLinkType.M3U8
            }

            val link = newExtractorLink(
                source = "BHOOM TV",
                name = name,
                url = source,
                type = type
            ) {
                referer = data
                headers = mapOf("Origin" to mainUrl)
            }

            callback(link)
            linkCount++
        }

        // Send embedded player URLs through CloudStream's extractor registry.
        playerUrls
            .filterNot { it.contains("b9e3e814.delivery.rocketcdn.me", ignoreCase = true) }
            .distinct()
            .forEach { player ->
                try {
                    loadExtractor(
                        url = player,
                        referer = data,
                        subtitleCallback = subtitleCallback,
                        callback = {
                            linkCount++
                            callback(it)
                        }
                    )
                } catch (_: Exception) {
                    // One dead mirror must not prevent the other sources from loading.
                }
            }

        return linkCount > 0
    }

    private fun parseChannelPage(doc: Document): List<SearchResponse> {
        return doc.select("a[href*='/live/']")
            .mapNotNull { anchor ->
                val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }
                if (!href.contains("/live/")) return@mapNotNull null

                val title = anchor.selectFirst("h2, h3, .title, .entry-title")?.text()?.trim()
                    ?: anchor.text().trim()

                if (title.isBlank()) return@mapNotNull null

                val poster = findPoster(anchor)

                newLiveSearchResponse(title, href) {
                    posterUrl = poster
                }
            }
            .distinctBy { it.url }
    }

    private fun findPoster(anchor: Element): String? {
        val direct = anchor.selectFirst("img")?.let {
            it.absUrl("src").ifBlank { it.attr("src") }
        }
        if (!direct.isNullOrBlank()) return direct

        val parentPoster = anchor.parent()?.selectFirst("img")?.let {
            it.absUrl("src").ifBlank { it.attr("src") }
        }
        if (!parentPoster.isNullOrBlank()) return parentPoster

        val grandParentPoster = anchor.parent()?.parent()?.selectFirst("img")?.let {
            it.absUrl("src").ifBlank { it.attr("src") }
        }
        if (!grandParentPoster.isNullOrBlank()) return grandParentPoster

        return anchor.parents()
            .asSequence()
            .mapNotNull { parent ->
                parent.selectFirst("img")?.let {
                    it.absUrl("src").ifBlank { it.attr("src") }
                }
            }
            .firstOrNull { !it.isNullOrBlank() }
    }

    private fun normalizeUrl(raw: String?, baseUrl: String): String? {
        if (raw.isNullOrBlank()) return null

        var value = raw.trim()
            .replace("\\/", "/")
            .replace("&amp;", "&")
            .replace("\\u0026", "&")
            .replace("\\u002F", "/")

        if (value.startsWith("javascript:", ignoreCase = true)) return null
        if (value.startsWith("data:", ignoreCase = true)) return null

        return try {
            when {
                value.startsWith("//") -> "https:$value"
                value.startsWith("http://", ignoreCase = true) ||
                    value.startsWith("https://", ignoreCase = true) -> value
                else -> URI(baseUrl).resolve(value).toString()
            }
        } catch (_: Exception) {
            null
        }
    }
}
