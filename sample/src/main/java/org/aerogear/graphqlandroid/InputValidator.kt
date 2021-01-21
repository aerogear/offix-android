package org.aerogear.graphqlandroid

import java.util.regex.Pattern

object InputValidator{
     val EMAIL_PATTERN = Pattern.compile(
         "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
         "\\@" +
         "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
         "(" +
         "\\." +
         "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
         ")+"
     )
    fun taskValidation(id: String, title: String, description: String): Boolean{
        return (description.length <= 360
                && id.length!=0
                && title.length<100)
    }
    fun userValidation(idUser: String,
                             taskId: String,
                             title: String,
                             firstName: String,
                             lastName: String,
                             email: String): Boolean{
        return (firstName.length<20
                && lastName.length<20
                && EMAIL_PATTERN.matcher(email).matches()
                && idUser.length!=0
                && taskId.length!=0
                && title.length<100)
    }
}