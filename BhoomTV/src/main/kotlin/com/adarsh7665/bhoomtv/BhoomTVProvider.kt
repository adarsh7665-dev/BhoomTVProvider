package com.adarsh7665.bhoomtv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*

class BhoomTVProvider : MainAPI() {
    override var mainUrl = "https://bhoomtv.org"
    override var name = "BHOOM TV"
    override var lang = "ml"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Live)

    private val channelPage = "$mainUrl/channel/malayalam/"

    /**
     * One playable source for a channel.
     * A channel can have several of these (e.g. HD + SD, or a backup feed).
     * CloudStream lists each one in the "Sources" picker while playing.
     * [label] is the name shown in that picker; if null, "Stream 1", "Stream 2"... is used.
     */
    private data class Stream1(
        val url: String,
        val referer: String = "",
        val label: String? = null
    )

    private fun one(url: String, referer: String = "") = listOf(Stream1(url, referer))

    /*
     * To give a channel more than one source, list several entries, e.g.
     *
     *   "some-channel" to listOf(
     *       Stream1("https://example.com/hd.m3u8", label = "HD"),
     *       Stream1("https://example.com/sd.m3u8", label = "SD")
     *   ),
     *
     * Key = the slug from the channel page URL (bhoomtv.org/live/<slug>/).
     */
    private val streamsBySlug: Map<String, List<Stream1>> = mapOf(
        "keralam-hd" to one("http://51.75.127.199:3141/zeekeralamhd/index.m3u8"),
        "globeon-television" to one("https://bhoomtv.net/geo/live.m3u8?id=3565&vtoken=st=1790654897", mainUrl),
        "wayanad-vision-plus" to one("https://online.wayanadvision.in/hls/streaming1.m3u8"),
        "wayanad-vision-movies" to one("https://online.wayanadvision.in/hls/wvmovies.m3u8"),
        "cranganore-tv" to one("https://live.applelive.in/cranganorechannel/cranganorechannel/index.m3u8"),
        "kerala-gtpl-tv" to one("https://kerala-gtpl.keralive.workers.dev/keralagtpltv/keralagtpltv/index.m3u8", mainUrl),
        "janapriyam-tv" to one("https://bhoomtv.net/geo/live.m3u8?id=1455&vtoken=st=1790654998", "https://bhoomtv.me/"),
        "flowers-tv-usa" to one("https://yuppmedtaorire.akamaized.net/v1/master/a0d007312bfd99c47f76b77ae26b1ccdaae76cb1/flowers_nim_https/050522/flowers/playlist.m3u8"),
        "flowers-tv" to one("https://yt-live-proxy.npashru.workers.dev/flowers"),
        "kairali-tv" to one("https://mumt01.tangotv.in/O5aw8Zn3KAIRALI/index.m3u8"),
        "kairali-we" to one("https://streams.tangotv.in/WETV/ORIGIN/index.m3u8"),
        "kairali-arabia" to one("https://streamhub.dhruvpatil681.workers.dev/3452.m3u8"),
        "mazhavil-hd" to one("https://ddozob4sbfsmt.cloudfront.net/out/v1/51aaeddf56854312add90dfa8df07e39/index.m3u8"),
        "middle-east" to one("https://mumt03.tangotv.in/Dsly5z3HASIANETMIDDLEEAST/index.m3u8"),
        "ntv-uae-hd" to one("https://stream.logichost.in/NTV/live/playlist.m3u8"),
        "amrita-tv" to one("https://ddash74r36xqp.cloudfront.net/master_2000.m3u8"),
        "raj-music-malayalam" to one("https://colorsportscreen.com/freetv/stream.m3u8?id=743e3ee3b8c3", "https://colorsportscreen.com/"),
        "vibgyor-tv" to one("https://livestream.vibgyortv.com/hls/stream.m3u8"),
        "safari-tv" to one("https://j78dp346yq5r-hls-live.5centscdn.com/safari/live.stream/playlist.m3u8"),
        "kappa-tv" to one("https://mumt03.tangotv.in/Dsly5z3HKAPPATV/index.m3u8"),
        "dd-malayalam" to one("https://d3eyhgoylams0m.cloudfront.net/v1/manifest/93ce20f0f52760bf38be911ff4c91ed02aa2fd92/ed7bd2c7-8d10-4051-b397-2f6b90f99acb/562ee8f9-9950-48a0-ba1d-effa00cf0478/2.m3u8"),
        "kerala-vision" to one("https://mumt03.tangotv.in/Dsly5z3HKERALAVISION/index.m3u8"),
        "kaumudy-tv" to one("https://oqgdrkxby4rm-hls-live.5centscdn.com/kaumudytv/live.stream/chunks.m3u8"),
        "magnavision-tv" to one("https://d1taaads3ztvmu.cloudfront.net//120723//smil:magnavision.smil//chunklist_b2628000.m3u8"),
        "jeevan-tv" to one("https://yupp-tv.keralive.workers.dev/120723/smil:jeevan.smil/playlist.m3u8", mainUrl),
        "darshana-tv" to one("https://colorsportscreen.com/freetv/stream.m3u8?id=8914ef2ce69d", "https://colorsportscreen.com/"),
        "kasaragod-vision" to one("https://live.skystream.in/live/kasaragodvsn/index.m3u8"),
        "v9-news-vadakkencherry" to one("https://5a1178b42cc03.streamlock.net/8212/8212/playlist.m3u8"),
        "newstar" to one("https://live.skystream.in/live/newsstar2924/index.m3u8"),
        "media-plus" to one("https://live.applelive.in/mediaplus/mediaplus/index.m3u8"),
        "south-vision" to one("https://live.skystream.in/live/southvsnklm/index.m3u8"),
        "tcn-trikaripur" to one("https://live.skystream.in/live/tcn8909/index.m3u8"),
        "k7-keralam" to one("https://live.skystream.in/live/keralam2742/index.m3u8"),
        "ccnet-cherupuzha" to one("https://live.skystream.in/live/ccnet7082/index.m3u8"),
        "kcn-kasaragod" to one("https://live.skystream.in/live/kcnksrgod/index.m3u8"),
        "vtv-entertainment" to one("https://play.applelive.in/vtv/vtv.m3u8"),
        "drisya-news-pala" to one("https://live.skystream.in/live/DrisyaPala2924/index.m3u8"),
        "new-vision-mundakayam" to one("https://live.skystream.in/live/newvision2924/index.m3u8"),
        "city-channel-kasargod" to one("https://live.skystream.in/live/citytv2924/index.m3u8"),
        "city-vision-kannur" to one("https://live.skystream.in/live/citykannurcity/index.m3u8"),
        "network-payyanur" to one("https://live.skystream.in/live/networkpayyanur/index.m3u8"),
        "utv-utsav" to one("https://em4qj6nedyvg-hls-live.wmncdn.net/liveunit/89b1e919eed04e59383cf820d644c20e.sdp/chunks.m3u8"),
        "drishya-tv" to one("https://live.skystream.in/live/drishya5060/index.m3u8"),
        "ctv-mukkam" to one("https://live.skystream.in/live/ctv1805/index.m3u8"),
        "drisya-tv" to one("https://live.skystream.in/live/DrisyaPala2924/index.m3u8"),
        "kerala-today" to one("https://live.skystream.in/live/krlatoday/index.m3u8"),
        "media-today" to one("https://live.skystream.in/live/mediatday/index.m3u8"),
        "thanima-tv" to one("https://live.skystream.in/live/thanimatvtvm/index.m3u8"),
        "gtv" to one("https://play.applelive.in/gtv/gtv.m3u8"),
        "gtv-max" to one("https://play.applelive.in/gtvmax/gtvmax.m3u8"),
        "periyar-vision" to one("https://stream.onecloudlive.in/periyarvision/periyarvision/index.m3u8"),
        "powran-tv" to one("https://stream.onecloudlive.in/powranmedia/livestream/index.m3u8"),
        // NOTE: teak-vision-nilambur and n-vision-music use the same id (2933d0818d2f).
        // One of them is probably wrong - check on bhoomtv.org and fix the id.
        "teak-vision-nilambur" to one("https://colorsportscreen.com/freetv/stream.m3u8?id=2933d0818d2f", "https://colorsportscreen.com/"),
        "nattuvartha-tv" to one("https://live.skystream.in/live/nattuvartha5945/index.m3u8"),
        "cctv-pradeshikam" to one("https://96vd9q4kyxq7-hls-live.5centscdn.com/CCTVWEB/3d17933d07af044d3a2caa608d6a8afe.sdp/playlist.m3u8"),
        "cctv-kunnamkulam" to one("https://96vd9q4kyxq7-hls-live.5centscdn.com/livestream/f25aeb3e9210967d546a077de6e105e4.sdp/playlist.m3u8"),
        "n-media-channel" to one("https://live.skystream.in/live/newmedia2924/index.m3u8"),
        "starnet-kerala" to one("https://play.applelive.in/starnettv/starnettv.m3u8"),
        "nick-malayalam" to one("https://jio.drmlive.au/jio/bpk-tv/Nick_Malayalam_MOB/Fallback/index.m3u8"),
        "pravasi-channel-hd" to one("https://m6gdavepdn93-hls-live.5centscdn.com/indialife/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"),
        "kannur-vision" to one("https://stream.logichost.in/kannurvision/live/playlist.m3u8"),
        "vcv-vadakkanchery" to one("https://5a1178b42cc03.streamlock.net/8210/8210/playlist.m3u8"),
        "c-malayalam-tv" to one("https://2-fss-2.streamhoster.com/pl_120/206508-3261972-1/playlist.m3u8"),
        "starnet-vadakkanchery" to one("https://5a1178b42cc03.streamlock.net/8220/8220/playlist.m3u8"),
        "garshom-tv" to one("https://og2qd3aal7an-hls-live.5centscdn.com/garshomtv/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"),
        "divine-tv" to one("https://playout7multirtmp.tulix.tv/live21/Stream1/playlist.m3u8"),
        "media-vision-tv" to one("https://iptv.mediavisionlive.in/jalworldiptv/mvtv/playlist.m3u8"),
        "wayanad-vision" to one("https://online.wayanadvision.in/hls/wayanadvision.m3u8"),
        "utv-palakkad" to one("https://em4qj6nedyvg-hls-live.wmncdn.net/utv/ab2de115318ae40bdd434e193a2f2e7f.sdp/playlist.m3u8"),
        "harvest-usa" to one("https://7mbd4ogkr3gx-hls-live.wmncdn.net/harvestusa/d57ffba6564caea2fee3f4085f19a098.sdp/chunks.m3u8"),
        "pulari-tv" to one("https://royalstarindia.co.in/pularitv_hls/pularitv.m3u8"),
        "n-vision" to one("https://ipcloud.live/nvision/nvisionhd/index.m3u8"),
        "n-vision-music" to one("https://colorsportscreen.com/freetv/stream.m3u8?id=2933d0818d2f", "https://colorsportscreen.com/"),
        "koickal-news" to one("https://play.applelive.in/koickalnews/koickalnews.m3u8"),
        "victers-channel" to one("https://932y4x26ljv8-hls-live.5centscdn.com/victers/tv.stream/playlist.m3u8"),
        "metro-live" to one("https://kcnsdhlsapp.ylivestream.com/kcnsdapphls/index.m3u8"),
        "malabar-channel" to one("http://cloud.logichost.in:1935/live/malabarnews/playlist.m3u8"),
        "malabar-plus" to one("http://cloud.logichost.in:1935/live/mplus/playlist.m3u8"),
        "swantham-channel" to one("http://cloud.logichost.in:1935/SWANTHAM/live/playlist.m3u8"),
        "kvtv" to one("https://2-fss-2.themediacdn.com/pl_138/206324-3157542-1/chunklist.m3u8"),
        "kvtv-news" to one("https://2-fss-2.themediacdn.com/pl_138/206324-3157542-1/chunklist.m3u8"),
        "word-to-world" to one("https://live.wmncdn.net/worldtoworld/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"),
        "goodness-tv" to one("https://mumt07.tangotv.in/zHjX9OFlGOODNESSTV/index.m3u8"),
        "harvest-tv-keralam" to one("https://7mbd4ogkr3gx-hls-live.wmncdn.net/harvesttvlive1/bbb19eae240ec100af921d511efc86a0.sdp/index.m3u8"),
        "power-vision-tv" to one("https://live.drmlive-02.workers.dev/yupptv/fta/power-vision-tv.m3u8"),
        "shalom-tv" to one("https://d2c4zqo2rb5uf1.cloudfront.net/master.m3u8"),
        "shalom-global" to one("https://d28xtgmk9tfk6b.cloudfront.net/master.m3u8"),
        "shekinah-tv" to one("https://livetv.timeiptv.in/ShekinahNewsIndia/955ad3298db330b5ee880c2c9e6f23a0.sdp/chunks.m3u8")
    )

    // Container pages on BHOOM that are not real channels.
    private val hiddenTitles = setOf("mollywood tv", "mollywood plus", "mollywood max")

    private fun pageUrlFor(slug: String) = "$mainUrl/live/$slug/"

    private fun slugOf(url: String): String =
        url.substringAfter("/live/", "").substringBefore("/").substringBefore("?").lowercase()

    private fun prettyName(slug: String): String =
        slug.split("-").filter { it.isNotBlank() }
            .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val pageNumber = page.coerceAtLeast(1)
        val url = if (pageNumber == 1) channelPage else "$mainUrl/channel/malayalam/page/$pageNumber/"
        val doc = app.get(url, referer = mainUrl).document

        val items = parseChannelPage(doc)

        val maxPage = doc.select("a[href*='/channel/malayalam/page/']")
            .mapNotNull { link ->
                Regex("""/channel/malayalam/page/(\d+)/?""").find(link.attr("href"))
                    ?.groupValues?.getOrNull(1)?.toIntOrNull()
            }.maxOrNull()

        val hasNext = maxPage?.let { pageNumber < it } ?: (items.isNotEmpty() && pageNumber < 4)

        return newHomePageResponse(
            listOf(HomePageList("Malayalam Live TV", items, isHorizontalImages = false)),
            hasNext = hasNext
        )
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val q = query.trim()
        val results = ArrayList<SearchResponse>()

        for (page in 1..4) {
            val url = if (page == 1) channelPage else "$mainUrl/channel/malayalam/page/$page/"
            val doc = try {
                app.get(url, referer = mainUrl).document
            } catch (e: Exception) {
                continue
            }
            results += parseChannelPage(doc).filter { q.isBlank() || it.name.contains(q, true) }
        }

        return results.distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        val slug = slugOf(url)

        // Old saved favourites/bookmarks used the raw stream URL as the id.
        if (slug.isBlank()) {
            return newLiveStreamLoadResponse(name = "Live Stream", url = url, dataUrl = url)
        }

        val pageUrl = pageUrlFor(slug)
        var title = prettyName(slug)
        var poster: String? = null

        try {
            val doc = app.get(pageUrl, referer = mainUrl).document
            val ogTitle = doc.selectFirst("meta[property=og:title]")?.attr("content").orEmpty()
            val cleaned = ogTitle.substringBefore(" Live Online").removePrefix("Watch ").trim()
            if (cleaned.isNotBlank()) title = cleaned
            poster = doc.selectFirst("meta[property=og:image]")?.attr("content")?.takeIf { it.isNotBlank() }
        } catch (e: Exception) {
            // Fall back to the slug-based name.
        }

        return newLiveStreamLoadResponse(name = title, url = pageUrl, dataUrl = pageUrl) {
            posterUrl = poster
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val slug = slugOf(data)

        val candidates: List<Stream1> = when {
            slug.isNotBlank() -> streamsBySlug[slug].orEmpty()
            data.startsWith("http://", true) || data.startsWith("https://", true) -> listOf(Stream1(data))
            else -> emptyList()
        }
        if (candidates.isEmpty()) return false

        // Do not probe the manifest from inside loadLinks(): some live servers reject
        // the probe request even though ExoPlayer can play the same URL.
        candidates.forEachIndexed { index, candidate ->
            val type = if (candidate.url.contains(".mpd", true)) {
                ExtractorLinkType.DASH
            } else {
                ExtractorLinkType.M3U8
            }

            val label = candidate.label
                ?: if (candidates.size > 1) "Stream ${index + 1}" else name

            callback(
                newExtractorLink(source = name, name = label, url = candidate.url, type = type) {
                    referer = candidate.referer
                    headers = mapOf(
                        "User-Agent" to USER_AGENT,
                        "Accept" to "*/*"
                    )
                    quality = Qualities.Unknown.value
                }
            )
        }

        return true
    }

    private fun parseChannelPage(doc: org.jsoup.nodes.Document): List<SearchResponse> {
        return doc.select("a[href*='/live/']")
            .mapNotNull { anchor ->
                val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }
                if (!href.contains("/live/")) return@mapNotNull null

                val title = anchor.selectFirst("h2, h3, .title, .entry-title")?.text()?.trim()
                    ?: anchor.text().trim()
                if (title.isBlank()) return@mapNotNull null
                if (title.lowercase() in hiddenTitles) return@mapNotNull null

                val slug = slugOf(href)
                if (!streamsBySlug.containsKey(slug)) return@mapNotNull null

                val poster = findPoster(anchor)

                // The channel page URL is the id, so two channels that happen to share a
                // stream URL no longer collapse into one entry.
                newLiveSearchResponse(title, pageUrlFor(slug)) {
                    posterUrl = poster
                }
            }
            .distinctBy { it.url }
    }

    private fun findPoster(anchor: org.jsoup.nodes.Element): String? {
        val direct = anchor.selectFirst("img")?.let {
            it.absUrl("src").ifBlank { it.attr("src") }
        }
        if (!direct.isNullOrBlank()) return direct

        val parentPoster = anchor.parent()?.selectFirst("img")?.let {
            it.absUrl("src").ifBlank { it.attr("src") }
        }
        if (!parentPoster.isNullOrBlank()) return parentPoster

        return anchor.parents()
            .asSequence()
            .mapNotNull { parent ->
                parent.selectFirst("img")?.let {
                    it.absUrl("src").ifBlank { it.attr("src") }
                }
            }
            .firstOrNull { !it.isNullOrBlank() }
    }
}
