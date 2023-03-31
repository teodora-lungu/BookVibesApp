package com.example.bookvibes

import android.app.AlertDialog
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
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

        /** get books from Firebase **/
        getBooksFromFirebase(uid, title, author)
        addBookButton.setOnClickListener {
            showAddBookDialog()
        }
        val bundle = Bundle()
        bundle.apply {
            putString("stop", "BLABLA")
        }

        val fragment = StoppedReadingFragment()
        fragment.arguments = bundle
//            val secondFragment = StoppedReadingFragment().apply {
//                arguments = bundle
//            }
        //return inflater.inflate(R.layout.fragment_my_books, container, false)
        return view
    }

    override fun onMenuClicked(book: Books) {
        //super.onMenuClicked(book)
        println("nmenuCLICKE" + book)
        sharedViewModel.setSelectedBook(book)
        adapter.updateSelectedBook(book)
        addBookToStoppedReadling(book)
    }

    private fun addBookToStoppedReadling(bookStop : Books) {
        if (currentUser != null) {

            //userRef.child(uid).child("StoppedReading").push().setValue(book)
            userRef.child(uid).child("StoppedReading").addListenerForSingleValueEvent(object  : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val books = snapshot.children

                    if (!snapshot.exists()) {
                        userRef.child(uid).child("StoppedReading").push().setValue(bookStop)
                    }
                    for (book in books) {
                        val bookTitle = book.child("title").getValue(String::class.java)
                        println("BOOKTITLE:" + bookTitle)
                        val bookSTOPTITLE = bookStop.title
                        println("SOTPPPP" + bookSTOPTITLE)
                        if (book.child("title").getValue(String::class.java).equals(bookStop.title)) {
                            println("la fel")
                        } else {
                            userRef.child(uid).child("StoppedReading").push().setValue(bookStop)
                        }
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
                //println(books)
                for (book in books) {
                    //println(book)
                    //println(book.getValue(Books::class.java))
                    //println(book.child("title").getValue(String::class.java))
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    //title.text = titleFromFirebase

                    val authorFromFirebase = book.child("author").getValue(String::class.java)
                    //author.text = authorFromFirebase

                    booksArrayList.add(Books("Title: $titleFromFirebase",
                                             "Author: $authorFromFirebase"))
//                    val bundle = Bundle().apply {
//                        putString("stop", Gson().toJson(booksArrayList))
//                    }
//                    val secondFragment = StoppedReadingFragment().apply {
//                        arguments = bundle
          //          }
                    adapter.notifyDataSetChanged()
                }

                //val title = titleEditText.text.toString()
                //val author = authorEditText.text.toString()

                //adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to get user nickname", error.toException())
            }
        })

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
            //booksArrayList.add(book)
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
                println("Title:" + title)
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)
                    val tit = "Title: " + titleFromFirebase
                    val aut = "Author: " + authorFromFirebase
                    println("Title from FB:" + titleFromFirebase)
                    if (title.equals(tit) &&
                        author.equals(aut)) {
                        println("DELETEEEEE")
                        userRef.child(uid).child("MyBooks").child(book.key!!).removeValue()
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to delete from Firebase", error.toException())
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

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        //dataInitialize()
//        val layoutManager = LinearLayoutManager(context)
//        recyclerView = view.findViewById(R.id.recycler_view)
//        recyclerView.layoutManager = layoutManager
//        recyclerView.setHasFixedSize(true)
//        adapter = MyAdapter(booksArrayList)
//        recyclerView.adapter = adapter
//    }

    /** method to initialize data for MyBooks**/
    private fun dataInitialize() {

        booksArrayList = arrayListOf<Books>()

        imageId = arrayOf(
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall,
            R.drawable.booksmall

        )

        title = arrayOf(
            "Another Title",
            "Cetatea celor o mie de sori",
            "M-am plictisit Title",
            "Prea multe pacanele",
            "Titlu 7000",
            "Titlu 8000",
            "Another Title",
            "Cetatea celor o mie de sori",
            "M-am plictisit Title",
            "Prea multe pacanele",
            "Titlu 7000",
            "Titlu 8000"
        )

        author = arrayOf(
            "Author 1",
            "Author doi",
            "Author 3",
            "Author 4",
            "Author 5",
            "Author 6",
            "Author 1",
            "Author doi",
            "Author 3",
            "Author 4",
            "Author 5",
            "Author 6"
        )

        for (i in imageId.indices) {
            val books = Books(title[i], author[i])
                //, imageId[i])
            booksArrayList.add(books)
        }
       /** TODO("Initialize from Firebase") **/
    }
}