package io.atomofiron.estimoji.screen.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import io.atomofiron.estimoji.log
import java.lang.ref.WeakReference

abstract class BaseViewModel<R : BaseRouter>(app: Application) : AndroidViewModel(app) {
    protected abstract val router: R
    protected var provider: ViewModelProvider? = null

    open fun onFragmentAttach(fragment: Fragment) {
        router.onFragmentAttach(fragment)
        provider = ViewModelProviders.of(fragment.activity!!)
    }

    open fun onActivityAttach(activity: Activity) {
        router.onActivityAttach(activity as AppCompatActivity)
        provider = ViewModelProviders.of(activity)
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
    }
}
