package com.adarsh7665.bhoomtv

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import java.net.URI

class BhoomTVProvider : MainAPI() {
    override var mainUrl = "https://bhoomtv.org"
    override var name = "BHOOM TV"
    override var lang = "ml"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Live)

    private val channelPage = "$mainUrl/channel/malayalam/"

    private data class Stream1(
        val url: String,
        val referer: String = ""
    )

    /*
     * BHOOM's webpage now loads its player/source dynamically with Shaka Player.
     * CloudStream therefore should not crawl the BHOOM page looking for a
     * manifest. For playback, use one direct Stream-1 manifest per channel.
     *
     * These entries are intentionally single-source: no Stream 2/3/4 fallback
     * is added. HLS is preferred wherever a public HLS Stream-1 endpoint exists.
     */
    private val stream1BySlug = mapOf(
        "keralam-hd" to Stream1(
            "http://51.75.127.199:3141/zeekeralamhd/index.m3u8"
        ),
        "globeon-television" to Stream1(
            "https://bhoomtv.net/geo/live.m3u8?id=3565&vtoken=st=1790654897",
            mainUrl
        ),
        "wayanad-vision-plus" to Stream1(
            "https://online.wayanadvision.in/hls/streaming1.m3u8"
        ),
        "wayanad-vision-movies" to Stream1(
            "https://online.wayanadvision.in/hls/wvmovies.m3u8"
        ),
        "cranganore-tv" to Stream1(
            "https://live.applelive.in/cranganorechannel/cranganorechannel/index.m3u8"
        ),
        "kerala-gtpl-tv" to Stream1(
            "https://kerala-gtpl.keralive.workers.dev/keralagtpltv/keralagtpltv/index.m3u8",
            mainUrl
        ),
        "janapriyam-tv" to Stream1(
            "https://bhoomtv.net/geo/live.m3u8?id=1455&vtoken=st=1790654998",
            "https://bhoomtv.me/"
        ),
        "flowers-tv-usa" to Stream1(
            "https://yuppmedtaorire.akamaized.net/v1/master/a0d007312bfd99c47f76b77ae26b1ccdaae76cb1/flowers_nim_https/050522/flowers/playlist.m3u8"
        ),
        "flowers-tv" to Stream1(
            "https://yt-live-proxy.npashru.workers.dev/flowers"
        ),
        "kairali-tv" to Stream1(
            "https://mumt01.tangotv.in/O5aw8Zn3KAIRALI/index.m3u8"
        ),
        "kairali-we" to Stream1(
            "https://streams.tangotv.in/WETV/ORIGIN/index.m3u8"
        ),
        "kairali-arabia" to Stream1(
            "https://streamhub.dhruvpatil681.workers.dev/3452.m3u8"
        ),
        "mazhavil-hd" to Stream1(
            "https://ddozob4sbfsmt.cloudfront.net/out/v1/51aaeddf56854312add90dfa8df07e39/index.m3u8"
        ),
        "middle-east" to Stream1(
            "https://mumt03.tangotv.in/Dsly5z3HASIANETMIDDLEEAST/index.m3u8"
        ),
        "ntv-uae-hd" to Stream1(
            "https://stream.logichost.in/NTV/live/playlist.m3u8"
        ),
        "amrita-tv" to Stream1(
            "https://ddash74r36xqp.cloudfront.net/master_2000.m3u8"
        ),
        "raj-music-malayalam" to Stream1(
            "https://colorsportscreen.com/freetv/stream.m3u8?id=743e3ee3b8c3",
            "https://colorsportscreen.com/"
        ),
        "vibgyor-tv" to Stream1(
            "https://livestream.vibgyortv.com/hls/stream.m3u8"
        ),
        "safari-tv" to Stream1(
            "https://j78dp346yq5r-hls-live.5centscdn.com/safari/live.stream/playlist.m3u8"
        ),
        "kappa-tv" to Stream1(
            "https://mumt03.tangotv.in/Dsly5z3HKAPPATV/index.m3u8"
        ),
        "dd-malayalam" to Stream1(
            "https://d3eyhgoylams0m.cloudfront.net/v1/manifest/93ce20f0f52760bf38be911ff4c91ed02aa2fd92/ed7bd2c7-8d10-4051-b397-2f6b90f99acb/562ee8f9-9950-48a0-ba1d-effa00cf0478/2.m3u8"
        ),
        "kerala-vision" to Stream1(
            "https://mumt03.tangotv.in/Dsly5z3HKERALAVISION/index.m3u8"
        ),
        "kaumudy-tv" to Stream1(
            "https://oqgdrkxby4rm-hls-live.5centscdn.com/kaumudytv/live.stream/chunks.m3u8"
        ),
        "magnavision-tv" to Stream1(
            "https://d1taaads3ztvmu.cloudfront.net//120723//smil:magnavision.smil//chunklist_b2628000.m3u8"
        ),
        "jeevan-tv" to Stream1(
            "https://yupp-tv.keralive.workers.dev/120723/smil:jeevan.smil/playlist.m3u8",
            mainUrl
        ),
        "darshana-tv" to Stream1(
            "https://colorsportscreen.com/freetv/stream.m3u8?id=8914ef2ce69d",
            "https://colorsportscreen.com/"
        ),
        "kasaragod-vision" to Stream1(
            "https://live.skystream.in/live/kasaragodvsn/index.m3u8"
        ),
        "v9-news-vadakkencherry" to Stream1(
            "https://5a1178b42cc03.streamlock.net/8212/8212/playlist.m3u8"
        ),
        "newstar" to Stream1(
            "https://live.skystream.in/live/newsstar2924/index.m3u8"
        ),
        "media-plus" to Stream1(
            "https://live.applelive.in/mediaplus/mediaplus/index.m3u8"
        ),
        "south-vision" to Stream1(
            "https://live.skystream.in/live/southvsnklm/index.m3u8"
        ),
        "tcn-trikaripur" to Stream1(
            "https://live.skystream.in/live/tcn8909/index.m3u8"
        ),
        "k7-keralam" to Stream1(
            "https://live.skystream.in/live/keralam2742/index.m3u8"
        ),
        "ccnet-cherupuzha" to Stream1(
            "https://live.skystream.in/live/ccnet7082/index.m3u8"
        ),
        "kcn-kasaragod" to Stream1(
            "https://live.skystream.in/live/kcnksrgod/index.m3u8"
        ),
        "vtv-entertainment" to Stream1(
            "https://play.applelive.in/vtv/vtv.m3u8"
        ),
        "drisya-news-pala" to Stream1(
            "https://live.skystream.in/live/DrisyaPala2924/index.m3u8"
        ),
        "new-vision-mundakayam" to Stream1(
            "https://live.skystream.in/live/newvision2924/index.m3u8"
        ),
        "city-channel-kasargod" to Stream1(
            "https://live.skystream.in/live/citytv2924/index.m3u8"
        ),
        "city-vision-kannur" to Stream1(
            "https://live.skystream.in/live/citykannurcity/index.m3u8"
        ),
        "network-payyanur" to Stream1(
            "https://live.skystream.in/live/networkpayyanur/index.m3u8"
        ),
        "utv-utsav" to Stream1(
            "https://em4qj6nedyvg-hls-live.wmncdn.net/liveunit/89b1e919eed04e59383cf820d644c20e.sdp/chunks.m3u8"
        ),
        "drishya-tv" to Stream1(
            "https://live.skystream.in/live/drishya5060/index.m3u8"
        ),
        "ctv-mukkam" to Stream1(
            "https://live.skystream.in/live/ctv1805/index.m3u8"
        ),
        "drisya-tv" to Stream1(
            "https://live.skystream.in/live/DrisyaPala2924/index.m3u8"
        ),
        "kerala-today" to Stream1(
            "https://live.skystream.in/live/krlatoday/index.m3u8"
        ),
        "media-today" to Stream1(
            "https://live.skystream.in/live/mediatday/index.m3u8"
        ),
        "thanima-tv" to Stream1(
            "https://live.skystream.in/live/thanimatvtvm/index.m3u8"
        ),
        "gtv" to Stream1(
            "https://play.applelive.in/gtv/gtv.m3u8"
        ),
        "gtv-max" to Stream1(
            "https://play.applelive.in/gtvmax/gtvmax.m3u8"
        ),
        "periyar-vision" to Stream1(
            "https://stream.onecloudlive.in/periyarvision/periyarvision/index.m3u8"
        ),
        "powran-tv" to Stream1(
            "https://stream.onecloudlive.in/powranmedia/livestream/index.m3u8"
        ),
        "teak-vision-nilambur" to Stream1(
            "https://colorsportscreen.com/freetv/stream.m3u8?id=2933d0818d2f",
            "https://colorsportscreen.com/"
        ),
        "nattuvartha-tv" to Stream1(
            "https://live.skystream.in/live/nattuvartha5945/index.m3u8"
        ),
        "cctv-pradeshikam" to Stream1(
            "https://96vd9q4kyxq7-hls-live.5centscdn.com/CCTVWEB/3d17933d07af044d3a2caa608d6a8afe.sdp/playlist.m3u8"
        ),
        "cctv-kunnamkulam" to Stream1(
            "https://96vd9q4kyxq7-hls-live.5centscdn.com/livestream/f25aeb3e9210967d546a077de6e105e4.sdp/playlist.m3u8"
        ),
        "n-media-channel" to Stream1(
            "https://live.skystream.in/live/newmedia2924/index.m3u8"
        ),
        "starnet-kerala" to Stream1(
            "https://play.applelive.in/starnettv/starnettv.m3u8"
        ),
        "nick-malayalam" to Stream1(
            "https://jio.drmlive.au/jio/bpk-tv/Nick_Malayalam_MOB/Fallback/index.m3u8"
        ),
        "pravasi-channel-hd" to Stream1(
            "https://m6gdavepdn93-hls-live.5centscdn.com/indialife/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"
        ),
        "kannur-vision" to Stream1(
            "https://stream.logichost.in/kannurvision/live/playlist.m3u8"
        ),
        "vcv-vadakkanchery" to Stream1(
            "https://5a1178b42cc03.streamlock.net/8210/8210/playlist.m3u8"
        ),
        "c-malayalam-tv" to Stream1(
            "https://2-fss-2.streamhoster.com/pl_120/206508-3261972-1/playlist.m3u8"
        ),
        "starnet-vadakkanchery" to Stream1(
            "https://5a1178b42cc03.streamlock.net/8220/8220/playlist.m3u8"
        ),
        "garshom-tv" to Stream1(
            "https://og2qd3aal7an-hls-live.5centscdn.com/garshomtv/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"
        ),
        "divine-tv" to Stream1(
            "https://playout7multirtmp.tulix.tv/live21/Stream1/playlist.m3u8"
        ),
        "media-vision-tv" to Stream1(
            "https://iptv.mediavisionlive.in/jalworldiptv/mvtv/playlist.m3u8"
        ),
        "wayanad-vision" to Stream1(
            "https://online.wayanadvision.in/hls/wayanadvision.m3u8"
        ),
        "utv-palakkad" to Stream1(
            "https://em4qj6nedyvg-hls-live.wmncdn.net/utv/ab2de115318ae40bdd434e193a2f2e7f.sdp/playlist.m3u8"
        ),
        "harvest-usa" to Stream1(
            "https://7mbd4ogkr3gx-hls-live.wmncdn.net/harvestusa/d57ffba6564caea2fee3f4085f19a098.sdp/chunks.m3u8"
        ),
        "pulari-tv" to Stream1(
            "https://royalstarindia.co.in/pularitv_hls/pularitv.m3u8"
        ),
        "n-vision" to Stream1(
            "https://ipcloud.live/nvision/nvisionhd/index.m3u8"
        ),
        "n-vision-music" to Stream1(
            "https://colorsportscreen.com/freetv/stream.m3u8?id=2933d0818d2f",
            "https://colorsportscreen.com/"
        ),
        "koickal-news" to Stream1(
            "https://play.applelive.in/koickalnews/koickalnews.m3u8"
        ),
        "victers-channel" to Stream1(
            "https://932y4x26ljv8-hls-live.5centscdn.com/victers/tv.stream/playlist.m3u8"
        ),
        "metro-live" to Stream1(
            "https://kcnsdhlsapp.ylivestream.com/kcnsdapphls/index.m3u8"
        ),
        "malabar-channel" to Stream1(
            "http://cloud.logichost.in:1935/live/malabarnews/playlist.m3u8"
        ),
        "malabar-plus" to Stream1(
            "http://cloud.logichost.in:1935/live/mplus/playlist.m3u8"
        ),
        "swantham-channel" to Stream1(
            "http://cloud.logichost.in:1935/SWANTHAM/live/playlist.m3u8"
        ),
        "kvtv" to Stream1(
            "https://2-fss-2.themediacdn.com/pl_138/206324-3157542-1/chunklist.m3u8"
        ),
        "kvtv-news" to Stream1(
            "https://2-fss-2.themediacdn.com/pl_138/206324-3157542-1/chunklist.m3u8"
        ),
        "word-to-world" to Stream1(
            "https://live.wmncdn.net/worldtoworld/d0dbe915091d400bd8ee7f27f0791303.sdp/playlist.m3u8"
        ),
        "goodness-tv" to Stream1(
            "https://mumt07.tangotv.in/zHjX9OFlGOODNESSTV/index.m3u8"
        ),
        "harvest-tv-keralam" to Stream1(
            "https://7mbd4ogkr3gx-hls-live.wmncdn.net/harvesttvlive1/bbb19eae240ec100af921d511efc86a0.sdp/index.m3u8"
        ),
        "power-vision-tv" to Stream1(
            "https://live.drmlive-02.workers.dev/yupptv/fta/power-vision-tv.m3u8"
        ),
        "shalom-tv" to Stream1(
            "https://d2c4zqo2rb5uf1.cloudfront.net/master.m3u8"
        ),
        "shalom-global" to Stream1(
            "https://d28xtgmk9tfk6b.cloudfront.net/master.m3u8"
        ),
        "shekinah-tv" to Stream1(
            "https://livetv.timeiptv.in/ShekinahNewsIndia/955ad3298db330b5ee880c2c9e6f23a0.sdp/chunks.m3u8"
        )
    )

    /*
     * BHOOM's Mollywood TV / Plus pages are container pages.
     * Expose the actual child channels individually in CloudStream.
     *
     * Duplicate rule:
     * - Keep HD when both HD and SD are present.
     * - Keep SD only when no HD version exists.
     * - Do not expose the three Mollywood container pages themselves.
     *
     * Logos below use stable public channel-logo URLs where available.
     */
    private data class MollywoodChannel(
        val name: String,
        val stream: Stream1,
        val poster: String? = null
    )

    private val mollywoodChannels = listOf(
        MollywoodChannel(
            "Asianet HD - JIO",
            Stream1("https://raw.githubusercontent.com/amazeyourself/adaptive-streams/refs/heads/main/streams/in/YuppTV/AsianetHD.m3u8"),
            "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_ASIANET_HD/images/LOGO_HD/image.png"
        ),
        MollywoodChannel(
            "Asianet Movies HD",
            Stream1("https://anet.keralive.workers.dev/v1/master/a0d007312bfd99c47f76b77ae26b1ccdaae76cb1/asianetmovies_live_https/index.m3u8"),
            "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_ASIANET_MOVIES_HD/images/LOGO_HD/image.png"
        ),
        MollywoodChannel(
            "Asianet Plus",
            Stream1(
                "https://anet.keralive.workers.dev/v1/master/a0d007312bfd99c47f76b77ae26b1ccdaae76cb1/asianetplus_live_https/index.m3u8",
                "https://tulnit.com"
            ),
            "https://xstreamcp-assets-msp.streamready.in/assets/LIVETV/LIVECHANNEL/LIVETV_LIVETVCHANNEL_ASIANET_PLUS/images/LOGO_HD/image.png"
        ),
        MollywoodChannel(
            "Zee Keralam HD",
            Stream1("http://indtv.online/zee5/zee5/0-9-129.m3u8"),
            "https://akamaividz2.zee5.com/image/upload/resources/0-9-129/channel_list/1170x658withlogoea00fd123614470c9f82e2fde66280e4.png"
        ),
        MollywoodChannel(
            "Surya TV FHD",
            Stream1("http://indtv.online/sunnxt/sunnxt/SuryaTVHD.m3u8"),
            "https://sund-images.sunnxt.com/194397/1000x1000_SuryaTVHD_194397_4c99c17b-92d4-49be-a490-b5958067190a.png"
        ),
        MollywoodChannel(
            "Surya Comedy",
            Stream1("http://indtv.online/sunnxt/sunnxt/SuryaComedy.m3u8"),
            "https://sund-images.sunnxt.com/30835/1000x1000_143a4af4-2f02-4c9c-814b-af149e6a5a95.jpg"
        ),
        MollywoodChannel(
            "Surya Movies",
            Stream1("http://indtv.online/sunnxt/sunnxt/SuryaMovies.m3u8"),
            "https://sund-images.sunnxt.com/9019/1000x1000_71ddcc0b-16e7-48e9-9998-aa023200f4bc.jpg"
        )
    )

    private val streamInfoByUrl =
        (stream1BySlug.values + mollywoodChannels.map { it.stream })
            .associateBy { it.url }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val pageNumber = page.coerceAtLeast(1)
        val url = if (pageNumber == 1) channelPage else "$mainUrl/channel/malayalam/page/$pageNumber/"
        val doc = app.get(url, referer = mainUrl).document

        val items = buildList {
            addAll(parseChannelPage(doc))
            if (pageNumber == 1) {
                addAll(mollywoodChannels.map { channel ->
                    newLiveSearchResponse(channel.name, channel.stream.url) {
                        posterUrl = channel.poster
                    }
                })
            }
        }.distinctBy { it.url }

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
        val normalizedQuery = query.trim()

        val pages = (1..4).map { page ->
            val url = if (page == 1) channelPage else "$mainUrl/channel/malayalam/page/$page/"
            app.get(url, referer = mainUrl).document
        }

        val pageResults = pages.flatMap { it.select("a[href*='/live/']") }.mapNotNull { anchor ->
            val href = anchor.absUrl("href").ifBlank { anchor.attr("href") }
            if (!href.contains("/live/")) return@mapNotNull null

            val title = anchor.selectFirst("h2, h3, .title, .entry-title")?.text()?.trim()
                ?: anchor.text().trim()
            if (title.isBlank()) return@mapNotNull null

            if (title.equals("Mollywood TV", true) || title.equals("Mollywood Plus", true) ||
                title.equals("Mollywood Max", true)) return@mapNotNull null

            if (normalizedQuery.isNotBlank() && !title.contains(normalizedQuery, true)) {
                return@mapNotNull null
            }

            val slug = href.substringAfter("/live/").substringBefore("/").lowercase()
            val stream = stream1BySlug[slug] ?: return@mapNotNull null
            val poster = findPoster(anchor)

            newLiveSearchResponse(title, stream.url) { posterUrl = poster }
        }

        val mollywoodResults = mollywoodChannels
            .filter { normalizedQuery.isBlank() || it.name.contains(normalizedQuery, true) }
            .map { channel ->
                newLiveSearchResponse(channel.name, channel.stream.url) {
                    posterUrl = channel.poster
                }
            }

        return (pageResults + mollywoodResults).distinctBy { it.url }
    }

    override suspend fun load(url: String): LoadResponse {
        val title = mollywoodChannels.firstOrNull { it.stream.url == url }?.name ?: "Live Stream"

        return newLiveStreamLoadResponse(name = title, url = url, dataUrl = url)
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        if (!data.startsWith("http://", true) && !data.startsWith("https://", true)) return false

        val stream = streamInfoByUrl[data] ?: Stream1(data)
        val type = when {
            data.contains(".mpd", true) -> ExtractorLinkType.DASH
            data.contains(".m3u8", true) -> ExtractorLinkType.M3U8
            else -> ExtractorLinkType.VIDEO
        }

        callback(
            newExtractorLink(source = name, name = name, url = data, type = type) {
                referer = stream.referer
                headers = mapOf("User-Agent" to USER_AGENT, "Accept" to "*/*")
                quality = Qualities.Unknown.value
            }
        )
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

                // Do not show Mollywood container pages.
                if (title.equals("Mollywood TV", ignoreCase = true) ||
                    title.equals("Mollywood Plus", ignoreCase = true) ||
                    title.equals("Mollywood Max", ignoreCase = true)
                ) {
                    return@mapNotNull null
                }

                val slug = href
                    .substringAfter("/live/")
                    .substringBefore("/")
                    .lowercase()

                if (!stream1BySlug.containsKey(slug)) return@mapNotNull null

                val poster = findPoster(anchor)

                newLiveSearchResponse(title, stream1BySlug.getValue(slug).url) {
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
