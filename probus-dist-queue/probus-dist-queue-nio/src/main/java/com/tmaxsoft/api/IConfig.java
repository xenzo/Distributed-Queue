/*
 * IConfig.java Version 1.0 May 6, 2013
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
package com.tmaxsoft.api;

/**
 * The Interface IConfig.
 */
public interface IConfig {
    /**
     * Sets the float value.
     * @param key the key
     * @param f the f
     * @return the float
     */
    public abstract float setFloatValue(String key, float f);

    /**
     * Gets the float value.
     * @param key the key
     * @param def the default value if null
     * @return the float value
     */
    public abstract float getFloatValue(String key, float def);

    /**
     * Gets the float value.
     * @param key the key
     * @return the float value
     */
    public abstract float getFloatValue(String key);

    /**
     * Sets the double value.
     * @param key the key
     * @param d the d
     * @return the double
     */
    public abstract double setDoubleValue(String key, double d);

    /**
     * Gets the double value.
     * @param key the key
     * @param def the default value if null
     * @return the double value
     */
    public abstract double getDoubleValue(String key, double def);

    /**
     * Gets the double value.
     * @param key the key
     * @return the double value
     */
    public abstract double getDoubleValue(String key);

    /**
     * Sets the string value.
     * @param key the key
     * @param value the value
     * @return the string
     */
    public abstract String setStringValue(String key, String value);

    /**
     * Gets the string value.
     * @param key the key
     * @param def the default value if null
     * @return the string value
     */
    public abstract String getStringValue(String key, String def);

    /**
     * Gets the string value.
     * @param key the key
     * @return the string value
     */
    public abstract String getStringValue(String key);

    /**
     * Sets the long value.
     * @param key the key
     * @param l the l
     * @return the long
     */
    public abstract long setLongValue(String key, long l);

    /**
     * Gets the long value.
     * @param key the key
     * @param def the default value if null
     * @return the long value
     */
    public abstract long getLongValue(String key, long def);

    /**
     * Gets the long value.
     * @param key the key
     * @return the long value
     */
    public abstract long getLongValue(String key);

    /**
     * Sets the int value.
     * @param key the key
     * @param i the i
     * @return the int
     */
    public abstract int setIntValue(String key, int i);

    /**
     * Gets the int value.
     * @param key the key
     * @param def the default value if null
     * @return the int value
     */
    public abstract int getIntValue(String key, int def);

    /**
     * Gets the int value.
     * @param key the key
     * @return the int value
     */
    public abstract int getIntValue(String key);

    /**
     * Sets the char value.
     * @param key the key
     * @param c the c
     * @return the char
     */
    public abstract char setCharValue(String key, char c);

    /**
     * Gets the char value.
     * @param key the key
     * @param def the default value if null
     * @return the char value
     */
    public abstract char getCharValue(String key, char def);

    /**
     * Gets the char value.
     * @param key the key
     * @return the char value
     */
    public abstract char getCharValue(String key);
}
