package com.lousseief.vault.view

import com.lousseief.vault.controller.UserController
import com.lousseief.vault.dialog.PasswordConfirmDialog
import com.lousseief.vault.dialog.SingleInputDialog
import com.lousseief.vault.model.AssociationProxy
import com.lousseief.vault.model.AssociationModel
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.IntegerBinding
import javafx.beans.property.SimpleStringProperty
import javafx.concurrent.Task
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import tornadofx.*
import java.awt.GridBagConstraints.VERTICAL

class EntryView: View() {

    val model: AssociationProxy by param()
    val controller = find<UserController>()
    val originalMainIdentifier: SimpleStringProperty by param()
    val secondaryIdentifier = SimpleStringProperty(null)
    val sizeProperty: IntegerBinding = Bindings.size(controller.categories)
    val header = object: SimpleStringProperty(null) {

        override
        fun getValue(): String {
            return "Entry: " + (super.getValue() ?: "(unnamed entry)")
        }
    }.apply { bind(originalMainIdentifier) }
    override
    fun onUndock() {
        println("UNDOCKING FRAGMENT");
    }

    class CategoryListCell : ListCell<String?>() {

        override
        fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item === "") {
                setText("Select category");
                setDisable(true)
                setStyle("-fx-opacity: 0.4")
            } else {
                setText(item)
                setDisable(false)
                setStyle("-fx-opacity: 1.0")
            }
        }
    }

    class CategoryCallback : Callback<ListView<String?>, ListCell<String?>> {

        override
        fun call(arg0: ListView<String?>): ListCell<String?> =
            CategoryListCell()
    }

    override
    val root = vbox {
        style {
            borderColor += box(all = Color.LIGHTGRAY)
            borderWidth += box(all = 1.px)
            borderStyle += BorderStrokeStyle.SOLID
            backgroundColor += Color.gray(0.97)

        }
        minWidth = 900.0
        hgrow = Priority.ALWAYS
        vgrow = Priority.ALWAYS
        spacing = 10.0
        alignment = Pos.CENTER
        label("Select an entry to view and edit it") {
            removeWhen { !model.empty }
        }
        vbox {
            vgrow = Priority.ALWAYS
            removeWhen { model.empty }
            form {
                vgrow = Priority.ALWAYS
                fieldset {
                    padding = Insets(0.0)
                    vgrow = Priority.ALWAYS
                    icon = FontAwesomeIconView(FontAwesomeIcon.PAPERCLIP)
                    this.textProperty.bind(header)
                    //this.textProperty.bind() as SimpleStringProperty)
                    hbox {
                        padding = Insets(0.0)
                        spacing = 10.0
                        hgrow = Priority.ALWAYS
                        vgrow = Priority.ALWAYS
                        vbox {
                            padding = Insets(0.0)
                            spacing = 10.0
                            hgrow = Priority.ALWAYS
                            vgrow = Priority.ALWAYS
                            maxWidth = Double.MAX_VALUE
                            field("Main identifier") {
                                textfield(model.mainIdentifier) {
                                    hgrow = Priority.ALWAYS
                                    useMaxWidth = true
                                    validator {
                                        if (it.isNullOrBlank()) error("The main identifier cannot be empty") else null
                                    }
                                }
                                labelPosition = Orientation.VERTICAL
                            }
                            vbox {
                                spacing = 10.0
                                hgrow = Priority.ALWAYS
                                vgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                                alignment = Pos.CENTER_RIGHT
                                field("Secondary identifiers", Orientation.VERTICAL) {
                                    vgrow = Priority.ALWAYS
                                    listview(model.secondaryIdentifiers) {
                                        hgrow = Priority.ALWAYS
                                        vgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        placeholder = label("No secondary identifiers.") {
                                            textAlignment = TextAlignment.CENTER
                                            isWrapText = true
                                        }
                                        fixedCellSize = 25.0
                                        bindSelected(secondaryIdentifier)
                                        padding = Insets(1.0)
                                        maxHeight = Double.MAX_VALUE
                                        minHeight = 77.0
                                        prefHeight = 77.0
                                    }
                                    padding = Insets(padding.top, padding.right, 0.0, padding.left)
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    vgrow = Priority.ALWAYS
                                    labelPosition = Orientation.VERTICAL
                                }
                                hbox {
                                    spacing = 10.0
                                    button("Remove secondary identifier") {
                                        vgrow = Priority.NEVER
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        minWidth = 180.0
                                        textAlignment = TextAlignment.CENTER
                                        alignment = Pos.CENTER
                                        disableWhen(secondaryIdentifier.isNull)
                                        action {
                                            if (secondaryIdentifier.value !== null && secondaryIdentifier.value != "")
                                                model.secondaryIdentifiers.remove(secondaryIdentifier.value)
                                                //controller.removeSecondaryIdentifier(model.mainIdentifier.value, secondaryIdentifier.value)
                                            /*val removedId = SingleInputDialog({ input: String? ->
                                                if (input === null || input.isEmpty())
                                                    throw Exception("Empty secondary identifier not allowed, please try again.")
                                                else if (model.secondaryIdentifiers.value.filter { it.equals(input, true) }
                                                        .isNotEmpty())
                                                    throw Exception("Secondary identifier already exists, all categories must have unique names.")
                                            }, "Enter a name for your new secondary identifier").showAndWait()
                                            if (newId.isPresent) {
                                                controller.addSecondaryIdentifier(model.mainIdentifier.value, newId.get())
                                            }*/
                                        }
                                    }
                                    button("Add secondary identifier") {
                                        vgrow = Priority.NEVER
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        minWidth = 180.0
                                        textAlignment = TextAlignment.CENTER
                                        alignment = Pos.CENTER
                                        action {
                                            val newId = SingleInputDialog({ input: String?, _: ActionEvent ->
                                                if (input === null || input.isEmpty())
                                                    throw Exception("Empty secondary identifier not allowed, please try again.")
                                                else if (model.secondaryIdentifiers.value.filter { it.equals(input, true) }
                                                        .isNotEmpty())
                                                    throw Exception("Secondary identifier already exists, all secondary identifier must be unique.")
                                            }, "Enter a name for your new secondary identifier").showAndWait()
                                            if (newId.isPresent)
                                                model.secondaryIdentifiers.add(newId.get())
                                                //controller.addSecondaryIdentifier(model.mainIdentifier.value, newId.get())
                                        }
                                    }
                                }
                            }
                            hbox {
                                spacing = 10.0
                                padding = Insets(0.0)
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                                field("Category") {
                                    alignment = Pos.BASELINE_LEFT
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    combobox<String> {
                                        items = controller.categories
                                        //bindSelected(model.category)
                                        valueProperty().bindBidirectional(model.category)
                                        cellFactory = CategoryCallback()
                                        buttonCell = CategoryListCell()
                                        //cellFormat { text = if(it.mainIdentifier === "") "Unnamed"  else it.mainIdentifier }
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        disableWhen{ sizeProperty.lessThanOrEqualTo(1) }
                                    }
                                    labelPosition = Orientation.VERTICAL
                                   Platform.runLater{ println(padding.top); padding = Insets(padding.top, padding.right, 0.0, padding.left) }

                                }
                                button("New category") {
                                    vgrow = Priority.NEVER
                                    maxWidth = 120.0
                                    minWidth = 120.0
                                    textAlignment = TextAlignment.CENTER
                                    text
                                    alignment = Pos.CENTER
                                    action {
                                        val newCat = SingleInputDialog({ input: String, _: ActionEvent ->
                                            controller.validateNewCategory(input)
                                        }, "Enter a name for your new category").showAndWait()
                                        if (newCat.isPresent) {
                                            controller.addCategory(newCat.get())
                                        }
                                    }
                                }
                            }
                        }
                        separator(Orientation.VERTICAL)
                        vbox {
                            padding = Insets(0.0)
                            spacing = 10.0
                            vgrow = Priority.ALWAYS
                            hgrow = Priority.NEVER
                            field("Comments", Orientation.VERTICAL) {
                                textarea(model.comment) {
                                    vgrow = Priority.ALWAYS
                                }
                                labelPosition = Orientation.VERTICAL
                                label.textAlignment = TextAlignment.RIGHT
                            }
                            hbox {
                                spacing = 10.0
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                                label("Is needed?")
                                checkbox().bind(model.isNeeded)
                                region {
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    minWidth = 5.0
                                }
                                label("Should be deactivated?")
                                checkbox().bind(model.shouldBeDeactivated)
                                region {
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    minWidth = 5.0
                                }
                                label("Is deactivated?")
                                checkbox().bind(model.isDeactivated)
                            }
                        }
                    }
                }
            }
            separator() {
                padding = Insets(0.0, 10.0, 0.0, 10.0)
            }
            hbox {
                padding = Insets(10.0)
                spacing = 10.0
                alignment = Pos.CENTER
                button("Credentials") {
                    action {
                        /*PasswordConfirmDialog({ password: String, _: ActionEvent ->
                            val oldIdentifier = originalMainIdentifier.value
                            val newIdentifier = model.mainIdentifier.value
                            controller.updateAssociation(
                                oldIdentifier,
                                model.mainIdentifier.value,
                                model.getCurrentStateAsAssociation(),
                                password)
                            println("Before commit: " + model.mainIdentifier)
                            model.commit()
                            assert(model.mainIdentifier.value !== null)
                            assert((oldIdentifier == newIdentifier) == (oldIdentifier == model.mainIdentifier.value))
                            println("After commit: " + model.mainIdentifier)
                        }).showAndWait()*/
                        find<CredentialsView>().openModal()
                        //openModal<LoginView>()
                    }
                }
                region {
                    hgrow = Priority.ALWAYS
                }
                button("Delete entry") {
                    action {
                        val deleteButtonType = ButtonType("Delete entry", ButtonBar.ButtonData.OK_DONE)
                        val a = Alert(Alert.AlertType.WARNING).apply {
                            headerText = "Confirm delete"
                            contentText = "Are you sure you want to delete this entry? This action cannot be undone."
                            dialogPane.buttonTypes.add(ButtonType.CANCEL)
                            dialogPane.buttonTypes.remove(ButtonType.OK)
                            dialogPane.buttonTypes.add(deleteButtonType)
                            dialogPane.lookupButton(deleteButtonType).addEventFilter(ActionEvent.ACTION) { event ->
                                val password = PasswordConfirmDialog({
                                        password: String, _: ActionEvent ->
                                    controller.removeEntry(model.mainIdentifier.value, password)
                                    //model.item = null
                                }).showAndWait()
                                if(password.isEmpty)
                                    event.consume()
                            }
                        }
                        val res = a.showAndWait()
                        if(res.isPresent && res.get() === deleteButtonType)
                            alert(
                                type = Alert.AlertType.INFORMATION,
                                header = "Entry successfully removed",
                                content = "The entry was successfully deleted from your vault"
                            )
                        /*confirm(
                            header = "Confirm delete",
                            content = "Are you sure you want to delete this entry? This action cannot be undone.",
                            actionFn = {
                                val
                                //
                            }
                        )*/
                    }
                }
                button("Reset to initial") {
                    enableWhen(model.dirty)
                    action {
                        model.rollback()
                        model.onRollback()
                    }
                }
                button("Keep changes") {
                    enableWhen(model.dirty)
                    action {
                        if(model.isValid) {
                            PasswordConfirmDialog({ password: String, _: ActionEvent ->
                                val oldIdentifier = originalMainIdentifier.value
                                val newIdentifier = model.mainIdentifier.value
                                controller.updateAssociation(
                                    oldIdentifier,
                                    model.mainIdentifier.value,
                                    model.getCurrentStateAsAssociation(),
                                    password)
                                println("Before commit: " + model.mainIdentifier)
                                model.commit()
                                assert(model.mainIdentifier.value !== null)
                                assert((oldIdentifier == newIdentifier) == (oldIdentifier == model.mainIdentifier.value))
                                println("After commit: " + model.mainIdentifier)
                            }).showAndWait()
                        }
                        else
                            alert(
                                type = Alert.AlertType.ERROR,
                                header = "Entry could not be saved",
                                content = "The entry was not saved due to validation errors, please check your entry again"
                            )

                        }
                }
            }
        }
    };

    init {
        //println("Root children in En: " + root.children[2].toString())
        /*model.empty.onChange {
            isEmpty ->
                if (isEmpty) root.replaceChildren(emptyContent) else root.replaceChildren(form)
        }*/
    }

}