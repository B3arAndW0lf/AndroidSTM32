package com.example.myapplication10

import com.google.protobuf.ByteString
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.WireFormat

class ProtoDecoder2 {


    fun decodeSerializedString3(serializedString: String): String {
        val serializedBytes = serializedString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        var index = 0
        var messageId = 0
        var temperatureInt = 0
        var temperatureFraction = 0

        while (index < serializedBytes.size) {
            val tag = (serializedBytes[index].toInt() and 0x7F) ushr 3
            val wireType = serializedBytes[index].toInt() and 0x07
            index++
            when (tag) {
                1 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    messageId = value
                    index = newIndex
                }
                2 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureInt = value
                    index = newIndex
                }
                3 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureFraction = value
                    index = newIndex
                }
                else -> {
                    // Unknown field, ignore or handle accordingly
                }
            }
        }

        val temperatureString = "$temperatureInt,$temperatureFraction"
        return temperatureString
    }

     fun decodeVarint(serializedBytes: ByteArray, startIndex: Int): Pair<Int, Int> {
        var value = 0
        var shift = 0
        var currentIndex = startIndex
        while (true) {
            val byte = serializedBytes[currentIndex]
            value = value or ((byte.toInt() and 0x7F) shl shift)
            currentIndex++
            if (byte.toInt() and 0x80 == 0) {
                break
            }
            shift += 7
        }
        return Pair(value, currentIndex)
    }


    fun decodeSerializedStringBytes(serializedString: String): ByteArray? {
        val serializedBytes = serializedString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        var index = 0
        var messageId = 0
        var temperatureInt = 0
        var temperatureFraction = 0

        while (index < serializedBytes.size) {
            val tag = (serializedBytes[index].toInt() and 0x7F) ushr 3
            val wireType = serializedBytes[index].toInt() and 0x07
            index++
            when (tag) {
                1 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    messageId = value
                    index = newIndex
                }
                2 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureInt = value
                    index = newIndex
                }
                3 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureFraction = value
                    index = newIndex
                }
                else -> {
                    // Unknown field, ignore or handle accordingly
                }
            }
        }

        println("Message ID: $messageId")
        println("Temperature Int: $temperatureInt")
        println("Temperature Fraction: $temperatureFraction")

        return byteArrayOf(messageId.toByte(), temperatureInt.toByte(), temperatureFraction.toByte())
    }

     fun decodeSerializedString(serializedString: String) : String?{
        val serializedBytes = serializedString.chunked(2).map { it.toInt(16).toByte() }.toByteArray()

        var index = 0
        var messageId = 0
        var temperatureInt = 0
        var temperatureFraction = 0

        while (index < serializedBytes.size) {
            val tag = (serializedBytes[index].toInt() and 0x7F) ushr 3
            val wireType = serializedBytes[index].toInt() and 0x07
            index++
            when (tag) {
                1 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    messageId = value
                    index = newIndex
                }
                2 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureInt = value
                    index = newIndex
                }
                3 -> {
                    val (value, newIndex) = decodeVarint(serializedBytes, index)
                    temperatureFraction = value
                    index = newIndex
                }
                else -> {
                    // Unknown field, ignore or handle accordingly
                }
            }
        }

        println("Message ID: $messageId")
        println("Temperature Int: $temperatureInt")
        println("Temperature Fraction: $temperatureFraction")

         return temperatureInt.toString()+","+temperatureFraction.toString()
    }

    fun main() {
        // Sample serialized string
        val serializedString = "0801101a18a505"

        // Decode the serialized string
        decodeSerializedString(serializedString)
    }
}


fun encodeMessageToString(messageId: Int, temperatureInt: Int, temperatureFraction: Int): String {
    val outputStream = ByteString.newOutput()
    val codedOutputStream = CodedOutputStream.newInstance(outputStream)
    fun encodeVarint(tag: Int, value: Int) {
        codedOutputStream.writeTag(tag, WireFormat.WIRETYPE_VARINT)
        codedOutputStream.writeUInt32NoTag(value.toUInt().toInt())
    }
    encodeVarint(1, messageId)
    encodeVarint(2, temperatureInt)
    encodeVarint(3, temperatureFraction)
    codedOutputStream.flush()
    val byteArray = outputStream.toByteString().toByteArray()
    return byteArray.joinToString("") { "%02x".format(it) }
}





fun decodeByteArrayToString(byteArray: ByteArray): String {
    return String(byteArray)
}

fun main() {
    //encoded
    val messageId = 1
    val temperatureInt = 10
    val temperatureFraction = 100

    val serializedString = encodeMessageToString(messageId, temperatureInt, temperatureFraction)
    println("Encoded String: $serializedString")


    println( ProtoDecoder2().decodeSerializedString3("0801100A186400") )
}