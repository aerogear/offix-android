package org.aerogear.graphqlandroid

import android.util.Log

val TAG = "test"

fun main() {

    val mutation = UpdateCurrentTaskMutation.builder().id("Id").title("title").version(2).build()

    val responseClassName = mutation.javaClass.name

    val classReflected : Class<*> = Class.forName(responseClassName)

    val constructor = classReflected.constructors.first()

    val parameters = constructor.parameterTypes

    parameters.forEach {
        println(it.name)
    }

    val obj = constructor.newInstance("id","title",2)


}
