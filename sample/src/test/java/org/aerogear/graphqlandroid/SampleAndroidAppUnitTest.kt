package org.aerogear.graphqlandroid

import org.junit.Test
import org.junit.Assert.*
import org.aerogear.graphqlandroid.validate.ValidateUserInputs

class SampleAndroidAppUnitTest {
    @Test
    fun validateUserTitleisTrue(){
    	assertTrue(ValidateUserInputs.validateUserTitle("test title"))
    }
    @Test
    fun validateUserTitleisfalse(){
    	assertFalse(ValidateUserInputs.validateUserTitle(""))
    }
    @Test
    fun validateUserFirstNameisTrue(){
        assertTrue(ValidateUserInputs.validateUserFirstName("test first name"))
    }
    @Test
    fun validateUserFirstNameisfalse(){
        assertFalse(ValidateUserInputs.validateUserFirstName(""))
    }
    @Test
    fun validateUserLastNameisTrue(){
        assertTrue(ValidateUserInputs.validateUserLastName("test last name"))
    }
    @Test
    fun validateUserLastNameisfalse(){
        assertFalse(ValidateUserInputs.validateUserLastName(""))
    }
    @Test
    fun validateUserEmailisTrue(){
        assertTrue(ValidateUserInputs.validateUserEmail("test@test.com"))
    }
    @Test
    fun validateUserEmailisfalse(){
        assertFalse(ValidateUserInputs.validateUserEmail("test@test"))
    }
}
