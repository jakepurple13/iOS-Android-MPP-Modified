package com.jetbrains.handson.mpp.mobile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GlobalScope.launch {
            val sharedPref = getSharedPreferences(
                "jokes",
                Context.MODE_PRIVATE
            )
            val json = Joke.fromJSONString(sharedPref.getString("dailyjoke", "")!!) ?: Joke(
                "Sorry",
                "No Joke",
                "2000-1-1"
            )
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val d = dateFormat.parse(json.date)
            val today = Date(System.currentTimeMillis())
            val cal1 = Calendar.getInstance()
            val cal2 = Calendar.getInstance()
            cal1.time = d!!
            cal2.time = today
            val sameDay =
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(
                    Calendar.YEAR
                ) == cal2.get(Calendar.YEAR)
            val joke = if (!sameDay) {
                getJokeOfTheDay() ?: Joke(
                    "Sorry",
                    "No Joke",
                    "2000-1-1"
                )
            } else {
                json
            }
            runOnUiThread {
                jokeOfTheDay.text = "${joke.title}\n${joke.joke}"
            }
            sharedPref.edit().putString("dailyjoke", joke.toJSONString()).apply()
        }

        var count = 0
        musicGame.setOnClickListener {
            startActivity(Intent(this@MainActivity, MusicGameActivity::class.java))
        }
        val kotlinIsAwesome = "${createApplicationScreenMessage()} ${Build.VERSION.SDK_INT}"
        tv.text = kotlinIsAwesome
        tv.setOnClickListener {
            tv.text = "$kotlinIsAwesome\npressed ${++count} times"
        }
        books.setOnClickListener {
            startActivity(Intent(this@MainActivity, BookActivity::class.java))
        }
    }

    fun goToShow(v: View) {
        startActivity(Intent(this, ShowListActivity::class.java).apply {
            putExtra("show_type", Source.valueOf(v.tag as String).link)
        })
    }

}
