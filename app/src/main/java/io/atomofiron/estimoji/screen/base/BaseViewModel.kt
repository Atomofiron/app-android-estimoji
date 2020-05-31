package io.atomofiron.estimoji.screen.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.util.KObservable
import io.atomofiron.estimoji.log

abstract class BaseViewModel<R : BaseRouter>(app: Application) : AndroidViewModel(app) {
    protected abstract val router: R
    protected var provider: ViewModelProvider? = null
    protected val onClearedCallback = KObservable.RemoveObserverCallback()

    open fun onFragmentAttach(fragment: Fragment) {
        router.onFragmentAttach(fragment)
        provider = ViewModelProvider(fragment.activity!!)
    }

    open fun onActivityAttach(activity: Activity) {
        router.onActivityAttach(activity as AppCompatActivity)
        provider = ViewModelProvider(activity)
    }

    fun onCreate(context: Context, arguments: Bundle?) {
        onCreate(context, Intent().putExtras(arguments ?: Bundle()))
    }

    open fun onCreate(context: Context, intent: Intent) = log("onCreate")

    open fun onShow() {
        //router.unblockUi()
        log("onShow")
    }

    open fun onViewDestroy() = router.onViewDestroy()

    override fun onCleared() {
        provider = null
        onClearedCallback.invoke()
    }

    open fun onBackPressed(): Boolean = false
}
