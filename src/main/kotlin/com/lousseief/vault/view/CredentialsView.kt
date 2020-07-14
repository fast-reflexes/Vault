package com.lousseief.vault.view

import com.lousseief.vault.controller.CredentialsController
import com.lousseief.vault.controller.UserController
import com.lousseief.vault.dialog.AddCredentialDialog
import com.lousseief.vault.dialog.PasswordConfirmDialog
import com.lousseief.vault.dialog.SingleInputDialog
import com.lousseief.vault.model.Credential
import com.lousseief.vault.model.CredentialModel
import com.lousseief.vault.model.CredentialProxy
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonBar
import javafx.scene.control.ButtonType
import javafx.scene.input.*
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.concurrent.Callable

class CredentialsView: View() {

    private fun timeToStringCallable(time: Instant?): String =
        time
            ?.atZone(ZoneId.of("Europe/Stockholm"))
            ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
        ?: "-"

    val mainIdentifier: SimpleStringProperty by param()
    private val controller: CredentialsController by inject()
    private val userController: UserController by inject()
    private val model: CredentialProxy by inject()
    private val lastUpdatedString = Bindings.createStringBinding(
        Callable<String> { timeToStringCallable(model.lastUpdated.value) }
        , model.lastUpdated
    )
    private val createdString = Bindings.createStringBinding(
        Callable<String> { timeToStringCallable(model.created.value) }
        , model.created
    )
    private val lastUpdatedLabel = Bindings.concat("Last updated: " ).concat(lastUpdatedString)
    private val createdLabel = Bindings.concat("Created: " ).concat(createdString)
    private val currentCredential = SimpleIntegerProperty(0)
    private val currentCredentialLabel = Bindings.concat(currentCredential).concat(" / ").concat(controller.credentials.sizeProperty)
    private val identity = SimpleStringProperty("")
    private  var copyButton: Button by singleAssign()

    private val headerText = object: SimpleStringProperty() {

        override
        fun getValue(): String {
            return "Credentials for entry " + (super.getValue() ?: "(unnamed entry)")
        }

    }

    private fun copySelectionToClipboard(string: String) {
        val clipboardContent = ClipboardContent()
        clipboardContent.putString(string)
        Clipboard.getSystemClipboard().setContent(clipboardContent)
    }

    private fun setCurrent(value: Int) {
        if(value != currentCredential.value)
            currentCredential.set(value)
        else
            setModel(value)
    }

    private fun setModel(newValue: Number) {
        if(newValue == 0)
            model.item = null
        else {
            model.item = controller.credentials[newValue.toInt() - 1]
            copyButton.requestFocus()
        }
    }

    init {
        if(controller.credentials.size > 0)
            model.item = controller.credentials[0]
        else
            model.item = null
        headerText.bind(mainIdentifier)
        currentCredential.addListener{ _, _, newValue -> setModel(newValue) }
        controller.credentials.addListener(
            object: ListChangeListener<CredentialModel> {
                override fun onChanged(change: ListChangeListener.Change<out CredentialModel>) {
                    println("clist changed!)")
                    change.next()
                    if(change.wasReplaced()) {
                        // both removed and added = loaded a new non-empty list of credentials, indicates loaded view
                        println("clist replaced!)" + controller.credentials.size)
                      setCurrent(if (controller.credentials.size > 0) 1 else 0)
                    }
                    else if(change.wasAdded()) {
                        if(change.addedSize > 1)
                            setCurrent(if (controller.credentials.size > 0) 1 else 0)
                        else // indicates loaded view
                           setCurrent(controller.credentials.size)
                    }
                    else if(change.wasRemoved()) {
                        // whenever an entry is removed OR we removed a list of credentials and the new one was empty
                        println("" + controller.credentials.size + " " + currentCredential.value)
                        if(controller.credentials.size < currentCredential.value)
                            setCurrent(controller.credentials.size) // will trigger set model
                        else
                            setCurrent(currentCredential.value)
                    }
                }
            }
        )
    }

