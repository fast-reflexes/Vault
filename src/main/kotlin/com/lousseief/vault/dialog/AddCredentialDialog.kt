package com.lousseief.vault.dialog

import com.lousseief.vault.crypto.CryptoUtils
import javafx.application.Platform
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import javafx.util.StringConverter
import tornadofx.*
import java.lang.Integer.parseInt


class AddCredentialDialog(defaultPasswordLength: Int, evaluator: (String, ActionEvent) -> Unit, header: String): Dialog<String?>() {

    companion object {
        const val PASSWORD_LENGTH_MAX = 40
        const val PASSWORD_LENGTH_MIN = 5
    }

    val errorProperty = SimpleStringProperty("")

    val lowerCaseProperty = SimpleBooleanProperty(true)
    val upperCaseProperty = SimpleBooleanProperty(true)
    val numbersProperty = SimpleBooleanProperty(true)
    val specialCharsProperty = SimpleBooleanProperty(true)
    val passwordLengthProperty = SimpleIntegerProperty(defaultPasswordLength)
    val passwordLength by passwordLengthProperty
    val passwordProperty = SimpleStringProperty("")
    val password by passwordProperty

    var passwordField: TextField by singleAssign()
    var intSpinner: Spinner<Number> by singleAssign()

    fun getCharPoolContent(): String =
        listOf(
            Pair(lowerCaseProperty, "abcdefghijklmnopqrstuvwxyz"),
            Pair(upperCaseProperty, "ABCDEFGHIJKLMNOPQRSTUVWXYZ"),
            Pair(numbersProperty, "0123456789"),
            Pair(specialCharsProperty, "-_?+=)(&%#!.:,;<>@$£")
        )
            .map { (property, chars) -> if(property.value) chars else "" }
            .joinToString("")

    init {
        headerText = header

        val okButton = ButtonType.OK
        val cancelButton = ButtonType.CANCEL
        dialogPane.buttonTypes.add(cancelButton)
        dialogPane.buttonTypes.add(okButton)
        dialogPane.lookupButton(okButton).disableWhen(passwordProperty.isEmpty)

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

        val v = vbox() {
            spacing = 10.0
            maxWidth = Double.MAX_VALUE
            hgrow = Priority.ALWAYS
            form {
                padding = Insets(0.0)
                fieldset {
                    padding = Insets(0.0)
                    labelPosition = Orientation.HORIZONTAL
                    hbox {
                        spacing = 10.0
                        alignment = Pos.CENTER_LEFT
                        field("Password:", Orientation.HORIZONTAL) {
                            hgrow = Priority.ALWAYS
                            maxWidth = Double.MAX_VALUE
                            textfield {
                                bind(passwordProperty)
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                                passwordField = this
                                Platform.runLater { this.requestFocus() }
                            }
                        }
                        button("Generate password") {
                            action {
                                passwordProperty.set(
                                    CryptoUtils.generateRandomString(
                                        getCharPoolContent(),
                                        passwordLengthProperty.value
                                    )
                                )
                            }
                            //Platform.runLater { this.requestFocus() }
                        }
                    }

                }
            }
            form {
                padding = Insets(0.0)
                fieldset {
                    padding = Insets(0.0)
                    hbox {
                        field("Character groups to use:", Orientation.HORIZONTAL) {
                            alignment = Pos.CENTER
                            labelPosition = Orientation.VERTICAL
                            vgrow = Priority.ALWAYS
                            hbox {
                                prefHeight = 30.0
                                spacing = 15.0
                                alignment = Pos.CENTER_LEFT
                                vgrow = Priority.ALWAYS
                                maxHeight = Double.MAX_VALUE
                                checkbox("A-Z").bind(upperCaseProperty)
                                checkbox("a-z").bind(lowerCaseProperty)
                                checkbox("0-9").bind(numbersProperty)
                                checkbox("Special characters").bind(specialCharsProperty)
                            }
                        }
                        region {
                            hgrow = Priority.ALWAYS
                        }
                        field("Length:", Orientation.HORIZONTAL) {
                            alignment = Pos.CENTER
                            labelPosition = Orientation.VERTICAL
                            hgrow = Priority.NEVER
                            maxWidth = 80.0
                            hbox {
                                prefHeight = 30.0
                                alignment = Pos.CENTER_LEFT
                                vgrow = Priority.ALWAYS
                                spinner(5, 40, defaultPasswordLength, 1, true, passwordLengthProperty) {
                                    maxWidth = 80.0
                                    prefWidth = 80.0
                                    intSpinner = this
                                }
                            }
                        }
                    }
                }
            }
            this += errorLabel
        }
        dialogPane.content = v
        /*v.spacing = 5.0
        v.add(passwordField)
        v.add(errorLabel)
        v.maxWidth = Double.MAX_VALUE
        v.hgrow = Priority.ALWAYS*/
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

        intSpinner.getValueFactory().setConverter(
            object : StringConverter<Number?>() {

                override fun toString(value: Number?): String =
                    value?.toInt()?.toString() ?: "0"

                override fun fromString(value: String?): Number =
                    (value
                        ?.let {
                            try { parseInt(value.trim()) }
                            catch(e: Exception) { null }
                        }
                        ?.let {
                            when {
                                it > PASSWORD_LENGTH_MAX -> PASSWORD_LENGTH_MAX
                                it < PASSWORD_LENGTH_MIN -> PASSWORD_LENGTH_MIN
                                else -> it
                            }
                        }
                        ?: passwordLength)
                        .also {
                            intSpinner.editor.text = toString(it)
                        }

            }
        )

    }
}
