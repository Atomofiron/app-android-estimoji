package io.atomofiron.estimoji.work

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.atomofiron.common.util.ServiceConnectionImpl
import io.atomofiron.estimoji.android.ForegroundService
import io.atomofiron.estimoji.injactable.channel.PublicChannel
import io.atomofiron.estimoji.logD
import io.atomofiron.estimoji.logE
import io.atomofiron.estimoji.model.JsonServerFrame
import io.atomofiron.estimoji.util.Const
import io.ktor.client.HttpClient
import io.ktor.client.features.websocket.DefaultClientWebSocketSession
import io.ktor.client.features.websocket.WebSockets
import io.ktor.client.features.websocket.ws
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.close
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.math.BigInteger
import java.net.Inet4Address
import java.net.InetAddress
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

    private val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val nickname = workerParameters.inputData.getString(KEY_NICKNAME)!!
    private val password = workerParameters.inputData.getString(KEY_PASSWORD)!!
    private val ipJoin = workerParameters.inputData.getString(KEY_IP_JOIN) ?: Const.LOCAL_HOST + ":" + Const.PORT

    private lateinit var client: HttpClient
    private var session: DefaultClientWebSocketSession? = null

    private var result = Result.success()
    private val isActive: Boolean get() = (session?.isActive == true) && result is Result.Success

    val connection = ServiceConnectionImpl()

    override fun onStopped() {
        super.onStopped()
        logD("onStopped")
        GlobalScope.launch {
            close(CloseReason.Codes.GOING_AWAY)
        }
    }

    private fun startForegroundService() {
        val intent = Intent(applicationContext, ForegroundService::class.java)
        applicationContext.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    private fun stopForegroundService() = applicationContext.unbindService(connection)

    override fun doWork(): Result {
        logD("doWork")
        client = HttpClient {
            install(WebSockets)
        }
        GlobalScope.launch(block = ::connect)

        startForegroundService()

        while (!isStopped && isActive) {
            Thread.sleep(Const.CLIENT_SLEEP_PERIOD)
        }

        stopForegroundService()

        logD("doWork end")
        return result
    }

    private suspend fun connect(scope: CoroutineScope) {
        try {
            client.ws(::requestBuilder) {
                session = this
                val loopbackAddress = Inet4Address.getLocalHost().hostAddress
                val shareIpJoin = when {
                    ipJoin.startsWith(loopbackAddress) -> {
                        val hostPort = ipJoin.split(Const.PORT_SEPARATOR)
                        val port = hostPort[1].toInt()
                        val ipAddress = wifiManager.connectionInfo.ipAddress.toLong()
                        val array = BigInteger.valueOf(ipAddress).toByteArray()
                        array.reverse()
                        Inet4Address.getByAddress(array).hostAddress + Const.PORT_SEPARATOR + port
                    }
                    else -> ipJoin
                }
                PublicChannel.ipJoin.setAndNotify(shareIpJoin)

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