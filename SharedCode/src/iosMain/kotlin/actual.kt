@file:Suppress("UNCHECKED_CAST")

package com.jetbrains.handson.mpp.mobile

import cocoapods.HTMLKit.CSSSelector
import cocoapods.HTMLKit.HTMLElement
import cocoapods.HTMLKit.HTMLParser
import cocoapods.SBJson5.SBJson5Parser
import kotlinx.serialization.json.Json
import platform.Foundation.*
import platform.UIKit.UIDevice

actual fun platformName(): String {
    return UIDevice.currentDevice.systemName() +
            " " +
            UIDevice.currentDevice.systemVersion
}

private fun HTMLElement.getHref() = attributes.objectForKey("href") as String

private fun HTMLElement.getAttribute(attr: String) = attributes.objectForKey(attr) as String

private fun getHTML(url: String) =
    HTMLParser(NSString.create(contentsOfURL = NSURL.URLWithString(url)!!)!!.toString()).parseDocument()

private fun getHTMLCode(url: String) =
    NSString.create(contentsOfURL = NSURL.URLWithString(url)!!)!!.toString()

/**
 * The actual api!
 */
actual class ShowApi actual constructor(private val source: Source) {
    private val doc = getHTML(source.link)

    /**
     * returns a list of the show's from the wanted source
     */
    actual val showInfoList: List<ShowInfo>
        get() = if (source.recent)
            getRecentList()
        else
            getList()

    private fun getList(): ArrayList<ShowInfo> {
        return if (source.link.contains("gogoanime")) {
            if (source == Source.ANIME_MOVIES || source.movie)
                gogoAnimeMovies()
            else
                gogoAnimeAll()
        } else if (source.link.contains("putlocker")) {
            val d = doc.querySelectorAll("a.az_ls_ent") as List<HTMLElement>
            val listOfShows = arrayListOf<ShowInfo>()
            for (i in d) {
                listOfShows += ShowInfo(
                    i.textContent.trim(),
                    "https://www1.putlocker.fyi${i.getHref()}"
                )
            }
            listOfShows
        } else {
            val listOfStuff = doc.querySelectorAll("td a[href^=http]") as List<HTMLElement>
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                listOfShows.add(
                    ShowInfo(
                        element.textContent.trim(),
                        element.getHref()
                    )
                )
            }
            listOfShows.sortBy { it.name }
            listOfShows
        }
    }

    private fun gogoAnimeAll(): ArrayList<ShowInfo> {
        val listOfShows = arrayListOf<ShowInfo>()
        val listOfStuff =
            doc.elementsMatchingSelector(CSSSelector.selectorWithString("ul.arrow-list li")!!) as List<HTMLElement>
        for (element in listOfStuff) {
            listOfShows.add(
                ShowInfo(
                    element.textContent.trim(),
                    element.querySelector("a[href^=http]")!!.getHref()
                )
            )
        }
        listOfShows.sortBy { it.name }
        listOfShows.removeAll { it.name.contains("Episode", ignoreCase = true) }
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
            var listOfStuff =
                doc.querySelectorAll("div.left_col table#updates a[href^=http]") as List<HTMLElement>
            if (listOfStuff.isEmpty()) {
                listOfStuff =
                    doc.querySelectorAll("div.s_left_col table#updates a[href^=http]") as List<HTMLElement>
            }
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                val showInfo =
                    ShowInfo(element.textContent.trim(), element.getHref())//.attr("abs:href"))
                if (!element.textContent.trim().contains("Episode"))
                    listOfShows.add(showInfo)
            }
            listOfShows
        }
    }

    private fun gogoAnimeRecent(): ArrayList<ShowInfo> {
        val listOfStuff =
            doc.querySelectorAll("div.dl-item") as List<HTMLElement>
        val listOfShows = arrayListOf<ShowInfo>()
        for (element in listOfStuff) {
            val tempUrl =
                element.querySelector("div.name a[href^=http]")!!.getHref()//.select("div.name").select("a[href^=http]").attr("abs:href")
            val showInfo = ShowInfo(
                element.querySelector("div.name")!!.textContent.trim().replace(
                    "\n",
                    ""
                ).replace("\r", ""),
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
    private val doc = getHTML(source.url)

    /**
     * The name of the Show
     */
    actual val name: String
        get() = when {
            source.url.contains("putlocker") -> (doc.querySelectorAll("li.breadcrumb-item").last() as HTMLElement).textContent.trim()
            source.url.contains("gogoanime") -> doc.querySelector("div.anime-title")!!.textContent.trim()
            else -> doc.querySelector("div.right_col h1")!!.textContent.trim()
        }
    /**
     * The url of the image
     */
    actual val image: String
        get() {
            return when {
                source.url.contains("putlocker") -> doc.querySelector("div.thumb img[src^=http]")!!.getAttribute(
                    "src"
                )
                source.url.contains("gogoanime") -> doc.querySelector("div.animeDetail-image img[src^=http]")!!.getAttribute(
                    "src"
                )
                else -> doc.querySelector("div.left_col img[src^=http]#series_image")!!.getAttribute(
                    "src"
                )
            }
        }

    /**
     * the description
     */
    actual val showDescription: String
        get() {
            when {
                source.url.contains("putlocker") -> {
                    try {
                        var textToReturn = "asdf"

                        val url = "http://www.omdbapi.com/?t=${name.replace(
                            " ",
                            "+"
                        )}&plot=full&apikey=e91b86ee"

                        val code = getHTMLCode(url)

                        val s = SBJson5Parser.parserWithBlock(block = { res, boo ->
                            val dict = res as NSDictionary
                            val year = dict.objectForKey("Year")
                            val released = dict.objectForKey("Released")
                            val plot = dict.objectForKey("Plot")
                            val des = "Years Active: $year\nReleased: $released\n$plot"
                            textToReturn = des
                        }, errorHandler = {
                            println(it)
                        }) as SBJson5Parser
                        s.parse(code.nsdata())

                        if (textToReturn == "asdf") {
                            throw Exception()
                        } else {
                            return textToReturn
                        }
                    } catch (e: Exception) {
                        var textToReturn = ""
                        val des = doc.querySelector(".mov-desc")!!
                        val para = des.querySelectorAll("p") as List<HTMLElement>
                        for (i in para.withIndex()) {
                            val text = when (i.index) {
                                1 -> "Release: "
                                2 -> "Genre: "
                                3 -> "Director: "
                                4 -> "Stars: "
                                5 -> "Synopsis: "
                                else -> ""
                            } + i.value.textContent.trim()
                            textToReturn += text + "\n"
                        }
                        return textToReturn.trim()
                    }
                }
                source.url.contains("gogoanime") -> {
                    val des = doc.querySelector("p.anime-details")!!.textContent.trim()
                    return if (des.isBlank()) "Sorry, an error has occurred" else des
                }
                else -> {
                    val des =
                        if (doc.querySelector("div#series_details span#full_notes") != null) {
                            doc.querySelector("div#series_details span#full_notes")!!.textContent.trim()
                                .removeSuffix(
                                    "less"
                                )
                        } else {
                            val d =
                                doc.querySelector("div#series_details div")!!.textContent.trim()
                            try {
                                d.substring(
                                    d.indexOf("Description: ") + 13,
                                    d.indexOf("Category: ")
                                ).replace("Description: ", "")
                            } catch (e: Exception) {
                                d
                            }
                        }
                    return if (des.isBlank()) "Sorry, an error has occurred" else des
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
                    val rowList =
                        doc.querySelectorAll("div.col-lg-12 div.row a.btn-episode") as List<HTMLElement>
                    for (i in rowList) {
                        val ep = EpisodeInfo(
                            i.getAttribute("title").trim(),
                            "https://www.putlocker.fyi/embed-src/${i.attributes.objectForKey("data-pid")}"
                        )
                        listOfShows.add(ep)
                    }
                }
                source.url.contains("gogoanime") -> {
                    val stuffList = doc.querySelectorAll("ul.check-list li") as List<HTMLElement>
                    val showList = arrayListOf<EpisodeInfo>()
                    for (i in stuffList) {
                        val urlInfo = i.querySelector("a[href^=http]")!!
                        val epName = if (urlInfo.textContent.trim().contains(name)) {
                            urlInfo.textContent.trim().substring(name.length)
                        } else {
                            urlInfo.textContent.trim()
                        }.trim()
                        showList.add(EpisodeInfo(epName, urlInfo.getHref()))
                    }
                    listOfShows = showList.distinctBy { it.name } as ArrayList<EpisodeInfo>
                }
                else -> {
                    fun getStuff(url: String) {
                        val doc1 = getHTML(url)
                        val stuffList =
                            doc1.querySelectorAll("div#videos a[href^=http]") as List<HTMLElement>
                        for (i in stuffList) {
                            listOfShows.add(EpisodeInfo(i.textContent.trim(), i.getHref()))
                        }
                    }
                    getStuff(source.url)
                    val stuffLists =
                        doc.querySelectorAll("ul.pagination button[href^=http]") as List<HTMLElement>
                    for (i in stuffLists) {
                        getStuff(i.getHref())
                    }
                }
            }
            return listOfShows
        }

    override fun toString(): String {
        return "$name - ${episodeList.size} eps - $showDescription - imageUrl is $image".replace(
            "\n",
            ""
        )
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
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(getHTMLCode(url))
            val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex()
                .find(getHTMLCode(d!!.groupValues.last()))
            return "https://verystream.com/gettoken/${a!!.groupValues.last()}?mime=true"
        } else if (url.contains("gogoanime")) {
            val doc = getHTML(url)
            return doc.querySelector("a[download^=http]")!!.getAttribute("download")
        } else {
            val episodeHtml = getHTMLCode(url)
            val matcher = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().find(episodeHtml)

            val videoHtml = getHTMLCode(matcher!!.groupValues.last())
            val reg = "var video_links = (\\{.*?\\});".toRegex().find(videoHtml)
            val json = reg!!.groupValues.last()
            val j = Json.parse(NormalLink.serializer(), json)
            return j.normal!!.storage!![0]!!.link!!
        }
    }

    /**
     * returns a url link to the episodes video
     * # Use for movies
     */
    actual fun getVideoLinks(): List<String> {
        if (url.contains("putlocker")) {
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(getHTMLCode(url))
            val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex()
                .find(getHTMLCode(d!!.groupValues.last()))
            val link = "https://verystream.com/gettoken/${a!!.groupValues.last()}?mime=true"
            return arrayListOf(link)
        } else if (url.contains("gogoanime")) {
            val doc = getHTML(url)
            return arrayListOf(doc.querySelector("a[download^=http]")!!.getAttribute("download"))
        } else {
            val htmld = getHTMLCode(url)
            val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().find(htmld)

            val list = m!!.groupValues

            val regex =
                "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"

            val htmlc: Any = if (regex.toRegex().find(list[0])!!.groupValues.isNotEmpty()) {
                list
            } else {
                getHTMLCode(list[0])
            }

            when (htmlc) {
                is ArrayList<*> -> {
                    val urlList = arrayListOf<String>()
                    for (info in htmlc) {
                        val reg = "var video_links = (\\{.*?\\});".toRegex()
                            .find(getHTMLCode(info.toString()))

                        for (i in reg!!.groupValues) {
                            val j = Json.parse(NormalLink.serializer(), i)
                            urlList += j.normal!!.storage!![0]!!.link!!
                        }
                    }
                    return urlList
                }
                is String -> {
                    val reg = "var video_links = (\\{.*?\\});".toRegex().find(htmlc)
                    val json = reg!!.groupValues.last()
                    val j = Json.parse(NormalLink.serializer(), json)
                    return arrayListOf(j.normal!!.storage!![0].link!!)
                }
            }
        }
        return arrayListOf()
    }

}

