/*
 * ConfigSupport.java Version 1.0 May 6, 2013
 * *
 * Copyright (c) 2010 by Tmax Soft co., Ltd.
 * All rights reserved.
 * *
 * This software is the confidential and proprietary information of
 * Tmax Soft co.,Ltd("Confidential Information").
 * You shall not disclose such Confidential Information
 * and shall use it only in accordance with the terms of the license agreement
 * entered into with Tmax Soft co., Ltd.
 */
package com.tmaxsoft;


import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import com.tmaxsoft.api.IConfig;


/**
 * The Class ConfigSupport.
 */
public class ConfigSupport implements IConfig {
    private final Map<String, Object> _map = new ConcurrentSkipListMap<String, Object>();

    /**
     * Gets the map.
     * @return the map
     */
    protected final Map<String, Object> getMap() {
        return _map;
    }

    /**
     * Gets the value.
     * @param <T> the generic type
     * @param key the key
     * @param ifNull the if null
     * @param claz the claz
     * @return the value
     */
    public final <T> T getValue(String key, T ifNull, Class<? extends T> claz) {
        Object value = getMap().get(key);
        if (value == null) return ifNull;
        else return claz.cast(value);
    }

    /**
     * Gets the value.
     * @param <T> the generic type
     * @param key the key
     * @param claz the claz
     * @return the value
     */
    public final <T> T getValue(String key, Class<? extends T> claz) {
        return getValue(key, null, claz);
    }

    /**
     * Sets the value.
     * @param <T> the generic type
     * @param key the key
     * @param value the value
     * @param claz the claz
     * @return the t
     */
    public final <T> T setValue(String key, T value, Class<? extends T> claz) {
        return claz.cast(getMap().put(key, value));
    }

    /** @InheritDoc */
    @Override public char getCharValue(String key) {
        return getCharValue(key, '\u0000');
    }

    /** @InheritDoc */
    @Override public char getCharValue(String key, char def) {
        return getValue(key, def, Character.class);
    }

    /** @InheritDoc */
    @Override public char setCharValue(String key, char c) {
        return setValue(key, c, Character.class);
    }

    /** @InheritDoc */
    @Override public int getIntValue(String key) {
        return getIntValue(key, 0);
    }

    /** @InheritDoc */
    @Override public int getIntValue(String key, int def) {
        return getValue(key, def, Integer.class);
    }

    /** @InheritDoc */
    @Override public int setIntValue(String key, int i) {
        return setValue(key, i, Integer.class);
    }

    /** @InheritDoc */
    @Override public long getLongValue(String key) {
        return getLongValue(key, 0L);
    }

    /** @InheritDoc */
    @Override public long getLongValue(String key, long def) {
        return getValue(key, def, Long.class);
    }

    /** @InheritDoc */
    @Override public long setLongValue(String key, long l) {
        return setValue(key, l, Long.class);
    }

    /** @InheritDoc */
    @Override public String getStringValue(String key) {
        return getStringValue(key, "");
    }

    /** @InheritDoc */
    @Override public String getStringValue(String key, String def) {
        return getValue(key, def, String.class);
    }

    /** @InheritDoc */
    @Override public String setStringValue(String key, String value) {
        return setValue(key, value, String.class);
    }

    /** @InheritDoc */
    @Override public double getDoubleValue(String key) {
        return getDoubleValue(key, 0.0d);
    }

    /** @InheritDoc */
    @Override public double getDoubleValue(String key, double def) {
        return getValue(key, def, Double.class);
    }

    /** @InheritDoc */
    @Override public double setDoubleValue(String key, double d) {
        return setValue(key, d, Double.class);
    }

    /** @InheritDoc */
    @Override public float getFloatValue(String key) {
        return getFloatValue(key, 0f);
    }

    /** @InheritDoc */
    @Override public float getFloatValue(String key, float def) {
        return getValue(key, def, Float.class);
    }

    /** @InheritDoc */
    @Override public float setFloatValue(String key, float f) {
        return setValue(key, f, Float.class);
    }
}
