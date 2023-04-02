package com.example.bookvibes

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var adapter: MyAdapter
private lateinit var adapter2 : MainAdapter
private lateinit var combinedAdapter : CombinedAdapter
private lateinit var recyclerView: RecyclerView
private lateinit var booksArrayList: ArrayList<Books>
private lateinit var booksArrayList2: ArrayList<Books>
private lateinit var combinedbooksArrayList: ArrayList<Books>

/**
 * A simple [Fragment] subclass.
 * Use the [FavoriteBooksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FavoriteBooksFragment : Fragment(), MyAdapter.OnBookMenuClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        booksArrayList = ArrayList()
        combinedbooksArrayList = ArrayList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favorite_books, container, false)
        val viewTitleAuthor = inflater.inflate(R.layout.book_list, container, false)
        val title = viewTitleAuthor.findViewById<TextView>(R.id.book_title)
        val author = viewTitleAuthor.findViewById<TextView>(R.id.book_author)
        val image = viewTitleAuthor.findViewById<ImageView>(R.id.book_image)

        //set RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.recycler_view_fav)
        recyclerView.layoutManager = layoutManager
        // recyclerView.setHasFixedSize(true)
        adapter = MyAdapter(booksArrayList, this)
        recyclerView.adapter = adapter
//        adapter1 = MyAdapter(booksArrayList1, this)
//        adapter2 = MainAdapter(booksArrayList2, this)
//        combinedAdapter = CombinedAdapter(combinedbooksArrayList,adapter1, adapter2)
//        recyclerView.adapter = combinedAdapter

        /** Get Fav Books from Firebase **/
        getFavBooksFromFirebase(uid, title, author)

        return view
    }

    private fun getFavBooksFromFirebase(uid: String, title: TextView?, author: TextView?) {
        userRef.child(uid).child("Favorites").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val books = snapshot.children
                for (book in books) {
                    val titleFromFirebase = book.child("title").getValue(String::class.java)
                    val authorFromFirebase = book.child("author").getValue(String::class.java)

                    booksArrayList.add(Books("$titleFromFirebase", "$authorFromFirebase"))

                    //adapter.setFavoriteState(0, true)
                    adapter.notifyDataSetChanged()
//                    booksArrayList2.add(Books("$titleFromFirebase", "$authorFromFirebase"))
//                    combinedbooksArrayList.add(Books("$titleFromFirebase", "$authorFromFirebase", "https://thumbs.dreamstime.com/b/books-heart-vector-illustration-library-decorated-tea-roses-green-branches-isolated-white-background-160123159.jpg"))
//                    combinedAdapter.notifyDataSetChanged()
                }
                setFullHeartIcon(booksArrayList)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(ContentValues.TAG, "Failed to get user's fav books from Firebase", error.toException())
            }
        })

    }

    private fun setFullHeartIcon(booksArrayList: java.util.ArrayList<Books>) {
        for (i in booksArrayList.indices)
            adapter.setFavoriteState(i, true)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FavoriteBooksFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FavoriteBooksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}