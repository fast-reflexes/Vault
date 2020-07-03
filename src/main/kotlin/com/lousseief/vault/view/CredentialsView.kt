package com.lousseief.vault.view

import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Orientation
import javafx.scene.control.skin.TextInputControlSkin
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import tornadofx.*

class CredentialsView: View() {

    val mainIdentifier = SimpleStringProperty("elof")
    val headerText = object: SimpleStringProperty() {

        override
        fun getValue(): String {
            return "Credentials for entry " + (super.getValue() ?: "(unnamed entry)")
        }

    }

    init {
        headerText.bind(mainIdentifier)
    }

    override val root = vbox {
        form {
            spacing = 10.0
            fieldset {
                textProperty.bind(headerText)
                spacing = 0.0
                paddingBottom = 0.0
                labelPosition = Orientation.VERTICAL
                hbox {
                    spacing = 10.0
                    vbox {
                        field("Passwords") {
                            listview(listOf("ajkljdlkjl", "jjkljkljklj").asObservable())
                        }
                        button("Delete credential") {
                            hgrow = Priority.ALWAYS
                            maxWidth = Double.MAX_VALUE
                        }
                    }
                    vbox {
                        field("Associated identities") {
                            listview(listOf("elias.lousseief@blackwinged-angel.com").asObservable())
                        }
                        button("Add identity") {
                            hgrow = Priority.ALWAYS
                            maxWidth = Double.MAX_VALUE
                        }
                    }

                }
            }
            separator()
            hbox {
                button("Add new credential")
                region {
                    hgrow = Priority.ALWAYS
                    maxWidth = Double.MAX_VALUE
                }
                button("Close") {
                    action {
                        close()
                    }
                }
            }
        }
    }
}