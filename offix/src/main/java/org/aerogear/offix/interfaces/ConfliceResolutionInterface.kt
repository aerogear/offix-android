package org.aerogear.offix.interfaces

/*
 Users can implement ConfliceResolutionInterface interface in their app and provide a custom conflict resolution implementation.
 */
interface ConfliceResolutionInterface {
    fun resolveConflict(
        serverState: Map<String, Any>,
        clientState: Map<String, Any>,
        operationType: String
    )
}
