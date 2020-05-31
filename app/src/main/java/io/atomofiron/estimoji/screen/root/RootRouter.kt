package io.atomofiron.estimoji.screen.root

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.common.util.setOneTimeBackStackListener
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
    fun onBack(): Boolean {
        if (isBlocked) {
            return true
        }
        return manager {
            val lastVisible = fragments
                .filter { it is Backable }
                .findLast { !it.isHidden }
            when {
                (lastVisible as Backable?)?.onBack() == true -> true
                backStackEntryCount > 0 -> {
                    isBlocked = true
                    setOneTimeBackStackListener {
                        isBlocked = false
                    }
                    popBackStack()
                    beginTransaction().commit()
                    true
                }
                fragments.size > 1 && fragments[0] != lastVisible -> {
                    isBlocked = true
                    beginTransaction().apply {
                        hide(lastVisible!!)
                        show(fragments[fragments.indexOf(lastVisible).dec()])
                        runOnCommit {
                            isBlocked = false
                        }
                        commit()
                    }
                    true
                }
                else -> false
            }
        }
    }
}