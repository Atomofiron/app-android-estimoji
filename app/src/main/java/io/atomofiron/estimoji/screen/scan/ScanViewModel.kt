package io.atomofiron.estimoji.screen.scan

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import io.atomofiron.estimoji.screen.base.BaseViewModel
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.auth.AuthViewModel
import io.atomofiron.estimoji.util.Util

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
