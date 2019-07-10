package org.aerogear.offixsdk.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import org.aerogear.offixsdk.isNetwork

internal class Provider : ContentProvider() {

    private val TAG: String = javaClass.simpleName

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun onCreate(): Boolean {
        Log.e(TAG, " NetStatus in Provider: ${isNetwork(context)}")
        return isNetwork(context)
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int = 0

    override fun getType(uri: Uri): String? = null

}