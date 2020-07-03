package com.lousseief.vault.view

import com.lousseief.vault.controller.UserController
import com.lousseief.vault.dialog.PasswordConfirmDialog
import com.lousseief.vault.dialog.SingleInputDialog
import com.lousseief.vault.model.AssociationModel
import com.lousseief.vault.model.AssociationProxy
import com.lousseief.vault.model.Profile
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView
import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.binding.ListBinding
import javafx.beans.binding.ObjectBinding
import javafx.beans.binding.StringBinding
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableStringValue
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import javafx.util.Callback
import javafx.util.StringConverter
import tornadofx.*
import java.util.concurrent.Callable

class Converter: StringConverter<AssociationModel>() {

    override
    fun toString(am: AssociationModel): String {
        return am.mainIdentifier
    }

    override
    fun fromString(string: String): AssociationModel? {
        println("call fromString");
        return null;
    }
}

class MainView : View() {

    val user: Profile by param()
    val controller: UserController by inject()
    val model = AssociationProxy()

    var textField: TextField by singleAssign()

    var table: ListView<AssociationModel> by singleAssign()

    val placeHolderFn = object : Callable<String> {
        override fun call(): String {
            println("Evaluating placeholder!")
            if (controller.items.size == 0)
                return "No entries in table.\n Press \"Add new entry\" to create your first entry"
            else
                return "No entries were matched by your filter.\n Please try again!"
        }
    }
    val tablePlaceHolder = Bindings.createStringBinding(
        placeHolderFn,
        controller.items
    )
    val parameterQuery = SimpleStringProperty("All")
    val query = SimpleStringProperty("")
    val isNeededQuery = SimpleBooleanProperty(false)
    val shouldBeDeactivatedQuery = SimpleBooleanProperty(false)
    val isDeactivatedQuery = SimpleBooleanProperty(false)
    val useQuery = SimpleBooleanProperty(false)

    //val filterConditions: StringBinding
    //val filterFn: Callable<ObservableList<AssociationModel>>
    //val filtered: ObjectBinding<ObservableList<AssociationModel>>
    //val filtered: ListBinding<AssociationModel>

    val filtered = FilteredList<AssociationModel>(controller.items)
    val originalMainIdentifier = SimpleStringProperty(null)
    val identity = SimpleStringProperty()
    val current = SimpleObjectProperty(AssociationModel())
    val prop = SimpleStringProperty();
    //val entryView = find<EntryView>(mapOf(EntryView::hej to prop))

    override
    fun onDock() {
        super.onDock()
        print("Docking!")
        //primaryStage.minHeight = 600.0
        //primaryStage.minWidth = 700.0
        currentWindow?.centerOnScreen()
        //currentStage!!.width = 500.0
        //currentWindow!!.width = 500.0
        //primaryStage.sizeToScene()
        //currentWindow!!.sizeToScene()
        //currentStage!!.sizeToScene()
        //primaryStage!!.minWidth = 50.0
        //primaryStage!!.maxWidth = 50.0
        //primaryStage.height = 768.0
        //primaryStage.sizeToScene()
        //currentStage!!.sizeToScene()
        //primaryStage.sizeToScene()
        //
    }

    class CL : ListCell<AssociationModel>() {

        override
        fun updateItem(item: AssociationModel?, empty: Boolean) {
            super.updateItem(item, empty)
            if (item === null) {
                setText("Select entry to view or edit");
                setDisable(true)
                setStyle("-fx-opacity: 0.4")
            } else {
                setText(if (item.mainIdentifier === "") "(unnamed entry)" else item.mainIdentifier)
                setDisable(false)
                setStyle("-fx-opacity: 1.0")
            }
        }
    }

    class CB(val lv: ListView<AssociationModel>) : Callback<ListView<AssociationModel>, ListCell<AssociationModel>> {

        override
        fun call(arg0: ListView<AssociationModel>): ListCell<AssociationModel> {
            val lc = object : ListCell<AssociationModel>() {

                override
                fun updateItem(item: AssociationModel?, empty: Boolean) {
                    super.updateItem(item, empty)
                    if (empty) {
                        setText(null)
                    } else {
                        if(item !== null) {
                            setText(if (item.mainIdentifier === "") "(unnamed entry)" else item.mainIdentifier)
                            setDisable(false)
                            setStyle("-fx-opacity: 1.0")
                        }
                    }
                }
            }
            lc.prefWidthProperty().bind(lv.widthProperty());
            return lc
        }
    }
    /*val tb = tableview(persons) {
                column("Name", Person::nameProperty)
                column("Title", Person::titleProperty)

                // Update the person inside the view model on selection change
                model.rebindOnChange(this) { selectedPerson ->
                    item = selectedPerson ?: Person()
                }
    }*/

