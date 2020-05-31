package app.atomofiron.estimoji.util

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.util.regex.Pattern

object Util {
    var isDarkTheme = false // todo service
    const val HOST = "estimoji.io"
    val patternIp = Pattern.compile("^((1?[0-9]?[0-9]|2[0-5][0-5])\\.){3}(1?[0-9]?[0-9]|2[0-5][0-5])$") // todo check ^$
    val patternIpPort = Pattern.compile("((1?[0-9]?[0-9]|2[0-5][0-5])\\.){3}(1?[0-9]?[0-9]|2[0-5][0-5])(:[0-9]{1,5})?")
    val patternAddress = Pattern.compile("^https?://((1?[0-9]?[0-9]|2[0-5][0-5])\\.){3}(1?[0-9]?[0-9]|2[0-5][0-5])(:[0-9]{1,5})?/+estimoji.io(?=$|/)")

    fun parseUri(data: Uri?): String? = parseUri(data?.toString())

    fun parseUri(uri: String?): String? {
        val data = uri ?: return null // http://192.168.1.153:777/estimoji.io
        var matcher = patternAddress.matcher(data)
        if (matcher.find()) {
            val address = matcher.group()
            matcher = patternIpPort.matcher(address)
            if (matcher.find()) {
                return matcher.group() // 192.168.1.153:777
            }
        }
        return null
    }

    fun parseUri2(uri: String?): String? {
        var data = uri ?: return null
        data = data.replace(Regex("^[a-z]+:/+"), "")
        if (!data.startsWith(HOST)) {
            return null
        }
        data = data.replace(Regex("^$HOST/*"), "")
        data = data.replace(Regex("/.*"), "")

        val matcher = patternIp.matcher(data)
        return if (matcher.matches()) data else null
    }

    fun encodeAsBitmap(width: Int, height: Int, str: String): Bitmap? {
        val result: BitMatrix
        try {
            result = MultiFormatWriter()
                .encode(str, BarcodeFormat.QR_CODE, width, height, null)
        } catch (iae: IllegalArgumentException) {
            // Unsupported format
            return null
        }

        val w = result.width
        val h = result.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (result.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, w, h)
        return bitmap
    }
}