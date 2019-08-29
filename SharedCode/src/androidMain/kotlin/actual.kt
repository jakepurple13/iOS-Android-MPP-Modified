@file:Suppress("RegExpRedundantEscape")

package com.jetbrains.handson.mpp.mobile
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

actual fun platformName(): String {
    return "Android"
}

/**
 * The actual api!
 */
actual class ShowApi actual constructor(private val source: Source) {
    private var doc: Document = Jsoup.connect(source.link).get()

    /**
     * returns a list of the show's from the wanted source
     */
    actual val showInfoList: List<ShowInfo>
        get() {
            return if (source.recent)
                getRecentList()
            else
                getList()
        }

    private fun getList(): ArrayList<ShowInfo> {
        return if (source.link.contains("gogoanime")) {
            if (source == Source.ANIME_MOVIES || source.movie)
                gogoAnimeMovies()
            else
                gogoAnimeAll()
        } else if (source.link.contains("putlocker")) {
            val d = doc.select("a.az_ls_ent")
            val listOfShows = arrayListOf<ShowInfo>()
            for (i in d) {
                listOfShows += ShowInfo(i.text(), i.attr("abs:href"))
            }
            listOfShows
        } else {
            val lists = doc.allElements
            val listOfStuff = lists.select("td").select("a[href^=http]")
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                listOfShows.add(
                    ShowInfo(
                        element.text(),
                        element.attr("abs:href")
                    )
                )
            }
            listOfShows.sortBy { it.name }
            listOfShows
        }
    }

    private fun gogoAnimeAll(): ArrayList<ShowInfo> {
        val listOfShows = arrayListOf<ShowInfo>()
        val lists = doc.allElements
        val listOfStuff = lists.select("ul.arrow-list").select("li")
        for (element in listOfStuff) {
            listOfShows.add(
                ShowInfo(
                    element.text(),
                    element.select("a[href^=http]").attr("abs:href")
                )
            )
        }
        listOfShows.sortBy { it.name }
        return listOfShows
    }

    private fun gogoAnimeMovies(): ArrayList<ShowInfo> {
        val list = gogoAnimeAll().filter {
            it.name.contains(
                "movie",
                ignoreCase = true
            )
        } as ArrayList<ShowInfo>
        list.sortBy { it.name }
        return list
    }

    private fun getRecentList(): ArrayList<ShowInfo> {
        return if (source.link.contains("gogoanime")) {
            gogoAnimeRecent()
        } else {
            var listOfStuff = doc.allElements.select("div.left_col").select("table#updates")
                .select("a[href^=http]")
            if (listOfStuff.size == 0) {
                listOfStuff = doc.allElements.select("div.s_left_col").select("table#updates")
                    .select("a[href^=http]")
            }
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                val showInfo =
                    ShowInfo(element.text(), element.attr("abs:href"))
                if (!element.text().contains("Episode"))
                    listOfShows.add(showInfo)
            }
            listOfShows
        }
    }

    private fun gogoAnimeRecent(): ArrayList<ShowInfo> {
        val listOfStuff =
            doc.allElements.select("div.dl-item")
        val listOfShows = arrayListOf<ShowInfo>()
        for (element in listOfStuff) {
            val tempUrl = element.select("div.name").select("a[href^=http]").attr("abs:href")
            val showInfo = ShowInfo(
                element.select("div.name").text(),
                tempUrl.substring(0, tempUrl.indexOf("/episode"))
            )
            listOfShows.add(showInfo)
        }
        return listOfShows
    }

}

/**
 * Actual Show information
 */
