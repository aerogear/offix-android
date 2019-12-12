package org.aerogear.offix

import com.apollographql.apollo.api.OperationName

object ConversionCheck{
    fun checkStringisJson(s: String): Boolean {
        return s.startsWith("{")&&s.endsWith("}")
    }
    fun operationNameExists(n: OperationName): Boolean{
        return !n.name().isBlank()
    }
}

