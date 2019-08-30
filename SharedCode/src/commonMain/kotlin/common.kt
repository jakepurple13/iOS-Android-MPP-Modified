package com.jetbrains.handson.mpp.mobile

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.parse
import kotlinx.serialization.stringify
import kotlin.jvm.JvmStatic
import kotlin.native.concurrent.ThreadLocal

expect fun platformName(): String

fun createApplicationScreenMessage(): String {
    return "Kotlin Rocks on ${platformName()}"
}

fun getNewString() = "HELLLLLLOOOOO"

fun getOldString() = "asdf"

fun getShow(source: Source): ShowApi = ShowApi(source)

fun newMessaged() {
    println("HELLO!")
}

/**
 * If you want to get the Show with all the information now rather than passing it into [EpisodeApi] yourself
 */
fun List<ShowInfo>.getEpisodeApi(index: Int): EpisodeApi = EpisodeApi(this[index])

expect class ShowApi(source: Source) {
    actual val showInfoList: List<ShowInfo>
}

/**
 * Actual Show information
 */
expect class EpisodeApi(source: ShowInfo, timeOut: Int = 10000) {
    /**
     * The name of the Show
     */
    val name: String

    /**
     * The url of the image
     */
    val image: String

    /**
     * the description
     */
    val showDescription: String

    /**
     * The episode list
     */
    val episodeList: List<EpisodeInfo>

}

enum class Source(val link: String, val recent: Boolean = false, var movie: Boolean = false) {
    //ANIME("http://www.animeplus.tv/anime-list"),
    ANIME("https://www.gogoanime1.com/home/anime-list"),
    CARTOON("https://www.animetoon.org/cartoon"),
    DUBBED("https://www.animetoon.org/dubbed-anime"),
    //ANIME_MOVIES("http://www.animeplus.tv/anime-movies"),
    ANIME_MOVIES("https://www.gogoanime1.com/home/anime-list", movie = true),
    CARTOON_MOVIES("https://www.animetoon.org/movies", movie = true),
    //RECENT_ANIME("http://www.animeplus.tv/anime-updates", true),
    RECENT_ANIME("https://www.gogoanime1.com/home/latest-episodes", true),
    RECENT_CARTOON("https://www.animetoon.org/updates", true),
    LIVE_ACTION("https://www.putlocker.fyi/a-z-shows/");

    companion object SourceUrl {
        fun getSourceFromUrl(url: String): Source {
            return when (url) {
                ANIME.link -> ANIME
                CARTOON.link -> CARTOON
                DUBBED.link -> DUBBED
                ANIME_MOVIES.link -> ANIME_MOVIES
                CARTOON_MOVIES.link -> CARTOON_MOVIES
                RECENT_ANIME.link -> RECENT_ANIME
                RECENT_CARTOON.link -> RECENT_CARTOON
                LIVE_ACTION.link -> LIVE_ACTION
                else -> ANIME
            }
        }
    }
}

/**
 * Info about the show, name and url
 */
open class ShowInfo(val name: String, val url: String) {
    override fun toString(): String {
        return "$name: $url"
    }
}

/**
 * Actual Episode info, name and url
 */
expect class EpisodeInfo(name: String, url: String) {

    /**
     * returns a url link to the episodes video
     * # Use for anything but movies
     */
    fun getVideoLink(): String

    /**
     * returns a url link to the episodes video
     * # Use for movies
     */
    fun getVideoLinks(): List<String>
}

@Serializable
data class NormalLink constructor(var normal: Normal? = null)

@Serializable
data class Normal constructor(var storage: Array<Storage>? = null) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Normal

        if (storage != null) {
            if (other.storage == null) return false
            if (!storage!!.contentEquals(other.storage!!)) return false
        } else if (other.storage != null) return false

        return true
    }

    override fun hashCode(): Int {
        return storage?.contentHashCode() ?: 0
    }
}

@Serializable
data class Storage constructor(
    var sub: String? = null,

    var source: String? = null,

    var link: String? = null,

    var quality: String? = null,

    var filename: String? = null
)

