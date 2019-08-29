package com.jetbrains.handson.mpp.mobile

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import com.dkv.bubblealertlib.BblContentFragment
import com.dkv.bubblealertlib.BblDialogManager
import com.dkv.bubblealertlib.ConstantsIcons
import com.dkv.bubblealertlib.IAlertClickedCallBack
import kotlinx.android.synthetic.main.activity_music_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

class MusicGameActivity : AppCompatActivity() {

    enum class QuestionChoice {
        SNIPPET, NAME
    }

    enum class AnswerChoice {
        NAME, ARTIST, ALBUM
    }

    private lateinit var trackList: MutableList<Track>

    private var qChoice = QuestionChoice.SNIPPET
    private var aChoice = AnswerChoice.NAME

    private var choices: Array<Track?> = arrayOf(null, null, null, null)

    private lateinit var current: Track

    private var correctTrack: Int = 0

    private var currentRight = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            score.text = "$currentRight/$total\n${trackList.size} left"
        }
    private var total = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            score.text = "$currentRight/$total\n${trackList.size} left"
        }

    @SuppressLint("SetTextI18n")
    private fun <T> MutableList<T>.randomRemoveAndUpdate(): T {
        val s = randomRemove()
        runOnUiThread {
            score.text = "$currentRight/$total\n${trackList.size} left"
        }
        return s
    }

    private fun <T> MutableList<T>.randomRemove(): T {
        return removeAt(Random.nextInt(0, size))
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_game)

        val ar: Array<Array<Int>> = arrayOf()

        getInfo()

        buttonA.setOnClickListener {
            onClick(0)
        }

        buttonB.setOnClickListener {
            onClick(1)
        }

        buttonC.setOnClickListener {
            onClick(2)
        }

        buttonD.setOnClickListener {
            onClick(3)
        }

        nextQuestionButton.setOnClickListener {
            GlobalScope.launch {
                nextQuestion()
            }
        }

        whatToGuess.text = "Q: ${when (qChoice) {
            QuestionChoice.SNIPPET -> "Song Snippet"
            else -> "Track Name"
        }
        }\nA: ${when (aChoice) {
            AnswerChoice.NAME -> "Song Name"
            AnswerChoice.ALBUM -> "Album Name"
            AnswerChoice.ARTIST -> "Artist Name"
        }
        }"

    }

    private fun getInfo() {
        BblDialogManager.showBblDialog(
            supportFragmentManager,
            LayoutInflater.from(this@MusicGameActivity),
            "Choose What Songs Can Be Shown",
            "OKAY!",
            "NEVER MIND",
            "Exit",
            ConstantsIcons.ALERT_ICON_INFO,
            object : IAlertClickedCallBack {
                override fun onOkClicked(tag: String?) {

                }

                override fun onCancelClicked(tag: String?) {
                    finish()
                }

                override fun onExitClicked(tag: String?) {
                    finish()
                }

            },
            this@MusicGameActivity,
            ""
        ) { frag ->
            frag!!.edtLibName.hint = "Number Of Songs to Have (Default is 100)"
            frag.edtLibName.inputType = InputType.TYPE_CLASS_NUMBER

            frag.edtLibName2.hint = "Artist Name"
            frag.edtLibName2.imeOptions = EditorInfo.IME_ACTION_NEXT

            frag.btnOk.setOnClickListener {
                frag.performOkClicked()
                GlobalScope.launch {
                    val amount = try {
                        frag.edtLibName.text.toString().toInt()
                    } catch (e: Exception) {
                        100
                    }
                    trackList = if (true) {
                        TrackApi().getTrackByInfo(
                            artistName = frag.edtLibName2.text.toString(),
                            amount = amount
                        ).toMutableList()
                    } else {
                        TrackApi().getTopTracks(ChartName.values().random(), amount).toMutableList()
                    }
                    nextQuestion()
                }
            }
            frag.txtDialogTitle.text = "Customize the game"
        }
    }

    private fun getButton(value: Int): Button? {
        return when (value) {
            0 -> buttonA
            1 -> buttonB
            2 -> buttonC
            3 -> buttonD
            else -> null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClick(value: Int) {
        if (current == choices[value]) {
            getButton(value)!!.text = "${getButton(correctTrack)!!.text} ✅"
            currentRight++
        } else {
            getButton(correctTrack)!!.text = "${getButton(correctTrack)!!.text} ✅"
        }
        nextQuestionButton.setNewColorEnable(true)
        enableButtons(false)
    }

    private fun getChoice(t: Track): String {
        return when (aChoice) {
            AnswerChoice.NAME -> t.track_name
            AnswerChoice.ALBUM -> t.album_name
            AnswerChoice.ARTIST -> t.artist_name
        }
    }

    @SuppressLint("SetTextI18n")
    fun nextQuestion() {
        if (trackList.size <= 4) {
            runOnUiThread {
                getInfo()
            }
        } else {
            current = trackList.randomRemoveAndUpdate()

            val listOfTracks = listOf(
                current,
                trackList.randomRemoveAndUpdate(),
                trackList.randomRemoveAndUpdate(),
                trackList.randomRemoveAndUpdate()
            ).shuffled()

            correctTrack = listOfTracks.indexOf(current)

            choices[0] = listOfTracks[0]//getChoice(listOfTracks[0])
            choices[1] = listOfTracks[1]//getChoice(listOfTracks[1])
            choices[2] = listOfTracks[2]//getChoice(listOfTracks[2])
            choices[3] = listOfTracks[3]//getChoice(listOfTracks[3])

            GlobalScope.launch {
                val qText = if (qChoice == QuestionChoice.NAME) {
                    current.track_name
                } else {
                    val snippet = LyricApi().getLyricSnippet(current)
                    snippet?.snippet_body
                        ?: "Sorry, something went wrong.\nFor your troubles: ${current.track_name}"
                }
                runOnUiThread {
                    questionText.text = qText
                    enableButtons(true)
                }
            }

            setButtons()

            println(current.track_name)

            runOnUiThread {
                nextQuestionButton.setNewColorEnable(false)
                total++
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setButtons() = runOnUiThread {
        buttonA.text = "(A) ${getChoice(choices[0]!!)}".trimStart()
        buttonB.text = "(B) ${getChoice(choices[1]!!)}".trimStart()
        buttonC.text = "(C) ${getChoice(choices[2]!!)}".trimStart()
        buttonD.text = "(D) ${getChoice(choices[3]!!)}".trimStart()
    }

    private fun enableButtons(enable: Boolean) {
        buttonA.setNewColorEnable(enable)
        buttonB.setNewColorEnable(enable)
        buttonC.setNewColorEnable(enable)
        buttonD.setNewColorEnable(enable)
    }

    private fun Button.setNewColorEnable(enable: Boolean) {
        isEnabled = enable
        //enabled 027AFF
        //disabled 2A2A2A
        val en = getColor(R.color.iosBlue)
        val di = getColor(R.color.iosDisabled)
        val start: Int
        val end: Int
        if (enable) {
            start = di
            end = en
        } else {
            start = en
            end = di
        }
        val valAnimate = ValueAnimator.ofObject(ArgbEvaluator(), start, end)
        valAnimate.addUpdateListener {
            setTextColor(it.animatedValue as Int)
        }
        valAnimate.start()
    }

}
