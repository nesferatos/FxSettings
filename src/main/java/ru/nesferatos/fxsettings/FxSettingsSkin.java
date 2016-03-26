package ru.nesferatos.fxsettings;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.KeyBinding;
import com.sun.javafx.scene.control.skin.BehaviorSkinBase;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.controlsfx.control.*;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.PropertyEditor;

import java.lang.reflect.Field;
import java.util.*;
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
            return new FxSettingsPropertyItem(field, fxSettings.getRoot(), item);
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
            list.addAll(propertiesList.stream().map(field -> new FxSettingsPropertyItem(field, data, propertyTreeItem)).collect(Collectors.toList()));

            PropertySheet propertySheet = new PropertySheet(list);


            final Callback<PropertySheet.Item, PropertyEditor<?>> stdPropertyEditorFactory = propertySheet.getPropertyEditorFactory();

            propertySheet.setPropertyEditorFactory(new Callback<PropertySheet.Item, PropertyEditor<?>>() {
                @Override
                public PropertyEditor<?> call(PropertySheet.Item param) {
                    FxSettingsPropertyItem item = (FxSettingsPropertyItem) param;
                    String registryName = ((FxSettingsPropertyItem) param).getSettingAnnotation().registryName();
                    if (!(registryName.isEmpty())) {
                        if (item.getType() == List.class) { //TODO: should be replaced by something like instanceof List
                            return createChoicesEditor(item, SettingsRegistry.getInstance().get(registryName));
                        }
                        return createChoiceEditor(item, SettingsRegistry.getInstance().get(registryName));
                    }
                    return stdPropertyEditorFactory.call(param);
                }
            });

            borderPane.setCenter(propertySheet);

        } else {
            Label label = new Label("null");
            borderPane.setCenter(label);
        }

        return borderPane;
    }

    public static final <T> PropertyEditor<?> createChoiceEditor(PropertySheet.Item property, final Collection<T> choices) {

        return new AbstractPropertyEditor<T, ComboBox<T>>(property, new ComboBox<T>()) {

            {
                getEditor().setConverter(new StringConverter<T>() {
                    @Override
                    public String toString(T object) {
                        return PropertyUtils.getNameFor(object, null);
                    }

                    @Override
                    public T fromString(String string) {
                        return null;
                    }
                });
                getEditor().setItems(FXCollections.observableArrayList(choices));
            }

            @Override
            protected ObservableValue<T> getObservableValue() {
                return getEditor().getSelectionModel().selectedItemProperty();
            }

            @Override
            public void setValue(T value) {
                getEditor().getSelectionModel().select(value);
            }
        };
    }

    public static final <T> PropertyEditor<?> createChoicesEditor(PropertySheet.Item property, final Collection<T> choices) {

        return new AbstractPropertyEditor<T, CheckComboBox<T>>(property, new CheckComboBox<T>()) {
            {
                getEditor().setConverter(new StringConverter<T>() {
                    @Override
                    public String toString(T object) {
                        return PropertyUtils.getNameFor(object, null);
                    }

                    @Override
                    public T fromString(String string) {
                        return null;
                    }
                });
                getEditor().getItems().addAll((FXCollections.observableArrayList(choices)));
                for (T o : (List<T>) property.getValue()) {
                    getEditor().getCheckModel().check(o);
                }

                getEditor().getCheckModel().getCheckedIndices().addListener(new ListChangeListener<Integer>() {
                    @Override
                    public void onChanged(Change<? extends Integer> c) {
                        ((Collection) property.getValue()).clear();
                        ((Collection) property.getValue()).addAll(getEditor().getCheckModel().getCheckedItems());
                    }
                });
            }

            @Override
            protected ObservableValue<T> getObservableValue() {
                ObservableValue<T> v = new SimpleObjectProperty<>((T) getEditor().getCheckModel());
                return v;
            }

            @Override
            public void setValue(T value) {
            }
        };
    }

}
