package app.atomofiron.estimoji.injactable.service

import app.atomofiron.estimoji.App
import app.atomofiron.estimoji.R
import app.atomofiron.estimoji.logD
import app.atomofiron.estimoji.util.Const
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket

class KtorServerService {

    private lateinit var adminNickname: String
    private lateinit var adminPassword: String

    private lateinit var server: NettyApplicationEngine

    fun start(nickname: String, password: String) {
        logD("start")
        adminNickname = nickname
        adminPassword = password

        doWork()
    }

    fun stop() {
        logD("stop")
        server.stop(3000L, 3000L)
    }

    fun doWork() {
        logD("doWork")
        server = embeddedServer(Netty, port = Const.PORT) {
            install(WebSockets)
            routing {
                get("/") {
                    call.respondText("Auth.html", ContentType.Text.Plain)
                }
                webSocket("/") {
                    val params = call.request.queryParameters
                    val nickname = params[Const.NICKNAME]
                    val password = params[Const.PASSWORD]

                    if (password != adminPassword) {
                        logD("WRONG nickname $nickname password $password")
                        val error = App.appContext.getString(R.string.fuckng_wronj_password)
                        //outgoing.send()
                        outgoing.send(Frame.Text("NUu"))
                        outgoing.send(Frame.Close(/*CloseReason(CloseReason.Codes.CANNOT_ACCEPT, error)*/))
                        return@webSocket
                    } else {
                        logD("PASSED nickname $nickname password $password")
                    }
                    for (frame in incoming) {
                        when (frame) {
                            is Frame.Text -> {
                                onMessage(frame.readText())
                                outgoing.send(Frame.Text("got it"))
                            }
                            is Frame.Close -> logD("Frame.Close")
                        }
                    }
                    logD("webSockett $nickname end")
                }
            }
        }
        server.start(wait = true)
    }

    private fun onMessage(message: String) {
        logD("onMessage $message")
    }
}