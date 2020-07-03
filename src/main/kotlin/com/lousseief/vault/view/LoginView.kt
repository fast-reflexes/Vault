package com.lousseief.vault.view

import com.google.gson.Gson
import com.lousseief.vault.controller.UserController
import com.lousseief.vault.exception.AuthenticationException
import com.lousseief.vault.model.Association
import com.lousseief.vault.model.AssociationProxy
import com.lousseief.vault.model.Profile
import com.lousseief.vault.service.FileService
import com.lousseief.vault.service.UserService
import com.lousseief.vault.service.VerificationService
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.Alert
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import tornadofx.*

class LoginView: View() {

    val name: SimpleStringProperty = SimpleStringProperty()
    val password: SimpleStringProperty = SimpleStringProperty()

    override
    fun onDock() {
        super.onDock()
        //primaryStage.width = 1000.0
        //primaryStage.height = 768.0
        //primaryStage.sizeToScene()
        //currentStage!!.sizeToScene()
        currentWindow!!.sizeToScene()

        System.out.println("before sceneW " + currentStage!!.getWidth());
        System.out.println("before sceneH " +  currentStage!!.getHeight());
        System.out.println("before stageW " + primaryStage.getWidth());
        System.out.println("before stageH " + primaryStage.getHeight());

        primaryStage.show();

        System.out.println("after sceneW " + currentStage!!.getWidth());
        System.out.println("after sceneH " +  currentStage!!.getHeight());
        System.out.println("after stageW " + primaryStage.getWidth());
        System.out.println("after stageH " + primaryStage.getHeight());

        primaryStage.setMinWidth(primaryStage.getWidth());
        primaryStage.setMinHeight(primaryStage.getHeight());
        //
    }

    private fun attemptLogin() {
        if (!FileService.userExists(name.value))
            alert(
                Alert.AlertType.ERROR,
                "Username or password invalid",
                "The username or password was invalid, please try again"
            )
        //else if(password.value !== passwordRepetition.value)
        //    alert(Alert.AlertType.ERROR, "Password mismatch", "The password and password repetition didn't match")
        else {
            try {
                val loggedInUser = UserService.loginUser(name.value, password.value)
                println("LOGGED IN")
                println(loggedInUser.passwordSalt.length)
                println(loggedInUser.verificationSalt.length)
                println(loggedInUser.verificationHash.length)
                val userScope = Scope()
                val loggedInView = find<MainView>(userScope, mapOf(MainView::user to loggedInUser))
                replaceWith(
                    loggedInView,
                    centerOnScreen = true,
                    sizeToScene = true
                    //transition = ViewTransition.Flip(500.millis)
                )
                /*val b = Association()
                b.mainIdentifier = "elias"
                val str = Gson().toJson(b, Association::class.java)
                println(str)
                val c = Gson().fromJson(str, Association::class.java)
                println(c.mainIdentifier)*/
            }
            catch(e: AuthenticationException) {
                alert(Alert.AlertType.ERROR, "Login failed", e.message)
            }

        }
    }

    override
    val root =
        form {
            fieldset("Login", FontAwesomeIconView(FontAwesomeIcon.HOME)) {
                field("Username") { textfield().bind(name) }
                field("Password") { passwordfield().bind(password) }
                borderpane {
                    center =
                        button("Log in") {
                            useMaxWidth= true
                            style{
                                minWidth = 100.percent
                            }
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                if(it.code === KeyCode.ENTER)
                                    attemptLogin()
                            }
                            action {
                                attemptLogin()
                            }
                            // Save button is disabled until every field has a value
                            disableProperty().bind(name.isNull.or(password.isNull));


                        }
                    right =
                        hyperlink("Register") {
                            action { replaceWith<RegisterView>(
                                centerOnScreen = true,
                                sizeToScene = true
                                //transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.LEFT)
                            ) }
                        }
                }
            }
        }
}