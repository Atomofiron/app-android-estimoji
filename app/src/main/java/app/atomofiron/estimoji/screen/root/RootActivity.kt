package app.atomofiron.estimoji.screen.root

import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AlertDialog
import androidx.work.WorkManager
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.log
import app.atomofiron.estimoji.screen.base.BaseActivity
import app.atomofiron.estimoji.util.Knife
import app.atomofiron.estimoji.util.Util
import app.atomofiron.estimoji.work.KtorServerWorker
import app.atomofiron.estimoji.work.WebClientWorker
import kotlin.reflect.KClass

class RootActivity : BaseActivity<RootViewModel>() {

    override val viewModelClass: KClass<RootViewModel> = RootViewModel::class

    private val flRoot = Knife<FrameLayout>(this, R.id.root_bfl)

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        viewModel.onIntent(intent)

        if (savedInstanceState == null) {
            val workManager = WorkManager.getInstance(applicationContext)
            workManager.cancelUniqueWork(KtorServerWorker.NAME)
            workManager.cancelUniqueWork(WebClientWorker.NAME)
        }
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
