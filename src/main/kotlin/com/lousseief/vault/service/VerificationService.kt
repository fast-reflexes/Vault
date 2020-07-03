package com.lousseief.vault.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lousseief.vault.exception.AuthenticationException
import com.lousseief.vault.model.*
import com.lousseief.vault.view.LoginView
import javafx.scene.control.Alert
import tornadofx.ViewTransition
import tornadofx.alert
import tornadofx.millis

object VerificationService {

    fun constantCompareStrings(a: String, b: String): Boolean {
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
    }

    fun authorize(password: String, passwordSalt: String, verification: String, verificationSalt: String): ByteArray {
        val (_, encryptionKey) = PBKDF2Service.deriveKey(password, ConversionService.Base64ToBytes(passwordSalt))
        val (_, hash) = PBKDF2Service.deriveKey(ConversionService.bytesToAscii(encryptionKey), ConversionService.Base64ToBytes(verificationSalt))
        val authorized = constantCompareStrings(verification, ConversionService.bytesToBase64(hash))
        return if(authorized) encryptionKey else throw AuthenticationException(AuthenticationException.AuthenticationExceptionCause.UNAUTHORIZED)
    }

}