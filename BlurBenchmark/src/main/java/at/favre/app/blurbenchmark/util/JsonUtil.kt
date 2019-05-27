package at.favre.app.blurbenchmark.util

import com.fasterxml.jackson.databind.ObjectMapper

import java.io.IOException

/**
 * Created by PatrickF on 16.04.2014.
 */
object JsonUtil {
    private val defaultMapper = ObjectMapper()

    @JvmOverloads
    fun toJsonString(obj: Any, mapper: ObjectMapper = defaultMapper): String {
        try {
            return mapper.writeValueAsString(obj)
        } catch (e: Exception) {
            throw JsonSerializeException(e)
        }

    }

    fun <T> fromJsonString(json: String, clazz: Class<T>): T {
        return fromJsonString(json, clazz, defaultMapper)
    }

    private fun <T> fromJsonString(json: String, clazz: Class<T>, mapper: ObjectMapper): T {
        try {
            return mapper.readValue(json, clazz)
        } catch (e: IOException) {
            throw JsonDeserializeException(e)
        }

    }

    class JsonSerializeException(throwable: Throwable) : RuntimeException(throwable)

    class JsonDeserializeException(throwable: Throwable) : RuntimeException(throwable)
}
