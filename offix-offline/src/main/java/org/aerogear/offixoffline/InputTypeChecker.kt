package org.aerogear.offixoffline

/*
Class to check whether the input parameter type of the receiver class of mutation matches to the Input<*> class of Apollo.
 */
class InputTypeChecker(val string: String) {

    /*
     To check is the string provided in the constructor of InputTypeChecker class matches apollo Input class or not.
     */
    fun inCheck(): Boolean = string.equals("com.apollographql.apollo.api.Input")

}