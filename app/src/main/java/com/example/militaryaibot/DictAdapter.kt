package com.example.militaryaibot

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.view.isVisible
import java.util.*
import kotlin.collections.ArrayList

class DictAdapter: BaseAdapter() {
    private val words: MutableList<DictItem> = ArrayList<DictItem>()

    override fun getCount(): Int {
        return words.size
    }

    override fun getItem(position: Int): DictItem {
        return words.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun getItemPos(itm: DictItem): Int {
        return words.indexOf(itm)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val context: Context? = parent?.context
        val view: View = LayoutInflater.from(context).inflate(R.layout.list_item1, parent, false)

        val word: TextView? = view?.findViewById(R.id.word)
        val description: TextView? = view?.findViewById(R.id.description)

        val curword = words.get(position)
        word?.setText(curword.word)
        if (curword.desc != "") {
            description?.setText(curword.desc)
            description?.visibility = View.VISIBLE
        }
        else {
            description?.visibility = View.GONE
        }

        return view
    }

    fun addItem(item: DictItem) {
        words.add(item)
    }
}
