package com.example.util

import java.security.MessageDigest

object PasswordHasher {
    private const val SALT = "Matematika_Milliy_Sertifikat_2026_Salt"

    fun hashPassword(password: String): String {
        val input = password + SALT
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        return hashPassword(password) == storedHash
    }
}
