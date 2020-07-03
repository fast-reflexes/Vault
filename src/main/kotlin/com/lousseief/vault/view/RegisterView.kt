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
import javafx.scene.control.Alert
import tornadofx.*

class RegisterView: View() {

    val name: SimpleStringProperty = SimpleStringProperty()
    val password: SimpleStringProperty = SimpleStringProperty()
    val passwordRepetition: SimpleStringProperty = SimpleStringProperty()

    override
    val root =
        form {
            fieldset("Register", FontAwesomeIconView(FontAwesomeIcon.USER)) {
                field("Username") { textfield().bind(name) }
                field("Password") { passwordfield().bind(password) }
                field("Password repetition") { passwordfield().bind(passwordRepetition) }
                borderpane {
                    right =
                        button("Register") {
                            action {
                                try {
                                    if(!password.value.equals(passwordRepetition.value))
                                        throw Exception("The password and password repetition didn't match.")
                                    if(password.value.length == 0)
                                        throw Exception("Empty password are not allowed.")
                                    val registeredUser = UserService.createUser(name.value, password.value)
                                    alert(
                                        Alert.AlertType.INFORMATION,
                                        "User added",
                                        "The user was successfully added! Please go ahead and login!"
                                    ) {
                                        replaceWith<LoginView>(
                                            transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.RIGHT)
                                        )
                                    }
                                }
                                catch(e: Exception) {
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
                            // Save button is disabled until every field has a value
                            disableProperty().bind(name.isNull.or(password.isNull).or(passwordRepetition.isNull));

                        }
                    left =
                        hyperlink("Back to login") {
                            action { replaceWith<LoginView>(
                                centerOnScreen = true,
                                sizeToScene = true
                                //transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.RIGHT)
                            ) }
                        }
                }
            }
        }
}