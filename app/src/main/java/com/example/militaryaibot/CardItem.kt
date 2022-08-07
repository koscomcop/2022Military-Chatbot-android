package com.example.militaryaibot

import kotlin.random.Random

data class CardItem (

    // COMMON VARIABLES
    val id: Long = counter++,
    val name: String,
    var cardtype: Int = CardType.CARD_IMAGE_FULL,

    // FULL IMAGE CARD
    val city: String = "",
    val url: String = "",

    // TEXT TYPE1 CARD (WEATHER INFO)
    var coloridx: Int = Random.nextInt(7),
    var info1: String = "",
    var info2: String = "",

    ) {
    companion object {
        private var counter = 0L
    }

    fun getCardType(): Int {
        return cardtype
    }
    fun setCardType(cardtype: Int) {
        this.cardtype = cardtype
    }

}