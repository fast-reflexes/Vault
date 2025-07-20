package com.lousseief.vault.view

import com.lousseief.vault.controller.UserController
import com.lousseief.vault.exception.AuthenticationException
import com.lousseief.vault.service.FileService
import com.lousseief.vault.service.UserService
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Priority
import tornadofx.*

class LoginView: View() {

    val name: SimpleStringProperty = SimpleStringProperty()
    val password: SimpleStringProperty = SimpleStringProperty()

    var userNameField: TextField by singleAssign()
    var passwordField: TextField by singleAssign()

    private val controller: UserController by inject()

    override
    fun onDock() {
        super.onDock()
        name.set(null)
        password.set(null)
        //primaryStage.width = 1000.0
        //primaryStage.height = 768.0
        //primaryStage.sizeToScene()
        //currentStage!!.sizeToScene()
        primaryStage.minWidth = 0.0
        primaryStage.minHeight = 0.0
        currentWindow!!.sizeToScene()
        //Platform.runLater { primaryStage.minWidth = currentStage!!.width }
        println("before sceneW " + currentStage!!.width)
        println("before sceneH " +  currentStage!!.height)
        println("before stageW " + primaryStage.width)
        println("before stageH " + primaryStage.height)

        primaryStage.show()

        println("after sceneW " + currentStage!!.width)
        println("after sceneH " +  currentStage!!.height)
        println("after stageW " + primaryStage.width)
        println("after stageH " + primaryStage.height)

        primaryStage.minWidth = primaryStage.width
        primaryStage.minHeight = primaryStage.height
    }

    private fun attemptLogin() {
        if (!FileService.userExists(name.value)) {
            alert(
                Alert.AlertType.ERROR,
                "Username or password invalid",
                "The username or password was invalid, please try again"
            )
            passwordField.requestFocus()
            passwordField.selectAll()
        }
        //else if(password.value !== passwordRepetition.value)
        //    alert(Alert.AlertType.ERROR, "Password mismatch", "The password and password repetition didn't match")
        else {
            try {
                val loggedInUser = UserService.loadUser(name.value)
                val associations = loggedInUser.initialize(password.value)
                println("LOGGED IN")
                println(loggedInUser.keyMaterialSalt.length)
                println(loggedInUser.verificationSalt.length)
                println(loggedInUser.verificationHash.length)
                controller.setUser(loggedInUser, associations, password.value)
                replaceWith(
                    find<MainView>(),
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
                passwordField.requestFocus()
                passwordField.selectAll()
            }
        }
    }

    override
    val root =
        stackpane {
            minWidth = 300.0
            alignment = Pos.TOP_RIGHT
            padding = Insets(5.0)
            form {
                padding = Insets(5.0)
                fieldset("Login", FontAwesomeIconView(FontAwesomeIcon.HOME)) {
                    paddingBottom = 0.0
                    field("Username") {
                        textfield {
                            bind(name)
                            userNameField = this
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                event ->
                                    if(event.code.equals(KeyCode.ENTER) && name.isNotEmpty.value)
                                        passwordField.requestFocus()
                            }
                        }
                    }
                    field("Password") {
                        passwordfield {
                            bind(password)
                            passwordField = this
                            addEventFilter(KeyEvent.KEY_PRESSED) {
                                event ->
                                    if(event.code.equals(KeyCode.ENTER) && password.isNotEmpty.value)
                                        attemptLogin()
                            }
                        }
                    }
                    region {
                        prefHeight = 5.0
                    }
                    button("Log in") {
                        hgrow = Priority.ALWAYS
                        maxWidth = Double.MAX_VALUE
                        addEventFilter(KeyEvent.KEY_PRESSED) {
                            if (it.code === KeyCode.ENTER)
                                attemptLogin()
                        }
                        action { attemptLogin() }
                        // Save button is disabled until every field has a value
                        disableProperty().bind(name.isNull.or(password.isNull))
                    }
                }
            }
            hyperlink("Register") {
                hgrow = Priority.NEVER
                vgrow = Priority.NEVER
                text = "Register"
                action {
                    replaceWith<RegisterView>(
                        centerOnScreen = true,
                        sizeToScene = true
                        //transition = ViewTransition.Metro(500.millis, ViewTransition.Direction.LEFT)
                    )
                }
            }
        }
}
