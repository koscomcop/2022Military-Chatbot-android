package com.example.militaryaibot

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MilDict::class], version = 1)
abstract class MilDictDB: RoomDatabase() {
    abstract fun mildictDao(): MilDictDao

    companion object {
        private var INSTANCE: MilDictDB? = null

        fun getInstance(context: Context): MilDictDB? {
            if (INSTANCE==null) {
                synchronized(MilDictDB::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, MilDictDB::class.java, "military_dict.db")
                        .createFromAsset("military_dict.db")
                        .build()
                }
            }
            return INSTANCE
        }
    }
}