@Suppress("CAST_NEVER_SUCCEEDS")
private fun String.nsdata(): NSData? {
    return (this as NSString).dataUsingEncoding(NSUTF8StringEncoding)
}

private fun NSData.string(): String? {
    return NSString.create(this, NSUTF8StringEncoding) as String?
}

internal actual fun getApiCalls(url: String): SnippetMessage {
    val code = NSString.create(contentsOfURL = NSURL.URLWithString(url)!!)!!//.toString()
    return getObjFromJson(code.toString())
}

internal actual fun getApiSnippetCall(url: String): Snippet? {
    val code = NSString.create(contentsOfURL = NSURL.URLWithString(url)!!)!!//.toString()
    return getSnippetFromJson(code.toString())
}

private fun prettyLog(msg: Any?) {
    //the main message to be logged
    var logged = msg.toString()
    //the arrow for the stack trace
    val arrow = "${9552.toChar()}${9655.toChar()}\t"
    //the stack trace
    val stackTraceElement = Throwable().getStackTrace()

    val elements = listOf(*stackTraceElement)
    val wanted = elements.filter { it.contains("jetbrains") && !it.contains("prettyLog") }

    var loc = "\n"

    for ((i, value) in wanted.withIndex().reversed()) {
        /*val fullClassName = wanted[i].className
        //get the method name
        val methodName = wanted[i].methodName
        //get the file name
        val fileName = wanted[i].fileName
        //get the line number
        val lineNumber = wanted[i].lineNumber*/
        //add this to location in a format where we can click on the number in the console
        loc += value//"$fullClassName.$methodName($fileName:$lineNumber)"

        if (wanted.size > 1 && i - 1 >= 0) {
            val typeOfArrow: Char =
                if (i - 1 > 0)
                    9568.toChar() //middle arrow
                else
                    9562.toChar() //ending arrow
            loc += "\n\t$typeOfArrow$arrow"
        }
    }

    logged += loc

    println(logged + "\n")
}
