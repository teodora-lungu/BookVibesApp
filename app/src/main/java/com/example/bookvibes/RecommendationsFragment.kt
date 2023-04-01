package com.example.bookvibes

import android.annotation.SuppressLint
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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

private lateinit var adapter: MainAdapter
private lateinit var recyclerView: RecyclerView
private lateinit var booksArrayList: ArrayList<Books>

lateinit var prefGen : java.util.ArrayList<String>

val mAuth = FirebaseAuth.getInstance()
val currentUser = mAuth.currentUser
val database = FirebaseDatabase.getInstance()
val userRef = database.reference.child("users")
//.child(currentUser?.uid!!)
val uid = currentUser?.uid.toString()


/**
 * A simple [Fragment] subclass.
 * Use the [RecommendationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecommendationsFragment : Fragment() {
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
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_recommendations, container, false)
        val layoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.recycler_view_main)
        recyclerView.layoutManager = layoutManager
        adapter = MainAdapter(booksArrayList)
        recyclerView.adapter = adapter

        /** Get pref genres from Firebase **/
        prefGen = java.util.ArrayList()
        getPrefGenresFromFirebase(prefGen)

        return view
    }

    private fun getPrefGenresFromFirebase(prefGen: ArrayList<String>) {
        /** Get menu View references **/
        val inflater = layoutInflater
        val menuView = inflater.inflate(R.layout.book_list, null)
        val bookRecmImg = menuView.findViewById<ImageView>(R.id.book_image)
        val bookRecmTitle = menuView.findViewById<TextView>(R.id.book_title)
        val bookRecmAuthor = menuView.findViewById<TextView>(R.id.book_author)
        if (currentUser != null) {
            userRef.child(uid).child("Pref Genres").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val genres = snapshot.children
                    for (gen in genres) {
                        prefGen.add(gen.key.toString())
                    }

                    setRecommendation(bookRecmTitle, bookRecmAuthor, bookRecmImg, prefGen)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(ContentValues.TAG, "Failed to get pref genres", error.toException())
                }

            })

        }
    }

    private fun setRecommendation(title: TextView, author: TextView, img: ImageView, prefGen: ArrayList<String>) {
        val bookType = database.reference.child("BookType")
        //println("Book type -> " + bookType)
        booksArrayList.clear()
        bookType.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val types = snapshot.children
                //println("Types: " + types)
                for (type in types) {
                    //println("Types from FIREBASE:" + type)
                    for (gen in prefGen.indices) {
                        println("Types from prefGEN: " + prefGen[gen])
                        println("TYPES FROM FIREBASE: " + type.key.toString())
                        if ((type.key.toString()).equals(prefGen[gen])) {
                            for (index in type.children) {
                                val titleFB = index.child("title").getValue(String::class.java)
                                val authorFB = index.child("author").getValue(String::class.java)
                                val imgFB = index.child("img").getValue().toString()

                                title.text = titleFB.toString()
                                author.text = authorFB.toString()
                                //img.setImageBitmap(null)
                                if (titleFB.toString().equals("It starts with us"))
                                    Glide.with(this@RecommendationsFragment)
                                        .load(imgFB).into(img)

                                booksArrayList.add(Books(titleFB, authorFB, imgFB))
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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
         * @return A new instance of fragment RecommendationsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RecommendationsFragment().apply {
                 arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}