package com.example.bookvibes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MainAdapter(private val bookList : ArrayList<Books>, private val listener: OnHeartIconListener) :
    RecyclerView.Adapter<MainAdapter.MyViewHolder>() {
    private val books = mutableListOf<Books>()

    interface OnHeartIconListener {
        fun onHeartClicked(book : Books)
    }
    inner class MyViewHolder(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        val bookTitle: TextView = itemView.findViewById(R.id.book_title)
        val bookAuthor: TextView = itemView.findViewById(R.id.book_author)
        val bookImg : ImageView = itemView.findViewById(R.id.book_image)
        val moreActions : ImageView = itemView.findViewById(R.id.more_action_View)
        val heartView : ImageView = itemView.findViewById(R.id.favorite_border_View)

        init {
            heartView.setOnClickListener {
                heartView.setImageResource(if (bookList[adapterPosition].isFavorite)
                    R.drawable.baseline_favorite_border_24
                else
                    R.drawable.baseline_favorite_24)
                listener.onHeartClicked(bookList[adapterPosition])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.book_list,
            parent, false
        )
        return MyViewHolder(itemView, parent.context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        //holder.bookImage.setImageResource(currentItem.bookImage)
        holder.bookTitle.text = currentItem.title
        holder.bookAuthor.text = currentItem.author
        Glide.with(holder.itemView)
            .load(currentItem.img)
            //.transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.bookImg)
        holder.moreActions.setImageBitmap(null)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }
}