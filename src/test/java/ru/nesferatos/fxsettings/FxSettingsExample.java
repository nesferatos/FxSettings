package ru.nesferatos.fxsettings;

import com.thoughtworks.xstream.XStream;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nesferatos on 17.09.2015.
 */

enum ContactType {
    PRIVATE, PUBLIC
}

class ContactSetting implements TreeItemValueProvider{
    @Setting
    ContactType contactType;

    @Override
    public String getTreeItemValue() {
        return "Contact Type";
    }
}

class Contact {
    @Setting
    String key;

    @Setting
    String value;

    @Setting
    Object parent;

}

class PrivateContact extends Contact implements TreeItemValueProvider {

    @Setting
    String txtSecret;

    @Override
    public String getTreeItemValue() {
        return null;//name + " " + key;
    }

    @Setting
    String name;

}

class Person implements TreeItemValueProvider {
    @Setting
    String name;

    @Setting(factoryName = "contactFactory", isEditableField = false)
    Contact mainContact;

    @Setting(factoryName = "contactFactory", isEditableField = false)
    List<Contact> contacts = new ArrayList() {
        @Setting
        String comment;

        @Setting(factoryName = "contactFactory", isEditableField = false)
        List<Contact> innerContacts = new ArrayList<>();

        @Setting(factoryName = "contactFactory", isEditableField = false)
        List<Contact> innerContactsBlackList = new ArrayList<>();
    };

    @Override
    public String getTreeItemValue() {
        return name;
    }
}

class ContactFactory extends SettingsFactory <Contact, ContactSetting> {
    @Override
    public Contact createProduct(ContactSetting settingObj, PropertyTreeItem parent) throws ValidationException {
        Contact contact;
        if (settingObj.contactType == ContactType.PRIVATE) {
            contact = new PrivateContact();
        } else if (settingObj.contactType == ContactType.PUBLIC) {
            contact = new Contact();
        } else {
            throw new ValidationException("contact type cannot be empty!");
        }
        contact.parent = parent.getData();
        return contact;
    }

    @Override
    public ContactSetting getProductCreateParams(PropertyTreeItem parent) {
        return new ContactSetting();
    }
}

public class FxSettingsExample extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FactoryRegistry.getInstance().register("contactFactory", new ContactFactory());


        BorderPane root = new BorderPane();
        Person person = new Person();
        FxSettings fxSettings = new FxSettings(person);
        root.setCenter(fxSettings);


        MenuBar menuBar = new MenuBar();
        XStream xStream = new XStream();
        Menu menu = new Menu("File");
        MenuItem saveMenuItem = new MenuItem("Save");
        MenuItem loadMenuItem = new MenuItem("Load");

        saveMenuItem.setOnAction(event -> {
            try {
                xStream.toXML(fxSettings.getRoot(), new FileWriter("test.xml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        loadMenuItem.setOnAction(event -> {
            System.out.println("load");
            try {
                fxSettings.setRoot(xStream.fromXML(new FileReader("test.xml")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        menu.getItems().add(saveMenuItem);
        menu.getItems().add(loadMenuItem);


        menuBar.getMenus().add(menu);

        root.setTop(menuBar);

        primaryStage.setTitle("FxSettings Example");
        primaryStage.setScene(new Scene(root, 600, 300));
        primaryStage.show();
    }

    @Test
    public void run() {
        launch();
    }
}
