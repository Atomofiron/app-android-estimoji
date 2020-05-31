package app.atomofiron.estimoji.screen.base

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import app.atomofiron.common.arch.view.Backable
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.log
import app.atomofiron.estimoji.util.findBooleanByAttr
import kotlin.reflect.KClass

abstract class BaseFragment<M : BaseViewModel<*>> : Fragment(), Backable {
    protected abstract val viewModelClass: KClass<M>
    protected lateinit var viewModel: M
    protected val dataProvider: M get() = viewModel

    protected abstract val layoutId: Int
    protected open val systemBarsColorId: Int = R.color.transparent
    protected open val systemBarsLights: Boolean get() = !context!!.findBooleanByAttr(R.attr.isDarkTheme)

    val thisContext: Context get() = requireContext()
    val thisActivity: AppCompatActivity get() = requireActivity() as AppCompatActivity
    val thisView: View get() = requireView()
    val thisArguments: Bundle get() = requireArguments()

    override fun onCreate(savedInstanceState: Bundle?) {
        log("onCreate")
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(viewModelClass.java)
        viewModel.onFragmentAttach(this)
        viewModel.onCreate(context!!, arguments)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = LayoutInflater.from(context).inflate(layoutId, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        log("onViewCreated")
        super.onViewCreated(view, savedInstanceState)
        viewModel.onShow()
        onSubscribeData(viewLifecycleOwner)
    }

    override fun onStart() {
        log("onStart")
        super.onStart()

        // todo bad idea do this in onStart()
        setStatusBarColor(systemBarsColorId)
        fixSystemBars(systemBarsLights)
    }

    override fun onResume() {
        log("onResume")
        super.onResume()
    }

    open fun onSubscribeData(owner: LifecycleOwner) = Unit

    open fun onUnsubscribeData(owner: LifecycleOwner) = Unit

    override fun onHiddenChanged(hidden: Boolean) {
        log("onHiddenChanged $hidden")
        super.onHiddenChanged(hidden)

        if (!hidden) {
            viewModel.onShow()
            setStatusBarColor(systemBarsColorId)
            fixSystemBars(systemBarsLights)
        }
    }

    override fun onDestroyView() {
        log("onDestroyView")
        super.onDestroyView()
    }

    override fun onDestroy() {
        log("onDestroy")
        viewModel.onViewDestroy()
        super.onDestroy()
    }

    private fun setStatusBarColor(colorId: Int) {
        val activity = activity as AppCompatActivity
        val color = ContextCompat.getColor(activity, colorId)
        activity.window.statusBarColor = color
        activity.window.navigationBarColor = color
    }

    private fun fixSystemBars(windowLightBars: Boolean) {
        // fix of the bug, when the flag is not applied by the system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity!!.window.decorView.apply {
                systemUiVisibility = when {
                    windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    systemUiVisibility = when {
                        windowLightBars -> systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        else -> systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                }
            }
        }
    }
}