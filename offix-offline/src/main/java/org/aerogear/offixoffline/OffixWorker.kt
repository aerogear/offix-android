package org.aerogear.offixoffline

import android.arch.persistence.room.Room
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.aerogear.offixoffline.persistence.Database

/**
 OffixWorker is an abstract class.
 If the user wants to use the Offline Service provided by this library, then he/she should create a worker class
 in the application and that class should extend this class.
 */

abstract class OffixWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {

    /*  Initialised database object. Get access to the database store.
    */
    private val libDb by lazy {
        Room.databaseBuilder(
            context,
            Database::class.java, "offline_db"
        )
            .fallbackToDestructiveMigration()
            .allowMainThreadQueries()
            .build()
    }

    /* Get the list of mutations from the database.
       @return List<(org.aerogear.offixoffline.persistence)Mutation>
     */
    fun getListOfMutations() = libDb.mutationDao().getAllMutations()

    /* Delete the mutation from the database.
      @param mutationNumber, serial number of the mutation to be deleted.
     */
    fun deleteAMutation(mutationNumber: Int) = libDb.mutationDao().deleteCurrentMutation(mutationNumber)
}
