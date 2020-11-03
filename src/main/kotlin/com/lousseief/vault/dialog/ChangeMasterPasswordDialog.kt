package com.lousseief.vault.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import tornadofx.*

class ChangeMasterPasswordDialog(evaluator: (String, String) -> Unit): Dialog<String?>() {

    private val errorProperty = SimpleStringProperty("")

    private val oldPasswordProperty = SimpleStringProperty("")
    private val oldPassword by oldPasswordProperty
    private val oldPasswordField = PasswordField().apply { bind(oldPasswordProperty) }

    private val passwordProperty = SimpleStringProperty("")
    private val password by passwordProperty
    private val passwordField = PasswordField().apply { bind(passwordProperty) }

    private val passwordRepetitionProperty = SimpleStringProperty("")
    private val passwordRepetition by passwordRepetitionProperty
    private val passwordRepetitionField = PasswordField().apply { bind(passwordRepetitionProperty) }

    private fun validate() =
        when {
            password != passwordRepetition -> {
                passwordRepetitionField.requestFocus()
                passwordRepetitionField.selectAll()
                throw Exception("The password and password repetition didn't match.")
            }
            password.isEmpty() -> {
                passwordField.requestFocus()
                throw Exception("Empty password is not allowed.")
            }
            else -> {
                oldPasswordField.requestFocus()
                oldPasswordField.selectAll()
            }
        }

    init {
        val okButton = ButtonType.OK
        val cancelButton = ButtonType.CANCEL

        val icon = Label().apply {
            styleClass.addAll("alert", "confirmation", "dialog-pane")
        }

        val errorLabel = Label().apply {
            removeWhen(errorProperty.isEmpty)
            hgrow = Priority.ALWAYS
            vgrow = Priority.NEVER
            maxHeight = Double.MAX_VALUE
            textProperty().bind(errorProperty)
            textAlignment = TextAlignment.RIGHT
            style = "-fx-text-fill: red"
            alignment = Pos.CENTER_RIGHT
            isWrapText = true
        }

        val v = VBox().apply {
            spacing = 10.0
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.ALWAYS
            children.addAll(
                *(listOf(
                    Pair("Current password:", oldPasswordField),
                    Pair("New password:", passwordField),
                    Pair("Repeat new password:", passwordRepetitionField)
                ).map {
                    HBox().apply {
                        alignment = Pos.CENTER
                        spacing = 5.0
                        children.addAll(
                            Region().apply {
                                minWidth = 0.0
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                            },
                            Label(it.first),
                            it.second.apply {
                                hgrow = Priority.NEVER
                                prefWidth = 200.0
                            }
                        )
                    }
                }.toTypedArray()),
                errorLabel
            )
        }

        setResultConverter {
            type: ButtonType ->
                when(type) {
                    ButtonType.OK -> password
                    ButtonType.CANCEL -> null
                    ButtonType.CLOSE -> null
                    else -> null
                }
        }

        dialogPane.apply {
            buttonTypes.addAll(cancelButton, okButton)
            hgrow = Priority.NEVER
            vgrow = Priority.ALWAYS
            content = v
            lookupButton(okButton).apply {
                addEventFilter(ActionEvent.ACTION) {
                    event ->
                        try {
                            validate()
                            evaluator(oldPassword, password)
                        }
                        catch(e: Exception) {
                            event.consume()
                            errorProperty.set(e.message)
                            dialogPane.scene.window.sizeToScene()
                        }
                }
                disableWhen(passwordProperty.isEmpty.or(oldPasswordProperty.isEmpty).or(passwordRepetitionProperty.isEmpty))
            }

        }
        headerText = "Change vault master password"
        graphic = icon
        Platform.runLater{
            errorLabel.maxWidth = v.width
            oldPasswordField.requestFocus()
        }
    }
}