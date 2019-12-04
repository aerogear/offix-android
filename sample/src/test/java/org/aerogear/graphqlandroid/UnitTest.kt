package org.aerogear.graphqlandroid

import org.aerogear.offix.getDao
import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class UnitTest {
    @Test
    fun isCorrect() {
        assertEquals(4, 2 + 2)
        assertEquals(null, getDao()?.getAllMutations()?.size)
        assertEquals("http://10.0.2.2:4000/graphql", Utils.BASE_URL)
    }
}
