package com.example.bookvibes

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(private val bookList : ArrayList<Books>) :
                RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    private val books = mutableListOf<Books>()

    inner class MyViewHolder(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        //val bookImage : ShapeableImageView = itemView.findViewById(R.id.book_image)
        val bookTitle: TextView
        val bookAuthor: TextView
        val moreActionsView: ImageView

        init {
            bookTitle = itemView.findViewById(R.id.book_title)
            bookAuthor = itemView.findViewById(R.id.book_author)
            moreActionsView = itemView.findViewById(R.id.more_action_View)

            moreActionsView.setOnClickListener {
                popupMenu(itemView)
            }
        }

        private fun popupMenu(itemView: View) {
            val popupMenu = PopupMenu(context, itemView, Gravity.END)
            popupMenu.inflate(R.menu.show_menu)
            val menu = popupMenu.menu

            /** Set icons **/
            for (i in 0 until menu.size()) {
                val menuItem = menu.getItem(i)
                when (menuItem.itemId) {
                    R.id.menu_item_delete -> menuItem.setIcon(R.drawable.baseline_delete_24)
                    R.id.menu_stop_read -> menuItem.setIcon(R.drawable.baseline_heart_broken_24)
                    R.id.menu_item_to_read -> menuItem.setIcon(R.drawable.baseline_filter_vintage_24)
                }
            }
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_item_delete -> {
                        //bookList.removeAt(adapterPosition)
                        AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setIcon(R.drawable.baseline_delete_24)
                            .setMessage("Are you sure you want to delete this book? ")
                            .setPositiveButton("Yes") { dialog, _ ->
                                //bookList.removeAt(adapterPosition)
                                val delete = MyBooksFragment()
                                println("Book title to delete" + bookList[adapterPosition].title!!)
                                delete.deleteBookFromDatabase(
                                    bookList[adapterPosition].title!!,
                                    bookList[adapterPosition].author!!
                                )
                                bookList.removeAt(adapterPosition)
                                notifyItemRemoved(absoluteAdapterPosition)
                                dialog.dismiss()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()

                        Toast.makeText(context, "Book Deleted", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_stop_read -> {
                        Toast.makeText(context, "Book Stop Read", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_item_to_read -> {
                        Toast.makeText(context, "Add to Book to Read", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> true
                }

            }
            popupMenu.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.book_list,
            parent, false
        )
        return MyViewHolder(itemView, parent.context)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = bookList[position]
        //holder.bookImage.setImageResource(currentItem.bookImage)
        holder.bookTitle.text = currentItem.title
        holder.bookAuthor.text = currentItem.author

        fun addBook(book: Books) {
            books.add(book)
            notifyItemInserted(books.size - 1)
        }
    }
}