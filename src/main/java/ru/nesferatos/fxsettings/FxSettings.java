package ru.nesferatos.fxsettings;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * Created by nesferatos on 16.09.2015.
 */
public class FxSettings extends Control {

    private ObjectProperty root = new SimpleObjectProperty<>();

    /***
     *
     * @param root Root object of settings model
     */
    public FxSettings(Object root) {
        this.root.set(root);
    }

    public Object getRoot() {
        return root.get();
    }

    public void setRoot(Object object) {
        root.set(object);
    }

    public ObjectProperty getRootProperty() {
        return root;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FxSettingsSkin(this);
    }
}
