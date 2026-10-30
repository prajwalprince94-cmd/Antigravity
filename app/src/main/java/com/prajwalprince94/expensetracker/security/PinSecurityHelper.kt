package com.prajwalprince94.expensetracker.security

import java.security.MessageDigest

/**
 * Cryptographic helper for hashing and verifying 4-digit security PINs.
 */
object PinSecurityHelper {

    private const val PIN_SALT = "EXPENSE_TRACKER_SECURE_SALT_V1"

    /**
     * Hashes a 4-digit PIN using salted SHA-256.
     */
    fun hashPin(pin: String): String {
        val salted = "$PIN_SALT:$pin:$PIN_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(salted.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Checks if input PIN matches the stored hash.
     */
    fun verifyPin(inputPin: String, storedHash: String): Boolean {
        if (storedHash.isBlank() || inputPin.length != 4) return false
        return hashPin(inputPin) == storedHash
    }
}
