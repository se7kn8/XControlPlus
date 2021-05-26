package com.github.se7_kn8.xcontrolplus.app.project

import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.app.dialog.ConfirmationDialog
import com.github.se7_kn8.xcontrolplus.app.dialog.TextInputDialog
import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import javafx.scene.control.Tab
import javafx.scene.layout.Pane

class SheetTab(project: Project, val sheet: Sheet) : Tab(sheet.name.get()) {

    init {
        with(sheet.gridHelper.gridView) {
            minScale = 0.1
            maxScale = 5.0
            // Only render when visible, to safe resources
            pauseProperty().bind(WindowContext.get().primaryStage.iconifiedProperty())
            textProperty().bind(sheet.name)
            content = /*VBox().apply {
                children.add(Button("Test"))
                children.add(*/Pane().also {
                // Set canvas size to largest possible size
                isFocusTraversable = true
                it.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE)
                widthProperty().bind(it.widthProperty())
                heightProperty().bind(it.heightProperty())
                it.children.add(this)
                //})
            }
        }
        setOnCloseRequest { closeEvent ->
            if (ConfirmationDialog("Delete this sheet?").showDialog()) {
                project.sheets.remove(sheet)
            } else {
                closeEvent.consume()
            }
        }

        contextMenu = ContextMenu(
            MenuItem("Rename").apply {
                setOnAction {
                    TextInputDialog("New name?", sheet.name.get()).showDialog()?.let {
                        sheet.name.set(it)
                    }
                }
            }
        )
    }
}
