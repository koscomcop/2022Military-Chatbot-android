package com.example.militaryaibot

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
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

                curholder.name.text = "${curcard.name}"
                curholder.city.text = curcard.city

                //ADD IMAGE CLICK LISTENER
                curholder.image.setOnClickListener { v ->
                    var browser = Intent(Intent.ACTION_VIEW)
                    browser.setData(Uri.parse(curcard.url))
                    v.context.startActivity(browser)

                }

                //SET IMAGE SOURCE
                Glide.with(curholder.image)
                        .load("http://221.168.37.9:8000/get_image?type=1")
                        .into(curholder.image)

            }
            CardType.CARD_TEXT1 -> {
                var curholder = holder as CardText1ViewHolder

                // RANDOM COLOR
//                var idx = curcard.coloridx
//                var color_main = Color.parseColor(CardType.CARD_COLOR_MAIN[idx])
//                var color_sub = Color.parseColor(CardType.CARD_COLOR_SUB[idx])
//
//                curholder.card.setStrokeColor(color_main)
//                curholder.header.setBackgroundColor(color_main)
//                curholder.content.setBackgroundColor(color_sub)
//                curholder.name.setTextColor(color_sub)
//                curholder.info1.setTextColor(color_main)
//                curholder.info2.setTextColor(color_main)

//                curholder.name.text = curcard.name

                // SET WEATHER INFOS
                var weatherConst = curcard.weatherInfo
                var infos = weatherConst!!.CODES

                if (weatherConst != null) {
                    var skycode = infos["SKY"]
                    when(skycode) {
                        "1" -> curholder.img1.setImageResource(R.drawable.weather1)
                        "3" -> curholder.img1.setImageResource(R.drawable.weather2)
                        "4" -> curholder.img1.setImageResource(R.drawable.weather3)
                    }
                    curholder.sky.setText(weatherConst.sky_detail[skycode])
                    curholder.tmn.setText("${
                        infos["TMN"].toString().split(".")[0]
                    }°C")
                    curholder.tmx.setText("${
                        infos["TMX"].toString().split(".")[0]
                    }°C")
                    curholder.tmp.setText("1시간 기온 ${infos["TMP"]}°C")
                    if(infos["PCP"] != "강수없음") curholder.pcp.setText("1시간 강수량 ${infos["PCP"]}")
                    if(infos["SNO"] != "적설없음") curholder.sno.setText("1시간 신적설 ${infos["SNO"]}")

                    curholder.pty.setText(weatherConst.pty_detail[infos["PTY"]])
                    curholder.pop.setText("${ infos["POP"] }%")

                    curholder.img2.setImageResource(R.drawable.arrow)
                    curholder.img2.rotation = infos["VEC"]?.toFloat()?:0.0f
                    curholder.vec.setText("풍향 ${infos["VEC"]}°")
                    curholder.wsd.setText("풍속 ${infos["WSD"]}m/s")

                    curholder.uuu.setText("풍속(동서) ${infos["UUU"]}m/s")
                    curholder.vvv.setText("풍속(남북) ${infos["VVV"]}m/s")
                    curholder.wav.setText("파고 ${infos["WAV"]}m")
                    curholder.reh.setText("습도 ${infos["REH"]}%")
                }
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

        var card: MaterialCardView = view.findViewById(R.id.card_view) as MaterialCardView
        var header: LinearLayout = view.findViewById(R.id.item_title) as LinearLayout
        var content: LinearLayout = view.findViewById(R.id.item_info) as LinearLayout

        // WEATHER INFOS
        var img1 = view.findViewById<AppCompatImageView>(R.id.weather_img1)
        var img2 = view.findViewById<AppCompatImageView>(R.id.weather_img2)
        var sky = view.findViewById<TextView>(R.id.weather_sky)
        var tmn = view.findViewById<TextView>(R.id.weather_tmn)
        var tmx = view.findViewById<TextView>(R.id.weather_tmx)
        var tmp = view.findViewById<TextView>(R.id.weather_tmp)
        var pcp = view.findViewById<TextView>(R.id.weather_pcp)
        var sno = view.findViewById<TextView>(R.id.weather_sno)
        var pty = view.findViewById<TextView>(R.id.weather_pty)
        var pop = view.findViewById<TextView>(R.id.weather_pop)
        var vec = view.findViewById<TextView>(R.id.weather_vec)
        var wsd = view.findViewById<TextView>(R.id.weather_wsd)
        var uuu = view.findViewById<TextView>(R.id.weather_uuu)
        var vvv = view.findViewById<TextView>(R.id.weather_vvv)
        var wav = view.findViewById<TextView>(R.id.weather_wav)
        var reh = view.findViewById<TextView>(R.id.weather_reh)

    }
}
