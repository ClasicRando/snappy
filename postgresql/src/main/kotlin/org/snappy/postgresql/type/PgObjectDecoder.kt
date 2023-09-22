package org.snappy.postgresql.type

import org.postgresql.util.PGobject
import org.snappy.decode.Decoder
import org.snappy.decodeError
import kotlin.reflect.KClass

interface PgObjectDecoder<T : Any> : Decoder<T> {
    val typeName: String
    val decodeClass: KClass<T>
    fun decodePgObjectValue(value: String): T? {
        throw NotImplementedError("Default PG object value decoder called")
    }

    fun decodePgObject(pgObject: PGobject): T? {
        if (pgObject.value == null) {
            return null
        }
        if (pgObject.type != typeName) {
            decodeError(decodeClass, pgObject.value)
        }
        return decodePgObjectValue(pgObject.value!!)
    }

    override fun decode(value: Any?): T? {
        if (value is PGobject) {
            return decodePgObject(value)
        }
        decodeError(decodeClass, value)
    }
}
