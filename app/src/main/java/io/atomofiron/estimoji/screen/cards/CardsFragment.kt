package io.atomofiron.estimoji.screen.cards

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.recycler.NanoAdapter
import io.atomofiron.estimoji.screen.base.BaseFragment
import io.atomofiron.estimoji.screen.cards.recycler.CardsAdapter
import io.atomofiron.estimoji.screen.cards.recycler.OnCardClickListener
import io.atomofiron.estimoji.util.*
import kotlin.reflect.KClass

class CardsFragment : BaseFragment<CardsViewModel>(), OnCardClickListener<String> {
    companion object {
        private const val ROW_COUNT = 3
    }

    override val viewModelClass: KClass<CardsViewModel> = CardsViewModel::class
    override val layoutId: Int = R.layout.fragment_cards

    private val coordinator = Knife<CoordinatorLayout>(this, R.id.cards_coordinator)
    private val cToolbar = Knife<CollapsingToolbarLayout>(this, R.id.cards_ctl)
    private val appBar = Knife<AppBarLayout>(this, R.id.cards_app_bar)
    private val toolbar = Knife<Toolbar>(this, R.id.cards_toolbar)
    private val btnSelect = Knife<Button>(this, R.id.cards_btn_select)
    private val etRename = Knife<EditText>(this, R.id.cards_btn_rename)
    private val rvCards = Knife<RecyclerView>(this, R.id.cards_rv)
    private val rvPacks = Knife<RecyclerView>(this, R.id.cards_rv_packs)

    private val adapter = CardsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvCards.view.layoutManager = GridLayoutManager(view.context, ROW_COUNT)
        rvPacks.view.layoutManager = LinearLayoutManager(context!!)
        rvPacks.view.adapter = NanoAdapter(R.layout.item_dude)

        val data = ArrayList<String>()
        data.add("0")
        data.add("0.5")
        data.add("1")
        data.add("2")
        data.add("3")
        data.add("5")
        data.add("8")
        data.add("13")
        data.add("20")
        data.add("40")
        data.add("100")
        data.add("∞")
        data.add("?")
        data.add("☕️")
        data.add("\uD83D\uDCA9")
        data.add("\uD83E\uDD14")

        adapter.setData(data)
        adapter.setOnCardClickListener(this)
        rvCards.view.adapter = adapter
        toolbar.view.setOnMenuItemClickListener(::onOptionsItemSelected)

        btnSelect.view.setOnClickListener(::onButtonSelectClick)
        val listener = OnToolbarCollapsedListener(toolbar.view, onCollapsed = { showPacks(false) })
        appBar.view.addOnOffsetChangedListener(listener)
        (appBar.view.layoutParams as CoordinatorLayout.LayoutParams)
            .behavior = CustomAppBarLayoutBehavior(appBar.view)
    }

    override fun onCardClick(item: String) {
        viewModel.onCardClick(item)
    }

    override fun onAddCardConfirm(item: String) {
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_edit -> onEditEnable()
            R.id.menu_cancel -> onEditEnable(enable = false)
            R.id.menu_confirm -> onEditConfirm()
            R.id.menu_add -> viewModel.onAddOptionClick()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onButtonSelectClick(v: View) {
        val defaultHeight = resources.getDimensionPixelSize(R.dimen.toolbar_height)
        showPacks(defaultHeight == appBar.view.height)
    }

    private fun showPacks(show: Boolean = true) {
        val defaultHeight = resources.getDimensionPixelSize(R.dimen.toolbar_height)
        rvPacks.view.visibility = if (show) View.VISIBLE else View.GONE
        appBar {
            layoutParams.height = when (show) {
                true -> defaultHeight + resources.getDimensionPixelSize(R.dimen.cards_pack_list_size)
                false -> defaultHeight
            }
            layoutParams = layoutParams
        }
    }

    private fun onEditEnable(enable: Boolean = true) {
        adapter.editable = enable
        toolbar {
            menu.findItem(R.id.menu_edit).isVisible = !enable
            menu.findItem(R.id.menu_cancel).isVisible = enable
            menu.findItem(R.id.menu_confirm).isVisible = enable
            menu.findItem(R.id.menu_add).isVisible = !enable
        }
        etRename.view.visibility = if (enable) View.VISIBLE else View.GONE
        btnSelect.view.visibility = if (enable) View.GONE else View.VISIBLE
        when (enable) {
            true -> etRename.view.setText(btnSelect.view.text.toString())
            false -> {
                btnSelect.view.text = etRename.view.text
                etRename.view.hideKeyboard()
            }
        }
    }

    private fun onEditConfirm() {
        onEditEnable(enable = false)
    }

    private fun onAddEnable() {

    }
}