actual class EpisodeApi actual constructor(val source: ShowInfo, timeOut: Int) {
    private var doc: Document = Jsoup.connect(source.url).timeout(timeOut).get()

    /**
     * The name of the Show
     */
    actual val name: String
        get() {
            return when {
                source.url.contains("putlocker") -> doc.select("li.breadcrumb-item").last().text()
                source.url.contains("gogoanime") -> doc.select("div.anime-title").text()
                else -> doc.select("div.right_col h1").text()
            }
        }

    /**
     * The url of the image
     */
    actual val image: String
        get() {
            return when {
                source.url.contains("putlocker") -> doc.select("div.thumb").select("img[src^=http]").attr("abs:src")
                source.url.contains("gogoanime") -> doc.select("div.animeDetail-image").select("img[src^=http]").attr("abs:src")
                else -> doc.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src")
            }
        }

    /**
     * the description
     */
    actual val showDescription: String
        get() {
            when {
                source.url.contains("putlocker") -> try {
                    val client = OkHttpClient()
                    val request = okhttp3.Request.Builder()
                        .url("http://www.omdbapi.com/?t=$name&plot=full&apikey=e91b86ee")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    val resString = response.body!!.string()
                    val jsonObj = JsonParser().parse(resString).asJsonObject
                    val year = jsonObj.get("Year")
                    val released = jsonObj.get("Released")
                    val plot = jsonObj.get("Plot")
                    return "Years Active: $year\nReleased: $released\n$plot"
                } catch (e: Exception) {
                    var textToReturn = ""
                    val des = doc.select(".mov-desc")
                    val para = des.select("p")
                    for (i in para.withIndex()) {
                        val text = when (i.index) {
                            1 -> "Release: "
                            2 -> "Genre: "
                            3 -> "Director: "
                            4 -> "Stars: "
                            5 -> "Synopsis: "
                            else -> ""
                        } + i.value.text()
                        textToReturn += text + "\n"
                    }
                    return textToReturn.trim()
                }
                source.url.contains("gogoanime") -> {
                    val des = doc.select("p.anime-details").text()
                    return if (des.isNullOrBlank()) "Sorry, an error has occurred" else des
                }
                else -> {
                    val des =
                        if (doc.allElements.select("div#series_details").select("span#full_notes").hasText())
                            doc.allElements.select("div#series_details").select("span#full_notes").text().removeSuffix(
                                "less"
                            )
                        else {
                            val d = doc.allElements.select("div#series_details")
                                .select("div:contains(Description:)").select("div").text()
                            try {
                                d.substring(d.indexOf("Description: ") + 13, d.indexOf("Category: "))
                            } catch (e: Exception) {
                                d
                            }
                        }
                    return if (des.isNullOrBlank()) "Sorry, an error has occurred" else des
                }
            }
        }

    /**
     * The episode list
     */
    actual val episodeList: List<EpisodeInfo>
        get() {
            var listOfShows = arrayListOf<EpisodeInfo>()
            when {
                source.url.contains("putlocker") -> {
                    val rowList = doc.select("div.col-lg-12").select("div.row")
                    val episodes = rowList.select("a.btn-episode")
                    for (i in episodes) {
                        val ep = EpisodeInfo(i.attr("title"), "https://www.putlocker.fyi/embed-src/${i.attr("data-pid")}")//i.attr("abs:href"))
                        listOfShows.add(ep)
                    }
                }
                source.url.contains("gogoanime") -> {
                    val stuffList = doc.select("ul.check-list").select("li")
                    val showList = arrayListOf<EpisodeInfo>()
                    for (i in stuffList) {
                        val urlInfo = i.select("a[href^=http]")
                        val epName = if (urlInfo.text().contains(name)) {
                            urlInfo.text().substring(name.length)
                        } else {
                            urlInfo.text()
                        }.trim()
                        showList.add(EpisodeInfo(epName, urlInfo.attr("abs:href")))
                    }
                    listOfShows = showList.distinctBy { it.name } as ArrayList<EpisodeInfo>
                }
                else -> {
                    fun getStuff(url: String) {
                        val doc1 = Jsoup.connect(url).get()
                        val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")
                        for (i in stuffList) {
                            listOfShows.add(
                                EpisodeInfo(
                                    i.text(),
                                    i.attr("abs:href")
                                )
                            )
                        }
                    }
                    getStuff(source.url)
                    val stuffLists =
                        doc.allElements.select("ul.pagination").select(" button[href^=http]")
                    for (i in stuffLists) {
                        getStuff(i.attr("abs:href"))
                    }
                }
            }
            return listOfShows
        }

    override fun toString(): String {
        return "$name - ${episodeList.size} eps - $showDescription - imageUrl is $image"
    }

}

/**
 * Actual Episode info, name and url
 */
