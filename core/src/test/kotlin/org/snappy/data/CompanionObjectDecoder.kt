package org.snappy.data

import org.snappy.decode.Decoder
import org.snappy.decodeError

data class CompanionObjectDecoder(val field: String) {
    companion object : Decoder<CompanionObjectDecoder> {
        override fun decode(value: Any?): CompanionObjectDecoder? {
            if (value == null) {
                return null
            }
            if (value is String) {
                return CompanionObjectDecoder(value)
            }
            decodeError<CompanionObjectDecoder>(value)
        }
    }
}