package com.accord.smart_nav_logger.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

class LoggingRepository @Inject constructor(
                    private val sharedNmeaMessageManager:SharedNmeaMessageManager,
                    private val sharedHamsaMessageManager: SharedHamsaMessageManager
                    ) {



    /**
     * Observable flow for location updates
     */
    @ExperimentalCoroutinesApi
    fun getNmea() = sharedNmeaMessageManager.nmeaFlow()

    /**
     * Observable flow for GnssStatus updates
     */
    @ExperimentalCoroutinesApi
    fun getHamsa() = sharedHamsaMessageManager.hamsaFlow()

}