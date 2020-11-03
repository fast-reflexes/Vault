package com.lousseief.vault.service

import com.lousseief.vault.crypto.Conversion
import com.lousseief.vault.crypto.CryptoUtils
import com.lousseief.vault.crypto.Hmac
import com.lousseief.vault.crypto.KeyDerivation
import com.lousseief.vault.exception.AuthenticationException
import com.lousseief.vault.exception.InternalException
import com.lousseief.vault.exception.VerificationException

object VerificationService {

    /*fun constantCompareStrings(a: String, b: String): Boolean {
        val aLen = a.length
        val bLen = b.length
        var match = aLen == bLen
        val maxLen = maxOf(aLen, bLen)
        match = match && maxLen != 0
        val aToUse = a.padEnd(maxLen, 'a')
        val bToUse = b.padEnd(maxLen, 'a')
        for (i in 0..(maxLen - 1)) {
            match = match && aToUse[i] == bToUse[i]
        }
        return match
    }*/

    fun authorize(password: String, keyMaterialSalt: String, verification: String, verificationSalt: String): ByteArray {
        val (_, keyMaterialBytes) = KeyDerivation.deriveKey(password, Conversion.Base64ToBytes(keyMaterialSalt))
        val (_, hashBytes) = KeyDerivation.deriveKey(Conversion.bytesToUTF8(keyMaterialBytes), Conversion.Base64ToBytes(verificationSalt))
        if(keyMaterialBytes.size != 64)
            throw InternalException(InternalException.InternalExceptionCause.UNEXPECTED_CRYPTO_SIZE)
        val authorized = CryptoUtils.constantCompareByteArrays(Conversion.Base64ToBytes(verification), hashBytes)
        return if(authorized) keyMaterialBytes else throw AuthenticationException(AuthenticationException.AuthenticationExceptionCause.UNAUTHORIZED)
    }

    fun verify(hMacKey: ByteArray, data: String, storedCheckSum: String) {
        val verified = Hmac.authenticateMac(
            Conversion.UTF8ToBytes(data),
            hMacKey,
            Conversion.Base64ToBytes(storedCheckSum)
        )
        if (!verified) throw VerificationException(VerificationException.VerificationExceptionCause.INTEGRITY_CHECK_FAILED)
    }

}