package com.example.bookvibes

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

//import org.jetbrains.anko.doAsync

class BookOfTheDayActivity : AppCompatActivity() {

    val database = FirebaseDatabase.getInstance()
    val myRef = database.getReference("Book1")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.activity_book_of_the_day, null)
        setContentView(view)

        myRef.addListenerForSingleValueEvent(object  : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                    val link_value = snapshot.child("link").getValue(String::class.java).toString()
                    val main_class = snapshot.child("class").getValue(String::class.java).toString()
                    val desc_class = snapshot.child("desc_class").getValue(String::class.java).toString()
                    Log.d(ContentValues.TAG, "Value is: $link_value")
                    Log.d(ContentValues.TAG, "Value is: $main_class")
                    Log.d(ContentValues.TAG, "Value of description is >>>: $desc_class")

                    performScraping(link_value, main_class, desc_class)

            }

            override fun onCancelled(error: DatabaseError) {
                // failed to read value
                Log.w(ContentValues.TAG, "Failed to read value", error.toException())
            }

        })
    }

    private fun performScraping(link : String, main_class : String, desc_class : String) {
        val img_list = ArrayList<String>()
        val book_img = findViewById<ImageView>(R.id.book_img)

        val desc_list = ArrayList<String>()
        val descView = findViewById<TextView>(R.id.description)

       /** GlobalScope.async {
            var doc = Jsoup.connect(link).get()
            var allinfo = doc.getElementsByClass(main_class)

            for (i in allinfo) {
                val img = i.getElementsByTag("img").attr("src")
                img_list.add(img)

                val description = i.getElementsByClass(desc_class)
                    .toString().replace("<br>", "").substringAfter("\n")
                //val description = i.getElementsByClass("BookPageTitleSection").toString()
                val desc_cleared = description.substring(0,description.indexOf("\n"))
                desc_list.add(desc_cleared)

                runOnUiThread {
            Glide.with(this@BookOfTheDayActivity).load(img_list[0]).into(book_img)
                descView.text = desc_cleared
            }
        }

        } **/

        GlobalScope.launch(Dispatchers.IO) {
            try {
                var doc = Jsoup.connect(link).get()
                var allinfo = doc.getElementsByClass(main_class)
                val img_list = mutableListOf<String>()
                val desc_list = mutableListOf<String>()

                for (i in allinfo) {
                    val img = i.getElementsByTag("img").attr("src")
                    img_list.add(img)

                    val description = i.getElementsByClass(desc_class)
                        .toString().replace("<br>", "").substringAfter("\n")
                    println(description)
                    val desc_cleared = description.substring(0, description.indexOf("\n"))
                    desc_list.add(desc_cleared)
                    println(img_list)
                }

                withContext(Dispatchers.Main) {
                    Glide.with(this@BookOfTheDayActivity).load(img_list[0]).into(book_img)
                    descView.text = desc_list[0]
                }

            }catch (e: Exception) {
                Log.e("BOOKOFTHEDAY", "Error during web scraping: ${e.message}")
            }
        }
    }
}