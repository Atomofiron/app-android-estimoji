package app.atomofiron.estimoji

import android.app.Application
import android.content.Context
import androidx.work.WorkManager
import app.atomofiron.estimoji.BuildConfig.YANDEX_API_KEY
import app.atomofiron.estimoji.work.KtorServerWorker
import app.atomofiron.estimoji.work.WebClientWorker
import com.google.zxing.client.android.BuildConfig
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig

class App : Application() {
    companion object {
        lateinit var appContext: Context
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val config = YandexMetricaConfig.newConfigBuilder(YANDEX_API_KEY)
            .withLocationTracking(false)
            .withCrashReporting(true)
            .build()
        YandexMetrica.activate(applicationContext, config)

        assets
    }
}