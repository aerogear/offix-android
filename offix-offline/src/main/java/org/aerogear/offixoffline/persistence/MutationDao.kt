package org.aerogear.offixoffline.persistence

import android.arch.persistence.room.*

@Dao
interface MutationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMutation(mutation: Mutation): Long

    @Delete
    fun deleteMutation(mutation: Mutation)

    @Query("SELECT * FROM MutationOffline")
    fun getAllMutations(): List<Mutation>

    @Query("SELECT * FROM MutationOffline WHERE sNo =:sno")
    fun getAMutation(sno: Int): Mutation


    @Query("DELETE FROM MutationOffline WHERE sNo= :sno ")
    fun deleteCurrentMutation(sno: Int)

    @Query("DELETE FROM MutationOffline")
    fun deleteAllMutations()
}