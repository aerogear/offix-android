package org.aerogear.graphqlandroid

import org.aerogear.graphqlandroid.InputValidator.taskValidation
import org.aerogear.graphqlandroid.InputValidator.userValidation
import org.aerogear.offix.getDao
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(MockitoJUnitRunner::class)
class UnitTest {
    @Test
    fun isCorrect() {
        assertEquals(4, 2 + 2)
        assertEquals(null, getDao()?.getAllMutations()?.size)
        assertEquals("http://10.0.2.2:4000/graphql", Utils.BASE_URL)
    }

    @Test
    fun taskValidator_ReturnsTrue() {
        assertTrue(taskValidation("0", "title", "desc"))
    }
    @Test
    fun taskValidator_ReturnsFalse() {
        assertFalse(taskValidation("", "title", "desc"))
    }
    @Test
    fun userValidator_ReturnsTrue() {
        assertTrue(userValidation("0", "00", "title", "fn", "ln", "name@example.com"))
    }
    @Test
    fun userValidator_ReturnsFalse() {
        assertFalse(userValidation("0", "00", "title", "fn1", "ln2", "name@example..com"))
    }

}
