package com.example.bookvibes

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MyBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MyBooksFragment : Fragment(), MyAdapter.OnBookMenuClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    /** set instance for View Model **/
    val sharedViewModel: SharedViewModel by viewModels()

    private lateinit var adapter: MyAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var booksArrayList: ArrayList<Books>
    private lateinit var favBooksList : ArrayList<Books>

    lateinit var imageId : Array<Int>
    lateinit var title : Array<String>
    lateinit var author : Array<String>
    //lateinit var books : Array<String>

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("users")
    //.child(currentUser?.uid!!)
    val uid = currentUser?.uid.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        booksArrayList = ArrayList()
        favBooksList = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_my_books, container, false)
        val viewTitleAuthor = inflater.inflate(R.layout.book_list, container, false)
        val addBookButton = view.findViewById<Button>(R.id.add_book_button)
        val title = viewTitleAuthor.findViewById<TextView>(R.id.book_title)
        val author = viewTitleAuthor.findViewById<TextView>(R.id.book_author)


        //set RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager
        // recyclerView.setHasFixedSize(true)
        adapter = MyAdapter(booksArrayList, this)
        recyclerView.adapter = adapter

        /** get fav book from Firebase**/
        getFavBooksFromFirebase(favBooksList)
        /** get books from Firebase **/
        getBooksFromFirebase(uid, title, author)
        addBookButton.setOnClickListener {
            showAddBookDialog()
        }
        return view
    }

    override fun onBookClicked(book: Books, item: MenuItem?) {
        return when (item?.itemId) {
            R.id.menu_stop_read -> {
                addBookToStoppedReading(book)
            }
            R.id.menu_item_to_read -> {
                addBookToRead(book)
            }
            else -> super.onBookClicked(book, item)
        }
    }

    private fun addBookToRead(bookToRead: Books) {
        if (currentUser != null) {
            userRef.child(uid).child("Books to Read").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var bookExists = false
                    for (book in snapshot.children) {
                        if (bookToRead.title == book.child("title").getValue(String::class.java)) {
                            bookExists = true
                            Toast.makeText(context, "Book already added", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }

                    if (!bookExists) {
                        userRef.child(uid).child("Books to Read").push().setValue(bookToRead)
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to get favorite books", error.toException())
                }

            })
        }
    }

    override fun onHeartClicked(book: Books) {
        if (book.isFavorite) {
            removeFavBook(book)
        } else {
            addFavBookToFirebase(book)
        }
        book.isFavorite = !book.isFavorite
    }

    private fun removeFavBook(bookFav: Books) {
        if (currentUser != null) {
            userRef.child(uid).child("Favorites").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (book in snapshot.children) {
                        val titleFromFirebase = book.child("title").getValue(String::class.java)
                        val authorFromFirebase = book.child("author").getValue(String::class.java)
                        if (bookFav.title.equals(titleFromFirebase) &&
                            bookFav.author.equals(authorFromFirebase)) {
                            userRef.child(uid).child("Favorites").child(book.key!!).removeValue()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to delete fav books from Firebase", error.toException())
                }
            })
        }
    }

    private fun addFavBookToFirebase(bookFav: Books) {
        if (currentUser != null) {
            userRef.child(uid).child("Favorites").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    var bookExists = false

                    for (book in snapshot.children) {
                        if (bookFav.title == book.child("title").getValue(String::class.java)) {
                            bookExists = true
                            break
                        }
                    }

                    if (!bookExists) {
                        userRef.child(uid).child("Favorites").push().setValue(bookFav)
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to get favorite books", error.toException())
                }

            })
        }
    }

    private fun addBookToStoppedReading(bookStop : Books) {
        if (currentUser != null) {
            userRef.child(uid).child("StoppedReading").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    var bookExists = false

                    for (book in snapshot.children) {
                        if (bookStop.title == book.child("title").getValue(String::class.java)) {
                            bookExists = true
                            Toast.makeText(context, "Book already added", Toast.LENGTH_SHORT).show()
                            break
                        }
                    }

                    if (!bookExists) {
                        userRef.child(uid).child("StoppedReading").push().setValue(bookStop)
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to get stopped reading books", error.toException())
                }

            })
    }
}

    fun getBooksFromFirebase(uid : String, title : TextView, author : TextView) {

        userRef.child(uid).child("MyBooks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children
                var bookIsFav = false
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)

                    for (i in favBooksList.indices) {
                        val tit = "Title: " + titleFromFirebase
                        if (tit == favBooksList[i].title) {
                            bookIsFav = true
                            break
                        } else {
                            bookIsFav = false
                            break
                        }
                    }

                    if (!bookIsFav)
                        booksArrayList.add(Books("Title: $titleFromFirebase",
                            "Author: $authorFromFirebase",
                            "https://thumbs.dreamstime.com/z/stack-books-textbooks-flowers-around-cartoon-flat-style-character-white-background-different-colored-covers-lot-169307038.jpg",
                            isFavorite = true))
                    else
                            booksArrayList.add(Books("Title: $titleFromFirebase",
                                "Author: $authorFromFirebase",
                                "https://thumbs.dreamstime.com/z/stack-books-textbooks-flowers-around-cartoon-flat-style-character-white-background-different-colored-covers-lot-169307038.jpg",
                                isFavorite = false))
                    setFullHeartIcon(booksArrayList)
                    adapter.notifyDataSetChanged()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to get user nickname", error.toException())
            }
        })

    }

    private fun setFullHeartIcon(booksArrayList: ArrayList<Books>) {
        for (i in booksArrayList.indices)
            adapter.setFavoriteState(i, booksArrayList[i].isFavorite)
    }

    private fun showAddBookDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Add book details")

        val inputLayout = LinearLayout(requireContext())
        inputLayout.orientation = LinearLayout.VERTICAL

        val titleEditText = EditText(requireContext())
        titleEditText.hint = "Title"
        inputLayout.addView(titleEditText)

        val authorEditText = EditText(requireContext())
        authorEditText.hint = "Author"
        inputLayout.addView(authorEditText)

        builder.setView(inputLayout)

        builder.setPositiveButton("Add") { _, _ ->
            val title = titleEditText.text.toString()
            val author = authorEditText.text.toString()

            val book = Books(title, author)
            addBooktoFirebase(book)
            booksArrayList.add(Books("Title: $title", "Author: $author"))

            adapter.notifyDataSetChanged()
            Toast.makeText(context, "Book Added Succesfully", Toast.LENGTH_SHORT).show()
            builder.create().dismiss()
        }
        builder.setNegativeButton("Cancel") {_, _ ->
            builder.create().dismiss()
            Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show()
        }
        builder.create()
        builder.show()
    }

    private fun addBooktoFirebase(book: Books) {
        if (currentUser != null) {
            userRef.child(uid).child("MyBooks").push().setValue(book)
        }
    }

    fun deleteBookFromDatabase(title : String, author: String) {

        userRef.child(uid).child("MyBooks").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)
                    val tit = "Title: " + titleFromFirebase
                    val aut = "Author: " + authorFromFirebase
                    if (title.equals(tit) &&
                        author.equals(aut)) {
                        userRef.child(uid).child("MyBooks").child(book.key!!).removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to delete from Firebase", error.toException())
            }
        })

        userRef.child(uid).child("Favorites").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)
                    val tit = "Title: " + titleFromFirebase
                    val aut = "Author: " + authorFromFirebase
                    if (title.equals(titleFromFirebase) &&
                        author.equals(authorFromFirebase)) {
                        userRef.child(uid).child("Favorites").child(book.key!!).removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to delete from Favorites Firebase", error.toException())
            }
        })
    }

    private fun getFavBooksFromFirebase(bookFavList : ArrayList<Books>) {
        com.example.bookvibes.userRef.child(uid).child("Favorites").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)

                    bookFavList.add(Books("$titleFromFirebase", "$authorFromFirebase"))
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to get user's fav books from Firebase", error.toException())
            }
        })

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MyBooksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyBooksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}