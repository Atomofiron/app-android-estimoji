package io.atomofiron.estimoji.screen.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.screen.base.BaseFragment
import io.atomofiron.estimoji.util.*
import kotlin.reflect.KClass

class AuthFragment : BaseFragment<AuthViewModel>() {

    override val viewModelClass: KClass<AuthViewModel> = AuthViewModel::class
    override val layoutId: Int = R.layout.fragment_auth

    private val toolbar = Knife<Toolbar>(this, R.id.auth_toolbar)
    private val etNickname = Knife<EditText>(this, R.id.auth_et_nickname)
    private val btnCreate = Knife<Button>(this, R.id.auth_btn_create)
    private val flJoin = Knife<FrameLayout>(this, R.id.auth_fl_join)
    private val btnJoin = Knife<Button>(this, R.id.auth_btn_join)
    private val llJoin = Knife<LinearLayout>(this, R.id.auth_ll_join)
    private val etJoin = Knife<EditText>(this, R.id.auth_et_join)
    private val btnScan = Knife<AppCompatImageButton>(this, R.id.auth_ib_qr)
    private val fabCards = Knife<FloatingActionButton>(this, R.id.auth_fab_cards)

    private val nickname: String get() = etNickname.view.text.toString()
    private val joinAddressWatcher = JoinAddressWatcher()
    private val joinAddress: String? get() = joinAddressWatcher.value

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.view.setOnMenuItemClickListener(::onOptionsItemSelected)
        etNickname.view.addTextChangedListener(TextWatcherImpl(
            afterTextChanged = { editable ->
                setControlsVisibility(editable != null && editable.isNotEmpty())
                viewModel.onNicknameInput(editable?.toString() ?: "")
            }
        ))
        btnCreate.view.setOnClickListener {
            if (nickname.isEmpty()) return@setOnClickListener
            etNickname.view.hideKeyboard()
            viewModel.onCreateClick()
        }
        btnScan.view.setOnClickListener {
            etNickname.view.hideKeyboard()
            viewModel.onScanClick()
        }
        etJoin.view.addTextChangedListener(joinAddressWatcher)
        etJoin.view.setOnClickListener {
            if (nickname.isEmpty()) return@setOnClickListener
            if (joinAddress == null) return@setOnClickListener
            etNickname.view.hideKeyboard()
            viewModel.onJoinClick()
        }
        fabCards.view.setOnClickListener {
            viewModel.onCardsClick()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_settings -> viewModel.onSettingsClick()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSubscribeData(owner: LifecycleOwner) {
        super.onSubscribeData(owner)

        viewModel.nickname.observe(owner, Observer {
            etNickname {
                if (it != null && it.toString() != text.toString()) {
                    setText(it)
                }
            }
        })
        viewModel.ipJoin.observe(owner, Observer {
            etJoin {
                if (it != text.toString()) {
                    setText(it)
                }
            }
        })
    }

    private fun setControlsVisibility(visible: Boolean) {
        TransitionManager.beginDelayedTransition(view as ViewGroup)
        val visibility = if (visible) View.VISIBLE else View.GONE
        btnCreate.view.visibility = visibility
        flJoin.view.visibility = visibility
        llJoin.view.visibility = visibility
    }

    private fun setJoinButtonVisibility(visible: Boolean) {
        TransitionManager.beginDelayedTransition(view as ViewGroup)
        val visibility = if (visible) View.VISIBLE else View.INVISIBLE
        btnJoin.view.visibility = visibility
        btnJoin.view.isEnabled = visible
    }

    private inner class JoinAddressWatcher : TextWatcher {
        var value: String = ""

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun afterTextChanged(s: Editable?) {
            val after = s?.toString() ?: ""
            val matcher = Util.patternIpPort.matcher(after)
            val matches = matcher.matches()
            setJoinButtonVisibility(matches)
            value = if (matches) after else ""
            viewModel.onIpInput(value)
        }
    }
}