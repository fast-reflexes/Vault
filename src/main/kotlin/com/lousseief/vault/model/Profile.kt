package com.lousseief.vault.modelimport com.lousseief.vault.crypto.Conversionimport com.lousseief.vault.crypto.Hmacimport com.lousseief.vault.exception.InternalExceptionimport com.lousseief.vault.service.*data class Settings(    var passwordLength: Int = 20,    var categories: MutableList<String> = mutableListOf(),    var savePasswordForMinutes: Int = 2    //var blocksEncoded: Int = 0)class Profile(    val name: String,    var keyMaterialSalt: String,    var verificationSalt: String,    var verificationHash: String,    var iv: String,    var encryptedData: String,    var checkSum: String = "",    var settings: Settings = Settings(),    var userNames: MutableMap<String, Int> = mutableMapOf()) {    fun toContentString(): String =        "${keyMaterialSalt}\n${verificationSalt}\n${verificationHash}\n${iv}\n${encryptedData}"    override fun toString(): String =        "${toContentString()}\n${checkSum}"    fun updateAssociation(oldIdentifier: String, assoc: Association, password: String, newIdentifier: String = oldIdentifier) {        accessVault(            password,            { (_, associations) ->                val existingAssociation = associations[oldIdentifier]                if(existingAssociation === null)                    throw Exception("CRAZYS!")                if(oldIdentifier !== newIdentifier)                    associations.remove(oldIdentifier)                associations[newIdentifier] = existingAssociation.copy(association = assoc)                Pair(this.settings, associations)            },            true        )    }    fun updateCredentials(identifier: String, credentials: List<Credential>, password: String) {        accessVault(            password,            { (settings, associations) ->                val existingAssociation = associations[identifier]                if(existingAssociation === null)                    throw Exception("CRAZYS!CREDS")                associations[identifier] = existingAssociation.copy(credentials = credentials)                Pair(settings, associations)            },            true)    }    fun remove(identifier: String, password: String) {        accessVault(            password,            { (settings, associations) ->                Pair(settings, associations.filterKeys { !it.equals(identifier) })            },            true)    }    fun add(identifier: String, password: String): Association {        val associationToAdd = Association(mainIdentifier = identifier)        accessVault(            password,            { (settings, associations) ->                associations[identifier] = AssociationWithCredentials(association = associationToAdd)                Pair(settings, associations)            },            true        )        return associationToAdd    }    fun accessVault(        password: String,        vaultManipulation: ((vault: Pair<Settings, MutableMap<String, AssociationWithCredentials>>) -> Pair<Settings, Map<String, AssociationWithCredentials>>)? = null,        encrypt: Boolean = false,        updatedPassword: String = password    )    :    Pair<Settings, Map<String, AssociationWithCredentials>> =        VerificationService.authorize(            password, keyMaterialSalt, verificationHash, verificationSalt        )            .let {                Pair(it.sliceArray(0 until 32), it.sliceArray(32 until 64))            }            .also {                (_, hMacKeyBytes) ->                    VerificationService.verify(hMacKeyBytes, toContentString(), checkSum)            }            .let {                (encryptionKeyBytes, hMacKeyBytes) ->                    VaultService.decryptVault(encryptedData, iv, encryptionKeyBytes)                        .let { if (vaultManipulation !== null) vaultManipulation(it) else it }                        .let { vault ->                            if (encrypt) {                                var encryptionKeyBytesToUse = encryptionKeyBytes                                var hmacKeyBytesToUse = hMacKeyBytes                                if(password != updatedPassword) {                                    val (                                        updatedSaltBytes,                                        updatedHashBytes,                                        updatedHashSalt,                                        updatedEncryptionKeyBytes,                                        updatedHmacKeyBytes                                    ) = UserService.createKeyMaterial(updatedPassword)                                    keyMaterialSalt = Conversion.bytesToBase64(updatedSaltBytes)                                    verificationHash = Conversion.bytesToBase64(updatedHashBytes)                                    verificationSalt = Conversion.bytesToBase64(updatedHashSalt)                                    encryptionKeyBytesToUse = updatedEncryptionKeyBytes                                    hmacKeyBytesToUse = updatedHmacKeyBytes                                }                                val (nextIv, nextCipherText) = VaultService.encryptVault(encryptionKeyBytesToUse, vault)                                iv = nextIv                                encryptedData = nextCipherText                                checkSum = Conversion.bytesToBase64(                                    Hmac.generateMac(                                        Conversion.UTF8ToBytes(toContentString()),                                        hmacKeyBytesToUse                                    ))                            }                            vault                        }            }    fun initialize(password: String): MutableMap<String, Association> {        val (fetchedSettings, fetchedAssociationsWithCredentials) = accessVault(password)        settings = fetchedSettings        val associations = fetchedAssociationsWithCredentials.mapValues { it.value.association }.toMutableMap()        userNames = fetchedAssociationsWithCredentials            .map {                it.value.credentials                    .map { it.identities }                    .flatten()            }            .flatten()            .groupBy { it }            .mapValues { it.value.size }            .toMutableMap()        /*accessVault(password)            .also {(settings, associations) ->                this.settings = settings                this.associations = associations.mapValues { it.value.association }.toMutableMap()            }*/        return associations    }    fun getCredentials(identifier: String, password: String): List<Credential> {        val (_, fetchedAssociationsWithCredentials) = accessVault(password)        return fetchedAssociationsWithCredentials.getOrElse(            identifier,            { throw InternalException(InternalException.InternalExceptionCause.MISSING_IDENTIFIER) }        ).credentials    }    fun save() =        FileService.writeFile(this, true)}