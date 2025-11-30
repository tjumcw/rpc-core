package com.mcw.distributed.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class RegistryCenterConfig {

    private static final Logger logger = LoggerFactory.getLogger(RegistryCenterConfig.class);

    private static final Properties properties = new Properties();

    static {
        try (InputStream inputStream = RegistryCenterConfig.class.getClassLoader().getResourceAsStream("properties/registry.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("加载注册中心配置文件失败", e);
            throw new RuntimeException("加载注册中心配置文件失败", e);
        }
    }

    /**
     * 获取字符串配置
     */
    public static String getString(String key, String defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            logger.debug("配置项 {} 未设置，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }
        return value.trim();
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    /**
     * 获取整数配置
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            logger.info("配置项 {} 未设置，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            logger.warn("配置项 {} 的值 {} 不是有效的整数，使用默认值: {}", key, value, defaultValue, e);
            return defaultValue;
        }
    }

    /**
     * 获取布尔配置
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            logger.info("配置项 {} 未设置，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }

        return Boolean.parseBoolean(value.trim());
    }

    public static long getLong(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            logger.warn("配置项 {} 解析失败，使用默认值: {}", key, defaultValue, e);
            return defaultValue;
        }
    }
}
