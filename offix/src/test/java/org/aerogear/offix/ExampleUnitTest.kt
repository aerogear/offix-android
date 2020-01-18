package org.aerogear.offix

import org.junit.Assert
import org.junit.Test

import com.apollographql.apollo.api.OperationName
import org.json.JSONObject

import org.aerogear.offix.persistence.Converters

import com.google.gson.JsonParser
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }

    @Test
    fun CheckStringToJson(){
        val name = Converters().StringToJson("{\"name\":\"sayam\"}")
        print(name.toString())
    }

    @Test
    fun CheckStringToOperName(){
        val testOperation = Converters().StringToOperName("Test")
        Assert.assertEquals("Test",testOperation.name())
    }

    @Test
    fun CheckOperationToString(){
        val testOperation = OperationName { "Test Operation" }
        val testString = Converters().OperationNameToString(testOperation)

        Assert.assertEquals("Test Operation",testString)
    }


}
