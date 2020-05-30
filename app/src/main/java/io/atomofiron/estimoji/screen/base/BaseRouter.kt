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
import app.atomofiron.common.util.setOneTimeBackStackListener
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.logI
import io.atomofiron.estimoji.screen.base.util.OneTimeBackStackListener
import io.atomofiron.estimoji.view.BlockFrameLayout
import java.lang.ref.WeakReference
import kotlin.reflect.KClass

abstract class BaseRouter {
    private lateinit var fragmentReference: WeakReference<Fragment?>
    private lateinit var activityReference: WeakReference<AppCompatActivity>

    protected val fragment: Fragment? get() = fragmentReference.get()
    protected val activity: AppCompatActivity? get() = activityReference.get()

    protected val isDestroyed: Boolean get() = fragment == null && activity == null
    protected var isBlocked = false

    protected val arguments: Bundle
        get() {
            var arguments = fragment?.arguments
            arguments = arguments ?: activity?.intent?.extras
            return arguments!!
        }

    protected open var fragmentContainerId: Int = 0
        get() {
            if (field == 0) {
                field = (fragment!!.requireView().parent as View).id
            }
            return field
        }

    protected fun <R> context(action: Context.() -> R): R = activity!!.action()
    protected fun <R> fragment(action: Fragment.() -> R): R = fragment!!.action()
    protected fun <R> activity(action: AppCompatActivity.() -> R): R = activity!!.action()
    protected fun <R> manager(action: FragmentManager.() -> R): R {
        return activity!!.supportFragmentManager.action()
    }

    open fun onAttachChildFragment(childFragment: Fragment) = Unit

    fun onFragmentAttach(fragment: Fragment) {
        fragmentReference = WeakReference(fragment)
        activityReference = WeakReference(fragment.activity as AppCompatActivity)
    }

    fun onActivityAttach(activity: AppCompatActivity) {
        fragmentReference = WeakReference(null)
        activityReference = WeakReference(activity)
    }

    fun onViewDestroy() {
        // nothing?
    }

    fun reattachFragments() {
        manager {
            val transaction = beginTransaction()
            fragments.filterIsInstance<BaseFragment<*>>()
                .forEach {
                    transaction.detach(it)
                    transaction.attach(it)
                }
            transaction.commit()
        }
    }

    protected fun startScreen(fragment: Fragment, addToBackStack: Boolean = true, runOnCommit: (() -> Unit)? = null) {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            val validFragment = filterAddedFragments(this, fragment)
            validFragment ?: return@manager
            isBlocked = true
            val current = fragments.find { !it.isHidden }
            beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                if (current != null) {
                    hide(current)
                }
                add(fragmentContainerId, validFragment, validFragment.javaClass.simpleName)
                if (addToBackStack) {
                    addToBackStack(null)
                    OneTimeBackStackListener(this@manager) {
                        isBlocked = false
                        runOnCommit?.invoke()
                    }
                } else {
                    runOnCommit {
                        isBlocked = false
                        runOnCommit?.invoke()
                    }
                }
                commit()
            }
        }
    }

    private fun filterAddedFragments(manager: FragmentManager, fragment: Fragment): Fragment? {
        val tag = fragment.javaClass.simpleName
        return when (manager.fragments.find { added -> added.tag == tag } == null) {
            true -> fragment
            else -> {
                logI("Fragment with tag = $tag is already added!")
                null
            }
        }
    }

    protected fun switchScreen(addToBackStack: Boolean = true, predicate: (Fragment) -> Boolean) {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            val current = fragments.find { !it.isHidden }!!
            if (predicate(current)) {
                return@manager
            }
            isBlocked = true
            beginTransaction()
                .hide(current)
                .show(fragments.find(predicate)!!)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .apply {
                    if (addToBackStack) {
                        addToBackStack(null)
                        OneTimeBackStackListener(this@manager) {
                            isBlocked = false
                        }
                    } else {
                        runOnCommit {
                            isBlocked = false
                        }
                    }
                }
                .commit()
        }
    }

    fun popScreen() {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            if (backStackEntryCount == 0) {
                return@manager
            }
            isBlocked = true
            setOneTimeBackStackListener {
                isBlocked = false
            }
            popBackStack()
        }
    }
}