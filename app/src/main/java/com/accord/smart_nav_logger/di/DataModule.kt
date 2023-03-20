package com.accord.smart_nav_logger.di

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.accord.smart_nav_logger.data.SharedLoggingManager
import com.accord.smart_nav_logger.data.SharedHamsaMessageManager
import com.accord.smart_nav_logger.data.SharedNmeaMessageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.GlobalScope
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context
    ): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)


    @Provides
    fun provideContext(
        @ApplicationContext context: Context,
    ): Context {
        return context
    }

    @Provides
    @Singleton
    fun provideLoggingManager(
        @ApplicationContext context: Context,
        prefs: SharedPreferences,
    ): SharedLoggingManager =
        SharedLoggingManager(context, GlobalScope, prefs)

    @Provides
    @Singleton
    fun provideNmeamsg(
        @ApplicationContext context: Context,
        prefs: SharedPreferences,
    ): SharedNmeaMessageManager =
        SharedNmeaMessageManager(context,GlobalScope, prefs)


    @Provides
    @Singleton
    fun provideHamsamsg(
        @ApplicationContext context: Context,
        prefs: SharedPreferences,
    ): SharedHamsaMessageManager =
        SharedHamsaMessageManager(context,GlobalScope , prefs)


}