package ru.nesferatos.fxsettings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by nesferatos on 08.08.2015.
 */

public class PropertyTreeItem extends TreeItem {

    public static Image addToListIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/add.png"));
    public static Image blankIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/blank.png"));
    public static Image recreatableIcon = new Image(PropertyTreeItem.class.getResourceAsStream("/recreatable.png"));

    public void setFirstTimeChildren(boolean firstTimeChildren) {
        this.firstTimeChildren = firstTimeChildren;
    }

    private boolean firstTimeChildren = true;
    private ObjectProperty dataProperty = new SimpleObjectProperty<>();
    private Field field;

    public String getFactoryName() {
        return factoryName;
    }

    private String factoryName = "";

    public PropertyTreeItem(Object data, Field field) {
        //super(field.getAnnotation(Setting.class).name().equals("") ? field.getName() : field.getAnnotation(Setting.class).name());
        super(PropertyUtils.getNameFor(data, field));
        dataProperty.set(data);
        this.field = field;
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

    /*public Object getObjectParent() {
        if (getData() instanceof List) {
            if (getParent() != null) {
                PropertyTreeItem par = (PropertyTreeItem)getParent();
                return par.getData();
            } else {
                return null;
            }
        } else {
            return getData();
        }
    }*/

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

        //update selection
        /*int selected = getTreeView().getSelectionModel().getSelectedIndex();
        getTreeView().getSelectionModel().clearSelection();
        getTreeView().getSelectionModel().select(selected);*/

    }

    @Override
    public ObservableList<TreeItem> getChildren() {
        if (firstTimeChildren) {
            firstTimeChildren = false;

            Object value = getData();

            if (value != null) {
                if (value instanceof List) {
                    for (Object i : ((List) value)) {
                        super.getChildren().add(new PropertyTreeItem(i, null));
                    }
                }
                List<Field> fields = PropertyUtils.getSettingNodes(value);
                for (Field field : fields) {
                    try {
                        Object obj = field.get(value);

                        Setting settingsAnnotation = field.getAnnotation(Setting.class);

                        PropertyTreeItem treeItem = new PropertyTreeItem(obj, field);
                        treeItem.factoryName = settingsAnnotation.factoryName();
                        if (!treeItem.factoryName.equals("")) {
                            ImageView imageView = new ImageView(getIconFor(obj));

                            treeItem.setGraphic(imageView);
                        }
                        super.getChildren().add(treeItem);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
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
}
