package ru.nesferatos.fxsettings;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;

import java.util.List;

/**
 * Created by nesferatos on 10.09.2015.
 */
class SettingsMasterCell extends TreeCell {


    private void factoryContextMenuInit(String factoryName) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem menuItem = new MenuItem(factoryName);
        menuItem.setOnAction(event -> {
            FactoryUtils.createProductByTreeItem((PropertyTreeItem) getTreeItem());
        });
        contextMenu.getItems().add(menuItem);

        setContextMenu(contextMenu);
    }


    private void factoryChildContextMenuInit(PropertyTreeItem parent, PropertyTreeItem i) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem menuItem = new MenuItem("remove");
        menuItem.setOnAction(event -> {
            if (!parent.getRegistryName().equals("")) {
                FactoryUtils.removeItem((PropertyTreeItem) getTreeView().getRoot(), i);
            }
            if (parent.getData() instanceof List) {
                ((List)parent.getData()).remove(i.getData());
                parent.rebuildChildren();
            }
        });
        contextMenu.getItems().add(menuItem);

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
            PropertyTreeItem parentTreeItem = (PropertyTreeItem) treeItem.getParent();
            if (!treeItem.getFactoryName().equals("")) {
                factoryContextMenuInit(treeItem.getFactoryName());
            } else if (parentTreeItem != null && !parentTreeItem.getFactoryName().equals("")) {
                factoryChildContextMenuInit(parentTreeItem, treeItem);
            } else {
                setContextMenu(null);
            }
        }
    }

}
