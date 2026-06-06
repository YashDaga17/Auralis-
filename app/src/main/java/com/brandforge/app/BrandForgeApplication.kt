package com.brandforge.app

import android.app.Application
import com.brandforge.app.core.debug.DebugErrorSeverity
import com.brandforge.app.core.debug.GlobalErrorLogger
import com.brandforge.app.core.startup.StartupCheckRecorder
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BrandForgeApplication : Application() {
    @Inject
    lateinit var startupCheckRecorder: StartupCheckRecorder

    @Inject
    lateinit var errorLogger: GlobalErrorLogger

    override fun onCreate() {
        super.onCreate()
        installCrashLogger()
        startupCheckRecorder.recordStartupCheck()
    }

    private fun installCrashLogger() {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            errorLogger.log(
                feature = "Uncaught Crash",
                screen = thread.name,
                throwable = throwable,
                severity = DebugErrorSeverity.Fatal,
            )
            previousHandler?.uncaughtException(thread, throwable)
        }
    }
}
