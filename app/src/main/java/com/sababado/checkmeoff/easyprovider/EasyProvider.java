package com.sababado.checkmeoff.easyprovider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.sababado.checkmeoff.myprovider.DatabaseHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Start by calling 'init' from the application class.
 * <p/>
 * Use the Column annotation and specify the index of each column in the table.
 * This way new fields can be added in the future worry free.
 * <p/>
 * One Id field must be specified using the Id annotation. All other fields must be annotated
 * with the Column annotation and must at least specify the column index. 0 is reserved for the ID field.
 * Created by robert on 2/28/16.
 */
public class EasyProvider extends ContentProvider {
    private static final String TAG = EasyProvider.class.getSimpleName();
    static String _AUTHORITY;
    private static Class<? extends SQLiteOpenHelper> _dbHelperClass;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private SQLiteOpenHelper dbOpenHelper;

    public static void init(Context applicationContext, Class<? extends SQLiteOpenHelper> dbHelperClass, Class<?>... tableClasses) {
        try {
            ApplicationInfo ai = applicationContext.getPackageManager().getApplicationInfo(
                    applicationContext.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            _AUTHORITY = bundle.getString("EasyProvider_Authority");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
            throw new RuntimeException(e);
        }

        _dbHelperClass = dbHelperClass;

        initTableClasses(tableClasses);
    }

    private static void initTableClasses(Class<?>[] tableClasses) {
        if (tableClasses == null || tableClasses.length == 0) {
            throw new RuntimeException("EasyProvider is missing tables.");
        }

        // This array is to help developers ensure they're using unique codes for each table.
        ArrayList<Integer> existingTableCodes = new ArrayList<>(tableClasses.length);

        for (int i = 0; i < tableClasses.length; i++) {
            Class cls = tableClasses[i];
            Table table = null;
            Annotation[] annotations = cls.getDeclaredAnnotations();
            for (int j = 0; j < annotations.length; j++) {
                if (annotations[j].annotationType().equals(Table.class)) {
                    table = (Table) annotations[j];
                    break;
                }
            }
            if (table == null) {
                throw new RuntimeException("Class " + cls.getSimpleName() + " should have a Table annotation.");
            }

            if (existingTableCodes.contains(table.code())) {
                throw new RuntimeException("The table code " + table.code() + " is being used more than once. The table code must be unique.");
            }
            existingTableCodes.add(table.code());

            int code = table.code() * 2;
            sUriMatcher.addURI(_AUTHORITY, table.name(), code);
            sUriMatcher.addURI(_AUTHORITY, table.name() + "/#", code + 1);
            Contracts.addContract(cls, table.name(), code);
        }
    }

    @Override
    public boolean onCreate() {
        try {
            Log.v(TAG, "" + DatabaseHelper.class);
            Log.v(TAG, "" + _dbHelperClass);
            dbOpenHelper = _dbHelperClass.getConstructor(Context.class).newInstance(getContext());
            return true;
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "Failed to create database helper: " + e.getMessage());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            Log.e(TAG, "Failed to create database helper: " + e.getMessage());
            e.printStackTrace();
        } catch (InstantiationException e) {
            Log.e(TAG, "Failed to create database helper: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Failed to create database helper: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private String getWhereId(final String idName, Uri uri, final String selection) {
        final String segment = uri.getLastPathSegment();
        return idName + " = " + segment
                + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        int matchedUri = sUriMatcher.match(uri);
        boolean matchFound = false;

        Collection<Contracts.Contract> contracts = Contracts.getContracts();
        for (Contracts.Contract contract : contracts) {
            if (matchedUri == contract.CODE) {
                qb.setTables(contract.TABLE_NAME);
                if (TextUtils.isEmpty(sortOrder)) {
                    sortOrder = BaseColumns._ID + " ASC";
                }
                matchFound = true;
                break;
            } else if (matchedUri == contract.CODE) {
                qb.setTables(contract.TABLE_NAME);
                qb.appendWhere(BaseColumns._ID + " = " + uri.getLastPathSegment());
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        long rowId;
        Uri result = null;
        int matchedUri = sUriMatcher.match(uri);

        Collection<Contracts.Contract> contracts = Contracts.getContracts();
        for (Contracts.Contract contract : contracts) {
            if (matchedUri == contract.CODE) {
                rowId = db.insert(contract.TABLE_NAME, null, values);
                if (rowId >= 0) {
                    result = ContentUris.withAppendedId(contract.CONTENT_URI, rowId);
                }
                break;
            }
        }
        if (result == null) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(result, null);

        return result;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;
        int matchedUri = sUriMatcher.match(uri);
        boolean matchFound = false;

        Collection<Contracts.Contract> contracts = Contracts.getContracts();
        for (Contracts.Contract contract : contracts) {
            if (matchedUri == contract.CODE + 1) {
                selection = getWhereId(BaseColumns._ID, uri, selection);
            }
            if (matchedUri == contract.CODE || matchedUri == contract.CODE + 1) {
                count = db.delete(contract.TABLE_NAME, selection, selectionArgs);
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbOpenHelper.getWritableDatabase();
        int count = 0;
        int matchedUri = sUriMatcher.match(uri);
        boolean matchFound = false;

        Collection<Contracts.Contract> contracts = Contracts.getContracts();
        for (Contracts.Contract contract : contracts) {
            if (matchedUri == contract.CODE + 1) {
                selection = getWhereId(BaseColumns._ID, uri, selection);
            }
            if (matchedUri == contract.CODE || matchedUri == contract.CODE + 1) {
                count = db.update(contract.TABLE_NAME, values, selection, selectionArgs);
                matchFound = true;
                break;
            }
        }
        if (!matchFound) {
            throw new IllegalArgumentException("Unsupported URI: " + uri);
        }

        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }
}
