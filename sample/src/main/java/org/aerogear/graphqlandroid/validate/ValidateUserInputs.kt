package org.aerogear.graphqlandroid.validate

import java.util.regex.Pattern


object ValidateUserInputs {
	fun checkUserTitle(x1: String): String{
		if(validateUserTitle(x1)){
			return x1
		}
		throw Exception("Exception: Null Title")
	}
	fun validateUserTitle(x1: String): Boolean{
    	if (x1.isBlank()){
    		return false
    	}
    	return true
    }
	fun checkUserFirstName(x2: String): String{
		if(validateUserFirstName(x2)){
			return x2
		}
		throw Exception("Exception: Null FirstName")
	}
	fun validateUserFirstName(x2: String): Boolean{
    	if (x2.isNullOrBlank()){
    		return false
    	}
    	return true
    }
	fun checkUserLastName(x3: String): String{
		if(validateUserLastName(x3)){
			return x3
		}
		throw Exception("Exception: Null LastName")
	}
	fun validateUserLastName(x3: String): Boolean{
    	if (x3.isNullOrBlank()){
    		return false
    	}
    	return true
    }
	fun checkUserEmail(x4: String): String{
		if(validateUserEmail(x4)){
			return x4
		}
		throw Exception("Exception: Invalid Email")
	}
	fun validateUserEmail(x4: String): Boolean{
		val EMAIL_PATTERN = Pattern.compile(
			"[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
					"\\@" +
					"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
					"(" +
					"\\." +
					"[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
					")+"
		)
		return EMAIL_PATTERN.matcher(x4).matches()
    }
}