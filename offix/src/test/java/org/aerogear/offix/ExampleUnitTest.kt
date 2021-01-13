package org.aerogear.offix

import org.junit.Assert
import org.junit.Test

import com.apollographql.apollo.api.OperationName
import org.json.JSONObject

import org.aerogear.offix.persistence.Converters
/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
class ExampleUnitTest {

    // Dummy Test
    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2)
    }

    // Check String To Json
    @Test
    fun CheckStringToJson(){
        val testJson:JSONObject = Converters().StringToJson("{'name': 'testName'}")
        Assert.assertEquals("testName",testJson["name"])
    }

    // Check String to Operation Name
    @Test
    fun CheckStringToOperName(){
        val testOperation:OperationName = Converters().StringToOperName("Test")
        Assert.assertEquals("Test",testOperation.name())
    }

    // Check Operation to String
    @Test
    fun CheckOperationToString(){
        val testOperation = OperationName { "Test Operation" }
        val testString:String = Converters().OperationNameToString(testOperation)

        Assert.assertEquals("Test Operation",testString)
    }


}
