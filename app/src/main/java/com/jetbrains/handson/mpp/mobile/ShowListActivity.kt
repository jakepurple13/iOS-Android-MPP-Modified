package com.jetbrains.handson.mpp.mobile

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.programmerbox.dragswipe.DragSwipeAdapter
import kotlinx.android.synthetic.main.activity_show_list.*
import kotlinx.android.synthetic.main.show_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ShowListActivity : AppCompatActivity() {

    lateinit var list: ArrayList<ShowInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_list)
        val source = Source.getSourceFromUrl(intent.getStringExtra("show_type")!!)

        val dividerItemDecoration = DividerItemDecoration(
            show_list.context,
            (show_list.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.drawable?.setTint(Color.GRAY)
        show_list.addItemDecoration(dividerItemDecoration)
        show_list.requestFocus()

        searchList.findViewById<EditText>(R.id.search_src_text).isEnabled = false
        searchList.queryHint = "Search Shows in $source"

        GlobalScope.launch {
            val s = ShowApi(source)
            list = s.showInfoList as ArrayList<ShowInfo>
            runOnUiThread {
                show_list.adapter =
                    ShowAdapter(list, this@ShowListActivity)
                searchList.findViewById<EditText>(R.id.search_src_text).isEnabled = true
            }
        }

        searchList.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String?): Boolean {
                show_list.adapter =
                    ShowAdapter(list.filter {
                        it.name.contains(
                            newText ?: "",
                            true
                        )
                    } as ArrayList<ShowInfo>, this@ShowListActivity)
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }
        })

    }
}

class ShowAdapter(list: ArrayList<ShowInfo>, val context: Context) :
    DragSwipeAdapter<ShowInfo, ViewHolder>(list) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.text.text = list[position].name
        holder.itemView.setOnClickListener {
            context.startActivity(Intent(context, EpisodeActivity::class.java).apply {
                putExtra("episode_link", list[position].url)
                putExtra("episode_name", list[position].name)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.show_layout, parent, false))
    }

}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val text: TextView = itemView.textView!!
}