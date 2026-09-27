package com.example.pesatrack.util

import java.security.MessageDigest

object SecurityUtil {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val hasMinLength: Boolean = false,
        val hasUppercase: Boolean = false,
        val hasDigit: Boolean = false,
        val hasSpecialChar: Boolean = false
    )

    fun validatePassword(password: String): ValidationResult {
        val hasMinLength = password.length >= 8
        val hasUppercase = password.any { it.isUpperCase() }
        val hasDigit = password.any { it.isDigit() }
        val specialCharacters = "!@#$%^&*()_+-=[]{};':\"|,.<>/?`~"
        val hasSpecialChar = password.any { specialCharacters.contains(it) }

        val isValid = hasMinLength && hasUppercase && hasDigit && hasSpecialChar

        val message = when {
            !hasMinLength -> "Password must be at least 8 characters"
            !hasUppercase -> "Password must include at least one capital letter (A-Z)"
            !hasDigit -> "Password must include at least one number (0-9)"
            !hasSpecialChar -> "Password must include at least one special character (!@#$%^&*...)"
            else -> null
        }

        return ValidationResult(
            isValid = isValid,
            errorMessage = message,
            hasMinLength = hasMinLength,
            hasUppercase = hasUppercase,
            hasDigit = hasDigit,
            hasSpecialChar = hasSpecialChar
        )
    }

    fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        val hashed = hashPassword(password)
        return hashed.equals(storedHash, ignoreCase = true)
    }
}
