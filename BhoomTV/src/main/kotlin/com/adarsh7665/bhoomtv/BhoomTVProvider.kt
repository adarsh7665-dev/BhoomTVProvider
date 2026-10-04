package com.adarsh7665.bhoomtv

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class BhoomTVProvider : MainAPI() {
    override var mainUrl = "https://bhoomtv.org"
    override var name = "BHOOM TV"
    override val supportedTypes = setOf(TvType.Live)
    override val lang = "ml"

    private val mapper = jacksonObjectMapper()

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get("${mainUrl}/channel/malayalam/page/${page + 1}/").document
        val items = doc.select("article, .item, .post, .channel-item, .bt_bb_grid_item").mapNotNull { it.toSearchResponse() }
            .ifEmpty {
                doc.select("a[href*='/live/']").mapNotNull { a ->
                    val href = a.attr("abs:href")
                    if (href.isBlank()) null else BhoomSearchResponse(
                        title = a.text().trim(),
                        url = href,
                        posterUrl = a.selectFirst("img")?.absUrl("src")
                    )
                }
            }
        return newHomePageResponse(
            listOf(HomePageList(request.name, items, isHorizontalImages = true)),
            hasNext = page < 3
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val doc = app.get("${mainUrl}/?s=${java.net.URLEncoder.encode(query, "UTF-8")}").document
        return doc.select("a[href*='/live/']").mapNotNull { a ->
            val href = a.absUrl("href")
            if (href.isBlank()) null else BhoomSearchResponse(
                title = a.text().trim(),
                url = href,
                posterUrl = a.selectFirst("img")?.absUrl("src")
            )
        }.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url).document
        val title = doc.selectFirst("h1")?.text()?.trim()
            ?: doc.title().substringBefore(" | ").trim()
        val poster = doc.selectFirst("meta[property='og:image']")?.attr("content")
            ?: doc.selectFirst("img")?.absUrl("src")
        return newLiveStreamLoadResponse(title, url, BhoomTVProvider::class.java, poster)
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val doc = app.get(data).document

        // Prefer explicitly exposed HLS/DASH source URLs in page markup.
        val html = doc.html()
        val directCandidates = Regex(
            """https?://[^"'\s<>]+\.(?:m3u8|mpd)(?:\?[^"'\s<>]*)?""",
            RegexOption.IGNORE_CASE
        ).findAll(html).map { it.value }.distinct().toList()

        for (source in directCandidates) {
            val type = if (source.contains(".mpd", ignoreCase = true)) ExtractorLinkType.DASH else ExtractorLinkType.M3U8
            callback(
                newExtractorLink(
                    sourceURL = source,
                    name = name,
                    url = source,
                    type = type,
                    quality = Qualities.Unknown.value
                )
            )
        }

        // Common HTML5 video/source tags.
        doc.select("video source[src], source[src], iframe[src]").forEach { el ->
            val src = el.absUrl("src").ifBlank { el.attr("src") }
            if (src.isBlank()) return@forEach
            if (src.contains(".m3u8", true) || src.contains(".mpd", true)) {
                val type = if (src.contains(".mpd", true)) ExtractorLinkType.DASH else ExtractorLinkType.M3U8
                callback(newExtractorLink(sourceURL = src, name = name, url = src, type = type, quality = Qualities.Unknown.value))
            }
        }

        // Look for structured JSON-LD / script data containing direct stream URLs.
        Regex(
            """["'](?:file|src|source|stream|url|playlist)["']\s*[:=]\s*["']([^"']+)["']""",
            RegexOption.IGNORE_CASE
        ).findAll(html).forEach { match ->
            val src = match.groupValues[1].replace("\/", "/")
            if (src.contains(".m3u8", true) || src.contains(".mpd", true)) {
                val type = if (src.contains(".mpd", true)) ExtractorLinkType.DASH else ExtractorLinkType.M3U8
                callback(newExtractorLink(sourceURL = src, name = name, url = src, type = type, quality = Qualities.Unknown.value))
            }
        }

        return directCandidates.isNotEmpty()
    }

    private fun Element.toSearchResponse(): SearchResponse? {
        val link = selectFirst("a[href*='/live/']")?.absUrl("href")
            ?: selectFirst("a[href]")?.absUrl("href")
            ?: return null
        val title = selectFirst("h2, h3, .title, .entry-title")?.text()?.trim()
            ?: selectFirst("a[href*='/live/']")?.text()?.trim()
            ?: return null
        val poster = selectFirst("img")?.absUrl("src")
        return BhoomSearchResponse(title, link, poster)
    }

    private class BhoomSearchResponse(
        title: String,
        url: String,
        posterUrl: String? = null
    ) : LiveSearchResponse(title, url, BhoomTVProvider::class.java, posterUrl)
}

@JsonIgnoreProperties(ignoreUnknown = true)
private data class BhoomStreamSource(
    val src: String? = null,
    val file: String? = null,
    val url: String? = null,
    val type: String? = null
)
