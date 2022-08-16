package com.example.militaryaibot

import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.json.JSONObject
import java.io.*
import java.net.HttpRetryException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class ChatRoomActivity : AppCompatActivity() {
    private var CHAT_STORE_PATH: String? = null

    private val objectMapper = ObjectMapper()
    private val fileExt = ".json"

    private var chatAllMsgs: File? = null
    private val chatFavMsgs: File? = null
    private var mChatTitleTxt: TextView? = null
    private var mChatDateTxt: TextView? = null
    private var mChatMsgTxt: EditText? = null
    private var mChatMsgView: RecyclerView? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mChatSendBtn: ImageButton? = null
    private var mChatMsgAdapter: ChatMsgAdapter? = null
    private var mFavChatMsgAdapter: ChatMsgAdapter? = null
    private var toolbar : Toolbar? = null
    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }

    //--CHAT ROOM INFO--
    private val chatRoomTitle = "AI Chatbot"
    private val chatMsgs: MutableList<ChatMessage> = ArrayList<ChatMessage>()
    private val favoritedMsgs: MutableList<ChatMessage> = ArrayList<ChatMessage>()
    private val chatDate = "2021-08-21"
    private val totUserNum = 2
    private var isFavoriteEnabled = false
    private var words: Array<String>? = null

    //--USER INFO--
    private val chatUsers: List<ChatUser> = ArrayList<ChatUser>()
    private var me: ChatUser? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        mChatSendBtn = findViewById<View>(R.id.chatSendButton) as ImageButton
        mChatTitleTxt = findViewById<View>(R.id.chatTitle) as TextView
        mChatDateTxt = findViewById<View>(R.id.chatDate) as TextView
        mChatMsgTxt = findViewById<View>(R.id.chatMessage) as EditText

        //--ADD CUSTOM APP BAR--
        toolbar = findViewById<View>(R.id.main_app_bar) as Toolbar
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowTitleEnabled(false)
        val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)").withLocale(Locale.forLanguageTag("ko")))
        mChatTitleTxt!!.text = chatRoomTitle
        mChatDateTxt!!.text = today

        //--GET DEVICE INFO
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)

        //--ADD CUSTOM CHAT LAYOUT--
        mChatMsgAdapter = ChatMsgAdapter(chatMsgs, (displayMetrics.widthPixels * 0.7).toInt())
        mFavChatMsgAdapter =
            ChatMsgAdapter(favoritedMsgs)
        mChatMsgView = findViewById<View>(R.id.recycleViewMessageList) as RecyclerView
        mSwipeRefreshLayout = findViewById<View>(R.id.message_swipe_layout) as SwipeRefreshLayout
        mLinearLayoutManager = LinearLayoutManager(this@ChatRoomActivity)
        mChatMsgView!!.layoutManager = mLinearLayoutManager
        mChatMsgView!!.adapter = mChatMsgAdapter

        //--SET UP NAVIGATION
        if (words == null) {
            val db: MilDictDao? = MilDictDB.getInstance(this)?.mildictDao()
            setupNavigation(db)
        }

        //--SEND MESSAGE EVENT LISTENER
        mChatSendBtn!!.setOnClickListener(View.OnClickListener {
            val message = mChatMsgTxt!!.text.toString()
            if (message != "") {
                addMessage(message)
                RequestChatRpl().execute(BuildConfig.CHAT_SERVER, message)
                return@OnClickListener
            }
        })

        init()

    }

    fun init() {

        //--LOAD USER INFO--
        me = ChatUser("kyungim", "lee")

        //--LOAD STORED CHAT MESSAGE--
        CHAT_STORE_PATH = this.filesDir.path + "/backup/"
        chatAllMsgs = File(CHAT_STORE_PATH + CHAT_ALL_MESSAGES + fileExt)
        val backupDir = File(CHAT_STORE_PATH)
        if (!backupDir.exists()) backupDir.mkdir()

        //--ADD GREETING MESSAGE
        addReply("Hello world")

    //        loadStoredChat()

    }

    private fun setupNavigation(dbDao: MilDictDao?) {
        // Toolbar
//        val toolbar = findViewById<Toolbar>(com.yuyakaido.android.cardstackview.R.id.toolbar)
//        setSupportActionBar(toolbar)

        // DrawerLayout
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.nav_app_bar_open_drawer_description, R.string.hello_first_fragment)
        actionBarDrawerToggle.syncState()
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        //--LOAD DATABASE
        var r = Runnable {
            words = dbDao?.loadAllWords()
            addWords()
        }
        val thread = Thread(r)
        thread.start()

    }

    //--INIT DICTIONARY
    fun addWords() {
        val dictAdapter:DictAdapter = DictAdapter()

        // ListView
        val drawerContent = findViewById<ListView>(R.id.drawer_content)
        drawerContent.adapter = dictAdapter
        drawerContent.setOnItemClickListener { parent, view, position, id ->
            val itm = parent.getItemAtPosition(position) as DictItem
            if (itm.desc == "") {
                val db: MilDictDao? = MilDictDB.getInstance(this)?.mildictDao()
                var r = Runnable {
                    itm.desc = db?.getDescWithWord(itm.word).toString()
                }
                val thread = Thread(r)
                thread.start()
            }
            else {
                itm.desc = ""
            }
            dictAdapter.notifyDataSetChanged()

        }

        if (words != null) {
            for (w in words!!) {
                dictAdapter.addItem(DictItem(w))
            }
            dictAdapter.notifyDataSetChanged()
        }
    }

    //--ADD CHAT MESSAGE--
    fun addReply(message: String?) {
        val now = nowTime
        val reply = ChatMessage(message, me, now, totUserNum)
        reply.chatType = ChatType.ChatYou
        chatMsgs.add(reply)
        refreshChatView()
    }

    //--ADD CHAT MESSAGE--
    fun addMessage(message: String?) {
        val now = nowTime
        chatMsgs.add(ChatMessage(message, me, now, totUserNum))
        refreshChatView()

        //--CLEAR MESSAGE--
        mChatMsgTxt!!.setText("")
    }

    fun addMessage(message: ChatMessage) {
        chatMsgs.add(message)
        refreshChatView()

        //--CLEAR MESSAGE--
        mChatMsgTxt!!.setText("")
    }

    //--DELETE CHAT MESSAGE--
    fun deleteMessage(pos: Int) {
        val curChat: ChatMessage = chatMsgs[pos]
        chatMsgs.remove(curChat)
        if (curChat.isFavorited()) {
            favoritedMsgs.remove(curChat)
        }
        refreshChatView()
    }

    //--SET FAVORITE / UNFAVORITE--
    fun favoriteMessage(pos: Int, favorite: Boolean) {
        val newMsg: ChatMessage = chatMsgs[pos]
        if (newMsg.setFavorite(favorite)) {
            //--DATA ACTUALLY CHANGED--
            chatMsgs[pos] = newMsg
            favoritedMsgs.add(newMsg)
            Collections.sort(favoritedMsgs)
        }
        refreshChatView()
    }

    fun refreshChatView() {
        mChatMsgAdapter?.notifyDataSetChanged()
        mChatMsgView!!.scrollToPosition(chatMsgs.size - 1)
        mSwipeRefreshLayout!!.isRefreshing = false
    }

    //--GET STORED CHAT MESSAGES--
    fun loadStoredChat() {
        try {
            if (chatAllMsgs!!.exists()) {
                val bufferedReader = BufferedReader(FileReader(chatAllMsgs))
                var line: String? = null
                while (bufferedReader.readLine().also { line = it } != null) {
                    val curChats = objectMapper.readTree(line)
                    for (i in 0 until curChats.size()) {
                        val curChat = curChats[i]
                        val userInfo =
                            curChat["from"]["fullName"].asText().split(" ").toTypedArray()
                        val curUser = ChatUser(userInfo[0], userInfo[1], userInfo[2])
                        val newMsg = ChatMessage(
                            curChat["id"].asText(),
                            curChat["message"].asText(),
                            curUser,
                            curChat["time"].asText(),
                            curChat["seenCount"].asInt(),
                            curChat["favorited"].asBoolean()
                        )
                        if (curChat["favorited"].asBoolean()) favoritedMsgs.add(newMsg)
                        addMessage(newMsg)
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, R.string.err_save_chat, Toast.LENGTH_LONG)
            e.printStackTrace()
        }
        return
    }

    override fun onDestroy() {
        super.onDestroy()
        storeChat()
    }

    //--SAVE CHAT DATA AS JSON FILE--
    fun storeChat() {
        try {
            val out = ByteArrayOutputStream()
            objectMapper.writeValue(out, chatMsgs)
            try {
                val data = out.toByteArray()
                val outputStream = FileOutputStream(chatAllMsgs)
                outputStream.write(data)
                outputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, R.string.err_save_chat, Toast.LENGTH_LONG)
        }
    }

    //--REQUEST RANDOM USER--
    internal inner class RequestChatRpl :
        AsyncTask<String?, Void?, String>() {
        override fun onPostExecute(string: String) {
            super.onPostExecute(string)
            if (string != "") {
                try {
                    val retNode = objectMapper.readTree(string)
                    val response = retNode["outputs"] as ObjectNode
                    addReply(getJsonData(response, "text"))
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
            }
        }

        private fun getJsonData(node: JsonNode, key: String): String {
            return node[key].asText()
        }

        override fun doInBackground(vararg params: String?): String? {
            var conn: HttpURLConnection? = null
            val `is`: InputStream? = null
            val sb = StringBuilder()
            var nRes = -1
            try {
                val url = URL(params[0])
                val msg = params[1]

                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.doOutput = true
                conn.doInput = true

                val wr = OutputStreamWriter(conn.outputStream)
                wr.write("input1=${msg}")
                wr.flush()

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

    //--GET SYSTEM TIME IN yy-mm-dd kk:mm:ss FORMAT--
    val nowTime: String
        get() {
            val simpleDateFormat = SimpleDateFormat("yyyy-mm-dd kk:mm:ss")
            return simpleDateFormat.format(Date(System.currentTimeMillis()))
        }

    //--SINGLE CHAT CONTEXT MENU SELECTED--
    override fun onContextItemSelected(item: MenuItem): Boolean {
        val curPos: Int = mChatMsgAdapter!!.getClickedPos()
        when (item.itemId) {
            R.id.chat_me_delete -> deleteMessage(curPos)
            R.id.chat_me_favorite -> favoriteMessage(curPos, true)
            else -> {
            }
        }
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chatroom_appbar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_favorite -> {
                //--SWAP CHAT VIEW--
                if (!isFavoriteEnabled) mChatMsgView!!.swapAdapter(
                    mFavChatMsgAdapter,
                    false
                ) else mChatMsgView!!.swapAdapter(mChatMsgAdapter, false)
                isFavoriteEnabled = !isFavoriteEnabled
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val CHAT_ALL_MESSAGES = "chat_messages"
    }
}