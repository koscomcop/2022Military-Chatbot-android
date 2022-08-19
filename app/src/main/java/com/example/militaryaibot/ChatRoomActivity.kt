package com.example.militaryaibot

import android.content.Intent
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private var progressDialog: ProgressDialog? = null

    private var chatAllMsgs: File? = null
    private val chatFavMsgs: File? = null
    private var mChatTitleTxt: TextView? = null
    private var mChatDateTxt: TextView? = null
    private var mChatMsgTxt: EditText? = null
    private var mChatMsgView: RecyclerView? = null
//    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var mChatSendBtn: ImageButton? = null
    private var mChatMsgAdapter: ChatMsgAdapter? = null
    private var mFavChatMsgAdapter: ChatMsgAdapter? = null
    private var toolbar : Toolbar? = null

    //--CHAT ROOM INFO--
    private val chatRoomTitle = "Hello ArBot"
    private val chatMsgs: MutableList<ChatMessage> = ArrayList<ChatMessage>()
    private val favoritedMsgs: MutableList<ChatMessage> = ArrayList<ChatMessage>()
    private val chatDate = "2021-08-21"
    private val totUserNum = 2
    private var isFavoriteEnabled = false

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
        progressDialog = ProgressDialog(this)

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
//        mSwipeRefreshLayout = findViewById<View>(R.id.message_swipe_layout) as SwipeRefreshLayout
        mLinearLayoutManager = LinearLayoutManager(this@ChatRoomActivity)
        mChatMsgView!!.layoutManager = mLinearLayoutManager
        mChatMsgView!!.adapter = mChatMsgAdapter

        //--SEND MESSAGE EVENT LISTENER
        mChatSendBtn!!.setOnClickListener(View.OnClickListener {
            val message = mChatMsgTxt!!.text.toString()
            if (message != "") {
                addMessage(message)
                if (message in arrayListOf<String>("식단", "도서", "주거", "복지", "급여", "정원")) addInfoReply(message)
                else RequestChatRpl().execute(BuildConfig.CHAT_SERVER, message)

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
        addReply(resources.getString(R.string.greeting_msg))
        addReply(resources.getString(R.string.greeting_msg2))
        addReply(resources.getString(R.string.greeting_msg3))
        addReply(resources.getString(R.string.greeting_msg4))

    //        loadStoredChat()

    }

    //--ADD CHAT MESSAGE--
    fun addReply(message: String?) {
        val now = nowTime
        val reply = ChatMessage(message, me, now, totUserNum)
        reply.chatType = ChatType.ChatYou
        chatMsgs.add(reply)
        refreshChatView()
    }

    //--ADD INFORMATION REPLY MESSAGE--
    fun addInfoReply(key: String) {
        val now = nowTime
        var desc = ""
        var sample = ""
        var keywords: MutableList<String> = mutableListOf()

        when (key) {
            "식단" -> {
                desc = "부대별 식단정보"
                keywords = mutableListOf("메뉴", "식단", "부대")
                sample = "7321부대 점심메뉴 뭐야?"
            }
            "도서" -> {
                desc = "군도서관 소장 도서/간행물 정보"
                keywords = mutableListOf("저자", "도서")
                sample = "저자 허지웅인 책 찾아줘"
            }
            "주거" -> {
                desc = "군/지역별 군사주택(관사) 정보"
                keywords = mutableListOf("관사")
                sample = "과천 근처 해군이 관리하는 관사 있어?"
            }
            "복지" -> {
                desc = "장병 복지/할인혜택 숙박정보"
                keywords = mutableListOf("혜택", "놀데")
                sample = "군인도 소노캄 혜택 있어?"
            }
            "급여" -> {
                desc = "직급/호봉별 급여정보"
                keywords = mutableListOf("급여", "월급")
                sample = "공군 소령 3호봉 급여 궁금해"
            }
            "정원" -> {
                desc = "군/직급별 인원 정보"
                sample = "육군 부사관은 총 몇명이야?"
            }
        }

        val msg = "[${key}] : ${desc}\n(키워드: ${keywords.joinToString(", ")})\n\n질문예시: ${sample}"
        val reply = ChatMessage(msg, me, now, totUserNum)
        reply.chatType = ChatType.ChatYou
        chatMsgs.add(reply)
        refreshChatView()
    }

    //--ADD ERROR CHAT MESSAGE--
    fun addErrReply(code: String) {
        val now = nowTime
        val errmsg = "죄송해요 답변을 찾지 못했어요(${code})"
        val reply = ChatMessage(errmsg, me, now, totUserNum)
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
//        mSwipeRefreshLayout!!.isRefreshing = false
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
        override fun onPreExecute() {
            progressDialog?.show()
        }

        override fun onPostExecute(string: String) {
            super.onPostExecute(string)
            val ret = string.toIntOrNull()
            if (string != "" && ret == null) {
                try {
                    val retNode = objectMapper.readTree(string)
                    val response = retNode["outputs"] as ObjectNode
                    addReply(getJsonData(response, "text"))
                } catch (e: JsonProcessingException) {
                    e.printStackTrace()
                }
            }
            else {
                addErrReply(string)
            }
            progressDialog?.dismiss()
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
                conn.connectTimeout = 15000
                conn.readTimeout = 15000
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
                return if (nRes == HttpURLConnection.HTTP_OK) sb.toString() else nRes.toString()
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
            R.id.action_search -> {
                //--SHOW DICT VIEW--
                startActivity(Intent(this, DictActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val CHAT_ALL_MESSAGES = "chat_messages"
    }
}