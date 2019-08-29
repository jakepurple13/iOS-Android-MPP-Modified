package com.jetbrains.handson.mpp.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var count = 0
        musicGame.setOnClickListener {
            startActivity(Intent(this@MainActivity, MusicGameActivity::class.java))
        }
        tv.text = createApplicationScreenMessage()
        tv.setOnClickListener {
            tv.text = "${createApplicationScreenMessage()}\npressed ${++count} times"
        }
    }

    fun goToShow(v: View) {
        startActivity(Intent(this, ShowListActivity::class.java).apply {
            putExtra("show_type", Source.valueOf(v.tag as String).link)
        })
    }

}
