package com.jetbrains.handson.mpp.mobile

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun kotTest() {
        println(getNewString())
        println(getOldString())
        //println(getShows(Source.RECENT_ANIME))
    }

    @Test
    fun apiTest() {
        //getXkcd(400)
        //prettyLog(getJokeOfTheDay())
        searchForBook("Cirque Du Freak")
    }

    @Test
    fun musicTest() {
        //val list = TrackApi().getTrackByInfo(artistName = "Ninja Sex Party", amount = 3).toMutableList()
        val list = TrackApi().getTrackByInfo(artistName = "twrp", amount = 3).toMutableList()
        prettyLog(list.joinToString { "$it\n" })
        val snip = LyricApi().getLyricSnippet(list.random())
        prettyLog(snip?.snippet_body!!)
    }

    @Test
    fun showTest() {
        /*val s = ShowApi(Source.LIVE_ACTION).showInfoList
        prettyLog(s.random().url)
        val s1 = EpisodeApi(s.random())
        prettyLog(s1.description)*/

        //val s = ShowApi(Source.CARTOON).showInfoList
        //prettyLog(s.getEpisodeApi(8).episodeList.random().getVideoLink())
        val s = ShowApi(Source.LIVE_ACTION).showInfoList
        val ep = s.getEpisodeApi(4)
        prettyLog(ep.episodeList)
        prettyLog(ep.source.name)
        prettyLog(ep.showDescription)
    }

    private fun prettyLog(msg: Any?) {
        //the main message to be logged
        var logged = msg.toString()
        //the arrow for the stack trace
        val arrow = "${9552.toChar()}${9655.toChar()}\t"
        //the stack trace
        val stackTraceElement = Thread.currentThread().stackTrace

        val elements = listOf(*stackTraceElement)
        val wanted = elements.filter { it.className.contains("jetbrains") && !it.methodName.contains("prettyLog") }

        var loc = "\n"

        for (i in wanted.indices.reversed()) {
            val fullClassName = wanted[i].className
            //get the method name
            val methodName = wanted[i].methodName
            //get the file name
            val fileName = wanted[i].fileName
            //get the line number
            val lineNumber = wanted[i].lineNumber
            //add this to location in a format where we can click on the number in the console
            loc += "$fullClassName.$methodName($fileName:$lineNumber)"

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

}
