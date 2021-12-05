package com.example.applicationlock.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.example.applicationlock.util.LogUtil;

public class AppProvider extends ContentProvider {
    private static final String TAG = "AppProvider";
    private static UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private DatabaseHelper databaseHelper;
    private Context mContext;

    public static final String AUTHORITY = "com.example.applicationlock.provider.app";
    public static final int SETTINGS_DIR_URI_CODE = 10000;
    public static final int SETTINGS_ITEM_URI_CODE = 10001;

    static {
        uriMatcher.addURI(AUTHORITY, "settings", SETTINGS_DIR_URI_CODE);
        uriMatcher.addURI(AUTHORITY, "settings/#", SETTINGS_ITEM_URI_CODE);
    }

    @Override
    public boolean onCreate() {
        mContext = getContext();
        databaseHelper = new DatabaseHelper(mContext);
        LogUtil.d(TAG, "onCreate: Create the databaseHelper");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase database = databaseHelper.getReadableDatabase();
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("query: tableName is null, UnSupportUri = " + uri);
        }
        Cursor cursor;
        String id = getId(uri);
        if (!TextUtils.isEmpty(id)) {
            if (TextUtils.isEmpty(selection)) {
                selection = "id = " + id;
            } else {
                selection = "id = " + id + " and " + selection;
            }
        }
        if (TextUtils.isEmpty(sortOrder)) {
            sortOrder = "id";
        }
        cursor = database.query(tableName, projection, selection, selectionArgs,
                null, null, sortOrder);
        cursor.setNotificationUri(mContext.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SETTINGS_DIR_URI_CODE:
                return "vnd.android.cursor.dir/settings";

            case SETTINGS_ITEM_URI_CODE:
                return "vnd.android.cursor.item/settings";

            default:
                throw new IllegalArgumentException("getType:unknown uri = " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("insert: tableName is null, UnSupportUri = " + uri);
        }
        if (values == null) {
            throw new NullPointerException("insert: values is null");
        }
        long raw_id = database.insert(tableName, "id", values);
        Uri newUri = null;
        if (raw_id > 0) {
            if (DatabaseHelper.SETTINGS_TABLE_NAME.equals(tableName)) {
                newUri = Uri.withAppendedPath(uri, values.getAsString("name"));
            } else {
                newUri = ContentUris.withAppendedId(uri, raw_id);
            }
            mContext.getContentResolver().notifyChange(uri, null);
        }
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("delete: tableName is null, UnSupportUri = " + uri);
        }
        int count = 0;
        String id = getId(uri);
        if (!TextUtils.isEmpty(id)) {
            if (TextUtils.isEmpty(selection)) {
                selection = "id = " + id;
            } else {
                selection = "id = " + id + " and " + selection;
            }
        }
        count = database.delete(tableName, selection, selectionArgs);
        mContext.getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        SQLiteDatabase database = databaseHelper.getWritableDatabase();
        String tableName = getTableName(uri);
        if (TextUtils.isEmpty(tableName)) {
            throw new IllegalArgumentException("delete: tableName is null, UnSupportUri = " + uri);
        }
        if (values == null) {
            throw new NullPointerException("update: values is null");
        }
        int count = 0;
        String id = getId(uri);
        if (!TextUtils.isEmpty(id)) {
            if (TextUtils.isEmpty(selection)) {
                selection = "id = " + id;
            } else {
                selection = "id = " + id + " and " + selection;
            }
        }
        count = database.update(tableName, values, selection, selectionArgs);
        mContext.getContentResolver().notifyChange(uri, null);
        return count;
    }

    private String getTableName(Uri uri) {
        if (uri == null) {
            throw new NullPointerException("getTableName: uri is null");
        }
        String tableName;
        switch (uriMatcher.match(uri)) {
            case SETTINGS_DIR_URI_CODE:
            case SETTINGS_ITEM_URI_CODE:
                tableName = DatabaseHelper.SETTINGS_TABLE_NAME;
                break;

            default:
                tableName = null;
                break;
        }

        return tableName;
    }

    private String getId(Uri uri) {
        if (uri == null) {
            throw new NullPointerException("getItem: uri is null");
        }
        String id;
        switch (uriMatcher.match(uri)) {
            case SETTINGS_ITEM_URI_CODE:
                id = uri.getPathSegments().get(1);
                break;

            default:
                id = null;
                break;
        }
        return id;
    }
}
