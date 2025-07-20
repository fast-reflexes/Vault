package com.lousseief.vault.dialog

import com.lousseief.vault.model.CredentialProxy
import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import tornadofx.*

class AddUserNameDialog(evaluator: (String, ActionEvent) -> Unit, header: String, userNameList: ObservableList<String>, model: CredentialProxy): TextInputDialog() {

    val errorProperty = SimpleStringProperty("")
    val error by errorProperty;
    val userName = SimpleStringProperty("")

    class UserNameListCell(val model: CredentialProxy, val userNameList: ObservableList<String>) : ListCell<String?>() {

        override
        fun updateItem(item: String?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item == "" || item in model.identities.value) {
                if(item == "") {
                    if (userNameList.size == 1)
                        setText("No previous user names found")
                    else
                        setText("Select a user name");
                }
                else
                    setText("$item (already in use)")
                setDisable(true)
                setStyle("-fx-opacity: 0.4")
            } else {
                setText(item)
                setDisable(false)
                setStyle("-fx-opacity: 1.0")
            }
        }
    }

    inner class UserNameCallback(val model: CredentialProxy, val userNameList: ObservableList<String>) : Callback<ListView<String?>, ListCell<String?>> {

        override
        fun call(arg0: ListView<String?>): ListCell<String?> =
            UserNameListCell(model, userNameList)
    }

    init {
        val errorLabel = Label()
        errorLabel.removeWhen(errorProperty.isEmpty)
        errorLabel.hgrow = Priority.ALWAYS
        errorLabel.vgrow = Priority.NEVER
        errorLabel.maxHeight = Double.MAX_VALUE
        errorLabel.textProperty().bind(errorProperty)
        errorLabel.textAlignment = TextAlignment.LEFT
        errorLabel.alignment = Pos.CENTER_LEFT
        errorLabel.style = "-fx-text-fill: red";
        errorLabel.setWrapText(true)
        Platform.runLater{errorLabel.setMaxWidth(editor.width)}

        dialogPane.hgrow = Priority.NEVER
        dialogPane.vgrow = Priority.ALWAYS
        headerText = header

        val orLabel = Label("or")
        orLabel.maxWidth = Double.MAX_VALUE
        orLabel.hgrow = Priority.ALWAYS
        orLabel.alignment = Pos.CENTER
        orLabel.padding = Insets(10.0)

        val topLabel = Label("Enter a user name:")

        val bottomLabel = Label("Choose from previously used user names:")

        val selectBox = ComboBox(userNameList)
        selectBox.bindSelected(userName)
        selectBox.selectionModel.selectFirst()
        selectBox.disableWhen{ userNameList.sizeProperty.isEqualTo(1) }
        selectBox.cellFactory = UserNameCallback(model, userNameList)
        selectBox.buttonCell = UserNameListCell(model, userNameList)
        selectBox.maxWidth = Double.MAX_VALUE
        selectBox.hgrow = Priority.ALWAYS
        selectBox.placeholder = Label("Select a user name")

        val g = dialogPane.content as GridPane
        g.vgap = 5.0
        g.children.removeAt(0)
        println(g.children[0])
        (g.children[0] as TextField).bind(userName)

        g.children.addAll(topLabel, bottomLabel, errorLabel, orLabel, selectBox)

        GridPane.setConstraints(topLabel, 0, 0)
        GridPane.setConstraints(g.children[0], 0, 1)
        GridPane.setConstraints(errorLabel, 0, 2)
        GridPane.setConstraints(orLabel, 0, 3);
        GridPane.setConstraints(bottomLabel, 0, 4)
        GridPane.setConstraints(selectBox, 0, 5);


        //g.isGridLinesVisible = true

        val okBut = getDialogPane().lookupButton(ButtonType.OK) as Button
        okBut.addEventFilter(ActionEvent.ACTION) { event ->
            try {
                evaluator(editor.text, event)
            }
            catch(e: Exception) {
                event.consume()
                errorProperty.set(e.message)
                e.printStackTrace()
                dialogPane.scene.window.sizeToScene()
            }
        }
    }
}
