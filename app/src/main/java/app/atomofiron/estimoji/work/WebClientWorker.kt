package app.atomofiron.estimoji.work

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.atomofiron.common.util.KObservable
import app.atomofiron.common.util.ServiceConnectionImpl
import app.atomofiron.estimoji.android.ForegroundService
import app.atomofiron.estimoji.injactable.channel.PublicChannel
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.logE
import app.atomofiron.estimoji.model.ConnectionState
import app.atomofiron.estimoji.model.JsonClientFrame
import app.atomofiron.estimoji.model.JsonServerFrame
import app.atomofiron.estimoji.util.Const
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
                // todo replace delay with callback from server
                true -> builder.setInitialDelay(Const.CLIENT_SLEEP_PERIOD, TimeUnit.MILLISECONDS)
                else -> inputBuilder.putString(KEY_IP_JOIN, ipJoin)
            }
            builder.setInputData(inputBuilder.build())
            return builder.build()
        }
    }

    private val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val onClearedCallback = KObservable.RemoveObserverCallback()

    private val nickname = workerParameters.inputData.getString(KEY_NICKNAME)!!
    private val password = workerParameters.inputData.getString(KEY_PASSWORD)!!
    private val ipJoin = workerParameters.inputData.getString(KEY_IP_JOIN) ?: Const.LOCAL_HOST + ":" + Const.PORT

    private lateinit var client: HttpClient
    private var session: DefaultClientWebSocketSession? = null

    private var result = Result.success()
    private val isActive: Boolean get() = (session?.isActive != false) && result is Result.Success

    private val connection = ServiceConnectionImpl()

    override fun onStopped() {
        super.onStopped()
        logD("onStopped")
        GlobalScope.launch {
            session?.outgoing?.send(Frame.Text(JsonClientFrame.Leave().toJson()))
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
        PublicChannel.appPaused.addObserver(onClearedCallback) {
            GlobalScope.launch {
                val frame = when {
                    it -> JsonClientFrame.Sleep()
                    else -> JsonClientFrame.Waking()
                }
                session?.outgoing?.send(Frame.Text(frame.toJson()))
            }
        }
        PublicChannel.chose.addObserver(onClearedCallback) {
            GlobalScope.launch {
                logD("WTF $it")
                session?.outgoing?.send(Frame.Text(JsonClientFrame.Chose(it).toJson()))
            }
        }

        while (!isStopped && isActive) {
            Thread.sleep(Const.CLIENT_SLEEP_PERIOD)
        }

        stopForegroundService()
        onClearedCallback.invoke()

        logD("doWork end")
        return result
    }

    private suspend fun connect(scope: CoroutineScope) {
        try {
            client.ws(::requestBuilder) {
                session = this
                val shareIpJoin = when {
                    ipJoin.startsWith(Const.LOCAL_HOST) -> {
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
            is JsonServerFrame.Forbidden -> {
                logD("---> Forbidden")
                PublicChannel.connectionStatus.setAndNotify(ConnectionState.Forbidden)
            }
            is JsonServerFrame.Authorized -> {
                logD("---> Authorized")
                PublicChannel.connectionStatus.setAndNotify(ConnectionState.Authorized)
            }
            is JsonServerFrame.Users -> {
                logD("---> Users")
                PublicChannel.users.setAndNotify(frame.users)
            }
            is JsonServerFrame.UserJoin -> {
                logD("---> UserJoin")
                PublicChannel.users.updateAndNotify {
                    it.toMutableList().apply { add(frame.user) }
                }
            }
            is JsonServerFrame.UserUpdate -> {
                logD("---> UserUpdate")
                PublicChannel.users.updateAndNotify { users ->
                    users.toMutableList().apply {
                        val index = indexOf(frame.user)
                        if (index == Const.UNKNOWN) {
                            add(frame.user)
                        } else {
                            removeAt(index)
                            logD("frame.user ${frame.user.chose}")
                            add(index, frame.user)
                        }
                    }
                }
            }
            is JsonServerFrame.UserLeave -> {
                logD("---> UserLeave")
                PublicChannel.users.updateAndNotify { users ->
                    users.toMutableList().apply {
                        val old = find { it.nickname == frame.nickname }
                        remove(old)
                    }
                }
            }
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