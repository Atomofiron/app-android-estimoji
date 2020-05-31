package app.atomofiron.estimoji

import android.util.Log

private var timestamp: Long = 0
private var nanotimestamp: Long = 0

private const val mute = false
private const val delay = false

fun Any.sleep(t: Long) = if (delay) Thread.sleep(t) else Unit

fun Any.log(s: String) = Unit//logD(s)

fun Any.logE(s: String) {
    Log.e("regextool", "[ERROR] [${this.javaClass.simpleName}] $s")
}

fun Any.logD(s: String) = logD(this.javaClass.simpleName, s)

fun Any.logD(context: Any, s: String) = logD(context.javaClass.simpleName, s)

fun Any.logD(label: String, s: String) {
    Log.e("regextool", "[$label] $s")
}

fun Any.logI(s: String) {
    if (mute) return

    Log.d("regextool", "[${this.javaClass.simpleName}] $s")
}

fun Any.tik(s: String) {
    if (mute) return

    val now = System.currentTimeMillis()
    val dif = now - timestamp
    timestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}

fun Any.natik(s: String) {
    if (mute) return

    val now = System.nanoTime()
    val dif = now - nanotimestamp
    nanotimestamp = now
    Log.e("regextool", "[${this.javaClass.simpleName}] $dif $s")
}
