package com.example.bookvibes

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CombinedAdapter(private val bookList : ArrayList<Books>,
                      private val adapter1 : RecyclerView.Adapter<MyAdapter.MyViewHolder>,
                      private val adapter2 : RecyclerView.Adapter<MainAdapter.MyViewHolder>)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_VIEWHOLDER1 -> adapter1.onCreateViewHolder(parent, viewType)
            TYPE_VIEWHOLDER2 -> adapter2.onCreateViewHolder(parent, viewType)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int {
        return bookList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_VIEWHOLDER1 -> adapter1.onBindViewHolder(holder as MyAdapter.MyViewHolder, position)
            TYPE_VIEWHOLDER2 -> adapter2.onBindViewHolder(holder as MainAdapter.MyViewHolder, position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        // return viewType based on position
        // for example:
        return if (position < adapter2.itemCount) {
            TYPE_VIEWHOLDER1
        } else {
            TYPE_VIEWHOLDER2
        }
    }

    companion object {
        private const val TYPE_VIEWHOLDER1 = 0
        private const val TYPE_VIEWHOLDER2 = 1
    }
}