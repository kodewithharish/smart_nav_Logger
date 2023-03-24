/*
 * Copyright 2019-2021 Google LLC, Sean Barbeau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.accord.smart_nav_logger.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.location.OnNmeaMessageListener
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.accord.smart_nav_logger.PreferenceUtils
import com.accord.smart_nav_logger.R
import com.accord.smart_nav_logger.util.hasPermission

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn

private const val TAG = "SharedNmeaManager"

/**
 * Wraps NMEA updates in callbackFlow
 *
 * Derived in part from https://github.com/android/location-samples/blob/main/LocationUpdatesBackgroundKotlin/app/src/main/java/com/google/android/gms/location/sample/locationupdatesbackgroundkotlin/data/MyLocationManager.kt
 * and https://github.com/googlecodelabs/kotlin-coroutines/blob/master/ktx-library-codelab/step-06/myktxlibrary/src/main/java/com/example/android/myktxlibrary/LocationUtils.kt
 */
open class SharedNmeaManager constructor(
    private val context: Context,
    externalScope: CoroutineScope,
    prefs: SharedPreferences
) {
    @ExperimentalCoroutinesApi
    @SuppressLint("MissingPermission")
    private val _nmeaUpdates = callbackFlow {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val callback = OnNmeaMessageListener { message: String, timestamp: Long ->
            PreferenceUtils.saveInt(
                context.getString(R.string.capability_key_nmea),
                PreferenceUtils.CAPABILITY_SUPPORTED,
                prefs
            )
          //  val nmeaWithTime = NmeaWithTime(timestamp, message)
         //   Log.d(TAG, "New nmea: ${nmeaWithTime}")
            // Send the new NMEA info to the Flow observers
            trySend(message)
        }


        if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
            !context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        ) close()

        Log.d(TAG, "Starting NMEA updates")

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                locationManager.addNmeaListener(ContextCompat.getMainExecutor(context), callback)
            } else {
                locationManager.addNmeaListener(callback, Handler(Looper.getMainLooper()))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in location flow: $e")
            close(e) // in case of exception, close the Flow
        }

        awaitClose {
            Log.d(TAG, "Stopping NMEA updates")
            locationManager.removeNmeaListener(callback) // clean up when Flow collection ends
        }
    }.shareIn(
        externalScope,
        replay = 0,
        started = SharingStarted.WhileSubscribed()
    )

    @RequiresApi(Build.VERSION_CODES.N)
    @ExperimentalCoroutinesApi
    fun nmeamFlow(): Flow<String> {
        return _nmeaUpdates
    }

}