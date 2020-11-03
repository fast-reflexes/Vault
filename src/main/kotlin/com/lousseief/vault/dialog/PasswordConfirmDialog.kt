package com.lousseief.vault.dialog

import com.lousseief.vault.controller.UserController
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import tornadofx.*

class PasswordConfirmDialog(evaluator: (String, ActionEvent) -> Unit = {_, _ -> Unit}): Dialog<String?>() {

    val errorProperty = SimpleStringProperty("")

    val passwordProperty = SimpleStringProperty("")
    val password by passwordProperty

    init {
        val okButton = ButtonType.OK
        val cancelButton = ButtonType.CANCEL
        dialogPane.buttonTypes.add(cancelButton)
        dialogPane.buttonTypes.add(okButton)
        dialogPane.lookupButton(okButton).disableWhen(passwordProperty.isEmpty)

        val passwordField = PasswordField()
        passwordField.bind(passwordProperty)
        passwordField.hgrow = Priority.ALWAYS
        passwordField.maxWidth = Double.MAX_VALUE
        Platform.runLater{ passwordField.requestFocus() }

        val icon = Label()
        icon.styleClass.addAll("alert", "confirmation", "dialog-pane")
        setGraphic(icon)

        val errorLabel = Label()
        errorLabel.removeWhen(errorProperty.isEmpty)
        errorLabel.hgrow = Priority.ALWAYS
        errorLabel.vgrow = Priority.NEVER
        errorLabel.maxHeight = Double.MAX_VALUE
        errorLabel.textProperty().bind(errorProperty)
        errorLabel.textAlignment = TextAlignment.LEFT
        errorLabel.style = "-fx-text-fill: red";
        errorLabel.alignment = Pos.CENTER_LEFT
        errorLabel.setWrapText(true)
        Platform.runLater{errorLabel.setMaxWidth(passwordField.width)}

        dialogPane.hgrow = Priority.NEVER
        dialogPane.vgrow = Priority.ALWAYS
        headerText = "Enter your vault password"

        val v = VBox()
        v.spacing = 5.0
        dialogPane.content = v
        v.add(passwordField)
        v.add(errorLabel)
        v.maxWidth = Double.MAX_VALUE
        v.hgrow = Priority.ALWAYS
        setResultConverter {
                type: ButtonType ->
                    when(type) {
                        ButtonType.OK -> password
                        ButtonType.CANCEL -> null
                        ButtonType.CLOSE -> null
                        else -> null
                    }
        }
        dialogPane.lookupButton(ButtonType.OK).addEventFilter(ActionEvent.ACTION) { event ->
            try {
                evaluator(passwordField.text, event)
            }
            catch(e: Exception) {
                event.consume()
                errorProperty.set(e.message)
                dialogPane.scene.window.sizeToScene()
            }
        }
    }
}