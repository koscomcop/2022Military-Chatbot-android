package com.example.militaryaibot

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.militaryaibot.ChatMsgAdapter.*
import com.google.android.material.card.MaterialCardView
import kotlin.random.Random

class CardStackAdapter(
    private var cards: List<CardItem> = emptyList()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            CardType.CARD_IMAGE_FULL -> {
                return CardImageViewHolder(inflater.inflate(R.layout.card_image, parent, false))
            }
            else -> {
                return CardText1ViewHolder(inflater.inflate(R.layout.card_weather, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val curcard = cards[position]

        when(curcard.cardtype) {
            CardType.CARD_IMAGE_FULL -> {
                var curholder = holder as CardImageViewHolder

                curholder.name.text = "${curcard.id}. ${curcard.name}"
                curholder.city.text = curcard.city
                Glide.with(curholder.image)
                    .load(curcard.url)
                    .into(curholder.image)
                curholder.itemView.setOnClickListener { v ->
                    Toast.makeText(v.context, curcard.name, Toast.LENGTH_SHORT).show()
                }
            }
            CardType.CARD_TEXT1 -> {
                var curholder = holder as CardText1ViewHolder

                var idx = curcard.coloridx
                var color_main = Color.parseColor(CardType.CARD_COLOR_MAIN[idx])
                var color_sub = Color.parseColor(CardType.CARD_COLOR_SUB[idx])

                curholder.card.setStrokeColor(color_main)
                curholder.header.setBackgroundColor(color_main)
                curholder.content.setBackgroundColor(color_sub)
                curholder.name.setTextColor(color_sub)
                curholder.info1.setTextColor(color_main)
                curholder.info2.setTextColor(color_main)

                curholder.name.text = curcard.name
                curholder.info1.text = curcard.info1
                curholder.info2.text = curcard.info2
            }
        }
    }
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
    }

    override fun getItemViewType(position: Int): Int {
        return cards.get(position).getCardType()
    }

    override fun getItemCount(): Int {
        return cards.size
    }

    fun setCards(cards: List<CardItem>) {
        this.cards = cards
    }

    fun getCards(): List<CardItem> {
        return cards
    }

    class CardImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        var city: TextView = view.findViewById(R.id.item_city)
        var image: ImageView = view.findViewById(R.id.item_image)

    }
    class CardText1ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.item_name)
        var info1: TextView = view.findViewById(R.id.item_info1)
        var info2: TextView = view.findViewById(R.id.item_info2)

        var card: MaterialCardView = view.findViewById(R.id.card_view) as MaterialCardView
        var header: LinearLayout = view.findViewById(R.id.item_title) as LinearLayout
        var content: LinearLayout = view.findViewById(R.id.item_info) as LinearLayout
    }
}
