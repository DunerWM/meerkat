package com.meerkat.base.util;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by wm on 16/9/27.
 */
public class ConfigPropertiesUtil {
    private static final Logger logger = LoggerFactory.getLogger(ConfigPropertiesUtil.class);

    private static ResourceBundle bundle = null;

    static {
        bundle = ResourceBundle.getBundle("config", Locale.CHINESE);
    }

    public static String getValue(String key) {
        try {
            String value = bundle.getString(key);
            if (StringUtils.isBlank(value)) {
                return value;
            } else {
                return new String(value.getBytes("ISO-8859-1"), "UTF-8");
            }
        } catch (Exception e) {
            logger.error("can't find config property: " + key);
            return null;
        }
    }
}
