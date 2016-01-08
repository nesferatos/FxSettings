package ru.nesferatos.fxsettings;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nesferatos on 21.09.2015.
 */
public class FxSettingsSimpleExample extends Application {


    class Human implements TreeItemValueProvider{
        @Setting
        String name;

        @Setting(factoryName = "childFactory")
        List<Human> children = new ArrayList<>();

        @Setting(registryName = "people")
        Human bestFriend;

        @Setting(registryName = "people")
        List friends = new ArrayList<>();

        @Override
        public String getTreeItemValue() {
            return name;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FactoryRegistry.getInstance().register("childFactory", new SettingsFactory<Human, Object>() {
            @Override
            public Human createProduct(Object settingObj, PropertyTreeItem parent) throws ValidationException {
                Human human = new Human();
                SettingsRegistry.getInstance().put("people", human);
                return human;
            }

            @Override
            public Object getProductCreateParams(PropertyTreeItem parent) {
                return null;
            }
        });


        Human adam = new Human();

        adam.name = "adam";

        FxSettings fxSettings = new FxSettings(adam);

        primaryStage.setTitle("FxSettings Simple Example");
        primaryStage.setScene(new Scene(fxSettings, 600, 300));
        primaryStage.show();

    }

    @Test
    public void run() {
        launch();
    }
}
