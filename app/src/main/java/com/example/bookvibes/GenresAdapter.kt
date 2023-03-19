package com.example.bookvibes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class GenresAdapter(private var genresList : ArrayList<Genres>) : RecyclerView.Adapter<GenresAdapter.MyViewHolder>() {

    //private val genres = mutableListOf<Genres>()
    //private val arrayList: ArrayList<Genres>

    val mAuth = FirebaseAuth.getInstance()
    val currentUser = mAuth.currentUser
    val database = FirebaseDatabase.getInstance()
    val userRef = database.reference.child("users")
    //.child(currentUser?.uid!!)
    val uid = currentUser?.uid.toString()

    init {
        this.genresList = genresList
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.genres_list,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return genresList.size
    }

    override fun onBindViewHolder(holder: GenresAdapter.MyViewHolder, position: Int) {
        val currentItem = genresList[position]
        holder.bookGenres.text = currentItem.categories
        holder.checkbox.isChecked = currentItem.isSelected
        //holder.checkbox.visibility = View.VISIBLE

       // for (category in currentItem.categories) {
           // if (holder.checkbox.isChecked)
             //   holder.checkbox.isChecked = holder.checkbox.isSelected
        //Toast.makeText(this, "Image change succesfully", Toast.LENGTH_SHORT).show()

       // }
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
         val checkbox : CheckBox = itemView.findViewById(R.id.type_checkboxView)
         val bookGenres : TextView = itemView.findViewById(R.id.book_type)
         //val saveButton : Button = itemView.findViewById(R.id.save_gen_button)

        init {
            checkbox.setOnClickListener { v ->
                val isChecked = (v as CheckBox).isChecked
                genresList[adapterPosition].isSelected = isChecked

                notifyDataSetChanged()

                for (i in genresList.indices) {
                    Log.d("TAG", genresList.toString())

                    //val intent = Intent(Context, MainActivity::class.java)
                    //startActivity(intent)
                }
            }

            //saveButton.setOnClickListener {


            //}
        }
    }

}