package com.example.myapplication10


import com.hoho.android.usbserial.driver.FtdiSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

internal object CustomProber {
    // e.g. device with custom VID+PID
    // e.g. device with custom VID+PID
    val customProber: UsbSerialProber
        get() {
            val customTable = ProbeTable()
            customTable.addProduct(
                1155,
                22336,
                FtdiSerialDriver::class.java
            ) // e.g. device with custom VID+PID
            customTable.addProduct(
                1155,
                22336,
                FtdiSerialDriver::class.java
            ) // e.g. device with custom VID+PID
            return UsbSerialProber(customTable)
        }
}