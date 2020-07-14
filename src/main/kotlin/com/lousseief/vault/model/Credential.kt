package com.lousseief.vault.model

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.ItemViewModel
import java.time.Instant
import tornadofx.*

// class representing a credential undergoing change in the UI
class CredentialProxy: ItemViewModel<CredentialModel>() {

    val identities = bind(CredentialModel::identitiesProperty)
    val password = bind(CredentialModel::passwordProperty)
    val created = bind(CredentialModel::createdProperty)
    val lastUpdated = bind(CredentialModel::lastUpdatedProperty)
}

// class representing a credential in the UI
class CredentialModel() {
    val identitiesProperty = SimpleListProperty<String>(this, "identities", FXCollections.observableArrayList())
    var identities by identitiesProperty
    val passwordProperty = SimpleStringProperty(this, "password", "")
    var password by passwordProperty
    val createdProperty = SimpleObjectProperty<Instant>(this, "created", Instant.now())
    var created by createdProperty
    val lastUpdatedProperty = SimpleObjectProperty<Instant>(this, "lastUpdated", Instant.now())
    var lastUpdated by lastUpdatedProperty

    constructor(cred: Credential) : this() {
        identitiesProperty.set(cred.identities.asObservable())
        passwordProperty.set(cred.password)
        createdProperty.set(cred.created)
        lastUpdatedProperty.set(cred.lastUpdated)
    }

    constructor(password: String): this() {
        passwordProperty.set(password)
    }

    override
    fun toString(): String =
        password
}