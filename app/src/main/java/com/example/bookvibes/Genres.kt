package com.example.bookvibes

data class Genres(var categories : String,
                  var isSelected : Boolean) {

    override fun toString(): String {
        return "Genres" + categories + '\'' +
                "isSelected=" + isSelected
    }

    //constructor() : this ("", false)

}

