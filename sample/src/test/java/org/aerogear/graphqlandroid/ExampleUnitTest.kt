package org.aerogear.graphqlandroid

import com.apollographql.apollo.api.OperationName
import org.aerogear.graphqlandroid.model.NamePair
import org.aerogear.graphqlandroid.model.UserOutput
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun baseURLEquals(){
        assertEquals("http://10.0.2.2:4000/graphql", Utils.BASE_URL)
    }

    @Test
    fun namePairCorrect(){
        val pair = NamePair("bagger", "Tea")
        assertEquals("bagger", pair.fName)
        assertEquals("Tea", pair.lName)
    }

    @Test
    fun userOutputCorrect(){
        val output = UserOutput(
            "Test",
            "By the order of explosive potato",
            0,
            "bagger",
            "Tea",
            "Teabagger12345",
            "explosivepotatoexploded@gmail.com"
        )
        with(output) {
            assertEquals("Test", title)
            assertEquals("By the order of explosive potato", desc)
            assertEquals(0, id)
            assertEquals("bagger", firstName)
            assertEquals("Tea", lastName)
            assertEquals("Teabagger12345", userId)
            assertEquals("explosivepotatoexploded@gmail.com", email)
        }
    }

    @Test
    fun operationNameTest(){
        assertTrue(OperationName { "Teabagger" }.name().equals("Teabagger"))
        assertFalse(OperationName { "Teabagger" }.name().equals("Potato"))

        assertFalse(OperationName { "Teabagger" }.name().isBlank())
    }

}
