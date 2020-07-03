package com.lousseief.vault.service

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lousseief.vault.model.AssociationWithCredentials
import com.lousseief.vault.model.Settings

object VaultService {

    fun encryptVault(encryptionKey: ByteArray, vault: Pair<Settings, Map<String, AssociationWithCredentials>>): Pair<String, String> =
        AES256Service.encrypt(
            encryptionKey,
            ConversionService.UTF8ToBytes(Gson().toJson(vault))
        )
        .let{ (cipherBytes, ivBytes) ->
            Pair(ConversionService.bytesToBase64(ivBytes), ConversionService.bytesToBase64(cipherBytes))
        }
    /*if(enc !== null) {
        val (cipherBytes, ivBytes) = enc
        val updatedUser = Profile(
            user.name,
            ConversionService.bytesToBase64(saltBytes),
            ConversionService.bytesToBase64(hashSaltBytes),
            ConversionService.bytesToBase64(hashBytes),
            AES256Service.CIPHER,
            AES256Service.PADDING,
            AES256Service.BLOCK_CIPHER_MODE,

        )
        val success = FileService.writeFile(newUser, false)
        if (success) {
            alert(
                Alert.AlertType.INFORMATION,
                "User added",
                "The user was successfully added! Please go ahead and login!"
            ) {
                replaceWith<LoginView>(
                    transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.RIGHT)
                )
            }
        } else {
            alert(
                Alert.AlertType.ERROR,
                "User couldn't be added",
                "something went wrong, perhaps the user already exists? Please try again!"
            )
        }
    }
}*/

    fun decryptVault(encryptedVault: String, iv: String, encryptionKey: ByteArray): Pair<Settings, MutableMap<String, AssociationWithCredentials>> {
        val plainBytes = AES256Service.decrypt(
            encryptionKey,
            ConversionService.Base64ToBytes(iv),
            ConversionService.Base64ToBytes(encryptedVault)
        )
        val plainText = ConversionService.bytesToUTF8(plainBytes)
        val itemType = object : TypeToken<Pair<Settings, MutableMap<String, AssociationWithCredentials>>>() {}.type
        return Gson().fromJson(plainText, itemType)
    }
}