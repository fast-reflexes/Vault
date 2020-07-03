package com.lousseief.vault.service

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AES256Service {

    val CIPHER = "AES"
    val BLOCK_CIPHER_MODE = "CBC"
    val KEY_BITS = 256
    val PADDING = "PKCS5PADDING"
    val IV_BITS = 128 // same as block size
    val IV_BYTES = IV_BITS / 8

    data class EncryptionDelivery(
        val cipherBytes: ByteArray,
        val ivBytes: ByteArray
    )

    fun encrypt(key: ByteArray, plainText: ByteArray): EncryptionDelivery {
        val ivBytes = ByteArray(IV_BYTES).also { SecureRandom.getInstanceStrong().nextBytes(it) }
        val ivSpec = IvParameterSpec(ivBytes)
        val keySpec = SecretKeySpec(key, CIPHER)

        val cipher = Cipher.getInstance("${CIPHER}/${BLOCK_CIPHER_MODE}/${PADDING}")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)

        val cipherBytes = cipher.doFinal(plainText)

        return EncryptionDelivery(cipherBytes, ivBytes)
    }

    fun decrypt(key: ByteArray, iv: ByteArray, cipherText: ByteArray): ByteArray {
        val ivSpec = IvParameterSpec(iv)
        val keySpec = SecretKeySpec(key, CIPHER)

        val cipher = Cipher.getInstance("${CIPHER}/${BLOCK_CIPHER_MODE}/${PADDING}")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val plainBytes = cipher.doFinal(cipherText)

        return plainBytes
    }
}


