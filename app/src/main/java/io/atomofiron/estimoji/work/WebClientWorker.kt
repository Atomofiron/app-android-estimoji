package io.atomofiron.estimoji.work

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.atomofiron.estimoji.logD
import io.atomofiron.estimoji.logE
import io.atomofiron.estimoji.logI
import io.atomofiron.estimoji.util.Const
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class WebClientWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    companion object {
        val NAME = WebClientWorker::class.java.simpleName

        private const val KEY_ERROR = "KEY_ERROR"

        private const val KEY_PASSWORD = "KEY_PASSWORD"
        private const val KEY_NICKNAME = "KEY_NICKNAME"
        private const val KEY_IP_JOIN = "KEY_IP_JOIN"

        fun create(nickname: String, password: String, ipJoin: String): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(WebClientWorker::class.java)
            val inputBuilder = Data.Builder()
            inputBuilder.putString(KEY_PASSWORD, password)
            inputBuilder.putString(KEY_NICKNAME, nickname)
            when (ipJoin.isBlank()) {
                true -> builder.setInitialDelay(Const.CLIENT_SLEEP_PERIOD, TimeUnit.MILLISECONDS)
                else -> inputBuilder.putString(KEY_IP_JOIN, ipJoin)
            }
            builder.setInputData(inputBuilder.build())
            return builder.build()
        }
    }

    private val nickname = workerParameters.inputData.getString(KEY_NICKNAME)!!
    private val password = workerParameters.inputData.getString(KEY_PASSWORD)!!
    private val ipJoin = workerParameters.inputData.getString(KEY_IP_JOIN) ?: Const.LOCAL_HOST + ":" + Const.PORT

    private lateinit var client: HttpClient
    private var session: DefaultClientWebSocketSession? = null

    private var result = Result.success()
    private val isActive: Boolean get() = (session?.isActive == true) && result is Result.Success

    override fun onStopped() {
        super.onStopped()
        logD("onStopped")
        GlobalScope.launch {
            close(CloseReason.Codes.GOING_AWAY)
        }
    }

    override fun doWork(): Result {
        logD("doWork")
        client = HttpClient {
            install(WebSockets)
        }
        GlobalScope.launch(block = ::connect)

        while (!isStopped && isActive) {
            Thread.sleep(Const.CLIENT_SLEEP_PERIOD)
        }

        logD("doWork end")
        return result
    }

    private suspend fun connect(scope: CoroutineScope) {
        try {
            client.ws(::requestBuilder) {
                session = this

                for (frame in incoming) when (frame) {
                    is Frame.Text -> onMessage(frame.readText())
                    else -> logE("frameType ${frame.frameType}")
                }
            }
        } catch (e: java.lang.Exception) {
            logE("connect $e")
            val builder = Data.Builder()
            builder.putString(KEY_ERROR, e.toString())
            result = Result.failure(builder.build())
        }
    }

    private fun onMessage(message: String) {
        val frame = JsonServerFrame.from(message)
        when (frame) {
            is JsonServerFrame.Forbidden -> logD("Forbidden!!!")
            is JsonServerFrame.Authorized -> logD("Authorized!!!")
            is JsonServerFrame.Users -> logD("Users!!!")
            else -> throw Exception("Unknown frame $frame!")
        }
    }

    private fun sendMessage(message: String) {
        GlobalScope.launch {
            session?.send(Frame.Text(message))
        }
    }

    private fun requestBuilder(builder: HttpRequestBuilder) {
        val hostPort = ipJoin.split(Const.PORT_SEPARATOR)
        val host = hostPort[0]
        val port = hostPort[1].toInt()
        builder.url.host = host
        builder.url.port = port
        builder.url.parameters.let {
            it[Const.NICKNAME] = nickname
            it[Const.PASSWORD] = password
        }
        builder.method = HttpMethod.Get
    }

    private suspend fun close(code: CloseReason.Codes) {
        logD("code $code")
        session?.close(CloseReason(code, ""))
        client.close()
    }
}