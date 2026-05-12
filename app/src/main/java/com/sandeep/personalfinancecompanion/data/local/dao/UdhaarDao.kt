package com.sandeep.personalfinancecompanion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarEntryEntity
import com.sandeep.personalfinancecompanion.data.local.entity.UdhaarPersonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UdhaarDao {

    @Query("SELECT * FROM udhaar_people ORDER BY name COLLATE NOCASE ASC")
    fun getPeople(): Flow<List<UdhaarPersonEntity>>

    @Query("SELECT * FROM udhaar_entries ORDER BY date DESC")
    fun getEntries(): Flow<List<UdhaarEntryEntity>>

    @Query("SELECT * FROM udhaar_entries WHERE personId = :personId ORDER BY date DESC")
    fun getEntriesForPerson(personId: String): Flow<List<UdhaarEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPersonOrIgnore(person: UdhaarPersonEntity): Long

    @Update
    suspend fun updatePerson(person: UdhaarPersonEntity)

    @Transaction
    suspend fun upsertPerson(person: UdhaarPersonEntity) {
        val insertedId = insertPersonOrIgnore(person)
        if (insertedId == -1L) {
            updatePerson(person)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: UdhaarEntryEntity)

    @Query("DELETE FROM udhaar_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("DELETE FROM udhaar_people WHERE id = :id")
    suspend fun deletePerson(id: String)
}
