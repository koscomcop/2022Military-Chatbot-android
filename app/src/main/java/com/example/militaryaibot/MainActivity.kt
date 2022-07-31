package com.example.militaryaibot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.yuyakaido.android.cardstackview.*
import java.util.*


class MainActivity : AppCompatActivity(), CardStackListener {

    private var permissionsHelper: PermissionsHelper? = null

    private var latitude: Double? = 0.0
    private var longitude: Double? = 0.0

    //--MEMBER VARIABLES
    private var toolbar: Toolbar? = null
    private var cardcount: TextView? = null

    //--CARD STACK ADAPTER
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter(createCardItems()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        cardcount = findViewById(R.id.card_count)

        setSupportActionBar(toolbar)
        cardcount?.setText("0")

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

        //--GET STORAGE PERMISSION
        permissionsHelper = PermissionsHelper(applicationContext, this)
        RequestRuntimePermissions()
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

//            Snackbar.make(findViewById(R.id.activity_main), "latitude: ${latitude} longtitude: ${longitude}", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
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
        val new = adapter.getSpots()
        Collections.rotate(new, -1)
//        val callback = CardDiffCallback(old, new)
//        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)

        //--UPDATE CARD COUNT
        cardcount?.setText("${new[0].id}/${adapter.itemCount}")

        adapter.notifyDataSetChanged()

    }

    fun goPrevCard() {
        val new = adapter.getSpots()
        Collections.rotate(new, 1)
//        val callback = CardDiffCallback(old, new)
//        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)

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

    //--CARD MODIFICATION
    private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createCardItems())
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)

        //--UPDATE CARD COUNT
        cardcount?.setText("${adapter.itemCount}/${callback.newListSize}")

        result.dispatchUpdatesTo(adapter)
    }

    private fun reload() {
        val old = adapter.getSpots()
        val new = createCardItems()
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addFirst(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                add(manager.topPosition, createCardItem())
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addLast(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            addAll(List(size) { createCardItem() })
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeFirst(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(manager.topPosition)
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeLast(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(this.size - 1)
            }
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun replace() {
        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            removeAt(manager.topPosition)
            add(manager.topPosition, createCardItem())
        }
        adapter.setSpots(new)
        adapter.notifyItemChanged(manager.topPosition)
    }

    private fun swap() {
        val old = adapter.getSpots()
        val new = mutableListOf<CardItem>().apply {
            addAll(old)
            val first = removeAt(manager.topPosition)
            val last = removeAt(this.size - 1)
            add(manager.topPosition, last)
            add(first)
        }
        val callback = CardDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
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
        CardItems.add(CardItem(name = "Yasaka Shrine", city = "Kyoto", url = "https://source.unsplash.com/Xq1ntWruZQI/600x800"))
        CardItems.add(CardItem(name = "Fushimi Inari Shrine", city = "Kyoto", url = "https://source.unsplash.com/NYyCqdBOKwc/600x800"))
        CardItems.add(CardItem(name = "Bamboo Forest", city = "Kyoto", url = "https://source.unsplash.com/buF62ewDLcQ/600x800"))
        CardItems.add(CardItem(name = "Brooklyn Bridge", city = "New York", url = "https://source.unsplash.com/THozNzxEP3g/600x800"))
        CardItems.add(CardItem(name = "Empire State Building", city = "New York", url = "https://source.unsplash.com/USrZRcRS2Lw/600x800"))
        CardItems.add(CardItem(name = "The statue of Liberty", city = "New York", url = "https://source.unsplash.com/PeFk7fzxTdk/600x800"))
        CardItems.add(CardItem(name = "Louvre Museum", city = "Paris", url = "https://source.unsplash.com/LrMWHKqilUw/600x800"))
        CardItems.add(CardItem(name = "Eiffel Tower", city = "Paris", url = "https://source.unsplash.com/HN-5Z6AmxrM/600x800"))
        CardItems.add(CardItem(name = "Big Ben", city = "London", url = "https://source.unsplash.com/CdVAUADdqEc/600x800"))
        CardItems.add(CardItem(name = "Great Wall of China", city = "China", url = "https://source.unsplash.com/AWh9C-QjhE4/600x800"))
        return CardItems
    }

}