package com.lousseief.vault.controller

import com.lousseief.vault.model.Association
import com.lousseief.vault.model.AssociationModel
import com.lousseief.vault.model.Profile
import com.lousseief.vault.model.Settings
import javafx.beans.Observable
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableArray
import javafx.collections.ObservableList
import javafx.util.Callback
import tornadofx.Controller
import tornadofx.asObservable
import com.lousseief.vault.exception.AuthenticationException;
import javafx.beans.property.SimpleBooleanProperty

class UserController: Controller() {

    val altered = SimpleBooleanProperty(false)

    var user: Profile? = null
    set(value) {
        println("Running setter")
        field = value
        /*categories = FXCollections.observableArrayList(
            object: Callback<String, Array<Observable>> {

                override
                fun call(s: String): Array<Observable> {
                    return Array<Observable>(1){ s }
                }
            }
        )*/
        //categories = mutableListOf<String>().asObservable()
        items.addAll((user?.associations ?: emptyMap<String, Association>()).map { AssociationModel(it.value) })
        categories.addAll(
            mutableListOf<String?>()
            .apply { addAll(user?.settings?.categories ?: emptyList<String>()) }
        )
    }

    //var items: ObservableList<AssociationModel> = mutableListOf<AssociationModel>().asObservable()
    var items: ObservableList<AssociationModel> = FXCollections.observableArrayList(
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
    private set

    var categories: ObservableList<String> = mutableListOf("").asObservable()
    private set

    fun unsetUser() {
        user = null;
    }

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
        val assoc: Association? = user!!.associations[mainId]
        if(assoc !== null) {
            assoc.secondaryIdentifiers.add(newSecondaryId)
            items
                .find { it?.mainIdentifier === mainId }?.secondaryIdentifiersProperty?.add(newSecondaryId)
            altered.set(true)
        }

    }

    fun removeSecondaryIdentifier(mainId: String, secondaryId: String) {
        val assoc: Association? = user!!.associations[mainId]
        if(assoc !== null) {
            assoc.secondaryIdentifiers.remove(secondaryId)
            items
                .find { it?.mainIdentifier === mainId }?.secondaryIdentifiersProperty?.remove(secondaryId)
        }
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

}