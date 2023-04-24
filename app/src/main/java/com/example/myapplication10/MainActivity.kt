package com.example.myapplication10

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import java.io.IOException


class MainActivity : AppCompatActivity() {



    lateinit var m_usbManager: UsbManager
    var m_device: UsbDevice? = null
    var m_serial: UsbSerialDevice? = null
    var m_connection: UsbDeviceConnection? = null
    val ACTION_USB_PERMISSION = "permission"
    val vendorId = "0483".toInt()


    fun UsbSerialFromMicky(){

        val manager = getSystemService(USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        println("available drivers: "+availableDrivers)

        if (availableDrivers.isEmpty())
        {
            Log.i("Serial","Drivers is empty")
            return
        }

        val driver = availableDrivers[0]
        val device = driver.device

        println(driver)

        if (!manager.hasPermission(device)) {
            Log.i("Serial","Permission not granted")
            val intent = Intent("com.android.example.USB_PERMISSION")
            val pi = PendingIntent.getBroadcast(this, 0, intent, 0)
            manager.requestPermission(device, pi)
            Log.i("Serial","Permission granted")
            return
        }



        val connection = manager.openDevice(device) ?: return
        println(connection)

        val port = driver.ports[0] // Most devices have just one port (port 0)
        println(port)

        try {
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
        } catch (ioException: IOException) {
            println("ioException")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        UsbSerialFromMicky()
        m_usbManager = getSystemService(Context.USB_SERVICE) as UsbManager


        println("m_serial"+m_serial.toString())
        println("m_connection"+m_connection.toString())
        println("accessory list:"+m_usbManager.accessoryList)
        println("usb manager : "+m_usbManager.deviceList)

        val filter = IntentFilter()
        filter.addAction(ACTION_USB_PERMISSION)
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED)
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
        registerReceiver(broadcastReceiver, filter)

        val on = findViewById<Button>(R.id.on)
        val off = findViewById<Button>(R.id.off)


        val disconnect = findViewById<Button>(R.id.disconnect)
        val connect = findViewById<Button>(R.id.connect)
        val deviceVendorId: Int? = m_device?.vendorId
        val usbDevices: HashMap<String,UsbDevice>? = m_usbManager.deviceList

        println("Devices:"+usbDevices)
        Log.i("Serial","devices:"+usbDevices)
        println("Vendor:"+deviceVendorId)
        Log.i("Serial","vendor:"+deviceVendorId)

        on.setOnClickListener { sendData("o") }
        off.setOnClickListener { sendData("x") }
        disconnect.setOnClickListener { disconnect() }
        connect.setOnClickListener { startUSBConnecting() }


    }

    private fun startUSBConnecting(){


       /* val intent: PendingIntent = PendingIntent.getBroadcast(this,0, Intent(ACTION_USB_PERMISSION),0)
        m_usbManager.requestPermission(m_device, intent)*/
        val usbDevices: HashMap<String,UsbDevice>? = m_usbManager.deviceList



        println("usb_devices"+usbDevices)
        println("isEmpty:"+usbDevices?.isEmpty())

        if (!usbDevices?.isEmpty()!!){
            var keep = true
            usbDevices.forEach { entry ->
                m_device = entry.value
                val deviceVendorId: Int? = m_device?.vendorId
                Log.i("Serial","vendor id: " + deviceVendorId)
                if (deviceVendorId == vendorId){

                    val intent: PendingIntent = PendingIntent.getBroadcast(this,0, Intent(ACTION_USB_PERMISSION),0)
                    m_usbManager.requestPermission(m_device, intent)
                    keep = false
                    Log.i("Serial","connection successful")
                }
                else {
                    m_connection = null
                    m_device = null
                    Log.i("Serial","unable to connect")
                }
                if (!keep){
                    return
                }
            }
            println("UsbDevices below: ")
            println(usbDevices)
        } else {
            Log.i("Serial","Usb devices list is empty")
        }
    }



    private fun sendData(input: String){

        m_serial?.write(input.toByteArray())
        Log.i("Serial","sending data"+input.toByteArray())
    }

    private fun disconnect(){
        m_serial?.close()
    }

    private val broadcastReceiver = object : BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action!! == ACTION_USB_PERMISSION){

                val granted: Boolean = intent.extras!!.getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED)
                if (granted){
                    m_connection = m_usbManager.openDevice(m_device)
                    m_serial = UsbSerialDevice.createUsbSerialDevice(m_device, m_connection)
                    if (m_serial!=null){
                        if (m_serial!!.open()){
                            m_serial!!.setBaudRate(9600)
                            m_serial!!.setDataBits(UsbSerialInterface.DATA_BITS_8)
                            m_serial!!.setStopBits(UsbSerialInterface.STOP_BITS_1)
                            m_serial!!.setParity(UsbSerialInterface.PARITY_NONE)
                            m_serial!!.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                            Log.i("Serial","port is open")
                        }
                        else{
                            Log.i("Serial","port not open")
                        }
                    } else {
                        Log.i("Serial","port is null")
                    }

                }
                else {
                    Log.i("Serial","permission not granted")
                }


            }

            else if (intent.action == UsbManager.ACTION_USB_ACCESSORY_ATTACHED){
                startUSBConnecting()
            }
            else if (intent.action == UsbManager.ACTION_USB_ACCESSORY_DETACHED){
                disconnect()
            }
        }
    }




}