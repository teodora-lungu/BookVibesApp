package com.example.bookvibes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val sharedBookList = MutableLiveData<Books>()

    fun setSelectedBook(book: Books) {
        sharedBookList.value = book
    }

    val selectedBook: LiveData<Books>
        get() = sharedBookList
}