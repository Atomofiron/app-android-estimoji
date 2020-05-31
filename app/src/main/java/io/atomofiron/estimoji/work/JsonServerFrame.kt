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
    class Forbidden : JsonServerFrame(Types.FORBIDDEN.value)
    class Authorized : JsonServerFrame(Types.AUTHORIZED.value)
    class Users : JsonServerFrame(Types.USERS.value)
    class UserJoin : JsonServerFrame(Types.USER_JOIN.value)
    class UserUpdate : JsonServerFrame(Types.USERS_UPDATE.value)
    class UserLeave : JsonServerFrame(Types.USERS_LEAVE.value)

    fun define(frame: String): JsonServerFrame {
        val enumType = Types.values().find { it.value == type }!!
        return gson.fromJson(frame, enumType.kClass.java)
    }

    fun toJson(): String = gson.toJson(this)

    private enum class Types(val value: String, val kClass: KClass<out JsonServerFrame>) {
        FORBIDDEN("forbidden", Forbidden::class),
        AUTHORIZED("authorized", Authorized::class),
        USERS("users", Users::class),
        USER_JOIN("user_join", UserJoin::class),
        USERS_UPDATE("user_update", UserUpdate::class),
        USERS_LEAVE("user_leave", UserLeave::class)
    }
}