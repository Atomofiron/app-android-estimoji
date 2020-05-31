package app.atomofiron.estimoji.screen.root

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import app.atomofiron.estimoji.screen.base.BaseViewModel
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.log
import app.atomofiron.estimoji.screen.auth.AuthFragment
import app.atomofiron.estimoji.screen.auth.AuthViewModel
import app.atomofiron.estimoji.screen.poker.PokerFragment
import app.atomofiron.estimoji.util.Util
import java.lang.ref.WeakReference

class RootViewModel(app: Application) : BaseViewModel<RootRouter>(app) {
    override val router = RootRouter()

    private lateinit var activityReference: WeakReference<RootActivity>
    private val activity: RootActivity get() = activityReference.get()!!

    override fun onActivityAttach(activity: Activity) {
        super.onActivityAttach(activity)
        this.activityReference = WeakReference(activity as RootActivity)
    }

    override fun onCreate(context: Context, intent: Intent) {
        super.onCreate(context, intent)

        if (!router.isScreenStarted(AuthFragment::class)) {
            router.startAuthScreen()
        }
    }

    fun onIntent(intent: Intent?) {
        log("onIntent ${intent?.action} ${intent?.data}")
        val data = intent?.data ?: return
        val ip = Util.parseUri(data)
        if (ip == null) {
            activity.snack(R.string.wrong_ip)
        } else {
            onJoin(ip)
        }
    }

    private fun onJoin(ip: String) {
        log("onJoin $ip")
        if (router.isScreenStarted(PokerFragment::class)) {
            activity.askForSession(ip)
        } else {
            provider!!.get(AuthViewModel::class.java).onIpInput(ip)
            router.resetToAuthScreen()
        }
    }

    fun onJoinConfirm(ip: String) {
        log("onJoinConfirm $ip")
        provider!!.get(AuthViewModel::class.java).onIpInput(ip)
        router.resetToAuthScreen()
    }

    override fun onBackPressed(): Boolean = when {
        router.onBack() -> true
        else -> super.onBackPressed()
    }
}