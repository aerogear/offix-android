package org.aerogear.offixsdk.persistence

import android.arch.persistence.room.*

@Dao
interface UrlDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUrl(url: Url): Long

    @Delete
    fun deleteUrl(url: Url)

    @Query("SELECT * FROM MutationUrl")
    fun getAllMutationsUrl(): List<Url>

    @Query("SELECT * FROM MutationUrl WHERE SNo =:sno")
    fun getAMutationUrl(sno: Int): Url


    @Query("DELETE FROM MutationUrl WHERE SNo= :sno ")
    fun deleteCurrentMutationUrl(sno: Int)

    @Query("DELETE FROM MutationUrl")
    fun deleteAllMutationsUrl()
}

