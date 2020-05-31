package app.atomofiron.estimoji.screen.scan

import android.app.Application
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.log
import app.atomofiron.estimoji.screen.auth.AuthViewModel
import app.atomofiron.estimoji.util.Util

class ScanViewModel(app: Application) : BaseViewModel<ScanRouter>(app) {

    override val router = ScanRouter()

    fun onScanResult(text: String) {
        log("onScanResult: $text")

        val ip = Util.parseUri(text)
        if (ip == null) {
            // todo
        } else {
            provider!!.get(AuthViewModel::class.java).onIpInput(ip)
            router.popScreen()
        }
    }
}
