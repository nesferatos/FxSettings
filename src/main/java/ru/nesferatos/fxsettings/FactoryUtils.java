package ru.nesferatos.fxsettings;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import ru.nesferatos.fxsettings.*;


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
                propertyTreeItem.createCommand(product);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        }

    }
}
