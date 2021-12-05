package com.example.applicationlock.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.Nullable;

import com.example.applicationlock.util.LogUtil;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DB_NAME = "app.db";
    private static final int DB_VERSION = 1;

    public static final String SETTINGS_TABLE_NAME = "settings";
    public static final String SETTINGS_NAME = "name";
    public static final String SETTINGS_VALUE = "value";

    private String CREATE_TABLE_SETTINGS = "create table if not exists " + SETTINGS_TABLE_NAME +
            "(id integer primary key autoincrement, " + SETTINGS_NAME +
            " varchar unique on conflict replace, " + SETTINGS_VALUE +
            " varchar)";

    public DatabaseHelper(Context context) {
        this(context, DB_NAME, null, DB_VERSION);
    }

    public DatabaseHelper(@Nullable Context context, @Nullable String name,
                          @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        LogUtil.d(TAG, "onCreate: -----");
        db.execSQL(CREATE_TABLE_SETTINGS);
        initAppLockSettings(db);
    }

    private void initAppLockSettings(SQLiteDatabase db) {
        if (db == null) {
            throw new NullPointerException("initAppLockSettings: db is nulll");
        }
        LogUtil.d(TAG, "initAppLockSettings: -----");
        String sql = "INSERT OR IGNORE INTO " + SETTINGS_TABLE_NAME + "(" + SETTINGS_NAME + ","
                + SETTINGS_VALUE + ") VALUES(?, ?)";
        SQLiteStatement statement = db.compileStatement(sql);

        statement.bindString(1, AppSettings.IS_APP_LOCK_OPEN);
        statement.bindString(2, "false");
        statement.execute();

        statement.bindString(1, AppSettings.IS_APP_LOCK);
        statement.bindString(2, "false");
        statement.execute();

        statement.bindString(1, AppSettings.APP_LOCK_MODE);
        statement.bindString(2, String.valueOf(AppSettings.LOCK_MODE_PASSWORD));
        statement.execute();

        statement.bindString(1, AppSettings.REMAIN_UNLOCK_COUNT);
        statement.bindString(2, "5");
        statement.execute();

        statement.bindString(1, AppSettings.APP_LOCK_ENABLE);
        statement.bindString(2, "true");
        statement.execute();

        statement.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //todo upgrade
    }
}
