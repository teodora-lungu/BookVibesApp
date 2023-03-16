package com.example.bookvibes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val bookList : ArrayList<Books>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val books = mutableListOf<Books>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.book_list,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount( ) :Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        //holder.bookImage.setImageResource(currentItem.bookImage)
        holder.bookTitle.text = currentItem.title
        holder.bookAuthor.text = currentItem.author
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
       // val bookImage : ShapeableImageView = itemView.findViewById(R.id.book_image)
        val bookTitle : TextView = itemView.findViewById(R.id.book_title)
        val bookAuthor : TextView = itemView.findViewById(R.id.book_author)
    }

    fun addBook(book: Books) {
        books.add(book)
        notifyItemInserted(books.size - 1)
    }
}