package com.example.militaryaibot

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
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

//                drawerContent?.smoothScrollToPositionFromTop(itmpos, 0, 0)
//                drawerContent?.performItemClick(drawerContent, itmpos, dictAdapter!!.getItemId(itmpos))
//                curpos = itmpos

                //--LOAD DATABASE
                var rets: Array<String>? = null
                var dbDao: MilDictDao? = MilDictDB.getInstance(this)?.mildictDao()
                CoroutineScope(Dispatchers.Main).launch {
                    withContext(Dispatchers.IO) {
                        rets = dbDao?.getDescsWithWord(curtxt)
                    }

                    val dialog: SearchDialog = SearchDialog(rets)
                    val bundle = Bundle()
                    bundle.putString("key", "value")
                    dialog.arguments = bundle
                    dialog.show(supportFragmentManager, "hello")
                }
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
class SearchDialog(rets: Array<String>?) : DialogFragment() {
    private var dictAdapter: DictAdapter? = null
    private var searchContent: ListView? = null
    private var results: Array<String>? = rets
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: LinearLayout = inflater.inflate(R.layout.dict_search_view, container, false) as LinearLayout
        dictAdapter = DictAdapter()

        //어느 다이어로그에서 왔는지
        val bundle = arguments

        val searchTtl: TextView = view.findViewById(R.id.search_ttl)
        val searchView: ListView = view.findViewById(R.id.search_content)
        searchView.adapter = dictAdapter
        searchView?.setOnItemClickListener { parent, view, position, id ->
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
        if (results != null) {
            for (w in results!!) {
                dictAdapter?.addItem(DictItem(w))
            }
            searchTtl.setText("검색결과 총 ${results!!.size}건")
        }
        else {
            searchTtl.setText("검색 결과가 없습니다")
        }

        return view
    }

}