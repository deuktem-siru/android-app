package com.deuktemsiru.common

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/** Keeps wire names and legacy aliases outside domain models. First name is the output name. */
class WireAliasAdapterFactory(private val aliases: Map<Class<*>, Map<String, List<String>>>) : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val fields = aliases[type.rawType] ?: return null
        val delegate = gson.getDelegateAdapter(this, type)
        val tree = gson.getAdapter(JsonElement::class.java)
        return object : TypeAdapter<T>() {
            override fun read(reader: JsonReader): T {
                val json = tree.read(reader)
                if (json.isJsonObject) {
                    val obj = json.asJsonObject
                    fields.forEach { (property, names) ->
                        names.firstOrNull { obj.has(it) }?.let { obj.add(property, obj[it]) }
                    }
                }
                return delegate.fromJsonTree(json)
            }
            override fun write(writer: JsonWriter, value: T) {
                val json = delegate.toJsonTree(value)
                if (json.isJsonObject) {
                    val obj = json.asJsonObject
                    fields.forEach { (property, names) ->
                        if (obj.has(property) && property != names.first()) obj.add(names.first(), obj.remove(property))
                    }
                }
                tree.write(writer, json)
            }
        }.nullSafe()
    }
}
