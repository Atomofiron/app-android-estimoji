package app.atomofiron.estimoji.screen.base

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import app.atomofiron.common.util.setOneTimeBackStackListener
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.logE
import app.atomofiron.estimoji.logI
import app.atomofiron.estimoji.screen.base.util.OneTimeBackStackListener
import java.lang.ref.WeakReference

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

    protected open var fragmentContainerId: Int = R.id.root_fl_container
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
        logD("onViewDestroy")
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

    protected fun startScreen(fragment: Fragment, vararg fragments: Fragment, addToBackStack: Boolean = true, runOnCommit: (() -> Unit)? = null) {
        if (isDestroyed || isBlocked) {
            return
        }
        val uncheckedFragments = fragments.toMutableList()
        uncheckedFragments.add(0, fragment)
        manager {
            val validFragments = filterAddedFragments(this, uncheckedFragments)
            if (validFragments.isEmpty()) {
                logE("No fragments to add!")
                return@manager
            }
            isBlocked = true
            val current = this.fragments.find { !it.isHidden }
            beginTransaction().apply {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                if (current != null) {
                    hide(current)
                }
                for (validFragment in validFragments) {
                    add(fragmentContainerId, validFragment, validFragment.javaClass.simpleName)
                }
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

    private fun filterAddedFragments(manager: FragmentManager, fragments: List<Fragment>): List<Fragment> {
        return fragments.filter {
            val tag = it.javaClass.simpleName
            when (manager.fragments.find { added -> added.tag == tag } == null) {
                true -> true
                else -> {
                    logI("Fragment with tag = $tag is already added!")
                    false
                }
            }
        }
    }

    protected fun switchScreen(addToBackStack: Boolean = true, predicate: (Fragment) -> Boolean) {
        if (isDestroyed || isBlocked) {
            return
        }
        manager {
            val current = fragments.find { !it.isHidden }!!
            logD("switchScreen current ${current.javaClass.simpleName}")
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