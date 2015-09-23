package ru.nesferatos.fxsettings;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.PropertyEditor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by nesferatos on 16.09.2015.
 */
public class FxSettingsSkin extends BehaviorSkinBase<FxSettings, BehaviorBase<FxSettings>> {

    TreeView treeView;
    SplitPane splitPane;

    public FxSettingsSkin(FxSettings fxSettings) {
        super(fxSettings, new BehaviorBase<>(fxSettings, Collections.<KeyBinding>emptyList()));

        splitPane = new SplitPane();
        splitPane.getItems().addAll(new Pane(), new Pane());
        splitPane.setOrientation(Orientation.HORIZONTAL);

        treeView = new TreeView();

        PropertyTreeItem item = new PropertyTreeItem(fxSettings.getRoot(), null);

        treeView.setRoot(item);

        fxSettings.getRootProperty().addListener((observable, oldValue, newValue) -> {
            PropertyTreeItem item1 = new PropertyTreeItem(fxSettings.getRoot(), null);
            treeView.setRoot(item1);
        });

        treeView.setCellFactory(param -> new SettingsMasterCell());

        treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                PropertyTreeItem propertyTreeItem = (PropertyTreeItem) newValue;
                setDetail(createDetailView(propertyTreeItem));
                propertyTreeItem.getDataProperty().addListener(observable1 -> {
                    setDetail(createDetailView(propertyTreeItem));
                });
            } else {
                System.out.println("changed null");//TODO: add logging
            }
        });

        setMaster(treeView);

        List<Field> propertiesList = PropertyUtils.getSettings(fxSettings.getRoot());
        ObservableList list = FXCollections.observableList(new ArrayList<>());
        list.addAll(propertiesList.stream().map(field -> {
            return new CustomPropertyItem(field, fxSettings.getRoot(), item);
        }).collect(Collectors.toList()));

        getChildren().add(splitPane);
    }

    private void setMaster(Node node) {
        splitPane.getItems().set(0, node);
    }

    private void setDetail(Node node) {
        splitPane.getItems().set(1, node);
    }

    private Node createDetailView(PropertyTreeItem propertyTreeItem) {
        Object data = propertyTreeItem.getData();
        BorderPane borderPane = new BorderPane();
        String factoryName = propertyTreeItem.getFactoryName();

        if (!factoryName.isEmpty()) {
            ToolBar toolBar = new ToolBar();
            Button addButton = new Button();

            addButton.setOnAction(event -> FactoryUtils.createProductByTreeItem(propertyTreeItem));

            if (data instanceof List) {
                addButton.setText("add to list");
            } else if (data == null) {
                addButton.setText("create");
            } else {
                addButton.setText("recreate");
            }

            toolBar.getItems().add(addButton);

            borderPane.setTop(toolBar);
        }

        if (data != null) {
            List<Field> propertiesList = PropertyUtils.getSettings(data);

            ObservableList list = FXCollections.observableList(new ArrayList<>());
            list.addAll(propertiesList.stream().map(field -> new CustomPropertyItem(field, data, propertyTreeItem)).collect(Collectors.toList()));

            PropertySheet propertySheet = new PropertySheet(list);

            borderPane.setCenter(propertySheet);

        } else {
            Label label = new Label("null");
            borderPane.setCenter(label);
        }

        return borderPane;
    }

    class CustomPropertyItem implements PropertySheet.Item {

        Class type;
        Object container;
        String name, category, description;
        Field field;
        PropertyTreeItem propertyTreeItem;

        public CustomPropertyItem(Field field, Object container, PropertyTreeItem propertyTreeItem) {
            this.field = field;
            this.container = container;
            this.type = field.getType();
            Setting setting = field.getAnnotation(Setting.class);
            this.category = setting.category();
            this.description = setting.desc();
            this.name = (setting.name().isEmpty()) ? field.getName() : setting.name();
            this.propertyTreeItem = propertyTreeItem;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Object getValue() {
            return PropertyUtils.get(container, field);
        }

        @Override
        public void setValue(Object value) {
            PropertyUtils.set(container, field, value);
            propertyTreeItem.setValue(PropertyUtils.getNameFor(container, propertyTreeItem.getField()));
        }

        @Override
        public Optional<Class<? extends PropertyEditor<?>>> getPropertyEditorClass() {
            return Optional.empty();
        }
    }
}
