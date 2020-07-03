package com.lousseief.vault.service

import org.apache.commons.codec.binary.Hex;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException;

object PBKDF2Service {

    val KEY_BITS = 256
    val KEY_BYTES = KEY_BITS / 8
    val ITERATIONS = 10000
    val SALT_BITS = 512
    val SALT_BYTES = SALT_BITS / 8

    data class PBKDF2Delivery(
        val salt: ByteArray,
        val key: ByteArray
    )

    fun deriveKey(password: String, salt: ByteArray?): PBKDF2Delivery {
        try {
            val saltToUse = salt ?: ByteArray(SALT_BYTES).also{ SecureRandom.getInstanceStrong().nextBytes(it) }
            val passwordChars = password.toCharArray()
            val skf = SecretKeyFactory.getInstance( "PBKDF2WithHmacSHA512" );
            val spec = PBEKeySpec(passwordChars, saltToUse, ITERATIONS, KEY_BITS);
            val key = skf.generateSecret(spec);
            println(key.format + " " + key.encoded.size)
            val res = key.getEncoded();
            return PBKDF2Delivery(saltToUse, res)
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException( e );
        } catch(e: InvalidKeySpecException) {
            throw RuntimeException(e);
        }
    }

    fun deriveKey(password: String) : PBKDF2Delivery =
        deriveKey(password, null)


}