package com.example.bookvibes

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
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
 * Use the [BookGenresFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookGenresFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("users")
    //.child(currentUser?.uid!!)
    val uid = currentUser?.uid.toString()

    private lateinit var adapter: GenresAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookGenresList: ArrayList<Genres>

    lateinit var genre : Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       //  dataInitialize()
        bookGenresList = ArrayList()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_genres, container, false)


        val genresView = inflater.inflate(R.layout.genres_list, container, false)
        val checkBox = genresView.findViewById<CheckBox>(R.id.type_checkboxView)

        //set RecyclerView
        val layoutManager = LinearLayoutManager(context)
        recyclerView = view.findViewById(R.id.recycler_view_bookType)
        recyclerView.layoutManager = layoutManager
        // recyclerView.setHasFixedSize(true)
        adapter = GenresAdapter(bookGenresList)
        recyclerView.adapter = adapter

        /** Get genre list form firebase **/
        getGenreList(uid, checkBox)

//        //set RecyclerView
//        val layoutManager = LinearLayoutManager(context)
//        recyclerView = view.findViewById(R.id.recycler_view_bookType)
//        recyclerView.layoutManager = layoutManager
//        recyclerView.itemAnimator = DefaultItemAnimator()
//        // recyclerView.setHasFixedSize(true)
//        adapter = GenresAdapter(bookGenresList)
//        recyclerView.adapter = adapter

        //val genresView = inflater.inflate(R.layout.genres_list, container, false)
        //val checkbox = genresView.findViewById<CheckBox>(R.id.type_checkboxView)
        //val genre = genresView.findViewById<TextView>(R.id.book_type)
        //genre.text = "mor"

        return view
    }

    private fun getGenreList(uid: String, checkBox: CheckBox) {

        userRef.child(uid).child("Pref Genres").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val genres = snapshot.children

                for (gen in genres) {

                    val category = gen.key
                    val isSelected = gen.getValue(Boolean::class.java) ?: false
                    //bookGenresList.add(Genres(category!!, isSelected))

                    for (genre in bookGenresList) {
                        if (genre.categories == category) {
                            genre.isSelected = isSelected
                            break
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            })


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dataInitialize()

        val saveButton = view.findViewById<Button>(R.id.save_gen_button)

        val layoutManager = LinearLayoutManager(context)

        recyclerView = view.findViewById(R.id.recycler_view_bookType)
        recyclerView.layoutManager = layoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        //recyclerView.setHasFixedSize(true)
        adapter = GenresAdapter(bookGenresList)
        recyclerView.adapter = adapter

        adapter.notifyDataSetChanged()

            saveButton.setOnClickListener {
                userRef.child(uid).child("Pref Genres").removeValue()
                for (i in bookGenresList.indices) {
                    //  Log.d("TAG", "---->>>>>" + bookGenresList[i])
                if (currentUser != null) {
                    if (bookGenresList[i].isSelected) {
                        val genre = bookGenresList[i].categories
                        val childRef = userRef.child(uid).child("Pref Genres").child(genre)
                        childRef.setValue(bookGenresList[i].isSelected)
                    }
                }
            }
                Toast.makeText(context, "Your data have been saved", Toast.LENGTH_SHORT).show()
                val intent = Intent(activity, MainActivity::class.java)
                startActivity(intent)
        }

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookGenresFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookGenresFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun dataInitialize(){
        bookGenresList = arrayListOf<Genres>()

        genre = arrayOf("Romance",
            "Thriller",
            "History",
            "Science Fiction",
            "Fantacy",
            "Biografy",
            "History",
            "Art",
            "YoungAdult",
            "Memories",
            "Filosofy")

        for (i in genre.indices) {
            val genre = Genres(genre[i], false)
            //, imageId[i])
            bookGenresList.add(genre)

            /** Save Data to Firebase **/
        }
    }
}