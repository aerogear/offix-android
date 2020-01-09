package org.aerogear.offix

import org.json.JSONObject
import com.apollographql.apollo.api.OperationName

object MutationDataCheck{
	fun checkOperationID(x1: String): String{
		if(validateOperationID(x1)){
			return x1
		}
		throw Exception("Exception: NullOperationID")
	}
	fun checkOperationName(x2: OperationName): OperationName{
		if(validateOperationName(x2)){
			return x2
		}
		throw Exception("Exception: EmptyOperationName")
	}
	fun checkOperationDoc(x3: String): String{
		if(validateOperationDoc(x3)){
			return x3
		}
		throw Exception("Exception: NullOperationDoc")
	}
	fun checkJSONObject(x4: JSONObject): JSONObject{
		if(validateJSONObject(x4)){
			return x4
		}
		throw Exception("Exception: NullJSONObject")
	}
	fun validateOperationID(x1: String): Boolean{
    	if (x1.isNullOrBlank()){
    		return false//Exception("Exception: NullOperationID")
    	}
    	return true
    }
    fun validateOperationName(x2: OperationName): Boolean{
    	if (x2.name().isBlank()){
			return false//throw Exception("Exception: EmptyOperationName")
    	}
    	return true
    }
    fun validateOperationDoc(x3: String): Boolean{
    	if (x3.isNullOrBlank()){
			return false//throw Exception("Exception: NullOperationDoc")
    	}
    	return true
    }
    fun validateJSONObject(x4: JSONObject): Boolean{
    	if (x4 == null){
			return false//throw Exception("Exception: NullJSONObject")
    	}
    	return true
    }
}