@Serializable
data class Track(

    val updated_time: String,

    val track_share_url: String,

    val primary_genres: PrimaryGenres? = null,

    val track_name_translation_list: List<@ContextualSerialization(String::class) Any?> = emptyList(),

    val artist_name: String,

    val commontrack_id: Int,

    val artist_id: Int,

    val explicit: Int,

    val num_favourite: Int,

    val track_rating: Int,

    val has_richsync: Int,

    val track_id: Int,

    val instrumental: Int,

    val album_name: String,

    val restricted: Int,

    val has_subtitles: Int,

    val album_id: Int,

    val has_lyrics: Int,

    val track_edit_url: String,

    val track_name: String
)

@Serializable
data class PrimaryGenres(
    val music_genre_list: List<@ContextualSerialization Any?>
)

@Serializable
data class SecondaryGenres(

    var music_genre_list: List<@ContextualSerialization Any?>
)

@Serializable
data class Lyrics(
    val lyricsID: Long,
    // val restricted: Long,
    // val instrumental: Long,
    val lyricsBody: String,
    val lyricsLanguage: String,
    val scriptTrackingURL: String,
    val pixelTrackingURL: String,
    val lyricsCopyright: String
    // val backlinkURL: String,
    // val updatedTime: String
)

@Serializable
data class Snippet(
    val snippet_language: String,
    val restricted: Int,
    val instrumental: Int,
    val snippet_body: String,
    val script_tracking_url: String,
    val pixel_tracking_url: String,
    val html_tracking_url: String,
    val updated_time: String,
    val snippet_id: Int
)

@Serializable
data class TrackList2(
    var track: Track
)

@Serializable
data class SnippetMessage(
    var track_list: List<TrackList2>
)

internal fun getApiInfo(url: String): SnippetMessage =
    getObjFromJson(getJson("https://api.musixmatch.com/ws/1.1/$url&apikey=67053f507ef88fc99c544f4d7052dfa8")!!)

internal fun getApiSnippet(url: String): Snippet? =
    getSnippetFromJson(getJson("https://api.musixmatch.com/ws/1.1/$url&apikey=67053f507ef88fc99c544f4d7052dfa8")!!)

enum class ChartName(val value: String) {
    TOP("top"), HOT("hot"), MXMWEEKLY("mxmweekly"), MXMWEEKLY_NEW("mxmweekly_new")
}

class TrackApi {

    //fun getTrack(name: String, artist: String? = null): Track = GetAPI.getInfo("matcher.track.get?q_track=$name${if (artist != null) "&q_artist=$artist" else ""}")

    fun getTopTracks(
        chartName: ChartName = ChartName.TOP, amount: Int = 5
    ): List<Track> {
        val s =
            getApiInfo("chart.tracks.get?chart_name=${chartName.value}&page=1&page_size=$amount&f_has_lyrics=1")
                .track_list
        val list = arrayListOf<Track>()
        s.forEach {
            list += it.track
        }
        return list
    }


    fun getTrackByInfo(
        trackName: String? = null,
        artistName: String? = null,
        anyLyrics: String? = null,
        amount: Int = 100
    ): List<Track> {
        val tName =
            if (trackName != null && !trackName.isBlankOrEmpty()) "q_track=${trackName.replace(
                " ",
                "-"
            )}" else ""
        val aName =
            if (artistName != null && !artistName.isBlankOrEmpty()) "${if (tName.isBlankOrEmpty()) "" else "&"}q_artist=${artistName.replace(
                " ",
                "-"
            )}" else ""
        val lyric =
            if (anyLyrics != null && !anyLyrics.isBlankOrEmpty()) "${if (tName.isBlankOrEmpty() && aName.isBlankOrEmpty()) "" else "&"}q_lyrics=${anyLyrics.replace(
                " ",
                "-"
            )}" else ""
        val s =
            getApiInfo("track.search?$tName$aName$lyric&page_size=$amount&page=1&f_has_lyrics=1").track_list
        val list = arrayListOf<Track>()
        s.forEach {
            list += it.track
        }
        return list
    }

}

