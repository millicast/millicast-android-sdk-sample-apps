package io.dolby.app

import android.app.Application
import io.dolby.app.di.navigationModule
import io.dolby.app.di.publishModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Log Koin into Android logger
            androidLogger()
            // Reference Android context
            androidContext(this@MainApplication)
            // Load modules
            modules(navigationModule, publishModule)
        }
    }
}
