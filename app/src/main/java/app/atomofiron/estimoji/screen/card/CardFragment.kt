package app.atomofiron.estimoji.screen.card

import android.os.Bundle
import android.view.View
import android.widget.TextView
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.screen.base.BaseFragment
import app.atomofiron.estimoji.util.Knife
import app.atomofiron.estimoji.view.FlippableCardView
import kotlin.reflect.KClass

class CardFragment : BaseFragment<CardViewModel>() {
    companion object {
        private const val EXTRA_VALUE = "EXTRA_VALUE"

        fun new(value: String): CardFragment {
            val bundle = Bundle()
            bundle.putString(EXTRA_VALUE, value)
            val fragment = CardFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val viewModelClass: KClass<CardViewModel> = CardViewModel::class
    override val layoutId: Int = R.layout.fragment_card

    private val flippableCardView = Knife<FlippableCardView>(this, R.id.card_fcv)
    private val tvValue = Knife<TextView>(this, R.id.card_tv_value)

    private val closeCardByDefault = true // todo preferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvValue.view.text = arguments!!.getString(EXTRA_VALUE)

        flippableCardView.view.onOpenedCardClickListener = viewModel::onOpenedCardClick

        if (closeCardByDefault) {
            flippableCardView.view.closeCard()
        }
    }
}
