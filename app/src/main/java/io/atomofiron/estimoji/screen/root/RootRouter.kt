package io.atomofiron.estimoji.screen.root

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import io.atomofiron.estimoji.screen.base.BaseRouter
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.auth.AuthFragment
import kotlin.reflect.KClass

class RootRouter : BaseRouter() {
    override var fragmentContainerId: Int = R.id.root_fl_container

    fun isScreenStarted(kClass: KClass<out Fragment>): Boolean {
        var started = false
        manager {
            started =  fragments.find { it::class == kClass } != null
        }
        return started
    }

    fun startAuthScreen() {
        startScreen(AuthFragment(), addToBackStack = false)
    }

    fun resetToAuthScreen() {
        log("resetToAuthScreen")
        manager {
            popBackStack(null, POP_BACK_STACK_INCLUSIVE)
        }
    }
}