internal fun getObjFromJson(json: String): SnippetMessage {
    val jsonObj = Json(JsonConfiguration.Stable).parseJson(json).jsonObject
    val msg = jsonObj["message"]!!.jsonObject
    val body = msg["body"]!!.jsonObject
    val transRegex = "(\\\"track_name_translation_list\\\":\\[\\],)".toRegex()
    val primRegex = "(,\\\"primary_genres\\\":\\{(.*?)\\]\\})".toRegex()
    return Json.parse(
        SnippetMessage.serializer(),
        body.toString().replace(transRegex, "").replace(primRegex, "")
    )
}

internal fun getSnippetFromJson(json: String): Snippet? {
    return try {
        val jsonObj = Json(JsonConfiguration.Stable).parseJson(json).jsonObject
        val msg = jsonObj["message"]!!.jsonObject
        val body = msg["body"]!!.jsonObject
        Json.parse(
            Snippet.serializer(),
            body["snippet"]!!.jsonObject.toString()
        )
    } catch (e: Exception) {
        println(json)
        null
    }
}

class LyricApi {

    fun getLyricSnippet(track: Track): Snippet? =
        getApiSnippet("track.snippet.get?track_id=${track.track_id}")

}

fun String.isBlankOrEmpty(): Boolean = isBlank() || isEmpty()

fun <T> checkMultiple(vararg args: T?, check: (T?) -> Boolean): Boolean {
    return args.none(check)
}

internal expect fun getJson(url: String): String?

fun getXkcd(num: Int) {
    println(getJson("http://xkcd.com/$num/info.0.json"))
}

@Serializable
data class Joke(val title: String, val joke: String, val date: String) {
    fun toJSONString(): String {
        return Json.stringify(serializer(), this)
    }

    @ThreadLocal
    companion object {
        fun fromJSONString(json: String): Joke? = try {
            Json.parse(serializer(), json)
        } catch (e: Exception) {
            null
        }
    }
}

fun getJokeOfTheDay(): Joke? {
    try {
        val json = getJson("https://api.jokes.one/jod")
        if (json != null) {
            val jsonObj = Json(JsonConfiguration.Stable).parseJson(json).jsonObject
            val contents = jsonObj["contents"]!!.jsonObject
            val jokes = contents["jokes"]!!.jsonArray
            val date = jokes.content[0].jsonObject["date"]!!
            val joke = jokes.content[0].jsonObject["joke"]!!.jsonObject
            val title = joke["title"]!!
            val actualJoke = joke["text"]!!
            return Joke(
                title.toString().replace("\"", ""),
                actualJoke.toString().replace("\"", ""),
                date.toString().replace("\"", "")
            )
        }
    } catch (e: Exception) {
        println(e.message)
    }
    return null
}

fun searchForBook(search: String): List<Book> {
    val s = getJson("http://openlibrary.org/search.json?q=${search.replace(" ", "+")}")!!
    return getBookInfo(s)
}

enum class CoverSize(internal val size: String) {
    SMALL("S"), MEDIUM("M"), LARGE("L")
}

data class Book(val title: String, val subtitle: String, val author: String, val coverId: String) {
    fun getCoverUrl(size: CoverSize = CoverSize.MEDIUM) =
        "http://covers.openlibrary.org/b/id/$coverId-${size.size}.jpg"
}

private fun getBookInfo(json: String): List<Book> {
    val jsonObj = Json(JsonConfiguration.Stable).parseJson(json).jsonObject
    val docs = jsonObj["docs"]!!.jsonArray
    val books = arrayListOf<Book>()
    for (b in docs) {
        val book = b.jsonObject
        val title = book["title"].toString().removeSurrounding("\"")
        val subtitle = book["subtitle"].toString().removeSurrounding("\"")
        val author =
            book["author_name"]?.jsonArray?.content?.get(0).toString().removeSurrounding("\"")
        val coverId = book["cover_i"].toString()
        books += Book(title, subtitle, author, coverId)
    }
    return books
}