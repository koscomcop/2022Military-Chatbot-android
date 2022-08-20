package com.example.militaryaibot

import androidx.room.*
import org.jetbrains.annotations.NotNull

@Entity(tableName = "military_dict")
data class MilDict(
    @PrimaryKey
    @ColumnInfo(name = "index") val index: Int,
    @ColumnInfo(name = "word") val word: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "word_eng") val word_eng: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "detail") val detail: String?,
    @ColumnInfo(name = "detail2") val detail2: String?
)

@Dao
interface MilDictDao {

    @Insert
    fun insertMilDict(vararg places: MilDict)

    @Update
    fun updateMilDict(vararg places: MilDict)

    @Delete
    fun deleteMilDict(vararg places: MilDict)

    @Query("SELECT word FROM military_dict")
    fun loadAllWords(): Array<String>

    @Query("SELECT description FROM military_dict WHERE word = :input")
    fun getDescWithWord(input: String): String

    @Query("SELECT word FROM military_dict WHERE word LIKE '%' || :input || '%'")
    fun getDescsWithWord(input: String): Array<String>
}
