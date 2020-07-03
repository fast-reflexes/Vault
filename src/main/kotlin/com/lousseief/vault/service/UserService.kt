package com.lousseief.vault.service

import com.lousseief.vault.model.AssociationWithCredentials
import com.lousseief.vault.model.Profile
import com.lousseief.vault.model.Settings

object UserService {

    fun loginUser(userName: String, password: String): Profile =
        FileService.readFile(userName).apply{ initialize(password) }
        /*with(FileService.readFile(userName)) {
            this
                ?.let {
                    VerificationService.authorize(this, password)
                        ?.let { VaultService.decryptVault(this, it) }
                }
                ?.also { (settings, associations) ->
                    this.settings = settings
                    this.associations = associations.mapValues { it.value.association }.toMutableMap()
                }
            return this
        }*/

    /*val user: Profile? = FileService.readFile(userName)
    if (user !== null) {
        val encrytionKey = authorize(user, password)
        val delivery = accessVault(user, password)
        if (delivery !== null) {
            val (settings, associations) = delivery
            user.settings = settings
            user.associations = associations.mapValues { it.value.association }.toMutableMap()
            return user
        }
    }
    return null*/

    fun createUser(name: String, password: String): Profile {
        if (FileService.userExists(name))
            throw Exception("The user name is already taken, please pick an other one.")
        val (saltBytes, encryptionKeyBytes) = PBKDF2Service.deriveKey(password)
        val (hashSaltBytes, hashBytes) = PBKDF2Service.deriveKey(ConversionService.bytesToAscii(encryptionKeyBytes))
        val (iv, cipherText) = VaultService.encryptVault(
            encryptionKeyBytes,
            Pair(Settings(), emptyMap<String, AssociationWithCredentials>())
        )
        val newUser = Profile(
            name,
            ConversionService.bytesToBase64(saltBytes),
            ConversionService.bytesToBase64(hashSaltBytes),
            ConversionService.bytesToBase64(hashBytes),
            AES256Service.CIPHER,
            AES256Service.PADDING,
            AES256Service.BLOCK_CIPHER_MODE,
            iv,
            cipherText
        )
        FileService.writeFile(newUser, false)
        return newUser
    }

}