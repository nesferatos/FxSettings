package ru.nesferatos.fxsettings;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nesferatos on 08.09.2015.
 */
public class FactoryRegistry {
    private static FactoryRegistry instance;
    private final Map<String, SettingsFactory> registry = new HashMap<>();

    public static FactoryRegistry getInstance() {
        if (instance == null) {
            instance = new FactoryRegistry();
        }
        return instance;
    }

    public synchronized boolean registered(String name) {
        return registry.containsKey(name);
    }

    public synchronized SettingsFactory get(String name) {
        return registry.get(name);
    }

    public synchronized void register(String name, SettingsFactory factory) {
        registry.put(name, factory);
    }

}
