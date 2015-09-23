package ru.nesferatos.fxsettings;

/**
 * Created by nesferatos on 08.09.2015.
 */
public abstract class SettingsFactory<PRODUCT_TYPE, PRODUCT_CREATE_PARAMS> {
    public abstract PRODUCT_TYPE createProduct(final PRODUCT_CREATE_PARAMS settingObj, final PropertyTreeItem parent) throws ValidationException;
    public abstract PRODUCT_CREATE_PARAMS getProductCreateParams(final PropertyTreeItem parent);
}
