package io.atomofiron.estimoji

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import io.atomofiron.estimoji.work.KtorServerWorker
import io.atomofiron.estimoji.work.WebClientWorker

class App : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.cancelUniqueWork(KtorServerWorker.NAME)
        workManager.cancelUniqueWork(WebClientWorker.NAME)
    }
}