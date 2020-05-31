package app.atomofiron.estimoji.model

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
    class Waking : JsonClientFrame(
        Types.WAKING.type)
    class Sleep : JsonClientFrame(
        Types.SLEEP.type)
    class Chose(val chose: String) : JsonClientFrame(
        Types.CHOSE.type)
    class Leave() : JsonClientFrame(
        Types.LEAVE.type)

    fun define(frame: String): JsonClientFrame {
        val enumType = Types.values().find { it.type == type }!!
        return gson.fromJson(frame, enumType.kClass.java)
    }

    fun toJson(): String = gson.toJson(this)

    private enum class Types(val type: String, val kClass: KClass<out JsonClientFrame>) {
        WAKING("waking", Waking::class),
        SLEEP("sleep", Sleep::class),
        CHOSE("chose", Chose::class),
        LEAVE("leave", Leave::class),
    }
}