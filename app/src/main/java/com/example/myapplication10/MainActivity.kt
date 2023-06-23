package com.example.myapplication10



import android.app.PendingIntent


import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice

import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.os.Bundle

import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import com.example.myapplication10.CustomProber.customProber

import com.google.protobuf.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.IOException
import java.net.URL
import java.net.URLEncoder



class MainActivity : AppCompatActivity() {




    val messageId_on = 1
    val temperatureInt_on = 10
    val temperatureFraction_on = 100

    val messageId_off = 2
    val temperatureInt_off = 20
    val temperatureFraction_off = 200

    val messageId_diod_on = 3
    val temperatureInt_diod_on = 30
    val temperatureFraction_diod_on = 300

    val messageId_diod_off = 4
    val temperatureInt_diod_off = 40
    val temperatureFraction_diod_off = 400

    val token = "6227002452:AAH7Emi9q5k0SaMBPOlpYABKbvbXzcW_SXk"
    val chatId = "569502265"



    private var temperatureThread: TemperatureThread? = null
    private lateinit var temperatureTextView: TextView




    val ACTION_USB_PERMISSION = "permission"




    fun sendMessage(token: String, chatId: String, text: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val urlString = "https://api.telegram.org/bot$token/sendMessage"
            val query = "chat_id=${URLEncoder.encode(chatId, "UTF-8")}&text=${URLEncoder.encode(text, "UTF-8")}"
            val url = URL("$urlString?$query")
            url.openConnection().getInputStream().close()

        }
    }

    fun writeDataToSTM32(data: ByteArray) {
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val device = findSTM32Device()
        val usbCustomProber = customProber

        val driver = usbCustomProber.findAllDrivers(manager)[0]

        if (!manager.hasPermission(device)) {
            // USB permission not granted, request permission
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(device, permissionIntent)
            return
        }

        val connection = manager.openDevice(device) ?: return

        try {
            val interfaceClass = 10 // Set the desired interface class (e.g., 2 for Communication)
            val interfaceSubclass = 0 // Set the desired interface subclass (e.g., 2 for Abstract Control Model)
            val usbInterface = findUsbInterface(driver.device, interfaceClass, interfaceSubclass)

            if (usbInterface != null) {
                val endpointOut = usbInterface.getEndpoint(0) // Use the appropriate endpoint for data output
                sendMessage(token,chatId,"endpoint: "+endpointOut)


                val timeout = 3000 // Set the write timeout in milliseconds

                val bytesWritten = connection.bulkTransfer(endpointOut, data, data.size, timeout)

                if (bytesWritten >= 0) {
                    sendMessage(token,chatId,"Data successfully written")
                    // Data successfully written
                    Log.d("Data written to stm", "Data written to STM32: ${String(data)}")
                } else {
                    sendMessage(token,chatId,"Error data written")

                    // Error occurred while writing data
                    Log.e("Error", "Error writing data to STM32")
                }
            } else {
                sendMessage(token,chatId,"Interface is null")

                Log.e("Interface", "Interface is null")
            }
        } catch (e: IOException) {
            sendMessage(token,chatId,e.stackTraceToString())

            Log.e("Exception", "Exception occurred while writing data to STM32: ${e.message}")
            e.printStackTrace()
        } finally {
            connection.close()
        }
    }
    private fun findSTM32Device(): UsbDevice? {
        val manager = getSystemService(USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList
        for (device in deviceList.values) {
            if (device.vendorId == 1155 ) {
                //sendMessage(token, chatId, "FOUND DEVICE:  " + device)
                return device
            }
            else {
                sendMessage(token, chatId, "CANT FOUND DEVICE:  " )
            }
        }
        return null
    }

    fun readDataFromSTM32(): String {

        val manager = getSystemService(USB_SERVICE) as UsbManager
        val device = findSTM32Device()
        val usbCustomProber = customProber
        val driver1 = usbCustomProber.findAllDrivers(manager)[0]
        var temperature: String = null.toString()


        //sendMessage(token,chatId,"driver1: "+driver1)
        //sendMessage(token,chatId,"device: "+driver1.device)


        if (!manager.hasPermission(device)) {
            sendMessage(token,chatId,"permission not granted: ")
            // USB permission not granted, request permission
            val permissionIntent = PendingIntent.getBroadcast(this, 0, Intent(ACTION_USB_PERMISSION), 0)
            manager.requestPermission(device, permissionIntent)

        }

        val connection = manager.openDevice(device)
        //sendMessage(token,chatId,"connection: "+connection)


        try {
           // sendMessage(token,chatId,"trying to open port .....")

            val interfaceClass = 10 // Set the desired interface class (e.g., 2 for Communication)
            val interfaceSubclass = 0 // Set the desired interface subclass (e.g., 2 for Abstract Control Model)
            val usbInterface = findUsbInterface(driver1.device, interfaceClass, interfaceSubclass)


            if (usbInterface != null) {

                try {

                    val endpoint2 = usbInterface.getEndpoint(1)
                    sendMessage(token, chatId, "Endpoint: " + endpoint2)
                    val bufferSize = 64 // Adjust the buffer size as per your requirement
                    val buffer = ByteArray(bufferSize)
                    val timeout = 3000 // Set the read timeout in milliseconds
                    val bytesRead = connection.bulkTransfer(endpoint2, buffer, bufferSize, timeout)
                    // Process the received data

                    temperature = String(buffer, 0, bytesRead)
                    sendMessage(token, chatId, "temperature_encode: " + temperature)


                    return temperature
                }
                catch (Execption: Exception){
                    sendMessage(token, chatId, "exeption_10: " + Execption.stackTraceToString())
                }

            }
            else {
                sendMessage(token,chatId,"interface is null")
            }

        } catch (e: IOException) {
            sendMessage(token,chatId,"exeption: "+e.stackTraceToString())
            e.printStackTrace()
        } finally {

            connection.close()
        }


        return temperature
    }

    private fun findUsbInterface(device: UsbDevice, interfaceClass: Int, interfaceSubclass: Int): UsbInterface? {
        for (i in 0 until device.interfaceCount) {
            val usbInterface = device.getInterface(i)
            if (usbInterface.interfaceClass == interfaceClass && usbInterface.interfaceSubclass == interfaceSubclass) {
                return usbInterface
            }
        }
        return null
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


    fun removeWhitespace(input: String): String {
        return input.replace("\\s".toRegex(), "")
    }

    fun decodeSerializedString(serializedString: String): String {
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

   inner class TemperatureThread : Thread() {
       private var isRunning = true

       override fun run() {
           while (isRunning) {
               try {

                   var temperature: String
                   temperature = readDataFromSTM32()
                   var s1 = removeWhitespace(temperature)
                   sendMessage(token, chatId, "temperature3:" + s1+"|")

                   if(temperature.isEmpty()){
                       sendMessage(token, chatId, "temperatura empty: " )
                   }

                   val decoded_temp = decodeSerializedString(temperature)
                   sendMessage(token, chatId, "decoded_temp: " + decoded_temp)

                   runOnUiThread {
                       // Update the temperature display
                       temperatureTextView.text = "Temperature: ${decoded_temp}"
                   }
                   // Sleep for a while before fetching the temperature again
                   Thread.sleep(1000)
               }
               catch (Exception:Exception){
                   sendMessage(token, chatId, "temperature3: " + Exception.stackTraceToString())
               }
           }
       }



       fun stopThread() {
           isRunning = false
       }
   }


    override fun onDestroy() {
        super.onDestroy()
        // Stop the temperature thread when the activity is destroyed
        temperatureThread?.stopThread()
    }



    override fun onCreate(savedInstanceState: Bundle?) {



        try {

            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            val filter = IntentFilter()
            filter.addAction(ACTION_USB_PERMISSION)
            filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)

            val button_On = findViewById<Button>(R.id.diod_on)
            val btn_Off = findViewById<Button>(R.id.diod_off)
            val temp_On = findViewById<Button>(R.id.TempOn)
            val temp_Off = findViewById<Button>(R.id.TempOff)
            val temperature = findViewById<Button>(R.id.temperature)


            temperature.setOnClickListener { show_temp() }

            btn_Off.setOnClickListener {
                writeDataToSTM32(
                    encodeMessageToString(
                        messageId_diod_off,
                        temperatureInt_diod_off,
                        temperatureFraction_diod_off
                    ).toByteArray()
                )
            }
            button_On.setOnClickListener {
                writeDataToSTM32(
                    encodeMessageToString(
                        messageId_diod_on,
                        temperatureInt_diod_on,
                        temperatureFraction_diod_on
                    ).toByteArray()
                )
            }
            temp_On.setOnClickListener {
                writeDataToSTM32(
                    encodeMessageToString(
                        messageId_on,
                        temperatureInt_on,
                        temperatureFraction_on
                    ).toByteArray()
                )
            }
            temp_Off.setOnClickListener {
                writeDataToSTM32(
                    encodeMessageToString(
                        messageId_off,
                        temperatureInt_off,
                        temperatureFraction_off
                    ).toByteArray()
                )
            }
        }
        catch (Exception: Exception){
            sendMessage(token, chatId, Exception.stackTraceToString())
        }
    }




    fun show_temp(){
        try {
            var temperature2: String
            temperature2 = readDataFromSTM32()
            var s1 = removeWhitespace(temperature2)
            var s2 = decodeSerializedString(s1)

            temperatureTextView = findViewById(R.id.temperatureTextView)
            sendMessage(token, chatId, "temperature3:" + s1 + "|")
            sendMessage(token, chatId, "decoded1:" + s2)
            temperatureTextView.text = "Temperature: ${s2}"
        }
        catch (Exception:Exception){
            sendMessage(token, chatId, Exception.stackTraceToString())
        }

    }




}