package io.atomofiron.estimoji.work

import com.google.gson.Gson
import kotlin.reflect.KClass

sealed class JsonClientFrame constructor(val type: String?) {
    companion object {
        private val gson = Gson()

        fun from(frame: String): JsonClientFrame {
            val jsonFrame = gson.fromJson(frame, Unparsed::class.java)
            return jsonFrame.define(frame)
        }
    }

    private class Unparsed : JsonClientFrame(null)
    class Online : JsonClientFrame(Types.ONLINE.value)
    class Chose(val chose: String) : JsonClientFrame(Types.CHOSE.value)

    fun define(frame: String): JsonClientFrame {
        val enumType = Types.values().find { it.value == type }!!
        return gson.fromJson(frame, enumType.kClass.java)
    }

    fun toJson(): String = gson.toJson(this)

    private enum class Types(val value: String, val kClass: KClass<out JsonClientFrame>) {
        ONLINE("online", Online::class),
        CHOSE("chose", Chose::class),
    }
}