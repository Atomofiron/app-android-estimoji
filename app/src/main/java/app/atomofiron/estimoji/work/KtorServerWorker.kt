package app.atomofiron.estimoji.work

import android.content.Context
import androidx.work.*
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.logE
import app.atomofiron.estimoji.model.JsonClientFrame
import app.atomofiron.estimoji.model.JsonServerFrame
import app.atomofiron.estimoji.model.User
import app.atomofiron.estimoji.util.Const
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketServerSession

class KtorServerWorker(context: Context, workerParameters: WorkerParameters) : Worker(context, workerParameters) {
    companion object {
        val NAME = KtorServerWorker::class.java.simpleName

        private const val KEY_PASSWORD = "KEY_PASSWORD"
        private const val KEY_NICKNAME = "KEY_NICKNAME"

        fun create(nickname: String, password: String): OneTimeWorkRequest {
            val builder = OneTimeWorkRequest.Builder(KtorServerWorker::class.java)
            val inputBuilder = Data.Builder()
            inputBuilder.putString(KEY_PASSWORD, password)
            inputBuilder.putString(KEY_NICKNAME, nickname)
            builder.setInputData(inputBuilder.build())
            return builder.build()
        }
    }

    private val adminNickname = workerParameters.inputData.getString(KEY_NICKNAME)
    private val adminPassword = workerParameters.inputData.getString(KEY_PASSWORD)

    private lateinit var server: NettyApplicationEngine

    private val clients = ArrayList<Client>()

    override fun onStopped() {
        logD("onStopped")
        server.stop(3000L, 3000L)
    }

    override fun doWork(): Result {
        logD("doWork")
        server = embeddedServer(Netty, port = Const.PORT) {
            install(WebSockets)
            routing {
                get(Const.ROOT) {
                    call.respondText("Auth.html", ContentType.Text.Plain)
                }
                webSocket(Const.ROOT) {
                    val params = call.request.queryParameters
                    val nickname = params[Const.NICKNAME]
                    val password = params[Const.PASSWORD]

                    when {
                        password != adminPassword -> {
                            logD("WRONG nickname $nickname password $password")
                            outgoing.send(Frame.Text(JsonServerFrame.Forbidden().toJson()))
                            return@webSocket
                        }
                        nickname == null || clients.find { it.nickname == nickname } != null -> {
                            logD("WRONG nickname $nickname password $password")
                            outgoing.send(Frame.Text(JsonServerFrame.Forbidden().toJson()))
                            return@webSocket
                        }
                        else -> {
                            outgoing.send(Frame.Text(JsonServerFrame.Authorized().toJson()))
                            logD("PASSED nickname $nickname password $password")
                        }
                    }
                    val client = Client(nickname, nickname == adminNickname, this, clients)
                    clients.forEach {
                        it.sendUserJoin(client.user)
                    }
                    clients.add(client)
                    val users = clients.map { it.user }
                    client.sendUsers(users)
                    logD("webSockett $nickname end")
                    client.listenFrames()
                }
            }
        }
        server.start(wait = true)

        logD("doWork end")
        return Result.success()
    }

    private class Client(
        val nickname: String,
        isAdmin: Boolean,
        private val session: DefaultWebSocketServerSession,
        private val clients: MutableList<Client>
    ) {
        var user = User(nickname, isAdmin, User.STATUS_WAKING, "")
            private set

        suspend fun listenFrames() = session.listenFrames()

        suspend fun DefaultWebSocketServerSession.listenFrames() {
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> onMessage(frame.readText())
                    is Frame.Close -> logD("$nickname: Frame.Close")
                    else -> logE("frameType ${frame.frameType}")
                }
            }
        }

        private suspend fun onMessage(message: String) {
            val frame = JsonClientFrame.from(message)
            when (frame) {
                is JsonClientFrame.Waking -> {
                    logD("<- Online")
                    user = user.copy(status = User.STATUS_WAKING)
                    clients.forEach { it.sendUserUpdate(user) }
                }
                is JsonClientFrame.Sleep -> {
                    logD("<- Sleep")
                    user = user.copy(status = User.STATUS_SLIPPING)
                    clients.forEach { it.sendUserUpdate(user) }
                }
                is JsonClientFrame.Chose -> {
                    logD("<- Chose ${frame.chose}")
                    user = user.copy(chose = frame.chose)
                    val allVote = clients.find { it.user.chose.isEmpty() } == null
                    if (allVote) {
                        val users = clients.map { it.user }
                        clients.forEach { it.sendUsers(users) }
                    } else {
                        clients.forEach { it.sendUserUpdate(user.copy(chose = "?")) }
                    }
                }
                is JsonClientFrame.Leave -> {
                    logD("<- Leave")
                    val client = clients.find { it.user.nickname == user.nickname }
                    clients.remove(client)
                    clients.forEach { it.sendUserLeave(user) }
                }
                else -> throw Exception("Unknown frame $frame!")
            }
        }

        suspend fun sendUsers(users: List<User>) {
            logD("sendUsers ${users.size}")
            val allVote = clients.find { it.user.chose.isEmpty() } == null
            if (allVote) {
                session.outgoing.send(Frame.Text(JsonServerFrame.Users(users).toJson()))
            } else {
                val items = users.map { it.copy(chose = if (it.chose.isEmpty()) "" else "?") }
                session.outgoing.send(Frame.Text(JsonServerFrame.Users(items).toJson()))
            }
        }

        suspend fun sendUserJoin(user: User) {
            logD("sendUserJoin ${user.nickname}")
            session.outgoing.send(Frame.Text(JsonServerFrame.UserJoin(user).toJson()))
        }

        suspend fun sendUserUpdate(user: User) {
            logD("sendUserUpdate ${user.nickname}")
            session.outgoing.send(Frame.Text(JsonServerFrame.UserUpdate(user).toJson()))
        }

        suspend fun sendUserLeave(user: User) {
            logD("sendUserLeave ${user.nickname}")
            session.outgoing.send(Frame.Text(JsonServerFrame.UserLeave(user.nickname).toJson()))
        }
    }
}