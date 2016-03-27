package ru.nesferatos.fxsettings;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TreeItem;
import ru.nesferatos.fxsettings.*;

import java.lang.reflect.Field;
import java.util.List;


/**
 * Created by nesferatos on 19.09.2015.
 */
public class FactoryUtils {

    public static void createProductByTreeItem(PropertyTreeItem propertyTreeItem) {
        SettingsFactory factory = FactoryRegistry.getInstance().get(propertyTreeItem.getFactoryName());
        //Tree parent = ((PropertyTreeItem) propertyTreeItem.getParent()).getData();
        PropertyTreeItem parentTreeItem = (PropertyTreeItem) propertyTreeItem.getParent();
        Object productParamObj = factory.getProductCreateParams(parentTreeItem);


        if (productParamObj != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("add new item");
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(600, 600);

            alert.getDialogPane().setContent(new FxSettings(productParamObj));

            Button okButton = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);

            okButton.addEventFilter(ActionEvent.ACTION, event -> {
                try {
                    Object product = factory.createProduct(productParamObj, parentTreeItem);//TODO: maybe it can be more beautiful)
                    putToRegistry(propertyTreeItem.getRegistryName(), product);
                    propertyTreeItem.createCommand(product);
                } catch (ValidationException e) {
                    alert.setHeaderText(e.getMessage());
                    event.consume();
                    alert.setAlertType(Alert.AlertType.WARNING);
                }
            });
            alert.showAndWait();
        } else {
            try {
                Object product = factory.createProduct(null, parentTreeItem);
                putToRegistry(propertyTreeItem.getRegistryName(), product);
                propertyTreeItem.createCommand(product);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

    }

    private static void putToRegistry(String registry, Object obj) {
        if (!registry.equals("")) {
            SettingsRegistry.getInstance().put(registry, obj);
        }
    }

    public static void removeItem(PropertyTreeItem root, PropertyTreeItem item) {
        SettingsRegistry.getInstance().remove(((PropertyTreeItem)item.getParent()).getRegistryName(), item.getData());
        recursiveRemoveItem(root, item);
    }

    private static void recursiveRemoveItem(PropertyTreeItem root, PropertyTreeItem item) {
        Object o =  root.getData();

        List<Field> settings = PropertyUtils.getSettings(o);

        if (o instanceof List) {
            for (Object oi : (List) o) {
                if (oi instanceof List) {
                    ((List) oi).remove(item.getData());
                }
            }
        }

        for (Field setting : settings) {
            Object property = PropertyUtils.get(o, setting);
            if (property instanceof List) {
                ((List) property).remove(item.getData());
            }
            if (property == item.getData()) {
                PropertyUtils.set(o, setting, null);
            }
        }

        List<TreeItem> children = root.getChildren();
        for (TreeItem child : children) {
            recursiveRemoveItem((PropertyTreeItem) child, item);
        }
    }

}
