package org.aerogear.offix.worker

import android.arch.persistence.room.Room
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.aerogear.offix.persistence.Database
import org.aerogear.offix.persistence.Mutation

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
       @param mutation to be deleted.
     */
    fun deleteMutation(mutation: Mutation) = libDb.mutationDao().deleteMutation(mutation)
}
