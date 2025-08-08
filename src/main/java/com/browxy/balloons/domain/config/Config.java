package com.browxy.balloons.domain.config;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Properties props = new Properties();
    private static Config config = null;
    private Map<String, ConfigResource> configResources;

    private Config() {
        initialize();
    }

    private void initialize() {
        configResources = new LinkedHashMap<String, ConfigResource>();
        loadConfigResources();
    }

    public static Config getInstance() {
        if (config == null) {
            createInstance();
        }
        return config;
    }

    private synchronized static void createInstance() {
        if (config == null) {
            config = new Config();
        }
    }

    public synchronized void updateConfigResources() {
        props.clear();
        loadConfigResources();
    }

    private synchronized void loadConfigResources() {
        for (String configResourceName : configResources.keySet()) {
            ConfigResource configResource = configResources.get(configResourceName);
            if (configResource.getType().equals(ConfigResource.Type.Resource)) {
                loadResourceInCurrentProperties(configResource.getPath());
            }
            if (configResource.getType().equals(ConfigResource.Type.File)) {
                loadAbsoluteProperties(configResource.getPath());
            }
        }
    }

    private synchronized void loadAbsoluteProperties(String propertyFilePath) {
        try (InputStream stream = new BufferedInputStream(new FileInputStream(propertyFilePath))) {
            props.load(stream);
        } catch (Exception e1) {
            throw new RuntimeException("error Loading local properties", e1);
        }
    }

    private void loadResourceInCurrentProperties(String resource) {
        InputStream stream = null;
        try {
            URL resourceURL = Thread.currentThread().getContextClassLoader().getResource(resource);
            if (resourceURL != null) {
                stream = resourceURL.openStream();
                props.load(stream);
            } else {
                logger.info("error loading addition local properties / Cannot find: " + resource);
            }
        } catch (Exception e1) {
            logger.trace("error loading addition local properties  for file: " + e1.getMessage());
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    logger.trace("problem closing file. loading additional local properties. resource: " + resource);
                }
            }
        }
    }

    public Properties getProperties() {
        return props;
    }

    public int getIntValue(String propertyKey, int defaultValue) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null && value.length() > 0) {
            try {
                return Integer.parseInt(value);
            } catch (Exception e1) {
                logger.debug("warning: property not properly set: " + propertyKey + " using default value: " + defaultValue);
                return defaultValue;
            }
        } else {
            logger.debug("warning: property not properly set: " + propertyKey + " using default value: " + defaultValue);
            return defaultValue;
        }
    }

    public String getStringValue(String propertyKey, String defaultValue) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null && value.length() > 0) {
            return value;
        } else {
            logger.debug("warning: property not properly set: " + propertyKey + " using default value: " + defaultValue);
            return defaultValue;
        }
    }

    public String getStringValue(String propertyKey) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null) {
            return value.trim();
        }
        return null;
    }

    public boolean getBooleanValue(String propertyKey) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null && value.length() > 0 && (value.equals("true") || value.equals("Y"))) {
            return true;
        } else {
            return false;
        }
    }

    public int getIntValue(String propertyKey) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null && value.length() > 0) {
            try {
                value = value.trim();
                return Integer.parseInt(value);
            } catch (Exception e1) {
                throw new RuntimeException("config property not properly set: " + propertyKey);
            }
        } else {
            throw new RuntimeException("config property not properly set: " + propertyKey);
        }
    }

    public String getNotBlankValue(String propertyKey) {
        String value = this.getProperties().getProperty(propertyKey);
        if (value != null && value.length() > 0) {
            return value;
        } else {
            throw new RuntimeException("config property not properly set: " + propertyKey);
        }
    }

    public void addConfigResource(ConfigResource configResource) {
        this.configResources.put(configResource.getName(), configResource);
        this.loadConfigResources();
    }

    public Map<String, String> getPropertiesAsMap() {
        Map<String, String> result = new HashMap<String, String>();
        for (Object key : props.keySet()) {
            result.put((String) key, (String) props.get(key));
        }
        return result;
    }

}

