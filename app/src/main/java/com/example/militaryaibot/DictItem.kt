package com.example.militaryaibot

import kotlin.random.Random

data class DictItem (

    // COMMON VARIABLES
    val word: String,
    var desc: String = ""

    ) {
    companion object {
        private var counter = 0L
    }

}