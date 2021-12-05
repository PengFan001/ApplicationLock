package com.example.applicationlock.provider;

import android.net.Uri;

public class AppProviderConstant {
    private static final String SETTINGS_URI_STRING = "content://" + AppProvider.AUTHORITY + "/"
            + DatabaseHelper.SETTINGS_TABLE_NAME;

    public static final Uri SETTINGS_URI = Uri.parse(SETTINGS_URI_STRING);
    public static final String SETTINGS_NAME = DatabaseHelper.SETTINGS_NAME;
    public static final String SETTING_VALUE = DatabaseHelper.SETTINGS_VALUE;
}
