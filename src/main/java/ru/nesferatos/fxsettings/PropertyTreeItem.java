package ru.nesferatos.fxsettings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nesferatos on 08.08.2015.
 */

interface PropertyTreeItemFunction {
    boolean process(PropertyTreeItem item);
}

class PropertyTreeItem extends TreeItem {

    public static Image addToListIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/add.png"));
    public static Image blankIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/blank.png"));
    public static Image recreatableIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/recreatable.png"));

    private boolean firstTimeChildren = true;
    private ObjectProperty dataProperty = new SimpleObjectProperty<>();
    private Field field;
    private String factoryName = "";
    private String registryName = "";

    public PropertyTreeItem(Object data, Field field) {
        super(PropertyUtils.getNameFor(data, field));
        dataProperty.set(data);
        this.field = field;
    }

    public static Image getIconFor(Object object) {
        if (object instanceof List) {
            return addToListIcon;
        } else {
            if (object == null) {
                return blankIcon;
            } else {
                return recreatableIcon;
            }
        }
    }

    public String getFactoryName() {
        return factoryName;
    }

    public Object getData() {
        return dataProperty.get();
    }

    public void setData(Object data) {
        dataProperty.set(data);
    }

    public ObjectProperty getDataProperty() {
        return dataProperty;
    }

    public void createCommand(Object obj) {

        if (getData() instanceof List) {
            ((List) getData()).add(obj);
            PropertyTreeItem item = new PropertyTreeItem(obj, null);
            getChildren().add(item);
        } else {
            setData(obj);
            PropertyTreeItem item = new PropertyTreeItem(obj, getField());
            try {
                getField().set(((PropertyTreeItem) getParent()).getData(), obj);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            getChildren().setAll(item.getChildren());
        }
        setGraphic(new ImageView(PropertyTreeItem.getIconFor(getData())));

        //getting root
        PropertyTreeItem parent = this;
        while (parent.getParent() != null) {
            parent = (PropertyTreeItem) parent.getParent();
        }

        iterateAllChildTreeItems(parent, it -> {
            if (it.getData() == getData()) {
                it.rebuildChildren();
                return true;
            }
            return false;
        });
    }

    private void iterateAllChildTreeItems(PropertyTreeItem parent, PropertyTreeItemFunction function) {
        for (TreeItem item : parent.getChildren()) {
            if (!function.process((PropertyTreeItem) item)) {
                iterateAllChildTreeItems((PropertyTreeItem) item, function);
            }
        }
    }

    public void rebuildChildren() {

        List newChildren = new ArrayList<>();

        Object value = getData();

        if (value != null) {
            if (value instanceof List) {
                for (Object i : ((List) value)) {
                    newChildren.add(new PropertyTreeItem(i, null));//TODO: make remove action
                }
            }
            List<Field> fields = PropertyUtils.getSettingNodes(value);
            for (Field field : fields) {
                try {
                    Object obj = field.get(value);

                    Setting settingsAnnotation = field.getAnnotation(Setting.class);

                    PropertyTreeItem treeItem = new PropertyTreeItem(obj, field);
                    treeItem.factoryName = settingsAnnotation.factoryName();
                    treeItem.registryName = settingsAnnotation.registryName();
                    if (!treeItem.factoryName.equals("")) {
                        ImageView imageView = new ImageView(getIconFor(obj));

                        treeItem.setGraphic(imageView);
                    }
                    newChildren.add(treeItem);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        getChildren().setAll(newChildren);

    }

    @Override
    public ObservableList<TreeItem> getChildren() {
        if (firstTimeChildren) {
            firstTimeChildren = false;
            rebuildChildren();
        }
        return super.getChildren();
    }

    @Override
    public boolean isLeaf() {
        return getChildren().isEmpty();
    }

    public Field getField() {
        return field;
    }

    public String getRegistryName() {
        return registryName;
    }

    public void setRegistryName(String registryName) {
        this.registryName = registryName;
    }
}
