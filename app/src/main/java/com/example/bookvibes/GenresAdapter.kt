package com.example.bookvibes

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
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
        holder.checkbox.text = currentItem.categories
        holder.checkbox.isChecked = currentItem.isSelected
    }

    inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
         val checkbox : CheckBox = itemView.findViewById(R.id.type_checkboxView)

        init {
            checkbox.setOnClickListener { v ->
                val isChecked = (v as CheckBox).isChecked
                genresList[adapterPosition].isSelected = isChecked

                notifyDataSetChanged()

                for (i in genresList.indices) {
                    Log.d("TAG", genresList.toString())
                }
            }
        }
    }

}