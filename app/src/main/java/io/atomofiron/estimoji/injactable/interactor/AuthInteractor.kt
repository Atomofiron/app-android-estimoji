package io.atomofiron.estimoji.injactable.interactor

import androidx.work.ExistingWorkPolicy
import androidx.work.Operation
import androidx.work.WorkManager
import io.atomofiron.estimoji.App
import io.atomofiron.estimoji.work.KtorServerWorker
import io.atomofiron.estimoji.work.WebClientWorker

class AuthInteractor {

    private val workManager: WorkManager by lazy(LazyThreadSafetyMode.NONE) {
        WorkManager.getInstance(App.appContext)
    }

    fun create(nickname: String, password: String) {
        val request = KtorServerWorker.create(nickname, password)
        val continuation = workManager.beginUniqueWork(
            KtorServerWorker.NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        continuation.enqueue()
    }

    fun join(nickname: String, password: String, ipJoin: String, callback: (Operation.State) -> Unit) {
        val request = WebClientWorker.create(nickname, password, ipJoin)
        val continuation = workManager.beginUniqueWork(
            WebClientWorker.NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
        continuation.enqueue().state.observeForever(callback)
    }
}