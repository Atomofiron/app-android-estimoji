package io.atomofiron.estimoji.screen.poker

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.recycler.NanoAdapter
import io.atomofiron.estimoji.screen.base.BaseFragment
import io.atomofiron.estimoji.util.CustomAppBarLayoutBehavior
import io.atomofiron.estimoji.util.Knife
import io.atomofiron.estimoji.util.OnToolbarCollapsedListener
import io.atomofiron.estimoji.util.findResIdByAttr
import kotlin.reflect.KClass

class PokerFragment : BaseFragment<PokerViewModel>() {
    companion object {
        const val KEY_NICKNAME = "KEY_NICKNAME"

        fun create(nickname: String): Fragment {
            val bundle = Bundle()
            bundle.putString(KEY_NICKNAME, nickname)
            val fragment = PokerFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClass: KClass<PokerViewModel> = PokerViewModel::class
    override val layoutId: Int = R.layout.fragment_poker
    override val systemBarsColorId: Int get() = context!!.findResIdByAttr(R.attr.colorPrimary)
    override val systemBarsLights: Boolean = false

    private val appBar = Knife<AppBarLayout>(this, R.id.app_bar)
    private val toolbar = Knife<Toolbar>(this, R.id.poker_toolbar)
    private val collapsing = Knife<CollapsingToolbarLayout>(this, R.id.poker_ctl)
    private val translucent = Knife<View>(this, R.id.poker_v_translucent)
    private val llShare = Knife<LinearLayout>(this, R.id.poker_ll_share)
    private val ivShare = Knife<ImageView>(this, R.id.poker_iv_share)
    private val tvShare = Knife<TextView>(this, R.id.poker_tv_share)
    private val fabShare = Knife<FloatingActionButton>(this, R.id.poker_fab_share)
    private val fabCards = Knife<FloatingActionButton>(this, R.id.poker_fab_cards)
    private val recyclerView = Knife<RecyclerView>(this, R.id.main_cv_recycler)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.view.layoutManager = LinearLayoutManager(view.context)
        recyclerView.view.adapter = NanoAdapter(R.layout.item_dude)
        fabCards.view.setOnClickListener { viewModel.onCardsClick() }
        fabShare.view.setOnClickListener { onShareClick() }

        toolbar {
            setNavigationOnClickListener { viewModel.onExitClick() }
            setOnMenuItemClickListener(::onOptionsItemSelected)
        }

        val listener = OnToolbarCollapsedListener(toolbar.view, onCollapsed = { showSharing(false) })
        appBar.view.addOnOffsetChangedListener(listener)
        (appBar.view.layoutParams as CoordinatorLayout.LayoutParams)
            .behavior = CustomAppBarLayoutBehavior(appBar.view)
    }

    private fun onShareClick() {
        showSharing(llShare.view.visibility != View.VISIBLE)
    }

    private fun showSharing(show: Boolean) {
        llShare {
            if ((visibility == View.VISIBLE) == show) {
                return@llShare
            }

            if (show) {
                visibility = View.VISIBLE
                fabShare.view.setImageResource(R.drawable.ic_close)
                translucent.view.layoutParams.height = toolbar.view.layoutParams.height
                val height = resources.getDimensionPixelOffset(R.dimen.share_layout)
                appBar.view.layoutParams.height = height
                toolbar.view.title = ""
                collapsing.view.title = ""
            } else {
                visibility = View.GONE
                fabShare.view.setImageResource(R.drawable.ic_share)
                translucent.view.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                val height = resources.getDimensionPixelOffset(R.dimen.app_bar_height)
                appBar.view.layoutParams.height = height
                toolbar.view.title = viewModel.nickname.value
                collapsing.view.title = viewModel.nickname.value
            }
            // fixes icon loss
            fabShare.view.hide()
            fabShare.view.show()
        }
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        super.onSubscribeData(owner)

        viewModel.nickname.observe(owner, Observer(toolbar.view::setTitle))
        viewModel.shareBitmap.observe(owner, Observer(ivShare.view::setImageBitmap))
        viewModel.shareAddress.observe(owner, Observer(tvShare.view::setText))
    }

    override fun onUnsubscribeData(owner: LifecycleOwner) {
        super.onUnsubscribeData(owner)

        viewModel.shareBitmap.removeObservers(owner)
        viewModel.shareAddress.removeObservers(owner)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> viewModel.onSettingsClick()
        }
        return super.onOptionsItemSelected(item)
    }
}
