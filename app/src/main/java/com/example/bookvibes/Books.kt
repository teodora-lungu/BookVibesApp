package com.example.bookvibes

data class Books(var title : String? = null,
                 var author : String? = null,
                 var img : String? = null)
    {
        constructor() : this("", "", "")
    }

//, var bookImage : Int)
