package com.example.bookvibes

data class Books(var title : String? = null,
                 var author : String? = null,
                 var img : String? = null,
                 var isFavorite : Boolean = false)
    {
        constructor() : this("", "", "", false)
    }