package io.atomofiron.estimoji

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
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

        /*val config = YandexMetricaConfig.newConfigBuilder(BuildConfig.YANDEX_API_KEY)
            .withLocationTracking(false)
            .withCrashReporting(true)
            .build()
        YandexMetrica.activate(applicationContext, config);*/
    }
}