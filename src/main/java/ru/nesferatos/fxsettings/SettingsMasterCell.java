package ru.nesferatos.fxsettings;

import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;

/**
 * Created by nesferatos on 10.09.2015.
 */
public class SettingsMasterCell extends TreeCell {


    private void factoryContextMenuInit(String factoryName) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuItem = new MenuItem(factoryName);
        contextMenu.getItems().add(menuItem);
        menuItem.setOnAction(event -> FactoryUtils.createProductByTreeItem((PropertyTreeItem) getTreeItem()));
        setContextMenu(contextMenu);
    }

    private static String getNameFor(Object item, Object data) {
        return item.toString();
    }

    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
            setGraphic(null);
        } else {
            PropertyTreeItem treeItem = (PropertyTreeItem) getTreeItem();

            if (treeItem != null && treeItem.getGraphic() != null) {
                setText(getNameFor(item, treeItem.getData()));
                setGraphic(treeItem.getGraphic());
            } else {
                if (item instanceof Node) {
                    setText(null);
                    setGraphic((Node) item);
                } else {
                    setText(getNameFor(item, treeItem.getData()));
                    setGraphic(null);
                }
            }
            if (!treeItem.getFactoryName().equals("")) {
                factoryContextMenuInit(treeItem.getFactoryName());
            } else {
                setContextMenu(null);
            }
        }
    }

}
