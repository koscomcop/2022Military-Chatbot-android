package com.example.militaryaibot

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.example.militaryaibot.databinding.ActivityDictBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DictActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDictBinding

    private var dictAdapter: DictAdapter? = null
    private var searchView: EditText? = null
    private var drawerContent: ListView? = null

    private var words: Array<String>? = null
    private var curpos: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDictBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var dbDao: MilDictDao? = MilDictDB.getInstance(this)?.mildictDao()

        searchView = findViewById(R.id.search_word)
        drawerContent = findViewById<ListView>(R.id.drawer_content)
        dictAdapter = DictAdapter()

        // Main Content ListView
        drawerContent?.adapter = dictAdapter
        drawerContent?.setOnItemClickListener { parent, view, position, id ->
            CoroutineScope(Dispatchers.Main).launch {
                val itm = parent.getItemAtPosition(position) as DictItem?
                if (itm?.desc == "") {
                    val db: MilDictDao? = MilDictDB.getInstance(parent.context)?.mildictDao()
                    withContext(Dispatchers.IO) {
                        itm.desc = db?.getDescWithWord(itm.word).toString()
                    }
                }
                else {
                    itm?.desc = ""
                }
                dictAdapter?.notifyDataSetChanged()
            }
        }

        //--LOAD DATABASE
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                words = dbDao?.loadAllWords()
            }

            if (words != null) {
                for (w in words!!) {
                    dictAdapter?.addItem(DictItem(w))
                }
            }
            dictAdapter?.notifyDataSetChanged()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_ENTER) {
            val curtxt = searchView?.text?.trim().toString()
            val itmpos: Int? = words?.indexOf(curtxt)
            searchView?.setText("")

            if (itmpos != null && curtxt != "") {
//                drawerContent?.smoothScrollToPosition(curpos)
//                drawerContent?.performItemClick(drawerContent, curpos, dictAdapter!!.getItemId(curpos))
                drawerContent?.smoothScrollToPositionFromTop(itmpos, 0, 100)
                drawerContent?.performItemClick(drawerContent, itmpos, dictAdapter!!.getItemId(itmpos))
                curpos = itmpos
            }
            return true
        }
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
            return true
        }
        return false
    }
}