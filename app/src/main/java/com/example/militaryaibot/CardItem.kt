package com.example.militaryaibot

data class CardItem (
    val id: Long = counter++,
    val name: String,
    val city: String,
    val url: String,
    var cardtype: Int = CardType.CARD_IMAGE_FULL
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