    override
    val root = VBox()

    fun filterList() {
        if (useQuery.value) {
            filtered.setPredicate { it !== null && it.mainIdentifier.contains(query.value) }
        } else
            filtered.setPredicate { println("In fn: " + it); it !== null }
    }

    init {
        controller.items.addListener(
            object: ListChangeListener<AssociationModel> {
                override fun onChanged(change: ListChangeListener.Change<out AssociationModel>) {
                    println("list changed!)")
                    filterList()
                }
            }
        )
        useQuery.addListener{ _ -> filterList() }
        query.addListener{ _ -> filterList() }
        parameterQuery.addListener{ _ -> filterList() }
        isNeededQuery.addListener{ _ -> filterList() }
        shouldBeDeactivatedQuery.addListener{ _ -> filterList() }
        isDeactivatedQuery.addListener{ _ -> filterList() }
        /*filterFn = object : Callable<ObservableList<AssociationModel>> {
            override fun call(): ObservableList<AssociationModel> {
                (
                        synchronized(controller.items) {
                            println("Inbinding")
                            if (useQuery.value) {
                                return controller.items.filtered {
                                    println(it)
                                    it !== null &&
                                            it.mainIdentifier.contains(query.value)
                                }
                            } else
                                return controller.items.filtered { println("In fn: " + it); it !== null }
                        }
                )
            }
        }*/
        /*filterConditions = Bindings.createStringBinding(
            object: Callable<String>{ override fun call() = query.value + parameterQuery.value + useQuery.value + isNeededQuery.value + shouldBeDeactivatedQuery.value + isDeactivatedQuery.value },
            useQuery, query, parameterQuery, isNeededQuery, shouldBeDeactivatedQuery, isDeactivatedQuery
        )
        filterConditions.addListener{ obs, old, new -> filterList() }*/
        /*filtered = Bindings.createObjectBinding(
            filterFn,
            useQuery, query, parameterQuery, isNeededQuery, shouldBeDeactivatedQuery, isDeactivatedQuery, controller.items
        )*/
        /*filtered = object: ListBinding<AssociationModel>() {

            init {
                bind(useQuery, query, parameterQuery, isNeededQuery, shouldBeDeactivatedQuery, isDeactivatedQuery, controller.items)
            }

            override fun computeValue(): ObservableList<AssociationModel> {
                println("Inbinding")
                if (useQuery.value) {
                    return controller.items.filtered {
                        println(it)
                        it !== null &&
                                it.mainIdentifier.contains(query.value)
                    }
                } else
                    return controller.items.filtered { println("In jfn: " + it); it !== null }

            }
        }*/

        /*filtered = Bindings.createObjectBinding(
            filterFn,
            useQuery, query, parameterQuery, isNeededQuery, shouldBeDeactivatedQuery, isDeactivatedQuery, controller.items
        )*/
        model.itemProperty.onChange {
            if(it === null) originalMainIdentifier.unbind()
            else originalMainIdentifier.bind(it.mainIdentifierProperty)
        }
        controller.user = user
        println(controller.items)

        /*val selectBox =
            combobox<AssociationModel> {
                items = controller.items
                bindSelected(current)
                cellFactory = CB()
                buttonCell = CL()
                //cellFormat { text = if(it.mainIdentifier === "") "Unnamed"  else it.mainIdentifier }
                minWidth = 200.0
                isDisable = (items.size == 1)
            }*/
        //selectBox.setConverter(Converter());
        //selectBox.valueProperty().addListener {obs, oldValue, newValue -> System.out.println("Change from " + oldValue?.mainIdentifier + " to " + newValue?.mainIdentifier)}
        //selectBox.selectionModelProperty().bindBidirectional(current)
        with(root) {
            this += hbox {
                padding = Insets(10.0)
                label("Offline vault - securing your passwords") {
                    font = Font(20.0)
                }
                region {
                    hgrow = Priority.ALWAYS
                }
                hyperlink("Logout", FontAwesomeIconView(FontAwesomeIcon.HOME)) {
                    action {
                        //userController.user = null
                        if(controller.altered.value == true)
                            alert(
                                type = Alert.AlertType.CONFIRMATION,
                                header = "Do you really want to log out",
                                content = "You have data successfully added to your vault that was not saved to disk, do you really want to log out without saving it? If no, press \"Cancel\" and then press \"Save vault to disk\".",
                                actionFn = { type ->
                                                if(type === ButtonType.OK)
                                                    replaceWith<LoginView>(
                                                        sizeToScene = true,
                                                        centerOnScreen = true
                                                    )
                                }
                            )
                        else
                            replaceWith<LoginView>(
                                sizeToScene = true,
                                centerOnScreen = true
                            )
                    }
                }
            }
            this += tabpane {
                tab("Associations") {
                    vgrow = Priority.ALWAYS
                    maxHeight = Double.MAX_VALUE
                    tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
                    vbox {
                        vgrow = Priority.ALWAYS
                        maxHeight = Double.MAX_VALUE
                        hbox {
                            maxHeight = Double.MAX_VALUE
                            maxWidth = Double.MAX_VALUE
                            hgrow = Priority.ALWAYS
                            vgrow = Priority.ALWAYS
                            vbox {
                                vgrow = Priority.ALWAYS
                                hgrow = Priority.ALWAYS
                                maxHeight = Double.MAX_VALUE
                                padding = Insets(10.0)
                                spacing = 10.0
                                prefWidth = 300.0
                                minWidth = 300.0
                                maxWidth = Double.MAX_VALUE
                                alignment = Pos.CENTER
                                titledpane() {
                                    val tp = this
                                    spacing = 5.0
                                    maxWidth = Double.MAX_VALUE
                                    hgrow = Priority.NEVER
                                    val hb = HBox().apply {
                                        hgrow = Priority.NEVER
                                        add(Label("Filter"))
                                        add(Region().apply {
                                            hgrow = Priority.ALWAYS
                                            maxWidth = Double.MAX_VALUE
                                        })
                                        add(CheckBox().apply {
                                            style { backgroundColor += Color.WHITE }
                                            bind(useQuery)
                                        })
                                        prefWidthProperty().bind(tp.widthProperty().subtract(38))
                                        maxWidthProperty().bind(tp.widthProperty().subtract(38))
                                    }
                                    this.setGraphic(hb)
                                    isCollapsible = true
                                    isExpanded = false
                                    form {
                                        padding = Insets(5.0, 8.0, 5.0, 8.0)
                                        vbox {
                                            fieldset {
                                                labelPosition = Orientation.VERTICAL
                                                padding = Insets(0.0)
                                                field("Keyword") {
                                                    paddingBottom = 5.0
                                                    textfield() {
                                                        bind(query)
                                                    }
                                                }
                                                field("Parameter") {
                                                    combobox<String> {
                                                        items = listOf("All", "Main identifier", "Secondary identifier", "Category", "Comment").toObservable()
                                                        bindSelected(parameterQuery)
                                                        selectionModel.selectFirst()
                                                        hgrow = Priority.ALWAYS
                                                        maxWidth = Double.MAX_VALUE
                                                    }
                                                }
                                            }
                                            fieldset {
                                                padding = Insets(10.0, 0.0, 5.0, 0.0)
                                                labelPosition = Orientation.HORIZONTAL
                                                vbox {
                                                    padding = Insets(0.0)
                                                    spacing = 5.0
                                                    checkbox("Is needed?") { bind(isNeededQuery) }
                                                    checkbox("Should be deactivated?") { bind(shouldBeDeactivatedQuery) }
                                                    checkbox("Is deactivated?") { bind(isDeactivatedQuery) }
                                                }
                                            }
                                        }
                                    }
                                }
                                titledpane("Vault entries") {
                                    isCollapsible = false
                                    maxWidth = Double.MAX_VALUE
                                    hgrow = Priority.ALWAYS
                                    vgrow = Priority.ALWAYS
                                    maxHeight = Double.MAX_VALUE
                                    val ti = this
                                    listview(filtered as ObservableList<AssociationModel>) {
                                    //listview(controller.items) {
                                        vgrow = Priority.ALWAYS
                                        hgrow = Priority.ALWAYS
                                        textOverrun = OverrunStyle.ELLIPSIS
                                        maxWidth = ti.width
                                        maxHeight = Double.MAX_VALUE
                                        maxWidth = Double.MAX_VALUE
                                        cellFactory = CB(this)
                                        bindSelected(model)
                                        selectionModel.selectFirst()
                                        minHeight = 200.0
                                        table = this
                                        Platform.runLater{ this.requestFocus() }
                                        //columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY
                                        /*column("Vault entries", AssociationModel::mainIdentifierProperty) {
                                            maxWidth = Double.MAX_VALUE
                                            hgrow = Priority.ALWAYS
                                            prefWidth = 400.0
                                            style {
                                                borderColor += box(all = Color.LIGHTGRAY)
                                                borderWidth += box(all = 1.px)
                                                borderStyle += BorderStrokeStyle.SOLID
                                            }
                                        }*/
                                        placeholder = label {
                                            bind(tablePlaceHolder)
                                            textAlignment = TextAlignment.CENTER
                                            isWrapText = true
                                        }
                                        // Update the person inside the view model on selection change
                                        /*model.rebindOnChange(this) { ass ->
                                            item = ass ?: AssociationModel()
                                            if(ass !== null)
                                                name.bind(ass.mainIdentifierProperty)
                                            else
                                                name.unbind()
                                        }*/
                                    }
                                }
                                button("Create new entry") {
                                    vgrow = Priority.NEVER
                                    hgrow = Priority.ALWAYS
                                    maxWidth = Double.MAX_VALUE
                                    action {
                                        val newEntry = SingleInputDialog({
                                                input: String, ev: ActionEvent ->
                                            controller.validateNewEntry(input)
                                            val password = PasswordConfirmDialog({
                                                    password: String, _: ActionEvent ->
                                                controller.addEntry(input, password)
                                            }).showAndWait()
                                            if(password.isEmpty)
                                                ev.consume()
                                        }, "Enter an identifier for your new entry").showAndWait()
                                        if(newEntry.isPresent) {
                                            table.selectionModel.selectLast()
                                            table.requestFocus()
                                        }
                                        /*object: TextInputDialog() {
                                            init {
                                                val errorLabel =
                                                    label {
                                                        hgrow = Priority.ALWAYS
                                                        vgrow = Priority.NEVER
                                                        maxHeight = Double.MAX_VALUE
                                                        minHeight = 0.0
                                                        maxWidth = Double.MAX_VALUE
                                                        textProperty().bind(errorProperty)
                                                        textAlignment = TextAlignment.LEFT
                                                        alignment = Pos.CENTER
                                                        style {
                                                            textFill = Color.RED
                                                            backgroundColor += Color.BLUE
                                                        }
                                                    }

                                            }
                                        }.show()*/
                                        /*find<ConfirmNewEntryView>().openModal(
                                            resizable = false,
                                            block = true
                                        )*/
                                        //controller.add();
                                        //selectBox.isDisable = false
                                    }
                                    // Save button is disabled until every field has a value
                                    //disableProperty().bind(SimpleBooleanProperty(user.associations.size == 0).asObject());
                                }
                            }
                            vbox {
                                style {
                                    backgroundColor += Color.gray(0.7)
                                }
                                minWidth = 400.0
                                minHeight = 400.0
                                hgrow = Priority.ALWAYS
                                vgrow = Priority.ALWAYS
                                maxHeight = Double.MAX_VALUE
                                padding = Insets(10.0)
                                this += find<EntryView>(mapOf(EntryView::model to model, EntryView::originalMainIdentifier to originalMainIdentifier)).root
                            }
                        }
                        separator()
                        hbox {
                            vgrow = Priority.NEVER
                            padding = Insets(10.0)
                            spacing = 10.0
                            alignment = Pos.CENTER_RIGHT
                            button("Save vault to disk") {
                                enableWhen(controller.altered.eq(true))
                                action {
                                    controller.save()
                                    controller.altered.set(false)
                                }
                            }
                        }
                    }
                }
                tab("Settings") {
                }
            }
            /*top = borderpane  {
                    left = selectBox
                    right =
                        hbox {
                            button("Create new entry") {
                                action {
                                    controller.add();
                                    selectBox.isDisable = false
                                }
                                // Save button is disabled until every field has a value
                                //disableProperty().bind(SimpleBooleanProperty(user.associations.size == 0).asObject());
                            }
                            hyperlink("Logout", FontAwesomeIconView(FontAwesomeIcon.HOME)) {
                                action {
                                    //userController.user = null
                                    replaceWith<LoginView>()
                                }
                            }
                        }
                }
                println(prop.isEmpty)
                println(prop.isNull)
                println(prop.isNotEmpty)
                println(prop.value)*/

            /*println(root.center)
        current.onChange {
            if(it === null) {
                root.center = null
                println("WAS NULL!");
            }
            else {
                println("" + "This is it: " + it)
                println("" + "This is it: " + current)
                println(it.mainIdentifier)
                model.item = it
                root.center = find<EntryView>(mapOf(EntryView::model to model)).root
            }
            // val net = find<EntryView>(mapOf(EntryView::hej to prop))
            //find<NullView>().replaceWith(entryView)
            //find<EntryView>(mapOf(EntryView::hej to it)).openModal()
            //openInternalWindow(net);
        }*/
            //selectBox.selectionModel.select(null)
        }
    }
}