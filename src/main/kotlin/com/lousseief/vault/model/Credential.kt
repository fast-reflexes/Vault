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

    val identities = bind(CredentialModel::identitiesProperty, true)
    val password = bind(CredentialModel::passwordProperty, true)
    val created = bind(CredentialModel::createdProperty, true)
    val lastUpdated = bind(CredentialModel::lastUpdatedProperty, true)
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
        identitiesProperty.setAll(cred.identities)
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

// actual underlying data classes with the full information
data class Credential(
    val identities: List<String>,
    val password: String,
    val created: Instant = Instant.now(),
    val lastUpdated: Instant = Instant.now()
) {
    companion object {
        fun fromModel(model: CredentialModel) =
            Credential(model.identities, model.password, model.created, model.lastUpdated)
    }
}