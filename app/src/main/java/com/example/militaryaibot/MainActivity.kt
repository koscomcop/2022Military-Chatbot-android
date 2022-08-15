package com.example.militaryaibot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.yuyakaido.android.cardstackview.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpRetryException
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.net.ssl.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), CardStackListener {

    private var permissionsHelper: PermissionsHelper? = null

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    //--MEMBER VARIABLES
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private var toolbar: Toolbar? = null
//    private var cardcount: TextView? = null

    //--CARD STACK ADAPTER
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter(createCardItems()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)

        //--ADD LISTENERS
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            startActivity(Intent(this, ChatRoomActivity::class.java))
        }

        //--SET UP CARD STACK
        manager.setStackFrom(StackFrom.Top)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }

        //--SET UP NAVIGATION
        val db: MilDictDao? = MilDictDB.getInstance(this)?.mildictDao()
        setupNavigation(db)

        //--GET STORAGE PERMISSION
        permissionsHelper = PermissionsHelper(applicationContext, this)
        RequestRuntimePermissions()
    }

    private fun setupNavigation(dbDao: MilDictDao?) {
        // Toolbar
//        val toolbar = findViewById<Toolbar>(com.yuyakaido.android.cardstackview.R.id.toolbar)
//        setSupportActionBar(toolbar)

        // DrawerLayout
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_app_bar_open_drawer_description, R.string.hello_first_fragment)
        actionBarDrawerToggle.syncState()
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        // ListView
        val drawerContent = findViewById<ListView>(R.id.drawer_content)
        val dictAdapter:DictAdapter = DictAdapter()
        drawerContent.adapter = dictAdapter

        //--LOAD DATABASE
        var r = Runnable {
            val words = dbDao?.loadAllWords()
            if (words != null) {
                for (w in words) {
                    dictAdapter.addItem(DictItem(w))
                }
                dictAdapter.notifyDataSetChanged()
            }
        }
        val thread = Thread(r)
        thread.start()

    }


    // WRITE_EXTERNAL_STORAGE 권한 사용
    private fun RequestRuntimePermissions() {
        class pListener : PermissionsHelper.PermissionHelperListener {
            override fun onPermissionGranted() {
                onPermissionDone()
            }

            override fun onPermissionDenied(deniedPermissions: List<String?>?) {
//				Toast.makeText(super., "파일 권한이 필요합니다", Toast.LENGTH_LONG).show();
            }
        }
        permissionsHelper?.setPermissionListener(pListener())
                ?.setPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ?.checkPermissions()
    }

    fun onPermissionDone() {
        var locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        //--GET CURRENT LOCATION IF NETWORK IS ENBALED
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                var location = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                latitude = location?.latitude
                longitude = location?.longitude

//                with(CoroutineScope(Dispatchers.IO)) {
//                    getWeatherInfo(latitude!!, longitude!!)
//                }
                val ret = RequestWeatherInfo().execute(latitude, longitude)
            }

        }
    }
    //--REQUEST RANDOM USER--
    internal inner class RequestWeatherInfo :
        AsyncTask<Double?, Void?, String>() {

        override fun onPostExecute(string: String) {
            super.onPostExecute(string)
            if (string != "") {
                try {
                    val objectMapper = ObjectMapper()
                    val retNode = objectMapper.readTree(string)

                    if(retNode["response"]["header"]["resultCode"].textValue().equals(WeatherConst().NORMAL_SERVICE)) {
                        val nodes = retNode["response"]["body"]["items"]["item"] as ArrayNode
                        var weatherConst: WeatherConst = WeatherConst()
                        var prettyMessage = ""

                        for (item:JsonNode in nodes) {
                            weatherConst.CODES[getJsonData(item, "category")] = getJsonData(item, "fcstValue")
                            prettyMessage += String.format("[%s] %s\n", getJsonData(item, "category"), getJsonData(item, "fcstValue"))
                        }

                        addLast(1, CardItem(name = "기상정보", weatherInfo = weatherConst, cardtype = CardType.CARD_TEXT1)
                        )
                    }
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
            }
        }

        private fun getJsonData(node: JsonNode, key: String): String {
            return node[key].asText()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun doInBackground(vararg params: Double?): String? {
            var conn: HttpURLConnection? = null
            val `is`: InputStream? = null
            val sb = StringBuilder()
            var nRes = -1
            try {
                val latitude = params[0]?.toInt()
                val longitude = params[1]?.toInt()
                val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                val url = URL(Uri.parse(BuildConfig.WEATHER_API).buildUpon()
                        .appendQueryParameter("serviceKey", BuildConfig.WEATHER_KEY)
                        .appendQueryParameter("pageNo", "1")
                        .appendQueryParameter("numOfRows", "1000")
                        .appendQueryParameter("dataType", "JSON")
                        .appendQueryParameter("base_date", today)
                        .appendQueryParameter("base_time", "0500")
                        .appendQueryParameter("nx", latitude.toString())
                        .appendQueryParameter("ny", longitude.toString())
                        .build().toString())


                trustAllHosts()
                conn = url.openConnection() as HttpURLConnection
                conn.readTimeout = 15000
                conn!!.connectTimeout = 15000
                conn.requestMethod = "GET"


                conn.connect()
                nRes = conn.responseCode
                if (nRes != HttpURLConnection.HTTP_OK) throw HttpRetryException(
                    conn.responseMessage, nRes
                )
                var line: String? = ""
                val br = BufferedReader(
                    InputStreamReader(
                        conn.inputStream
                    )
                )
                while (br.readLine().also { line = it } != null) sb.append(line)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                conn?.disconnect()
                return if (nRes > 0) sb.toString() else ""
            }
        }
    }

    private fun trustAllHosts() {
        val trustAllCerts = arrayOf<TrustManager>(
            object : X509TrustManager {
                override fun getAcceptedIssuers(): Array<X509Certificate?> {
                    return arrayOfNulls(0)
                }

                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                }
            }
        )
        try {
            val e = SSLContext.getInstance("TLS")
            e.init(null as Array<KeyManager?>?, trustAllCerts, SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(e.socketFactory)
        } catch (e: java.lang.Exception) {
            print(e.stackTrace)
        }
    }


    //--CARD EVENTS
    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
//        if (manager.topPosition == adapter.itemCount - 5) {
//            paginate()
//        }
        when(direction) {
            Direction.Right, Direction.Top -> {
                goNextCard()
            }
            Direction.Left, Direction.Bottom -> {
                goNextCard()
//                goPrevCard()
            }
        }
    }

    fun goNextCard() {
        val new = adapter.getCards()
        Collections.rotate(new, -1)
//        val callback = CardDiffCallback(old, new)
//        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)

        //--UPDATE CARD COUNT
//        cardcount?.setText("${new[0].id}/${adapter.itemCount}")

        adapter.notifyDataSetChanged()

    }

    fun goPrevCard() {
        val new = adapter.getCards()
        Collections.rotate(new, 1)
//        val callback = CardDiffCallback(old, new)
//        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)

        //--UPDATE CARD COUNT
//        cardcount?.setText("${adapter.itemCount}/${callback.newListSize}")

        adapter.notifyDataSetChanged()
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    private fun addLast(size: Int, item: CardItem) {
        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            addAll(List(size) { item })
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }


    ////////////////////////////////////// Not Being Used
    //--CARD MODIFICATION
    private fun paginate() {
        val old = adapter.getCards()
        val new = old.plus(createCardItems())
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)

        //--UPDATE CARD COUNT
//        cardcount?.setText("${adapter.itemCount}/${callback.newListSize}")

        result.dispatchUpdatesTo(adapter)
    }

    private fun reload() {
        val old = adapter.getCards()
        val new = createCardItems()
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addFirst(size: Int) {
        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                add(manager.topPosition, createCardItem())
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }


    private fun removeFirst(size: Int) {
        if (adapter.getCards().isEmpty()) {
            return
        }

        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(manager.topPosition)
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeLast(size: Int) {
        if (adapter.getCards().isEmpty()) {
            return
        }

        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(this.size - 1)
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun replace() {
        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            removeAt(manager.topPosition)
            add(manager.topPosition, createCardItem())
        }
        adapter.setCards(new)
        adapter.notifyItemChanged(manager.topPosition)
    }

    private fun swap() {
        val old = adapter.getCards()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            val first = removeAt(manager.topPosition)
            val last = removeAt(this.size - 1)
            add(manager.topPosition, last)
            add(first)
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setCards(new)
        result.dispatchUpdatesTo(adapter)
    }

    //--SAMPLE CARD DATAS
    private fun createCardItem(): CardItem {
        return CardItem(
            name = "Yasaka Shrine",
            city = "Kyoto",
            url = "https://source.unsplash.com/Xq1ntWruZQI/600x800"
        )
    }

    private fun createCardItems(): List<CardItem> {
        val CardItems = ArrayList<CardItem>()
        CardItems.add(CardItem(name = "오늘의 주요 토픽", city = "국방일보", url = "https://www.gukbangnews.com/", cardtype = CardType.CARD_IMAGE_FULL))
//        CardItems.add(CardItem(name = "Yasaka Shrine", city = "Kyoto", url = "https://source.unsplash.com/Xq1ntWruZQI/600x800"))
//        CardItems.add(CardItem(name = "Fushimi Inari Shrine", city = "Kyoto", url = "https://source.unsplash.com/NYyCqdBOKwc/600x800"))
//        CardItems.add(CardItem(name = "Bamboo Forest", city = "Kyoto", url = "https://source.unsplash.com/buF62ewDLcQ/600x800"))
//        CardItems.add(CardItem(name = "Brooklyn Bridge", city = "New York", url = "https://source.unsplash.com/THozNzxEP3g/600x800"))
//        CardItems.add(CardItem(name = "Empire State Building", city = "New York", url = "https://source.unsplash.com/USrZRcRS2Lw/600x800"))
//        CardItems.add(CardItem(name = "The statue of Liberty", city = "New York", url = "https://source.unsplash.com/PeFk7fzxTdk/600x800"))
//        CardItems.add(CardItem(name = "Louvre Museum", city = "Paris", url = "https://source.unsplash.com/LrMWHKqilUw/600x800"))
//        CardItems.add(CardItem(name = "Eiffel Tower", city = "Paris", url = "https://source.unsplash.com/HN-5Z6AmxrM/600x800"))
//        CardItems.add(CardItem(name = "Big Ben", city = "London", url = "https://source.unsplash.com/CdVAUADdqEc/600x800"))
//        CardItems.add(CardItem(name = "Great Wall of China", city = "China", url = "https://source.unsplash.com/AWh9C-QjhE4/600x800"))
        return CardItems
    }
    fun getWeatherInfo(latitude: Double, longitude: Double) : Flow<String> {

        return callbackFlow {
            val `is`: InputStream? = null
            var conn: HttpURLConnection? = null
            val sb = StringBuilder()
            var nRes = -1
//            val url = URL(BuildConfig.WEATHER_API)
            val url = URL("https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst?serviceKey=yb1vVjpJ7ztUOk%2F1ze%2FsH9%2BtMLx1zw8M7FMGix0pTlVJFArMv2bpBnVB%2Fwe8QX5JuJmUO%2BAxS1zDoM9u5%2BmxTw%3D%3D&pageNo=1&numOfRows=1000&dataType=JSON&base_date=20220803&base_time=0600&nx=55&ny=127")

            conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = 5000
            conn!!.connectTimeout = 5000
            conn.requestMethod = "GET"
            conn.connect()
            nRes = conn.responseCode
            if (nRes != HttpURLConnection.HTTP_OK) throw HttpRetryException(
                    conn.responseMessage, nRes
            )
            var line: String? = ""
            val br = BufferedReader(
                    InputStreamReader(
                            conn.inputStream
                    )
            )
            while (br.readLine().also { line = it } != null) sb.append(line)

            try {
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (`is` != null) {
                    try {
                        `is`.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
                conn?.disconnect()

                var ret = if (nRes > 0) sb.toString() else ""
                Snackbar.make(findViewById(R.id.activity_main), "Weather Info\n\n${ret}", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show()
            }
        }
    }
}