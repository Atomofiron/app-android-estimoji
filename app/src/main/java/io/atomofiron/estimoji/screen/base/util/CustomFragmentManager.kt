package io.atomofiron.estimoji.screen.base.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import io.atomofiron.estimoji.log
import kotlin.reflect.KClass

class CustomFragmentManager(private val manager: FragmentManager) : FragmentManager.OnBackStackChangedListener {
    companion object {
        var mFragments = ArrayList<KClass<out Fragment>>()
        var backStack: List<KClass<out Fragment>> = ArrayList()
    }

    override fun onBackStackChanged() {
        log("onBackStackChanged")
        val newBackStack = manager.fragments.map { it::class }
        when {
            mFragments.isEmpty() -> mFragments.addAll(newBackStack)
            else -> backStack
                .filter { !newBackStack.contains(it) }
                .forEach { mFragments.remove(it) }
        }
        backStack = newBackStack
    }

    fun beginTransaction() = CustomFragmentTransaction(this)

    fun commit(transaction: ArrayList<CustomFragmentTransaction.Option>) {

    }

    class CustomFragmentTransaction(private val customFragmentManager: CustomFragmentManager) {
        private val transaction = ArrayList<Option>()

        fun add(id: Int, fragment: Fragment) {
            transaction.add(Option.Add(id, fragment))
        }
        fun show(fragment: Fragment) {
            transaction.add(Option.Show(fragment))
        }
        fun hide(fragment: Fragment) {
            transaction.add(Option.Hide(fragment))
        }
        fun commit() {
            customFragmentManager.commit(transaction)
        }

        sealed class Option(private val code: Int, val fragment: Fragment) {
            class Add(val id: Int, fragment: Fragment) : Option(0, fragment)
            class Show(fragment: Fragment) : Option(1, fragment)
            class Hide(fragment: Fragment) : Option(2, fragment)

            override fun equals(other: Any?): Boolean {
                return when {
                    other == null -> false
                    other !is Option -> false
                    other.code != code -> false
                    else -> fragment === fragment
                }
            }

            override fun hashCode(): Int {
                return code + fragment.hashCode() // не по ссылке
            }
        }
    }

    /**
     * add class if that is not contains
     * @return true if class is added
     */
    fun add(kClass: KClass<out Fragment>): Boolean = addAll(kClass)

    /**
     * add the all classes if no one is contains
     * @return true if all classes is added
     */
    fun addAll(vararg classes: KClass<out Fragment>): Boolean {
        return when {
            containsAny(*classes) -> false
            else -> {
                mFragments.addAll(classes)
                true
            }
        }
    }

    fun isLast(kClass: KClass<out Fragment>) = kClass == mFragments.last()

    private fun containsAny(vararg classes: KClass<out Fragment>): Boolean {
        classes.forEach {
            if (mFragments.contains(it)) {
                return true
            }
        }
        return false
    }
}