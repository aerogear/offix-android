package org.aerogear.graphqlandroid.persistence


import com.apollographql.apollo.api.internal.UnmodifiableMapBuilder
import java.util.Collections
import java.util.HashMap

class GenericUnmodifiableMapBuilder(map: MutableMap<String, Any>) : UnmodifiableMapBuilder<String, Any>() {

    private var map: MutableMap<String, Any> = map

    fun GenericUnmodifiableMapBuilder(initialCapacity: Int) {
        this.map = HashMap<String, Any>(initialCapacity)
    }
//
//    fun GenericUnmodifiableMapBuilder() {
//        this.map = HashMap<String, V>()
//    }

    override fun put(key: String, value: Any): UnmodifiableMapBuilder<String, Any> {
        map[key] = value
        return this
    }

    override fun build(): Map<String, Any> {
        return Collections.unmodifiableMap<String, Any>(map)
    }
}
