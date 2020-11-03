package com.lousseief.vault.model

import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import tornadofx.ItemViewModel
import java.time.Instant
import tornadofx.*
import java.util.concurrent.Callable

// class representing an association model undergoing change in the UI
class AssociationProxy: ItemViewModel<AssociationModel>() {
    val mainIdentifier = bind(AssociationModel::mainIdentifierProperty)
    val secondaryIdentifiers = bind(AssociationModel::secondaryIdentifiersProperty) as SimpleListProperty<String>
    val isNeeded = bind(AssociationModel::isNeededProperty)
    val shouldBeDeactivated = bind(AssociationModel::shouldBeDeactivatedProperty)
    val isDeactivated = bind(AssociationModel::isDeactivatedProperty)
    val category = bind(AssociationModel::categoryProperty)
    val comment = bind(AssociationModel::commentProperty)

    /* customized dirty check to allow rollback and commit to work by means of copying lists in which
    case the model is always marked as dirty from the start */
    val dirtyCheck = object : Callable<Boolean> {
            override fun call(): Boolean =
                (
                    item !== null && (
                        mainIdentifier.isDirty ||
                        secondaryIdentifiers.size != item.secondaryIdentifiers.size ||
                        !secondaryIdentifiers.containsAll(item.secondaryIdentifiers) ||
                        isNeeded.isDirty || shouldBeDeactivated.isDirty || isDeactivated.isDirty ||
                        category.isDirty || comment.isDirty

                    )
                )
        }
    override val dirty: BooleanBinding = Bindings.createBooleanBinding(
        dirtyCheck,
        mainIdentifier, secondaryIdentifiers, isNeeded, shouldBeDeactivated, isDeactivated, category, comment)


    private fun copyList() {
        secondaryIdentifiers.value = secondaryIdentifiers.value.map { it }.asObservable()
    }

    fun getCurrentStateAsAssociation() =
        Association(
            mainIdentifier.value,
            secondaryIdentifiers.value,
            isNeeded.value,
            shouldBeDeactivated.value,
            isDeactivated.value,
            category.value,
            comment.value
        )

    init {
        // copies the list after initial binding
        copyList() // this copy is onl needed if we initiate our ItemViewModel with something
        itemProperty.onChange {
            // copies the list whenever the item is changed
            copyList()
        }
    }

    // copies list whenever a commit is done so that subsequent commits and rollbacks work as they should (triggered automatically after a commit)
    override fun onCommit() {
        println("In on commit")
        copyList()
    }


    // copies list whenever a rollback is done so that subsequent commits and rollbacks work as they should (must be triggered manually)
    fun onRollback() =
        copyList()

}

// class representing an association in the UI
class AssociationModel() {
    val mainIdentifierProperty = SimpleStringProperty(this, "mainIdentifier", "")
    var mainIdentifier by mainIdentifierProperty
    val secondaryIdentifiersProperty = SimpleListProperty<String>(this, "secondaryIdentifiers", FXCollections.observableArrayList())
    var secondaryIdentifiers by secondaryIdentifiersProperty
    val isNeededProperty = SimpleBooleanProperty(this, "isNeeded", true)
    var isNeeded by isNeededProperty
    val shouldBeDeactivatedProperty = SimpleBooleanProperty(this, "shouldBeDeactivated", false)
    var shouldBeDeactivated by shouldBeDeactivatedProperty
    val isDeactivatedProperty = SimpleBooleanProperty(this, "isDeactivated", false)
    var isDeactivated by isDeactivatedProperty
    val categoryProperty = SimpleStringProperty(this, "category", null)
    var category by categoryProperty
    val commentProperty = SimpleStringProperty(this, "comment", null)
    var comment by commentProperty

    constructor(assoc: Association): this() {
        mainIdentifierProperty.set(assoc.mainIdentifier)
        secondaryIdentifiersProperty.set(FXCollections.observableArrayList(assoc.secondaryIdentifiers))
        isNeededProperty.set(assoc.isNeeded)
        shouldBeDeactivatedProperty.set(assoc.shouldBeDeactivated)
        isDeactivatedProperty.set(assoc.isDeactivated)
        categoryProperty.set(assoc.category)
        commentProperty.set(assoc.comment)
    }

    override
    fun toString(): String =
        mainIdentifier
}

data class Association(
    var mainIdentifier: String = "",
    var secondaryIdentifiers: MutableList<String> = mutableListOf(),
    var isNeeded: Boolean = true,
    var shouldBeDeactivated: Boolean = false,
    var isDeactivated: Boolean = false,
    var category: String = "",
    var comment: String = ""
)

data class AssociationWithCredentials(
    val association: Association = Association(),
    var credentials: List<Credential> = emptyList()
)
