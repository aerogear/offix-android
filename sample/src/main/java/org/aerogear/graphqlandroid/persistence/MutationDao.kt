package org.aerogear.graphqlandroid.persistence

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query

@Dao
interface MutationDao {

    //    (onConflict = OnConflictStrategy.REPLACE)
    @Insert
    fun insertMutation(mutation: Mutation): Long

    @Delete
    fun deleteMutation(mutation: Mutation)

    @Query("SELECT * FROM MutationOffline")
    fun getAllMutations(): List<Mutation>


    @Query("DELETE FROM MutationOffline WHERE SNo= :sno ")
    fun deleteCurrentMutation(sno: Int): Mutation

    @Query("DELETE FROM MutationOffline")
    fun deleteAllMutations()
}