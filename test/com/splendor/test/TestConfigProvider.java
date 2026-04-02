package com.splendor.test;

import com.splendor.config.IConfigProvider;
import com.splendor.exception.ConfigException;

public class TestConfigProvider implements IConfigProvider {
    @Override
    public void loadConfiguration() throws ConfigException {
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        return defaultValue;
    }

    @Override
    public int getIntProperty(String key, int defaultValue) {
        return defaultValue;
    }
}
