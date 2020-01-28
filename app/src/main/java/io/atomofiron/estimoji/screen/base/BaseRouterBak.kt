package io.atomofiron.estimoji.screen.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.base.util.BackStackWatcher
import io.atomofiron.estimoji.screen.base.util.OneTimeBackStackListener
import io.atomofiron.estimoji.view.BlockFrameLayout
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

abstract class BaseRouterBak {
    private lateinit var fragmentReference: WeakReference<Fragment?>
    private lateinit var activityReference: WeakReference<AppCompatActivity>

    protected val fragment: Fragment? get() = fragmentReference.get()
    protected val activity: AppCompatActivity? get() = activityReference.get()
    protected val isDestroyed: Boolean get() =  fragment == null && activity == null

    protected val arguments: Bundle get() {
        var arguments = fragmentReference.get()?.arguments
        arguments = arguments ?: activityReference.get()?.intent?.extras
        return arguments!!
    }

    protected open val indexFromEnd: Int = 0
    protected open var screenBlockerId: Int = R.id.root_bfl
    protected open var fragmentContainerId: Int = 0
        get() {
            if (field == 0) {
                field = (fragment!!.view!!.parent as View).id
            }
            return field
        }

    protected lateinit var backStackWatcher: BackStackWatcher
    private set

    protected fun context(action: Context.() -> Unit) = activityReference.get()!!.action()
    protected fun fragment(action: Fragment.() -> Unit) = fragmentReference.get()!!.action()
    protected fun activity(action: AppCompatActivity.() -> Unit) = activityReference.get()!!.action()
    protected fun childManager(action: FragmentManager.() -> Unit) {
        var manager = fragmentReference.get()?.childFragmentManager
        manager = manager ?: activityReference.get()?.supportFragmentManager
        manager!!.action()
    }
    protected fun manager(action: FragmentManager.() -> Unit) {
        var manager = fragmentReference.get()?.fragmentManager
        manager = manager ?: activityReference.get()?.supportFragmentManager
        manager!!.action()
    }

    protected fun nextIntent(clazz: KClass<out Activity>): Intent {
        var intent: Intent? = null
        context {
            intent = Intent(this, clazz.java)
        }
        return intent!!
    }

    fun onFragmentAttach(fragment: Fragment) {
        fragmentReference = WeakReference(fragment)
        activityReference = WeakReference(fragment.activity as AppCompatActivity)
        backStackWatcher =
            BackStackWatcher(fragment.fragmentManager!!)
        fragment.fragmentManager!!.addOnBackStackChangedListener(backStackWatcher)
    }

    fun onActivityAttach(activity: AppCompatActivity) {
        fragmentReference = WeakReference(null)
        activityReference = WeakReference(activity)
        backStackWatcher =
            BackStackWatcher(activity.supportFragmentManager)
        activity.supportFragmentManager.addOnBackStackChangedListener(backStackWatcher)
    }

    fun onViewDestroy() {
        fragmentReference.clear()
        activityReference.clear()
    }

    fun recreateActivity() {
        val activity = activity ?: fragment!!.activity!!
        activity.recreate()
    }

    private fun blockUi(block: Boolean = true) {
        val blocker = activity!!.findViewById<BlockFrameLayout>(screenBlockerId)
        if (block) {
            blocker?.block()
        } else {
            blocker?.unblock()
        }
    }

    protected fun startScreen(vararg fragmentsArg: Fragment,
                              addToBackStack: Boolean = true,
                              runOnCommit: (() -> Unit)? = null) {
        if (isDestroyed) {
            return
        }
        log("startScreen [0]: ${fragmentsArg[0]::class.java.simpleName}")
        if (fragment != null) {
            val kClass = fragment!!::class
            val indexFromEnd = backStackWatcher.indexOf(kClass)
            if (indexFromEnd != this.indexFromEnd) {
                log("${kClass.java.simpleName}'s indexFromEnd != indexFromEnd")
                return
            }
        }
        val arr = Array(fragmentsArg.size) { fragmentsArg[it]::class }
        backStackWatcher.addAll(*arr)
        manager {
            val current = fragments.find { !it.isHidden }
            beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                if (current != null) {
                    hide(current)
                }
                fragmentsArg.forEach { add(fragmentContainerId, it) }
                if (addToBackStack) {
                    addToBackStack(null)
                    if (runOnCommit != null) OneTimeBackStackListener(this@manager, runOnCommit)
                } else {
                    if (runOnCommit != null) runOnCommit(runOnCommit)
                }
                commit()
            }
        }
    }

    protected fun switchScreen(addToBackStack: Boolean = true, predicate: (Fragment) -> Boolean) {
        if (isDestroyed) {
            return
        }
        manager {
            val current = fragments.find { !it.isHidden }!!
            if (predicate(current)) {
                return@manager
            }
            beginTransaction()
                .hide(current)
                .show(fragments.find(predicate)!!)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .apply {
                    if (addToBackStack) {
                        addToBackStack(null)
                    }
                }
                .commit()
        }
    }

    fun closeScreen() {
        if (isDestroyed) {
            return
        }
        val fragment = fragment
        val activity = activity

        manager {
            onViewDestroy()
            when {
                fragment == null -> activity?.finish()
                backStackEntryCount > 0 -> popBackStack()
                fragments.size > 1 -> {
                    beginTransaction().apply {
                        remove(fragment)
                        fragments.findLast { it !== fragment }?.let(::show)
                        commit()
                    }
                }
            }
        }
    }
}