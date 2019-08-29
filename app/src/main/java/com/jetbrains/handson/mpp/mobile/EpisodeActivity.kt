package com.jetbrains.handson.mpp.mobile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_episode.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class EpisodeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode)

        val link = intent.getStringExtra("episode_link")!!
        val name = intent.getStringExtra("episode_name")!!
        val info = ShowInfo(name, link)

        val dividerItemDecoration = DividerItemDecoration(
            episode_list.context,
            (episode_list.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.drawable?.setTint(Color.GRAY)
        episode_list.addItemDecoration(dividerItemDecoration)

        GlobalScope.launch {
            val epInfo = EpisodeApi(info)
            val epName = epInfo.name
            val des = "${epInfo.source.url}\n${epInfo.showDescription}"
            val list = epInfo.episodeList as ArrayList<EpisodeInfo>
            runOnUiThread {
                Picasso.get().load(epInfo.image)
                    .error(R.drawable.ic_launcher_background)
                    .resize((600 * .6).toInt(), (800 * .6).toInt())
                    .into(coverImage)
                episodeTitle.text = epName
                descriptionInfo.text = des
                episode_list.adapter = EpisodeAdapter(list, this@EpisodeActivity)
            }
        }

    }
}

class EpisodeAdapter(list: ArrayList<EpisodeInfo>, val context: Context) :
    DragSwipeAdapter<EpisodeInfo, ViewHolder>(list) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = list[position].name
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.show_layout, parent, false))
    }

}