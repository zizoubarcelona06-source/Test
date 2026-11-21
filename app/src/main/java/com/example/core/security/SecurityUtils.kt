package com.example.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object SecurityUtils {
    fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$password".toByteArray(Charsets.UTF_8)
        val hashed = digest.digest(input)
        return hashed.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        return hashPassword(password, salt) == expectedHash
    }
}
