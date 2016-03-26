package ru.nesferatos.fxsettings;

import org.controlsfx.control.PropertySheet;

import java.lang.reflect.Field;

/**
 * Created by nesferatos on 26.03.2016.
 */
class FxSettingsPropertyItem implements PropertySheet.Item {

    private Class type;
    private Object container;
    private String name, category, description;
    private Field field;
    private PropertyTreeItem propertyTreeItem;

    public Setting getSettingAnnotation() {
        return settingAnnotation;
    }

    Setting settingAnnotation;

    public FxSettingsPropertyItem(Field field, Object container, PropertyTreeItem propertyTreeItem) {
        this.field = field;
        this.container = container;
        this.type = field.getType();
        settingAnnotation = field.getAnnotation(Setting.class);
        this.category = settingAnnotation.category();
        this.description = settingAnnotation.desc();
        this.name = (settingAnnotation.name().isEmpty()) ? field.getName() : settingAnnotation.name();
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

}
