package com.bin.consts;

import java.util.Map;

public class ResourceConfig {

    private Map<String, String> configurationValues;

    public ResourceConfig(Map<String, String> configurationValues) {
        this.configurationValues = configurationValues;
    }

    public String getValue(String key) {
        if (configurationValues.get(key) == null) {
            throw new NullPointerException("Configuration value " + key + " is null");
        }
        return configurationValues.get(key);
    }
}
