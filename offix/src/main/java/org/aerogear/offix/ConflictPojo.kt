package org.aerogear.offix

/* This class is the one to which the json response which comes from the server is matched when a conflict happens.
 */
class ConflictPojo(
    val errors: ArrayList<Errors>
)

class Errors(
    val extensions: Extensions
)

class Extensions(
    val exception: ExceptionClass
)

class ExceptionClass(
    val conflictInfo: ConflictData
)

class ConflictData(
    val serverState: ServerState
)

class ServerState(
    val title: String,
    val description: String,
    val version: Int,
    val id: String,
    val status: String
)
