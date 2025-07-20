package com.lousseief.vault.view

import com.google.gson.Gson
import com.lousseief.vault.model.Association
import com.lousseief.vault.model.AssociationWithCredentials
import com.lousseief.vault.model.Profile
import com.lousseief.vault.model.Settings
import com.lousseief.vault.service.*
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*

class RegisterView: View() {

    val name: SimpleStringProperty = SimpleStringProperty()
    val password: SimpleStringProperty = SimpleStringProperty()
    val passwordRepetition: SimpleStringProperty = SimpleStringProperty()

    var userNameField: TextField by singleAssign()
    var passwordField: PasswordField by singleAssign()
    var passwordRepetitionField: PasswordField by singleAssign()

    override fun onDock() {
        name.set(null)
        password.set(null)
        passwordRepetition.set(null)
    }

    fun registerUser() {
        try {
            if (password.value != passwordRepetition.value) {
                passwordRepetitionField.requestFocus()
                passwordRepetitionField.selectAll()
                throw Exception("The password and password repetition didn't match.")
            }
            else if (password.value.isEmpty()) {
                passwordField.requestFocus()
                throw Exception("Empty password are not allowed.")
            }
            else {
                userNameField.requestFocus()
                userNameField.selectAll()
            }
            UserService.createUser(name.value, password.value)
            alert(
                Alert.AlertType.INFORMATION,
                "User added",
                "The user was successfully added! Please go ahead and login!"
            ) {
                replaceWith<LoginView>(
                    centerOnScreen = true,
                    sizeToScene = true
                    //transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.RIGHT)
                )
            }
        } catch (e: Exception) {
            alert(Alert.AlertType.ERROR, "User registration failed", e.message)
        }

        /*val plainText =
            "${ConversionService.bytesToBase64(saltBytes)}\n" +
            "${ConversionService.bytesToBase64(hashSaltBytes)}\n" +
            "${ConversionService.bytesToBase64(hashBytes)}"
        println(plainText.length)
        val enc = AES256Service.encrypt(passwordBytes, ConversionService.AsciiToBytes(plainText))
        if(enc !== null) {
            val (cipherBytes, ivBytes) = enc
            val cipherText = ConversionService.bytesToBase64(cipherBytes)
            println(cipherText)
            val plainBytes = AES256Service.decrypt(
                passwordBytes,
                ivBytes,
                ConversionService.Base64ToBytes(cipherText)
            )
            if (plainBytes !== null) {
                val decText = ConversionService.bytesToAscii(plainBytes)
                val div = decText.split("\n");
                println(div.size)
                val decSalt = div[0]
                val decHashSalt = div[1]
                val decHash = div[2]
                val (verSalt, verPw) = PBKDF2Service.deriveKey(password.value, ConversionService.Base64ToBytes(decSalt))
                val (verHashsalt, verHash) = PBKDF2Service.deriveKey(ConversionService.bytesToAscii(verPw), ConversionService.Base64ToBytes(decHashSalt))
                println(ConversionService.bytesToBase64(verHash))
                println(decHash)

            }
        }*/

        //println(ConversionService.bytesToISO88591(p))
        //File("../elias.txt").forEachLine { l -> println(l) }
        println("Name is: " + name.get() + " " + password.get())
        //replaceWith<MainView>()
    }

    override
    val root =
        stackpane {
            minWidth = 300.0
            alignment = Pos.TOP_RIGHT
            padding = Insets(5.0)
            form {
                padding = Insets(5.0)
                fieldset("Register", FontAwesomeIconView(FontAwesomeIcon.USER)) {
                    paddingBottom = 0.0
                    field("Username") {
                        textfield {
                            userNameField = this
                            bind(name)
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                event ->
                                    if(event.code.equals(KeyCode.ENTER) && name.isNotEmpty.value)
                                        passwordField.requestFocus()
                            }
                        }
                    }
                    field("Password") {
                        passwordfield {
                            passwordField = this
                            bind(password)
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                event ->
                                    if(event.code.equals(KeyCode.ENTER) && password.isNotEmpty.value)
                                        passwordRepetitionField.requestFocus()
                            }
                        }
                    }
                    field("Password repetition") {
                        passwordfield {
                            passwordRepetitionField = this
                            bind(passwordRepetition)
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                event ->
                                    if(event.code.equals(KeyCode.ENTER) && passwordRepetition.isNotEmpty.value)
                                        registerUser()
                            }
                        }
                    }
                    region {
                        prefHeight = 5.0
                    }
                    button("Register") {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                        addEventFilter(KeyEvent.KEY_PRESSED) {
                            if (it.code === KeyCode.ENTER)
                                registerUser()
                        }
                        action { registerUser() }
                        // Save button is disabled until every field has a value
                        disableProperty().bind(name.isNull.or(password.isNull).or(passwordRepetition.isNull));

                    }
                }
            }
            hyperlink("Login") {
                hgrow = Priority.NEVER
                vgrow = Priority.NEVER
                text = "Login"
                action {
                    replaceWith<LoginView>(
                        centerOnScreen = true,
                        sizeToScene = true
                        //transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.RIGHT)
                    )
                }
            }
        }
}
