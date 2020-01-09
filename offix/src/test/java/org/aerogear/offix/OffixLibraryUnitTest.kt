package org.aerogear.offix

import com.apollographql.apollo.api.OperationName
import com.google.gson.JsonParser
import org.json.JSONObject
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


/**
 * This Offix Library unit test, will execute validate offix mutation data values for null and blank.
 *
 */
class OffixLibraryUnitTest {
    @Test
    fun validateOperationIDisTrue(){
        assertTrue(MutationDataCheck.validateOperationID("2"))
    }
    @Test
    fun validateOperationIDisFalse(){
        //String xx = null
        assertFalse(MutationDataCheck.validateOperationID(""))
    }
    @Test
    fun validateOperationNameisTrue(){
        assertTrue(MutationDataCheck.validateOperationName(OperationName { "testoperation" }))
    }
    @Test
    fun validateOperationNameisFalse(){
        assertFalse(MutationDataCheck.validateOperationName(OperationName { "" }))
    }
    @Test
    fun validateOperationDocisTrue(){
        assertTrue(MutationDataCheck.validateOperationDoc("doc value"))
    }
    @Test
    fun validateOperationDocisFalse(){
        assertFalse(MutationDataCheck.validateOperationDoc(""))
    }
    @Test
    fun validateJSONObjectisTrue(){
        val jo = JSONObject("""{"name":"test name", "age":25}""")
        assertTrue(MutationDataCheck.validateJSONObject(jo))
    }
} 