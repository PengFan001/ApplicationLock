package com.example.applicationlock.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.example.applicationlock.MyApplication;

import java.util.HashSet;

public class AppSettings {
    private static final String TAG = "AppSettings";

    private static HashSet<String> secureSets;
    /**
     * app weather set app lock
     */
    public static final String IS_APP_LOCK_OPEN = "is_app_lock_open";
    /**
     * app weather lock
     */
    public static final String IS_APP_LOCK = "is_app_lock";
    /**
     * app lock mode
     * 0-password lock mode
     * 1-graphical lock mode
     */
    public static final String APP_LOCK_MODE = "app_lock_mode";
    public static final int LOCK_MODE_PASSWORD = 0;
    public static final int LOCK_MODE_GRAPHICAL = 1;
    /**
     * app lock password
     */
    public static final String APP_LOCK_PASSWORD = "app_lock_password";
    /**
     * weather user can unlock the app lock
     */
    public static final String APP_LOCK_ENABLE = "app_lock_enable";
    /**
     * user can not unlock until ban_time end
     */
    public static final String APP_LOCK_BANED_TIME = "ban_time";
    /**
     * the user remain unlock count
     * max unlock count is 5
     * the user will be forbidden to input the password for a period of time when the remain unlock time
     * less than 0
     * forbidden time when remain unlock count less than 0
     * 0------------30s
     * -1-----------1min
     * -2-----------5min
     * -3-----------30min
     * -4-----------1hour
     */
    public static final String REMAIN_UNLOCK_COUNT = "remain_unlock_time";

    static {
        initSecureSets();
    }

    private static void initSecureSets() {
        secureSets = new HashSet<String>();
        Cursor cursor = MyApplication.getInstance().getContext().getContentResolver()
                .query(AppProviderConstant.SETTINGS_URI, null,
                        null, null, "id");
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                secureSets.add(cursor.getString(cursor.getColumnIndex(
                        AppProviderConstant.SETTINGS_NAME)));
            }
            cursor.close();
        }
    }

    public static void putString(ContentResolver resolver, String name, String value) {
        if (resolver == null || TextUtils.isEmpty(name)) {
            throw new NullPointerException("AppSettings: resolver = " + resolver
                    + "   name = " + name);
        }
        ContentValues contentValues = new ContentValues();
        if (secureSets.contains(name)) {
            String where = AppProviderConstant.SETTINGS_NAME + " = '" + name + "'";
            contentValues.put(AppProviderConstant.SETTING_VALUE, value);
            resolver.update(AppProviderConstant.SETTINGS_URI, contentValues,
                    where, null);
        } else {
            contentValues.put(AppProviderConstant.SETTINGS_NAME, name);
            contentValues.put(AppProviderConstant.SETTING_VALUE, value);
            Uri insetUri = resolver.insert(AppProviderConstant.SETTINGS_URI, contentValues);
            if (insetUri != null) {
                secureSets.add(name);
            }
        }
    }

    public static String getString(ContentResolver resolver, String name) {
        if (resolver == null || TextUtils.isEmpty(name)) {
            throw new NullPointerException("AppSettings: resolver = " + resolver
                    + "   name = " + name);
        }
        String value = null;
        if (secureSets.contains(name)) {
            String selection = AppProviderConstant.SETTINGS_NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(AppProviderConstant.SETTINGS_URI, null,
                    selection, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    value = cursor.getString(cursor
                            .getColumnIndex(AppProviderConstant.SETTING_VALUE));
                }
            }
        }
        return value;
    }

    public static void putInt(ContentResolver resolver, String name, int value) {
        putString(resolver, name, String.valueOf(value));
    }

    public static int getInt(ContentResolver resolver, String name, int defaultValue) {
        try {
            String value = getString(resolver, name);
            return Integer.parseInt(value);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static void putBoolean(ContentResolver resolver, String name, boolean enable) {
        putString(resolver, name, String.valueOf(enable));
    }

    public static boolean getBoolean(ContentResolver resolver, String name, boolean defaultEnable) {
        try {
            String value = getString(resolver, name);
            return Boolean.parseBoolean(value);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultEnable;
        }
    }

    public static void putLong(ContentResolver resolver, String name, long value) {
        putString(resolver, name, String.valueOf(value));
    }

    public static long getLong(ContentResolver resolver, String name, long defaultValue) {
        try {
            String value = getString(resolver, name);
            return Long.parseLong(value);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
