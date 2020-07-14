package com.lousseief.vault.controller

import javafx.collections.ObservableList
import tornadofx.*
import com.lousseief.vault.model.CredentialModel
import javafx.beans.property.SimpleBooleanProperty

class CredentialsController: Controller() {

    val altered = SimpleBooleanProperty(false)

    val credentials: ObservableList<CredentialModel> = mutableListOf<CredentialModel>().asObservable()

    fun addCredential(credentialPassword: String) {
        credentials.add(CredentialModel(credentialPassword))
        altered.set(true)
    }

    fun removeCredential(credentialPassword: String) {
        credentials.removeIf{ it.password == credentialPassword }
    }

    fun setAltered() =
        altered.set(true)

}