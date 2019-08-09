package org.aerogear.offix.interfaces

/*
 Users can implement ConflictResolutionImpl interface in their app and provide a custom conflict resolution implementation.
 */
interface ConflictResolutionImpl {
    fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    )
}
