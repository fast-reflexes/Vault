package com.lousseief.vault

import javafx.application.Application
import javafx.scene.paint.Color
import javafx.stage.Stage
import tornadofx.*
import com.lousseief.vault.view.LoginView

class VaultApp : App(LoginView::class) {

    override fun start(stage: Stage) {
        //stage.isResizable = false
        //stage.sizeToScene()
        super.start(stage)
    }

    override
    fun createPrimaryScene(view: UIComponent) =
        super.createPrimaryScene(view).apply{ fill = Color.valueOf("#EDEDED") }
}

fun main(args: Array<String>) {
    launch<VaultApp>(*args)
}