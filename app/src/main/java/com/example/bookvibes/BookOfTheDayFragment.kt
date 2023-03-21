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
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BookOfTheDayFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BookOfTheDayFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("Book1")

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
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_book_of_the_day, container, false)

        val book_img = view.findViewById<ImageView>(R.id.book_img)
        val descView = view.findViewById<TextView>(R.id.description)

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // retrive the data from the snapshot
                //val value = snapshot.getValue(String::class.java).toString()
                //for (i in snapshot.children) {
                val link_value = snapshot.child("link").getValue(String::class.java).toString()
                val main_class = snapshot.child("class").getValue(String::class.java).toString()
                val desc_class =
                    snapshot.child("desc_class").getValue(String::class.java).toString()
                // val desc_cleared = desc_class.replace("<br>", "\n")
                Log.d(ContentValues.TAG, "Value link is: $link_value")
                Log.d(ContentValues.TAG, "Value main class is: $main_class")
                Log.d(ContentValues.TAG, "Value of description is >>>: $desc_class")

                performScraping(link_value, main_class, desc_class, book_img, descView)
            }

            override fun onCancelled(error: DatabaseError) {
                // failed to read value
                Log.w(ContentValues.TAG, "Failed to read value", error.toException())
            }
        })

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment BookOfTheDayFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BookOfTheDayFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun performScraping(
        link: String, main_class: String, desc_class: String,
        book_img: ImageView, descView: TextView
    ) {

        GlobalScope.launch(Dispatchers.IO) {
//            val result = async(Dispatchers.Default) {
            try {
                var doc = Jsoup.connect(link).get()
                var allinfo = doc.getElementsByClass(main_class)
                println("main class is:" + main_class)
                //doc.html()
                val img_list = mutableListOf<String>()
                val desc_list = mutableListOf<String>()

                //delay(3000)

                //img_list.add(doc.select("ResponsiveImage").attr("src"))
                //desc_list.add(doc.select("description").first()!!.text())

                println("all info:" + allinfo)
                for (i in allinfo) {
                    val img = i.getElementsByTag("img").attr("src")
                    println("My img should be ------>>>>" + img)
                    img_list.add(img)

                    val description = i.getElementsByClass(desc_class)
                        .toString().replace("<br>", "").substringAfter("\n")
                    //val description = i.getElementsByClass("BookPageTitleSection").toString()
                    println(description)
                    val desc_cleared = description.substring(0, description.indexOf("\n"))
                    desc_list.add(desc_cleared)
                    println(img_list)
                }

                withContext(Dispatchers.Main) {
                    Glide.with(this@BookOfTheDayFragment).load(img_list[0]).into(book_img)
                    descView.text = desc_list[0]
                }

            }catch (e: Exception) {
                Log.e("BOOKOFTHEDAY", "Error during web scraping: ${e.message}")
            }
//            delay(3000)
//            val (img_list, desc_list) = result.await().first
//            println(img_list + desc_list)
//            //val desc_list = result.second
//            // Get the results from the Pair object
//            //val img_list = result.first
//            //val desc_list = result.second
//
////            Handler(Looper.getMainLooper()).post {
////
////                Glide.with(this@BookOfTheDayFragment).load(img_list[0]).into(book_img)
////                descView.text = desc_list[0]
//            }
        }
    }

}