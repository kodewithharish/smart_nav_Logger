package com.accord.smart_nav_logger.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.serialport.SerialPort
import android.util.Log
import com.accord.smart_nav_logger.data.config.Companion.COMMPORT_MXC0
import com.accord.smart_nav_logger.data.config.Companion.COMMPORT_MXC0_Baudrate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import java.io.File
import java.io.InputStream

class SharedNmeaMessageManager constructor(
    context: Context,
    externalScope: CoroutineScope,
    prefs: SharedPreferences){
  lateinit var  externalScope: CoroutineScope
  lateinit var  fd: InputStream
   val  buffer = ByteArray(4096)
    lateinit var t1:Thread



    init {
        try {
            val device = File(COMMPORT_MXC0)
            val baurate = COMMPORT_MXC0_Baudrate.toInt()
            val serialPort   = SerialPort(device, baurate)
            fd=serialPort.inputStream
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


     @ExperimentalCoroutinesApi
     @SuppressLint("MissingPermission")
     private val _NmeaUpdtaes = callbackFlow {

          t1 = Thread(){

              var running = false

             fun run() {
                 running = true

                 while (running) {

                     val length = fd.read(buffer)

                     val data = buffer.copyOf(length)

                     trySend(data)


                 }
             }

          }
         t1.start()



         awaitClose ()


     }.shareIn(
         externalScope,
         replay = 0,
         started = SharingStarted.WhileSubscribed()
     )


     /**
      * A flow of sensor orientations
      */
     @ExperimentalCoroutinesApi
     fun nmeaFlow(): Flow<ByteArray> {
         return _NmeaUpdtaes
     }

}