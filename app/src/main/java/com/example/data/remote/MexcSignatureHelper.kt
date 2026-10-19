package com.example.data.remote

import java.nio.charset.StandardCharsets
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object MexcSignatureHelper {
    fun createHmacSha256(data: String, secret: String): String {
        return try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256")
            sha256Hmac.init(secretKey)
            val hash = sha256Hmac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
            bytesToHex(hash)
        } catch (e: Exception) {
            ""
        }
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val i = byte.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
    }
}
