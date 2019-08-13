package org.aerogear.graphqlandroid.model

/*
As a user this is my mutation class to which the serverstate is to be matched
 */
class ServerstateClass(
    val title: String,
    val description: String,
    val version: Int,
    val id: String,
    val status: String
)
