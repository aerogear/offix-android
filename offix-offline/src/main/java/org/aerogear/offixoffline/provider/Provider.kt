package org.aerogear.offixoffline.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import org.aerogear.offixoffline.Offline

/**
 * This content provider is needed as it's automatically invoked when the app which uses the library is started.
 * Having this content provider injected in the app's manifest allows us to skip calling the start() method with the
 * library.
 */
internal class Provider : ContentProvider() {

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun onCreate(): Boolean {
/*
  This initialises the object of Offline class and also the database object whenever the application using our library is created.
  Also we get the context after the Offline object is initialised in the provider's onCreate() method.
 */
        context?.let {
            Offline.with(it).start()
            return true
        }
        return false
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

}