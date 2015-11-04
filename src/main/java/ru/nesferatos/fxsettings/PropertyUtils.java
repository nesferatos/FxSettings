package ru.nesferatos.fxsettings;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by nesferatos on 02.08.2015.
 */

interface Filter {
    boolean isSuitable(Field field);
}

public class PropertyUtils {

    private static String capitaliseName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /***
     * get setter method by property name
     *
     * @param object container of getter
     * @param name name of property
     * @param type type of property
     * @return requested setter
     */
    public static Method getSetter(Object object, String name, Class type) {
        String setterName = "set" + capitaliseName(name);
        return getMethod(object, setterName, type);
    }

    /***
     * get getter by name
     *
     * @param object container of setter
     * @param name name of property
     * @return requester getter
     */
    public static Method getGetter(Object object, String name) {
        String getterName = "get" + capitaliseName(name);
        return getMethod(object, getterName);
    }

    /***
     * get method by name
     *
     * @param object container of method
     * @param name name of method
     * @param params params of method
     * @return requested method
     */
    public static Method getMethod(Object object, String name, Object... params) {
        try {
            List<Class> classParams = new ArrayList<>();
            for (int i = 0; i < params.length; i++) {
                classParams.add((Class) params[i]);
            }
            Class<?> classParamsArray[] = new Class[classParams.size()];
            classParams.toArray(classParamsArray);
            return object.getClass().getMethod(name, classParamsArray);
        } catch (NoSuchMethodException e) {
        }
        return null;
    }

    /***
     * get property by field - using getter if defined
     *
     * @param object container of property
     * @param field property field
     * @param <T> type to return
     * @return property
     */
    public static <T> T get(Object object, Field field) {
        Method m = getGetter(field.getName(), field.getName());
        if (m == null) {
            field.setAccessible(true);
            try {
                return (T) field.get(object);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                T result = (T) m.invoke(object);
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /***
     * set property by field - using setter if defined
     *
     * @param object container of property
     * @param field property field
     * @param value value to set
     */
    public static void set(Object object, Field field, Object value) {
        Method method = getSetter(object, field.getName(), field.getType());
        if (method == null) {
            field.setAccessible(true);
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            try {
                method.invoke(object, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Field getField(Class clazz, String fieldName) {
        return getAllFields(new ArrayList<>(), clazz, new Filter() {
            @Override
            public boolean isSuitable(Field field) {
                return field.getName().equals(fieldName);
            }
        }).get(0);
    };

    /***
     * get all fields of object
     *
     * @param fields empty list to recursive fill
     * @param type class of fields container
     * @param filter fields filter
     * @return list of fields
     */
    public static List<Field> getAllFields(List<Field> fields, Class<?> type, Filter filter) {
        Field[] fieldsArr = type.getDeclaredFields();
        if (filter != null) {
            for (Field field : fieldsArr) {
                if (filter.isSuitable(field)) {
                    fields.add(field);
                }
            }
        } else {
            fields.addAll(Arrays.asList(type.getDeclaredFields()));
        }

        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass(), filter);
        }
        return fields;
    }

    private static boolean isSettingField(Field field) {
        int modifiers = field.getModifiers();
        if (Modifier.isStatic(modifiers) ||
                Modifier.isFinal(modifiers)) {
            return false;
        }
        if (field.getAnnotation(Setting.class) != null) {
            return true;
        }
        return false;
    }

    public static List<Field> getSettings(Object object) {
        return getAllFields(new ArrayList<>(), object.getClass(), new Filter() {
            @Override
            public boolean isSuitable(Field field) {
                if (isSettingField(field)) {
                    field.setAccessible(true);
                    return field.getAnnotation(Setting.class).isEditableField();
                } else {
                    return false;
                }
            }
        });
        /*return getAllFields(new ArrayList<>(), object.getClass()).stream().filter(field -> {
            if (isSettingField(field)) {
                field.setAccessible(true);
                return field.getAnnotation(Setting.class).isEditableField();
            } else {
                return false;
            }
        }).collect(Collectors.toList());*/
    }

    public static List<Field> getSettingNodes(Object object) {
        return getAllFields(new ArrayList<>(), object.getClass(), new Filter() {
            @Override
            public boolean isSuitable(Field field) {
                if (isSettingField(field)) {
                    field.setAccessible(true);
                    Setting annotation = field.getAnnotation(Setting.class);
                    if (annotation.forceNotNode()) {
                        return false;
                    }
                    if (!annotation.factoryName().equals("")) {
                        return true;
                    }
                    try {
                        Object obj = field.get(object);
                        if (obj != null) {
                            return !getAllFields(new ArrayList<>(), obj.getClass(), new Filter() {
                                @Override
                                public boolean isSuitable(Field field) {
                                    return field.getAnnotation(Setting.class) != null;
                                }
                            }).isEmpty();
                        } else {
                            return false;
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        /*return getAllFields(new ArrayList<>(), object.getClass()).stream().filter(field -> {
            if (isSettingField(field)) {
                field.setAccessible(true);
                if (!field.getAnnotation(Setting.class).factoryName().equals("")) {
                    return true;
                }
                try {
                    Object obj = field.get(object);
                    if (obj != null) {
                        return !getAllFields(new ArrayList<>(), obj.getClass()).stream().filter(innerField -> {
                            return innerField.getAnnotation(Setting.class) != null;
                        }).collect(Collectors.toList()).isEmpty();
                    } else {
                        return false;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return false;
        }).collect(Collectors.toList());*/
    }

    public static String getNameFor(Object data, Field field) {
        String name = "";
        if (data instanceof TreeItemValueProvider) {
            if (((TreeItemValueProvider) data).getTreeItemValue() != null) {
                name = ((TreeItemValueProvider) data).getTreeItemValue();
            }
        }
        if (name.trim().isEmpty()) {
            if (field != null) {
                Setting settingsAnnotation = field.getAnnotation(Setting.class);
                name = settingsAnnotation.name().equals("") ? field.getName() : settingsAnnotation.name();
            } else {
                name = data.toString();
            }
        }
        return name;
    }
}
