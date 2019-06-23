package org.aerogear.graphqlandroid

import com.apollographql.apollo.api.*
import com.apollographql.apollo.api.internal.UnmodifiableMapBuilder
import com.apollographql.apollo.api.internal.Utils
import io.reactivex.internal.util.BackpressureHelper.add
import org.aerogear.graphqlandroid.GenericMutation.Data.Mapper
import org.aerogear.graphqlandroid.GenericMutation.Variables
import org.aerogear.graphqlandroid.type.CustomType
import java.lang.String.format
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList

class GenericMutation(
    val operationId: String,
    val operationName: OperationName,
    val queryDoc: String,
    val valueMap: MutableMap<String, Any>

) : Mutation<GenericMutation.Data, GenericMutation.Data, Variables> {

    val fieldname = getFieldname(queryDoc)

    private val variables: Variables = Variables(valueMap)

    fun getFieldname(queryDoc: String): String {
        var fieldname = StringBuilder("")
        for (letter in queryDoc.indices) {
            if (queryDoc[letter] == '{') {
                var j = letter + 1
                while (queryDoc[j] != '(') {
                    fieldname.append(queryDoc[j])
                    j++
                }
            }
            break
        }
        return fieldname.toString()
    }

    fun getVariableName(valueMap: MutableMap<String, Any>): ArrayList<Any> {

        val listofVariables = arrayListOf<Any>()

        valueMap.forEach { (key, value) ->
            listofVariables.add(key)
        }
        return listofVariables
    }

    fun getNumberOfVar(valueMap: MutableMap<String, Any>): Int {
        return valueMap.size
    }


    override fun wrapData(data: Data?): Data? = data

    override fun variables(): Variables = variables

    override fun queryDocument(): String = queryDoc

    override fun responseFieldMapper(): ResponseFieldMapper<Data> = Data(OurTask()).Mapper()

    override fun operationId() = operationId

    override fun name(): OperationName = operationName


//    inner class Builder {
//
//        fun build(): GenericMutation {
//            Utils.checkNotNull<String>(, "id == null")
//
//            valueMap.forEach { (key, value) ->
//
//            }
//            return GenericMutation()
//        }
//    }


    inner class Variables(valueMap: MutableMap<String, Any>) : Operation.Variables() {

        @Transient
        var valmap: LinkedHashMap<String, Any> = valueMap as LinkedHashMap<String, Any>

        override fun valueMap(): Map<String, Any> {
            return Collections.unmodifiableMap(valmap)
        }

        override fun marshaller(): InputFieldMarshaller {
            return InputFieldMarshaller { writer ->

                valmap.forEach { (key, value) ->
                    writer.writeCustom(key, value as ScalarType, value)
                }
            }

        }

    }


    inner class Data(val ourTask: OurTask) : Operation.Data {

        internal val `$responseFields` = arrayOf(
            ResponseField.forObject(
                fieldname, fieldname, mapBuilder(), true, emptyList()
            )
        )


        fun mapBuilder(): MutableMap<String, Any>? {
            val unmodifiableMapBuilder = UnmodifiableMapBuilder<String, Any>(valueMap.size)

            valueMap.forEach { (key, value) ->

                val unmB = UnmodifiableMapBuilder<String, Any>(2)
                unmB.put("kind", "Variable")
                    .put("variableName", key).build()

                unmodifiableMapBuilder.put(
                    key, unmB
                )
            }
            return unmodifiableMapBuilder.build()
        }


        override fun marshaller(): ResponseFieldMarshaller {
            return ResponseFieldMarshaller { writer ->
                writer.writeObject(
                    `$responseFields`[0],
                    fieldname?.let {
                        marshaller()
                    }
                )
            }
        }


        inner class Mapper : ResponseFieldMapper<Data> {

            internal val fieldMapper = OurTask().Mapper()

            override fun map(responseReader: ResponseReader?): Data {

                val ourTask = responseReader?.readObject(
                    `$responseFields`[0]
                ) { reader -> fieldMapper.map(reader) }

                return Data(ourTask!!)
            }
        }
    }

    inner class OurTask {

//        constructor(__typename: String, vararg values: ResponseField) : this(){
//
//        }

        internal val arrayList: ArrayList<ResponseField> = responseFieldsArraylist()

        internal val `$responseFields`: Array<ResponseField> = arrayList.toArray() as Array<ResponseField>

        internal val __typename: String = ""


//        constructor(vararg responseField: Array<ResponseField>) : this() {
//
//        }

        fun responseFieldsArraylist(): ArrayList<ResponseField> {
            val list: ArrayList<ResponseField> = arrayListOf()
            list.add(ResponseField.forString("__typename", "__typename", null, false, emptyList()))
            valueMap.forEach { (key, value) ->
                list.add(ResponseField.forCustomType(key, key, null, false, value as ScalarType, emptyList()))
            }

            return list
        }

        fun marshaller(): ResponseFieldMarshaller {
            return ResponseFieldMarshaller { writer ->
                writer.writeString(`$responseFields`[0], __typename)

                for (i in 1 until arrayList.size) {
                    writer.writeCustom(
                        `$responseFields`[i] as ResponseField.CustomTypeField,
                        valueMap.getValue(`$responseFields`[i].fieldName())
                    )
                }
            }
        }


        internal inner class Mapper : ResponseFieldMapper<OurTask> {
            override fun map(reader: ResponseReader): OurTask {
                return OurTask()
            }
        }


    }


}


