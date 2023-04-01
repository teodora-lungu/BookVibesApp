package com.example.bookvibes

import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MyAdapter(private val bookList : ArrayList<Books>, private val listener: OnBookMenuClickListener) :
                RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    interface OnBookMenuClickListener {
        fun onMenuClicked(book: Books) {

        }

        fun onBookClicked(book: Books, item : MenuItem?) {

        }
    }


    private val books = mutableListOf<Books>()
    private var selectedBook: Books? = null

    inner class MyViewHolder(itemView: View, val context: Context) :
        RecyclerView.ViewHolder(itemView) {
        //val bookImage : ShapeableImageView = itemView.findViewById(R.id.book_image)
        val bookTitle: TextView
        val bookAuthor: TextView
        val bookImg: ImageView
        val moreActionsView: ImageView

        init {
            bookTitle = itemView.findViewById(R.id.book_title)
            bookAuthor = itemView.findViewById(R.id.book_author)
            bookImg = itemView.findViewById(R.id.book_image)
            moreActionsView = itemView.findViewById(R.id.more_action_View)

            moreActionsView.setOnClickListener {
                popupMenu(itemView)
            }
        }

        private fun popupMenu(itemView: View) {
            val popupMenu = PopupMenu(context, itemView, Gravity.END)
            popupMenu.inflate(R.menu.show_menu)
            val menu = popupMenu.menu
            val book: Books? = null

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
                println("POPUP MENU")

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
                        //val bundle = Bundle()
                        val title = bookTitle.text.toString()
                        val author = bookAuthor.text.toString()
                        val image = String()
                        Glide.with(itemView)
                            .load(image)
                            .into(bookImg)
                        for (i in bookList.indices) {
                            println(bookList[adapterPosition].title)
                            //val tit = "Title: " + title
                            println("TITLE ---- >" + title)
                            if (bookList[adapterPosition].title.toString() == title) {
                                //bookList.add(Books(title, author))
                                println("deja in my books")
                            } else {
                                bookList.add(Books(title, author))
                            }
                        }
                        //println("popupMenu.menu.getItem(it.itemId):" + popupMenu.menu.getItem(it.itemId))
                        listener?.onBookClicked(bookList[adapterPosition], it)
                        /** ABIA COMENTAT**/
                        //updateSelectedBook(book)

//                        listener?.onMenuClicked(bookList[adapterPosition])
//                        true
//                        val bookListJson = Gson().toJson(bookList[adapterPosition])
//                        bundle.apply {
//                            putString("stop", "VLAVLAVLA")
//                        }
//                        val fragment = StoppedReadingFragment()
//                        fragment.arguments = bundle
//                        fragment.fragmentManager?.beginTransaction()?.replace(R.id.book_author, fragment)?.commit()
//                        println("Stopped reading---->>" + bundle)


                        //listener.onMenuClicked(book)

                        //parentFragmentManager.setFragmentResult("stop", )

                        Toast.makeText(context, "Book Stop Read", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.menu_item_to_read -> {
                        val title = bookTitle.text.toString()
                        val author = bookAuthor.text.toString()
                        val image = String()
                        Glide.with(itemView).load(image).into(bookImg)
                        for (i in bookList.indices) {
                            println(bookList[adapterPosition].title)
                            println("TITLE ---- >" + title)
                            if (bookList[adapterPosition].title.toString() == title) {
                                println("deja in my books")
                            } else {
                                bookList.add(Books(title, author))
                            }
                        }
                        listener?.onBookClicked(bookList[adapterPosition], it)

                        Toast.makeText(context, "Add to Book to Read", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> true
                }

            }
            popupMenu.show()
        }

//        override fun onClick(p0: View?) {
//            println("ONCLICK FUNCTION ADAPTER")
//            books?.let {
//                listener.onBookClicked(bookList[adapterPosition], itemView.findViewById(R.id.menu_stop_read))
//            }
//        }

    }

//    fun updateSelectedBook(book: Books?) {
//            selectedBook = book
//            notifyDataSetChanged()
//    }

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
        Glide.with(holder.itemView)
            .load(currentItem.img)
            //.transition(DrawableTransitionOptions.withCrossFade())
            .into(holder.bookImg)
        //holder.moreActions.setImageBitmap(null)

        fun addBook(book: Books) {
            books.add(book)
            notifyItemInserted(books.size - 1)
        }
    }
}