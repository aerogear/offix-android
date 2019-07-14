package org.aerogear.offixOffline

/*
Class to check whether the input parameter type of the receiver class of mutation matches to the Input<*> class of Apollo.
 */
class InputTypeChecker(string: String) {

    val str = string

    fun inCheck(): Boolean {

        return str.equals("com.apollographql.apollo.api.Input")
    }
}