actual class EpisodeInfo actual constructor(name: String, url: String) : ShowInfo(name, url) {

    /**
     * returns a url link to the episodes video
     * # Use for anything but movies
     */
    actual fun getVideoLink(): String {
        if (url.contains("putlocker")) {
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
            if (d.find()) {
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(getHtml(d.group(1)!!))
                if (a.find()) {
                    return "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
                }
            }
        } else if (url.contains("gogoanime")) {
            val doc = Jsoup.connect(url).get()
            return doc.select("a[download^=http]").attr("abs:download")
        } else {
            val episodeHtml = getHtml(url)
            val matcher = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern()
                .matcher(episodeHtml)
            val list = arrayListOf<String>()
            while (matcher.find()) {
                list.add(matcher.group(1)!!)
            }

            val videoHtml = getHtml(list[0])
            val reg =
                "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(videoHtml)
            if (reg.find()) {
                val d = reg.group(1)
                val d1 = Json.parse(NormalLink.serializer(), d)
                return d1.normal!!.storage!![0].link!!
            }
        }
        return ""
    }

    /**
     * returns a url link to the episodes video
     * # Use for movies
     */
    actual fun getVideoLinks(): List<String> {
        if (url.contains("putlocker")) {
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
            if (d.find()) {
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(getHtml(d.group(1)!!))
                if (a.find()) {
                    val link = getFinalURL(URL("https://verystream.com/gettoken/${a.group(1)!!}?mime=true"))!!.toExternalForm()
                    return arrayListOf(link)
                }
            }
            return arrayListOf("N/A")
        } else if (url.contains("gogoanime")) {
            val doc = Jsoup.connect(url).get()
            return arrayListOf(doc.select("a[download^=http]").attr("abs:download"))
        } else {
            val htmld = getHtml(url)
            val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(htmld)
            var s = ""
            val list = arrayListOf<String>()
            while (m.find()) {
                val g = m.group(1)!!
                s += g + "\n"
                list.add(g)
            }

            val regex =
                "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"

            val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) {
                list
            } else {
                getHtml(list[0])
            }

            when (htmlc) {
                is ArrayList<*> -> {
                    val urlList = arrayListOf<String>()
                    for (info in htmlc) {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern()
                            .matcher(getHtml(info.toString()))
                        while (reg.find()) {
                            val d = reg.group(1)
                            val g = Gson()
                            val d1 = g.fromJson(d, NormalLink::class.java)
                            urlList.add(d1.normal!!.storage!![0].link!!)
                        }
                    }
                    return urlList
                }
                is String -> {
                    val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                    while (reg.find()) {
                        val d = reg.group(1)
                        val g = Gson()
                        val d1 = g.fromJson(d, NormalLink::class.java)
                        return arrayListOf(d1.normal!!.storage!![0].link!!)
                    }
                }
            }
        }
        return arrayListOf()
    }

    @Throws(IOException::class)
    private fun getHtml(url: String): String {
        // Build and set timeout values for the request.
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
        connection.addRequestProperty("Referer", "http://thewebsite.com")
        connection.connect()

        // Read and store the result line by line then return the entire string.
        val in1 = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(in1))
        val html = StringBuilder()
        var line: String? = ""
        while (line != null) {
            line = reader.readLine()
            html.append(line)
        }
        in1.close()

        return html.toString()
    }

    private fun getFinalURL(url: URL): URL? {
        try {
            val con = url.openConnection() as HttpURLConnection
            con.instanceFollowRedirects = false
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
            con.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
            con.addRequestProperty("Referer", "http://thewebsite.com")
            con.connect()
            //con.getInputStream();
            val resCode = con.responseCode
            if (resCode == HttpURLConnection.HTTP_SEE_OTHER
                || resCode == HttpURLConnection.HTTP_MOVED_PERM
                || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                var location = con.getHeaderField("Location")
                if (location.startsWith("/")) {
                    location = url.protocol + "://" + url.host + location
                }
                return getFinalURL(URL(location))
            }
        } catch (e: Exception) {
            println(e.message)
        }

        return url
    }

    override fun toString(): String {
        return "$name: $url"
    }
}

actual fun getApiCalls(url: String): SnippetMessage {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .get()
        .build()
    val response = client.newCall(request).execute()
    if (response.code == 200) {
        val resString = response.body!!.string()
        return getObjFromJson(resString)
    }
    throw Exception("Nope")
}

actual fun getApiSnippetCall(url: String): Snippet? {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
        .url(url)
        .get()
        .build()
    val response = client.newCall(request).execute()
    if (response.code == 200) {
        val resString = response.body!!.string()
        return getSnippetFromJson(resString)
    }
    throw Exception("Nope")
}
