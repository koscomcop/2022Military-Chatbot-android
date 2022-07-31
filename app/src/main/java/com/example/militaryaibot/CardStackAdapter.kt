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

                curholder.name.text = "기상정보"
                curholder.info1.text = "<전국 대부분 비 확대, 제주도와 남해안 중심 많은 비>"
                curholder.info2.text = "< 강수 현황과 전망 >\n" +
                        "○ (현황) 현재(07시) 서울을 포함한 전국 대부분 지역에 비가 내리거나 빗방울이 떨어지는 곳이 있으며, 호우특보가 발효중인 제주도산지와 제주도남부에는 천둥.번개를 동반한 시간당 30mm 이상의 매우 강한 비가 내리는 곳이 있습니다.\n" +
                        "\n" +
                        "○ (전망) 서해상으로 이동하는 태풍과 북태평양고기압 사이에서 따뜻한 수증기가 다량 유입되면서 이 비는 전국 대부분 지역에서 모레(8월 2일)까지 이어지겠으나, 내일(8월 1일) 밤(18~24시)부터 모레 아침(06~09시) 사이에는 소강상태를 보이는 곳이 있겠습니다.\n" +
                        "\n" +
                        "- 비가 내리는 동안 돌풍과 함께 천둥.번개가 치는 곳이 있겠고, 제주도는 오늘(31일), 남해안과 지리산 부근은 오늘부터 내일 아침 사이, 중부지방은 오늘 오후부터 내일 아침 사이에 강한 비가 집중되겠습니다.\n" +
                        "\n" +
                        "- 특히, 지형적인 영향을 받는 제주도산지와 남해안, 지리산 부근은 시간당 30~50mm 이상, 경기북부와 경북북부에는 시간당 20~30mm의 매우 강하고 많은 비가 내리는 곳이 있겠으니, 비 피해가 발생하지 않도록 유의하기 바랍니다."
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
