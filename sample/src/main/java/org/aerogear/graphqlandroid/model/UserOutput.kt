package org.aerogear.graphqlandroid.model

data class UserOutput(
    val title: String,
    val desc: String,
    val taskId: Int,
    val firstName: String,
    val lastName: String,
    val userId: String,
    val email: String
)