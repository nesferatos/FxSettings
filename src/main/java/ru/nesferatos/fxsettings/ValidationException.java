package ru.nesferatos.fxsettings;

import java.lang.reflect.Field;

/**
 * Created by nesferatos on 19.09.2015.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
}
