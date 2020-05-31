package io.atomofiron.estimoji.work

import com.google.gson.Gson
import kotlin.reflect.KClass

sealed class JsonServerFrame constructor(val type: String?) {
    companion object {
        private val gson = Gson()

        fun from(frame: String): JsonServerFrame {
            val jsonFrame = gson.fromJson(frame, Unparsed::class.java)
            return jsonFrame.define(frame)
        }
    }

    private class Unparsed : JsonServerFrame(null)
    class Forbidden : JsonServerFrame(Types.FORBIDDEN.type)
    class Authorized : JsonServerFrame(Types.AUTHORIZED.type)
    class Users : JsonServerFrame(Types.USERS.type)
    class UserJoin : JsonServerFrame(Types.USER_JOIN.type)
    class UserUpdate : JsonServerFrame(Types.USERS_UPDATE.type)
    class UserLeave(val nickname: String) : JsonServerFrame(Types.USERS_LEAVE.type)

    fun define(frame: String): JsonServerFrame {
        val enumType = Types.values().find { it.type == type }!!
        return gson.fromJson(frame, enumType.kClass.java)
    }

    fun toJson(): String = gson.toJson(this)

    private enum class Types(val type: String, val kClass: KClass<out JsonServerFrame>) {
        FORBIDDEN("forbidden", Forbidden::class),
        AUTHORIZED("authorized", Authorized::class),
        USERS("users", Users::class),
        USER_JOIN("user_join", UserJoin::class),
        USERS_UPDATE("user_update", UserUpdate::class),
        USERS_LEAVE("user_leave", UserLeave::class)
    }
}