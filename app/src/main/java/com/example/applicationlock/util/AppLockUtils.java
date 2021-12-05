package com.example.applicationlock.util;

import android.content.Context;

import com.example.applicationlock.provider.AppSettings;

public class AppLockUtils {
    public static final String ACTION_UNLOCK_APP_LOCK = "com.lock.action.UNLOCK";
    public static final String ACTION_MODIFY_LOCK_PASSWORD = "com.lock.action.MODIFY";
    public static final String ACTION_CLOSE_APP_LOCK = "ccom.lock.action.CLOSE";

    public static boolean isAppLockOpen(Context context) {
        return AppSettings.getBoolean(context.getContentResolver(), AppSettings.IS_APP_LOCK_OPEN,
                false);
    }

    public static boolean isAppLock(Context context) {
        return AppSettings.getBoolean(context.getContentResolver(), AppSettings.IS_APP_LOCK,
                false);
    }

    public static int getAppLockMode(Context context) {
        return AppSettings.getInt(context.getContentResolver(), AppSettings.APP_LOCK_MODE,
                AppSettings.LOCK_MODE_PASSWORD);
    }

    public static boolean isAppLockEnable(Context context) {
        return AppSettings.getBoolean(context.getContentResolver(), AppSettings.APP_LOCK_ENABLE,
                true);
    }
}