    override val root = vbox {
        borderpane {
            padding = Insets(20.0)
            top = hbox {
                paddingBottom = 20.0
                alignment = Pos.CENTER
                label {
                    alignment = Pos.CENTER
                    textAlignment = TextAlignment.CENTER
                    bind(headerText)
                    style {
                        fontSize = 1.5.em
                    }
                }
            }
            left = button {
                graphic = FontAwesomeIconView(FontAwesomeIcon.CHEVRON_LEFT)
                alignment = Pos.CENTER
                prefWidth = 30.0
                prefHeight = 30.0
                disableWhen { Bindings.equal(1, currentCredential).or(model.empty) }
                style {
                    backgroundRadius += box(50.percent)
                }
                action {
                    currentCredential.set(currentCredential.value - 1)
                }
            }
            center = vbox {
                alignment = Pos.CENTER
                spacing = 10.0
                label {
                    bind(currentCredentialLabel)
                    visibleWhen(!model.empty)
                }
            }
            right = button {
                alignment = Pos.CENTER
                graphic = FontAwesomeIconView(FontAwesomeIcon.CHEVRON_RIGHT)
                prefWidth = 30.0
                disableWhen { Bindings.equal(controller.credentials.sizeProperty, currentCredential).or(model.empty) }
                prefHeight = 30.0
                style {
                    backgroundRadius += box(50.percent)
                }
                action {
                    currentCredential.set(currentCredential.value + 1)
                }
            }
        }
        vbox {
            alignment = Pos.CENTER
            vgrow = Priority.ALWAYS
            maxHeight = Double.MAX_VALUE
            minWidth = 300.0
            prefWidth = 300.0
            style {
                backgroundColor += Color.gray(0.7)
            }
            padding = insets(10.0)
            stackpane {
                vgrow = Priority.ALWAYS
                maxHeight = Double.MAX_VALUE
                label("You have no credentials yet for this entry") {
                    visibleWhen { model.empty }
                }
                vbox {
                    hiddenWhen { model.empty }
                    vgrow = Priority.ALWAYS
                    maxHeight = Double.MAX_VALUE
                    style {
                        borderColor += box(all = Color.LIGHTGRAY)
                        borderWidth += box(all = 1.px)
                        borderStyle += BorderStrokeStyle.SOLID
                        backgroundColor += Color.gray(0.97)

                    }
                    form {
                        spacing = 10.0
                        maxHeight = Double.MAX_VALUE
                        vgrow = Priority.ALWAYS
                        fieldset {
                            spacing = 10.0
                            maxHeight = Double.MAX_VALUE
                            vgrow = Priority.ALWAYS
                            paddingBottom = 0.0
                            labelPosition = Orientation.VERTICAL
                            vbox {
                                hgrow = Priority.ALWAYS
                                vgrow = Priority.NEVER
                                maxWidth = Double.MAX_VALUE
                                field("Password:") {
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    textfield {
                                        bind(model.password)
                                        hgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        isEditable = false
                                        isMouseTransparent = false
                                        isFocusTraversable = true
                                        val keyCodeCopy = KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_ANY)
                                        setOnKeyPressed {
                                            event ->
                                                if(keyCodeCopy.match(event))
                                                    copySelectionToClipboard(model.password.value)
                                        }
                                    }

                                }
                                gridpane {
                                    hgap = 10.0
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    constraintsForColumn(0).percentWidth = 50.0
                                    constraintsForColumn(1).percentWidth = 50.0
                                    row {
                                        button("Copy password") {
                                            hgrow = Priority.ALWAYS
                                            maxWidth = Double.MAX_VALUE
                                            copyButton = this
                                            action {
                                                copySelectionToClipboard(model.password.value)
                                            }
                                        }
                                        button("Update password") {
                                            hgrow = Priority.ALWAYS
                                            disableWhen(model.empty)
                                            maxWidth = Double.MAX_VALUE
                                            action {
                                                val newPassword = AddCredentialDialog({ input: String?, _: ActionEvent ->
                                                    if (input === null || input.isEmpty())
                                                        throw Exception("Empty password is neither advisable nor allowed.")
                                                }, "Generate or enter a new password for your credential").showAndWait()
                                                if (newPassword.isPresent) {
                                                    model.password.value = newPassword.get()
                                                    model.commit()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            vbox {
                                hgrow = Priority.ALWAYS
                                maxWidth = Double.MAX_VALUE
                                vgrow = Priority.ALWAYS
                                maxHeight = Double.MAX_VALUE
                                field("Associated user names:", Orientation.VERTICAL) {
                                    maxHeight = Double.MAX_VALUE
                                    vgrow = Priority.ALWAYS
                                    listview(model.identities) {
                                        bindSelected(identity)
                                        minHeight = 80.0
                                        prefHeight = 80.0
                                        maxHeight = Double.MAX_VALUE
                                        vgrow = Priority.ALWAYS
                                        maxWidth = Double.MAX_VALUE
                                        hgrow = Priority.ALWAYS
                                        placeholder =
                                            label("You have no user names connected to the current password") {
                                                padding = Insets(10.0)
                                                textAlignment = TextAlignment.CENTER
                                                isWrapText = true
                                            }
                                    }
                                }
                                gridpane {
                                    hgap = 10.0
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    constraintsForColumn(0).percentWidth = 50.0
                                    constraintsForColumn(1).percentWidth = 50.0
                                    row {
                                        button("Add user name") {
                                            hgrow = Priority.ALWAYS
                                            disableWhen(model.empty)
                                            maxWidth = Double.MAX_VALUE
                                            action {
                                                val newLoginName =
                                                    SingleInputDialog({ input: String, _: ActionEvent ->
                                                        if (input.isEmpty())
                                                            throw Exception("Empty login name not allowed, please try again.")
                                                        else if (model.identities.value.any {
                                                                it.toLowerCase() == input.toLowerCase()
                                                            })
                                                            throw Exception("Login name already exists, please try again.")
                                                    }, "Enter a login name to add to your credential").showAndWait()
                                                if (newLoginName.isPresent)
                                                    model.identities.value.add(newLoginName.get())
                                            }
                                        }
                                        button("Remove user name") {
                                            hgrow = Priority.ALWAYS
                                            disableWhen(identity.isEmpty)
                                            maxWidth = Double.MAX_VALUE
                                            action {
                                                val deleteButtonType = ButtonType("Delete login name", ButtonBar.ButtonData.OK_DONE)
                                                val a = Alert(Alert.AlertType.WARNING).apply {
                                                    headerText = "Confirm delete"
                                                    contentText = "Are you sure you want to delete this login name?"
                                                    dialogPane.buttonTypes.add(ButtonType.CANCEL)
                                                    dialogPane.buttonTypes.remove(ButtonType.OK)
                                                    dialogPane.buttonTypes.add(deleteButtonType)
                                                    dialogPane
                                                        .lookupButton(deleteButtonType)
                                                        .addEventFilter(ActionEvent.ACTION) {
                                                            model.identities.value.remove(identity.value)
                                                        }
                                                }
                                                val res = a.showAndWait()
                                                if(res.isPresent && res.get() === deleteButtonType)
                                                    alert(
                                                        type = Alert.AlertType.INFORMATION,
                                                        header = "Login name successfully removed",
                                                        content = "The login name was successfully deleted from your credential"
                                                    )
                                            }
                                        }
                                    }
                                }
                            }
                            label().bind(createdLabel)
                            label().bind(lastUpdatedLabel)
                            button("Delete credential") {
                                hgrow = Priority.ALWAYS
                                disableWhen(model.empty)
                                maxWidth = Double.MAX_VALUE
                                action {
                                    val deleteButtonType = ButtonType("Delete credential", ButtonBar.ButtonData.OK_DONE)
                                    val a = Alert(Alert.AlertType.WARNING).apply {
                                        headerText = "Confirm delete"
                                        contentText = "Are you sure you want to delete this login credential? This action cannot be undone."
                                        dialogPane.buttonTypes.add(ButtonType.CANCEL)
                                        dialogPane.buttonTypes.remove(ButtonType.OK)
                                        dialogPane.buttonTypes.add(deleteButtonType)
                                        dialogPane
                                            .lookupButton(deleteButtonType)
                                            .addEventFilter(ActionEvent.ACTION) {
                                                    controller.removeCredential(model.password.value)
                                            }
                                    }
                                    val res = a.showAndWait()
                                    if(res.isPresent && res.get() === deleteButtonType)
                                        alert(
                                            type = Alert.AlertType.INFORMATION,
                                            header = "Credential successfully removed",
                                            content = "The credential was successfully deleted from your entry"
                                        )
                                }
                            }
                        }
                    }
                }
            }
        }
        hbox {
            spacing = 10.0
            padding = insets(10.0)
            button("Add new credential") {
                action {
                    val newPassword = AddCredentialDialog({ input: String?, _: ActionEvent ->
                        if (input === null || input.isEmpty())
                            throw Exception("Empty password is neither advisable nor allowed.")
                    }, "Generate or enter a password for your new credential").showAndWait()
                    if (newPassword.isPresent) {
                        controller.addCredential(
                            newPassword.get()
                        )
                    }
                }
            }
            region {
                hgrow = Priority.ALWAYS
                maxWidth = Double.MAX_VALUE
            }
            button("Close") {
                action {
                    val closeCredentialsWindow = { close() }
                    if(controller.altered.value == true)
                        alert(
                            type = Alert.AlertType.CONFIRMATION,
                            header = "Do you really want to close this window?",
                            content = "You have unsaved changes in these credentials, do you really want to close them without saving first? If no, press \"Cancel\" and then press \"Save\".",
                            actionFn = { type ->
                                if(type === ButtonType.OK)
                                    closeCredentialsWindow()
                            }
                        )
                    else
                        close()
                }
            }
            button("Save") {
                disableWhen(controller.altered.not())
                action {
                    PasswordConfirmDialog { password: String, _: ActionEvent ->
                        userController.updateCredentials(
                            mainIdentifier.value,
                            controller.credentials.map { Credential.fromModel(it) },
                            password
                        )
                        controller.altered.set(false)
                    }.showAndWait()
                }
            }
        }
    }
}