package com.lousseief.vault.controller

import com.lousseief.vault.dialog.PasswordConfirmDialog
import javafx.beans.Observable
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.util.Callback
import tornadofx.Controller
import tornadofx.asObservable
import com.lousseief.vault.exception.AuthenticationException;
import com.lousseief.vault.exception.InternalException
import com.lousseief.vault.model.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.event.ActionEvent
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule

class UserController: Controller() {

    // whether the data in memory is altered so that a save to file is possible / to recommend
    val altered = SimpleBooleanProperty(false)

    var user: Profile? = null
    set(value) {
        field = value
        if(value !== null) {
            /*categories = FXCollections.observableArrayList(
                object: Callback<String, Array<Observable>> {

                    override
                    fun call(s: String): Array<Observable> {
                        return Array<Observable>(1){ s }
                    }
                }
            )*/
            //categories = mutableListOf<String>().asObservable()
            categories.addAll(
                mutableListOf("") +
                mutableListOf<String?>()
                    .apply { addAll(user?.settings?.categories ?: emptyList<String>()) }
            )
        }
        else {
            items.clear()
            categories.clear()
        }
    }

    private fun cancelSavedMasterPassword() {
        savedPasswordExpiry = null
        savedPasswordResetter?.cancel()
        savedPasswordResetter = null
    }

    private fun resetSavedMasterPassword() {
        savedPasswordExpiry = Instant.now().plusMillis(user!!.settings.savePasswordForMinutes * 60 * 1000L)
        savedPasswordResetter?.cancel()
        savedPasswordResetter =  Timer(false)
            .schedule(user!!.settings.savePasswordForMinutes * 60 * 1000L) { println("reset!"); savedMasterPassword = null }
    }

    private var savedPasswordResetter: TimerTask? = null
    private var savedPasswordExpiry: Instant? = null
    private var savedMasterPassword: String? = null
        set(value) {
            if(value === null) {
                field = null
                cancelSavedMasterPassword()
            }
            else if (user!!.settings.savePasswordForMinutes != 0) {
                field = value
                resetSavedMasterPassword()
            }
        }

    fun passwordRequiredAction(action: (password: String, event: ActionEvent?) -> Unit): Boolean {
        if(savedMasterPassword !== null && savedPasswordExpiry !== null && Instant.now().isBefore(savedPasswordExpiry)) {
            resetSavedMasterPassword()
            action(savedMasterPassword!!, null)
            return true
        }
        else {
            savedMasterPassword = null
            val result = PasswordConfirmDialog { password: String, event: ActionEvent ->
                action(password, event)
                savedMasterPassword = password
            }.showAndWait()
            return result.isPresent
        }
    }

    fun setUser(userToSet: Profile, associations: MutableMap<String, Association>, password: String) {
        user = userToSet;
        items.addAll(associations.map { AssociationModel(it.value) })
        if(userToSet.settings.savePasswordForMinutes != 0)
            savedMasterPassword = password
    }


    val items: ObservableList<AssociationModel> = FXCollections.observableArrayList(
        object: Callback<AssociationModel, Array<Observable>> {
            override fun call(assoc: AssociationModel): Array<Observable> {
                return arrayOf<Observable>(
                    assoc.mainIdentifierProperty,
                    assoc.categoryProperty,
                    assoc.commentProperty,
                    assoc.isNeededProperty,
                    assoc.isDeactivatedProperty,
                    assoc.shouldBeDeactivatedProperty,
                    assoc.secondaryIdentifiersProperty
                )
            }
        }
    )

    val categories: ObservableList<String> = mutableListOf<String>().asObservable()

    fun addEntry(name: String, password: String?) {
        if(password === null || password.length == 0)
            throw AuthenticationException(AuthenticationException.AuthenticationExceptionCause.EMPTY_PASSWORDS_NOT_ALLOWED, null)
        val addedAssociation = user!!.add(name, password)
        //val addition = Association()
        //addition.mainIdentifier = name
        //user!!.associations.put(name, addition)
        items.add(AssociationModel(addedAssociation));
        altered.set(true)
    }

    fun removeEntry(name: String, password: String) {
        user!!.remove(name, password)
        items.removeIf{ it !== null && it.mainIdentifier.equals(name) }
        altered.set(true)
    }

    fun addCategory(name: String) {
        user!!.settings.categories.add(name)
        categories.add(name);
        altered.set(true)
    }

    fun addSecondaryIdentifier(mainId: String, newSecondaryId: String) {
        if(!items.any { it.mainIdentifier == mainId })
            throw InternalException(InternalException.InternalExceptionCause.MISSING_IDENTIFIER)
        items
            .find { it?.mainIdentifier === mainId }?.secondaryIdentifiersProperty?.add(newSecondaryId)
        altered.set(true)
    }

    fun removeSecondaryIdentifier(mainId: String, secondaryId: String) {
        if(!items.any { it.mainIdentifier == mainId })
            throw InternalException(InternalException.InternalExceptionCause.MISSING_IDENTIFIER)
        items
            .find { it?.mainIdentifier === mainId }?.secondaryIdentifiersProperty?.remove(secondaryId)
        altered.set(true)
    }

    fun validateNewEntry(name: String?) {
        if (items.filter { it !== null && name !== null && it.mainIdentifier.compareTo(name, true) == 0 }.isNotEmpty())
            throw Exception("Entry already exists, all entries must have unique names.")
        else if (name === null || name.isEmpty())
            throw Exception("Empty identifier not allowed, please try again.")
    }

    fun validateNewCategory(name: String?) {
        if (categories.filter { it !== null && name !== null && it.compareTo(name, true) == 0 }.isNotEmpty())
            throw Exception("Category already exists, all categories must have unique names.")
        else if (name === null || name.isEmpty())
            throw Exception("Empty category name not allowed, please try again.")
    }

    /*fun update(
        identifier: String,
        mainIdentifier: String
    ) {
        user.associations[mainIdentifier] = AssociationProxy(mainIdentifier)
        if(identifier !== mainIdentifier) {
            println("s dieeferent")
            user.associations.remove(identifier)
            //items
            //    .find { it !== null && it.mainIdentifier === identifier }
            //    .let{ it?.mainIdentifier = mainIdentifier }
            //items.remove{ it !== null && it.mainIdentifier !== identifier }
            items.removeIf{it !== null && it.mainIdentifier === identifier }
            items.add(AssociationProxy(mainIdentifier))
            println(items)
            }


    }*/

    fun save() {
        println("saving")
        user!!.save()
        println("saved")
    }

    fun updateAssociation(oldIdentifier: String, newIdentifier: String, assoc: Association, password: String) {
        println(oldIdentifier + " " + newIdentifier + " " + password)
        //val assoc: Association? = items.find{ it.mainIdentifier == newIdentifier }?.let{ Association(it) }
        //val assoc: Association? = user!!.associations[newIdentifier]
        user!!.updateAssociation(oldIdentifier, assoc, password, newIdentifier)
        println("updated")
        altered.set(true)
    }

    fun getCredentials(identifier: String, password: String) =
        user!!.getCredentials(identifier, password)

    fun updateCredentials(identifier: String, credentials: List<Credential>, password: String) =
        user!!.updateCredentials(identifier, credentials, password)

    fun getUserNames() =
        user!!.userNames

    fun setUserNames(nextUserNames: MutableMap<String, Int>) =
        user!!.userNames.apply {
            clear()
            putAll(nextUserNames)
        }

    fun changeMasterPassword(oldPassword: String, newPassword: String) =
        user!!.accessVault(oldPassword, null, true, newPassword)
}