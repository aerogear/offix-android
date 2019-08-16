package org.aerogear.graphqlandroid

import android.content.Context
import org.aerogear.offix.interfaces.ConflictResolutionInterface

/*
UserConflictResolutionHandler extends ConflictResolutionInterface.
Here the user provides the custom implementation of resolving conflicts.
 */
class UserConflictResolutionHandler(val context: Context) : ConflictResolutionInterface {
    val TAG = javaClass.simpleName

    /*
    Function which resolve the conflicts based on the user business logic.
     */
    override fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    ) {
    }
}