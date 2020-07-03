package com.lousseief.vault.dialog

import javafx.application.Platform
import javafx.beans.property.SimpleStringProperty
import javafx.event.ActionEvent
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import tornadofx.*

class SingleInputDialog(evaluator: (String, ActionEvent) -> Unit, header: String): TextInputDialog() {

    val errorProperty = SimpleStringProperty("")
    val error by errorProperty;

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

        val g = dialogPane.content as GridPane
        GridPane.setConstraints(errorLabel, 0, 1);
        g.vgap = 5.0
        g.getChildren().removeAt(0)
        println(g.getChildren()[0])
        val p = PasswordField()
        p.styleClass.add("text-input")
        p.styleClass.add("text-field")
        //g.getChildren()[0] = p
        //g.isGridLinesVisible = true
        GridPane.setConstraints(g.getChildren().get(0), 0, 0)
        g.getChildren().add(errorLabel)

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
