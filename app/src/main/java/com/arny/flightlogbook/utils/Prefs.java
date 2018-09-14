package com.arny.flightlogbook.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class Prefs {

    private static SharedPreferences settings = null;

    private static SharedPreferences getSettings(@NonNull Context context) {
        if(settings == null) {
            settings = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return settings;
    }

    /**
     * Получение конфига по ключу
     * @param key
     * Ключ
     * @param context
     * Контекст
     * @return
     * Значение конфига
     */
    public static String getString(String key,@NonNull Context context) {
        settings = getSettings(context);
        return settings.getString(key, null);
    }

    /**
     * Получение конфига по ключу
     * @param key
     * Ключ
     * @param context
     * Контекст
     * @param defaultVal
     * Значение по умолчанию
     * @return
     * Значение конфига
     */
    public static String getString(String key,@NonNull Context context,String defaultVal) {
        settings = getSettings(context);
        return settings.getString(key, defaultVal);
    }

    /**
     * Получение конфига по ключу
     * @param key
     * Ключ
     * @param context
     * Контекст
     * @return
     * Значение конфига
     */
    public static Integer getInt(String key,@NonNull Context context) {
        settings = getSettings(context);
        return settings.getInt(key, 0);
    }

	public static Long getLong(String key,@NonNull Context context) {
		settings = getSettings(context);
		return settings.getLong(key, 0);
	}

    /**
     * Получение конфига по ключу
     * @param key
     * Ключ
     * @param context
     * Контекст
     * @return
     * Значение конфига
     */
    public static boolean getBoolean(String key,boolean defaultVal,@NonNull Context context) {
        settings = getSettings(context);
        return settings.getBoolean(key, defaultVal);
    }

    /**
     * Установка конфига
     * @param key
     * Ключ
     * @param value
     * Значение
     * @param context
     * Текущий контекст
     */
    public static void setString(String key, String value,@NonNull Context context) {
        settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Установка числового конфига
     * @param key
     * Ключ
     * @param value
     * Значение
     * @param context
     * Текущий контекст
     */
    public static void setBoolean(String key, boolean value,@NonNull Context context) {
        settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Установка числового конфига
     * @param key
     * Ключ
     * @param value
     * Значение
     * @param context
     * Текущий контекст
     */
    public static void setInt(String key, Integer value,@NonNull Context context) {
        settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);
        editor.apply();
    }

	public static void setLong(String key, long value,@NonNull Context context) {
		settings = getSettings(context);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(key, value);
		editor.apply();
	}

    public static void setFloat(String key, float value, @NonNull Context context) {
        settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static float getFloat(String key, float defaultVal, @NonNull Context context) {
        settings = getSettings(context);
        return settings.getFloat(key, defaultVal);
    }

    /**
     * Удаление ключа из конфига
     * @param key
     * Ключ
     * @param context
     * Контекст
     */
    public static void remove(String key,@NonNull Context context) {
        settings = getSettings(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(key);
        editor.apply();
    }

}