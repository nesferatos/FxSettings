package ru.nesferatos.fxsettings;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by nesferatos on 01.09.2015.
 */

@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Setting {
    String name() default "";
    String desc() default "";
    String category() default "";
    String factoryName() default "";
    boolean isEditableField() default true;
    boolean forceNotNode() default false;
}
