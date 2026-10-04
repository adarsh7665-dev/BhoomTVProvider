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

    private val streamCache = mutableMapOf<String, String?>()

    private suspend fun findDirectStream(pageUrl: String): String? {
        if (streamCache.containsKey(pageUrl)) return streamCache[pageUrl]

        val result = try {
            val response = app.get(
                pageUrl,
                referer = mainUrl,
                headers = mapOf(
                    "User-Agent" to USER_AGENT,
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language" to "en-US,en;q=0.9"
                )
            )

            val html = response.text
                .replace("\\/", "/")
                .replace("\\u0026", "&")
                .replace("\\u002F", "/")
                .replace("&amp;", "&")

            val candidates = linkedSetOf<String>()

            fun add(raw: String?) {
                if (raw.isNullOrBlank()) return

                var value = raw.trim()
                    .replace("\\/", "/")
                    .replace("\\u0026", "&")
                    .replace("\\u002F", "/")
                    .replace("&amp;", "&")
                    .trim('"', '\'')

                val nested = Regex("""(?i)(?:https?:)?//[^"'\\s<>\\\\]+""")
                    .find(value)
                    ?.value

                if (!nested.isNullOrBlank()) value = nested

                val resolved = try {
                    when {
                        value.startsWith("//") -> "https:$value"
                        value.startsWith("http://", true) || value.startsWith("https://", true) -> value
                        value.startsWith("/") -> URI(pageUrl).resolve(value).toString()
                        else -> URI(pageUrl).resolve(value).toString()
                    }
                } catch (_: Exception) {
                    return
                }

                if (resolved.contains(".m3u8", true) || resolved.contains(".mpd", true)) {
                    candidates.add(resolved)
                }
            }

            // Direct URLs anywhere in the page source.
            Regex("""(?i)(?:https?:)?//[^"'\\s<>\\\\]+""")
                .findAll(html)
                .forEach { add(it.value) }

            // Common player/source configuration fields.
            Regex(
                """(?is)["'](?:file|src|source|stream|url|playlist|hls|dash|streamUrl|stream_url|fileUrl|file_url|m3u8|mpd)["']\\s*[:=]\\s*["']([^"']+)["']"""
            ).findAll(html).forEach {
                add(it.groupValues[1])
            }

            // HTML attributes.
            response.document.select(
                "[src], [data-src], [data-url], [data-source], [data-stream], " +
                    "[data-video], [data-video-url], [data-file], [data-m3u8], [data-mpd]"
            ).forEach { element ->
                listOf(
                    "src", "data-src", "data-url", "data-source", "data-stream",
                    "data-video", "data-video-url", "data-file", "data-m3u8", "data-mpd"
                ).forEach { attr ->
                    add(element.attr(attr))
                }
            }

            candidates.firstOrNull()
        } catch (_: Exception) {
            null
        }

        streamCache[pageUrl] = result
        return result
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val pageNumber = page.coerceAtLeast(1)
        val url = if (pageNumber == 1) channelPage else "$mainUrl/channel/malayalam/page/$pageNumber/"
        val doc = app.get(url, referer = mainUrl).document
        val items = parseChannelPage(doc)

        val maxPage = doc.select("a[href*='/channel/malayalam/page/']")
            .mapNotNull { link ->
                Regex("""/channel/malayalam/page/(\\d+)/?""")
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
        val normalizedQuery = query.trim()
        val pages = (1..4).map { page ->
            val url = if (page == 1) channelPage else "$mainUrl/channel/malayalam/page/$page/"
            app.get(url, referer = mainUrl).document
        }

        return pages
            .flatMap { it.select("a[href*='/live/']") }
            .mapNotNull { anchor ->
                val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }
                if (!href.contains("/live/")) return@mapNotNull null

                val title = anchor.selectFirst("h2, h3, .title, .entry-title")?.text()?.trim()
                    ?: anchor.text().trim()

                if (title.isBlank()) return@mapNotNull null
                if (normalizedQuery.isNotBlank() &&
                    !title.contains(normalizedQuery, ignoreCase = true)
                ) {
                    return@mapNotNull null
                }

                val poster = findPoster(anchor)

                // Keep the BHOOM channel page URL here. The actual stream is
                // resolved when the result is opened, so failed stream extraction
                // cannot make the channel disappear from search results.
                newLiveSearchResponse(
                    name = title,
                    url = href
                ) {
                    posterUrl = poster
                }
            }
            .distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse? {
        // Resolve the actual stream when the channel is opened.
        val stream = findDirectStream(url)

        return newLiveStreamLoadResponse(
            name = url.substringAfter("/live/").trim('/').replace("-", " "),
            url = url,
            dataUrl = stream ?: url
        )
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (!data.startsWith("http://", true) && !data.startsWith("https://", true)) {
            return false
        }

        // Normally load() has already converted the channel page into a
        // direct M3U8/MPD URL. Keep a fallback for pages where the stream
        // can only be resolved at playback time.
        val stream = if (
            data.contains(".m3u8", true) || data.contains(".mpd", true)
        ) {
            data
        } else {
            findDirectStream(data)
        }

        if (!stream.isNullOrBlank()) {
            val type = when {
                stream.contains(".mpd", true) -> ExtractorLinkType.DASH
                stream.contains(".m3u8", true) -> ExtractorLinkType.M3U8
                else -> null
            }

            if (type != null) {
                callback(
                    newExtractorLink(
                        source = name,
                        name = name,
                        url = stream,
                        type = type
                    ) {
                        referer = ""
                        quality = Qualities.Unknown.value
                    }
                )
                return true
            }
        }

        // Last fallback: let CloudStream's extractor system inspect the page.
        return try {
            loadExtractor(data, subtitleCallback, callback)
            true
        } catch (_: Exception) {
            false
        }
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
