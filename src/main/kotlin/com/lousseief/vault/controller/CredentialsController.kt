package com.lousseief.vault.controller

import com.lousseief.vault.exception.InternalException
import com.lousseief.vault.model.Credential
import javafx.collections.ObservableList
import tornadofx.*
import com.lousseief.vault.model.CredentialModel
import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.collections.FXCollections
import java.util.concurrent.Callable

class CredentialsController: Controller() {

    private val CREDENTIAL_LIST_OBSERVABLE_VALUES = { model: CredentialModel ->
        arrayOf<Observable>(
            model.passwordProperty,
            model.identitiesProperty
        )
    }

    val credentials: ObservableList<CredentialModel> = FXCollections.observableArrayList(CREDENTIAL_LIST_OBSERVABLE_VALUES)

    val originalCredentials: ObservableList<CredentialModel> = FXCollections.observableArrayList(CREDENTIAL_LIST_OBSERVABLE_VALUES)

    val userNames: MutableMap<String, Int> = mutableMapOf()

    var credentialsSaved = false;

    val altered = Bindings.createBooleanBinding(
        Callable<Boolean> { val b = isAltered(originalCredentials, credentials); println("RAN: " + b); b },
        credentials, originalCredentials
    )

    private fun isAltered(oldList: List<CredentialModel>, newList: List<CredentialModel>): Boolean =
        oldList.size != newList.size ||
        oldList.zip(newList).any { (oldCred, newCred) ->
            println("${oldCred.identities} ${newCred.identities}")
            oldCred.password != newCred.password ||
            oldCred.created.compareTo(newCred.created) != 0 ||
            oldCred.lastUpdated.compareTo(newCred.lastUpdated) != 0 ||
            oldCred.identities.size != newCred.identities.size ||
            oldCred.identities.zip(newCred.identities).any { (oldId, newId) ->
                oldId != newId
            }
        }

    fun setCredentials(inputCredentials: List<Credential>) {
        originalCredentials.setAll(inputCredentials.map{ CredentialModel(it) })
        credentials.setAll(inputCredentials.map{ CredentialModel(it) })
        credentialsSaved = false;
    }

    fun setUserNames(inputUserNames: MutableMap<String, Int>) {
        userNames.apply {
            clear()
            putAll(inputUserNames)
        }
    }

    fun addUserName(newUserName: String) {
        if (userNames.containsKey(newUserName))
            userNames[newUserName] = userNames[newUserName]!!.plus(1)
        else
            userNames[newUserName] = 1
    }

    fun removeUserName(userNameToRemove: String) {
        if(!userNames.containsKey(userNameToRemove))
            throw InternalException(InternalException.InternalExceptionCause.USERNAME_TO_REMOVE_NOT_FOUND)
        if(userNames[userNameToRemove]!!.compareTo(0) <= 0)
            throw InternalException(InternalException.InternalExceptionCause.USERNAME_TO_REMOVE_ZERO_OR_LESS)
        userNames[userNameToRemove] = userNames[userNameToRemove]!!.minus(1)
        if(userNames[userNameToRemove]!!.compareTo(0) == 0)
            userNames.remove(userNameToRemove)
    }

    fun addCredential(credentialPassword: String) {
        credentials.add(CredentialModel(credentialPassword))
    }

    fun removeCredential(credentialPassword: String) {
        credentials.removeIf{ it.password == credentialPassword }
    }

    fun saved() {
        originalCredentials.setAll(credentials)
        credentialsSaved = true
    }
}