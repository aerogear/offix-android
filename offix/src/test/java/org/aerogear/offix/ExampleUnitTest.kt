package org.aerogear.offix

import com.apollographql.apollo.api.OperationName
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }
    @Test
    fun stringJsonTrue(){
        assertTrue(ConversionCheck.checkStringisJson("{}"))
    }
    @Test
    fun stringJsonFalse(){
        assertFalse(ConversionCheck.checkStringisJson("sd"))
    }
    @Test
    fun operationNameTrue(){
        assertTrue(ConversionCheck.operationNameExists(OperationName { "testUser" }))
    }
    @Test
    fun operationNameFalse(){
        assertFalse(ConversionCheck.operationNameExists(OperationName { "" }))
    }
} 