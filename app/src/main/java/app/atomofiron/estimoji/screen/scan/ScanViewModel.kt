package app.atomofiron.estimoji.screen.scan

import android.app.Application
import app.atomofiron.estimoji.injactable.channel.PublicChannel
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.util.Util

class ScanViewModel(app: Application) : BaseViewModel<ScanRouter>(app) {

    override val router = ScanRouter()

    fun onScanResult(text: String) {
        logD("onScanResult: $text")

        val ipPort = Util.parseUri(text)
        if (ipPort == null) {
            // todo
        } else {
            router.popScreen()
            PublicChannel.ipResultScanned.setAndNotify(ipPort)
        }
    }
}
