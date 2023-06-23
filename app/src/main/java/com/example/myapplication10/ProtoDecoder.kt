package com.example.myapplication10

import com.fasterxml.jackson.annotation.JsonFormat
import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.DynamicMessage

class ProtoDecoder {
    fun decodeSerializedString(serializedString: String) {
        try {
            // Convert the serialized string to a byte array
            val serializedBytes = hexStringToByteArray(serializedString)

            // Manually parse the fields based on the protobuf structure
            var index = 0
            var fieldNumber = 0
            var value = 0
            var isFieldNumberRead = false

            while (index < serializedBytes.size) {
                val currentByte = serializedBytes[index].toInt()

                if (!isFieldNumberRead) {
                    fieldNumber = (currentByte ushr 3) and 0x0F
                    isFieldNumberRead = true
                } else {
                    value = (value shl 7) or (currentByte and 0x7F)

                    if (currentByte and 0x80 == 0) {
                        when (fieldNumber) {
                            1 -> println("Message ID: $value")
                            2 -> println("Temperature Int: $value")
                            3 -> println("Temperature Fraction: $value")
                            4 -> println("Fake Measure: $value")
                        }

                        value = 0
                        isFieldNumberRead = false
                    }
                }

                index++
            }
        } catch (e: Exception) {
            println("Error decoding the serialized string: ${e.message}")
        }
    }

    private fun hexStringToByteArray(hexString: String): ByteArray {
        val len = hexString.length / 2
        val result = ByteArray(len)
        for (i in 0 until len) {
            val index = i * 2
            val byteStr = hexString.substring(index, index + 2)
            result[i] = byteStr.toInt(16).toByte()
        }
        return result
    }
}

fun main() {
    // Sample serialized string
    val serializedString = "0801101a18a505"

    // Create an instance of ProtoDecoder and call decodeSerializedString
    val protoDecoder = ProtoDecoder()
    protoDecoder.decodeSerializedString(serializedString)
}