package ru.nesferatos.fxsettings;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nesferatos on 24.09.2015.
 */

class Container {
    @Setting
    Human human = new Human("adam");
    @Setting
    Human humanCopy = human;
}

class Human implements TreeItemValueProvider{
    public Human() {

    }

    public Human(String name) {
        this.name = name;
    }

    @Setting
    String name;

    @Setting(factoryName = "childFactory")
    List<Human> children = new ArrayList<>();

    @Setting(factoryName = "childFactory")
    List<Human> childrenCopy = children;

    @Override
    public String getTreeItemValue() {
        return "children " + children + " childrencopy " + childrenCopy;
    }
}

public class FxSettingsSyncExample extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FactoryRegistry.getInstance().register("childFactory", new SettingsFactory<Human, Object>() {
            @Override
            public Human createProduct(Object settingObj, PropertyTreeItem parent) throws ValidationException {
                return new Human();
            }

            @Override
            public Object getProductCreateParams(PropertyTreeItem parent) {
                return null;
            }
        });


        Container d = new Container();

        FxSettings fxSettings = new FxSettings(d);

        primaryStage.setTitle("FxSettings Simple Example");
        primaryStage.setScene(new Scene(fxSettings, 600, 300));
        primaryStage.show();

    }

    @Test
    public void run() {
        launch();
    }

}
