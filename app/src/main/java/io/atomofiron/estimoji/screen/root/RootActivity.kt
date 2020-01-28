package io.atomofiron.estimoji.screen.root

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import io.atomofiron.estimoji.R
import io.atomofiron.estimoji.log
import io.atomofiron.estimoji.screen.base.BaseActivity
import io.atomofiron.estimoji.util.Knife
import io.atomofiron.estimoji.util.Util
import kotlin.reflect.KClass

class RootActivity : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    private val flRoot = Knife<FrameLayout>(this, R.id.root_bfl)

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        viewModel.onIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        viewModel.onIntent(intent)
    }

    fun snack(stringId: Int) {
        Snackbar.make(flRoot.view, stringId, Snackbar.LENGTH_SHORT).show()
    }

    fun askForSession(ip: String) {
        log("askForSession")
        AlertDialog.Builder(this)
            .setTitle(R.string.join_to_another_session)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ ->
                viewModel.onJoinConfirm(ip)
            }.create().show()
    }

    private fun applyTheme() {
        val theme = if (Util.isDarkTheme) R.style.AppTheme_Activity_Dark else R.style.AppTheme
        setTheme(theme)
    }
}
