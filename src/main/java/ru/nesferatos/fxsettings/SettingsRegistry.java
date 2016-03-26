package ru.nesferatos.fxsettings;

import java.util.*;

/**
 * Created by nesferatos on 06.01.2016.
 */
public class SettingsRegistry {

    private static SettingsRegistry instance;
    private Map<String, List<Object>> registryMap = new HashMap<>();

    private SettingsRegistry() {

    }

    public static SettingsRegistry getInstance() {
        if (instance == null) {
            instance = new SettingsRegistry();
        }
        return instance;
    }

    public List get(String key) {
        if (registryMap.containsKey(key)) {
            return Collections.unmodifiableList(registryMap.get(key));
        } else {
            return Collections.emptyList();
        }
    }

    public void remove(String key, Object obj) {
        registryMap.get(key).remove(obj);
    }

    public void put(String key, Object obj) {
        if (!registryMap.containsKey(key)) {
            registryMap.put(key, new ArrayList<>());
        }
        registryMap.get(key).add(obj);
    }
}
