package org.aerogear.offix.interfaces

/*
 Users can implement ConflictResolutionInterface interface in their app and provide a custom conflict resolution implementation.
 */
interface ConflictResolutionInterface {
    fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    )
}
