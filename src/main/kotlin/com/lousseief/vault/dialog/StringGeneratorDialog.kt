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
import javafx.scene.input.*
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import javafx.util.StringConverter
import tornadofx.*
import java.lang.Integer.parseInt


class StringGeneratorDialog(defaultPasswordLength: Int): Dialog<String?>() {

    companion object {
        const val STRING_LENGTH_MAX = 100
        const val STRING_LENGTH_MIN = 1
    }

    val lowerCaseProperty = SimpleBooleanProperty(true)
    val upperCaseProperty = SimpleBooleanProperty(true)
    val numbersProperty = SimpleBooleanProperty(true)
    val specialCharsProperty = SimpleBooleanProperty(true)
    val stringLengthProperty = SimpleIntegerProperty(defaultPasswordLength)
    val stringLength by stringLengthProperty
    val generatedStringProperty = SimpleStringProperty("")
    val generatedString by generatedStringProperty

    var stringField: TextField by singleAssign()
    var intSpinner: Spinner<Number> by singleAssign()

    private fun copySelectionToClipboard(string: String) =
        ClipboardContent()
            .apply {
                putString(string)
                Clipboard.getSystemClipboard().setContent(this)
            }

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
        headerText = "Generate a random string for general use"

        val copyButton = ButtonType("Copy string to clipboard", ButtonBar.ButtonData.CANCEL_CLOSE)
        val generateButton = ButtonType("Generate string", ButtonBar.ButtonData.CANCEL_CLOSE)
        val closeButton = ButtonType.CLOSE

        dialogPane.buttonTypes.addAll(copyButton, generateButton, closeButton)
        ButtonBar.setButtonUniformSize(dialogPane.lookupButton(copyButton), false)
        ButtonBar.setButtonUniformSize(dialogPane.lookupButton(generateButton), false)
        ButtonBar.setButtonUniformSize(dialogPane.lookupButton(closeButton), false)

        val icon = Label()
        icon.styleClass.addAll("alert", "confirmation", "dialog-pane")
        setGraphic(icon)
        setOnCloseRequest { close() }

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
                    field("Generated string:", Orientation.HORIZONTAL) {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                        textfield {
                            bind(generatedStringProperty)
                            isEditable = false
                            isMouseTransparent = false
                            isFocusTraversable = true
                            hgrow = Priority.ALWAYS
                            maxWidth = Double.MAX_VALUE
                            stringField = this
                            val keyCodeCopy = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY)
                            setOnKeyPressed {
                                    event ->
                                if(keyCodeCopy.match(event))
                                    copySelectionToClipboard(generatedString)
                            }
                        }
                    }
                }
            }
            form {
                padding = Insets(0.0)
                fieldset {
                    padding = Insets(0.0)
                    hbox {
                        alignment = Pos.CENTER_LEFT
                        spacing = 15.0
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                        field("Character groups to use:", Orientation.HORIZONTAL) {
                            alignment = Pos.CENTER_LEFT
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
                            maxWidth = Double.MAX_VALUE
                        }
                        field("Length:", Orientation.HORIZONTAL) {
                            alignment = Pos.CENTER_LEFT
                            labelPosition = Orientation.VERTICAL
                            hgrow = Priority.NEVER
                            maxWidth = 80.0
                            hbox {
                                prefHeight = 30.0
                                alignment = Pos.CENTER_LEFT
                                vgrow = Priority.ALWAYS
                                spinner(1, 100, defaultPasswordLength, 1, true, stringLengthProperty) {
                                    maxWidth = 80.0
                                    prefWidth = 80.0
                                    intSpinner = this
                                }
                            }
                        }
                    }
                }
            }
        }
        dialogPane.content = v
        /*v.spacing = 5.0
        v.add(passwordField)
        v.add(errorLabel)
        v.maxWidth = Double.MAX_VALUE
        v.hgrow = Priority.ALWAYS*/

        dialogPane.lookupButton(generateButton).apply {
            addEventFilter(ActionEvent.ACTION) { event ->
                generatedStringProperty.set(
                    CryptoUtils.generateRandomString(
                        getCharPoolContent(),
                        stringLengthProperty.value
                    )
                )
                event.consume()
            }
            Platform.runLater { this.requestFocus() }
        }
        dialogPane.lookupButton(copyButton).apply {
            addEventFilter(ActionEvent.ACTION) { event ->
                copySelectionToClipboard(generatedString)
                event.consume()
            }
            disableWhen { generatedStringProperty.isEmpty }
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
                                it > STRING_LENGTH_MAX -> STRING_LENGTH_MAX
                                it < STRING_LENGTH_MIN -> STRING_LENGTH_MIN
                                else -> it
                            }
                        }
                        ?: stringLength)
                        .also {
                            intSpinner.editor.text = toString(it)
                        }

            }
        )
    }
}