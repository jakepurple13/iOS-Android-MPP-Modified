package com.jetbrains.handson.mpp.mobile

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_book.*
import kotlinx.android.synthetic.main.book_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book)

        val dividerItemDecoration = DividerItemDecoration(
            bookRV.context,
            (bookRV.layoutManager as LinearLayoutManager).orientation
        )
        dividerItemDecoration.drawable?.setTint(Color.GRAY)
        bookRV.addItemDecoration(dividerItemDecoration)
        bookRV.requestFocus()

        val alert = AlertDialog.Builder(this)
        val editText = EditText(this@BookActivity)
        alert.setTitle("Search for a Book")
        alert.setView(editText)
        alert.setPositiveButton("Search") { _, _ -> getBooks(editText.text.toString()) }
        alert.setNegativeButton("Never Mind") { _, _ -> finish() }
        alert.show()
    }

    private fun getBooks(search: String) = GlobalScope.launch {
        searchForBook(search).apply {
            runOnUiThread {
                bookRV.adapter = BookAdapter(this as ArrayList<Book>, this@BookActivity)
            }
        }
    }

}

class BookAdapter(list: ArrayList<Book>, val context: Context) :
    DragSwipeAdapter<Book, BookHolder>(list) {
    override fun onBindViewHolder(holder: BookHolder, position: Int) {
        holder.book = list[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookHolder {
        return BookHolder(LayoutInflater.from(context).inflate(R.layout.book_layout, parent, false))
    }
}

class BookHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val title: TextView = itemView.bookTitle!!
    private val cover: ImageView = itemView.book_cover!!
    private val author: TextView = itemView.bookAuthor!!
    private val subtitle: TextView = itemView.bookSubtitle!!
    var book: Book? = null
        set(value) {
            field = value
            if (book != null) {
                title.text = book!!.title
                subtitle.text = book!!.subtitle
                author.text = book!!.author
                Picasso.get().load(book!!.getCoverUrl(CoverSize.MEDIUM))
                    .error(android.R.drawable.stat_notify_error)
                    .placeholder(android.R.drawable.stat_sys_upload).into(cover)
            }
        }
}
