package com.sababado.checkmeoff.easyprovider;

import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by robert on 2/28/16.
 */
public abstract class Contracts {
    private static final Map<Class<?>, Contract> contractMap = new HashMap<>();

    static void addContract(Class<?> cls, String tableName, int code) {
        if (!contractMap.containsKey(cls)) {
            Contract contract = new Contract(cls, tableName, code);
            contractMap.put(cls, contract);
        }
    }

    public static Contract getContract(Class<?> cls) {
        return contractMap.get(cls);
    }

    public static Collection<Contract> getContracts() {
        return contractMap.values();
    }

    public static class Contract {
        public final String TABLE_NAME;
        public final String CONTENT_URI_STRING;
        public final Uri CONTENT_URI;
        public final int CODE;

        public final String SQL_CREATE;

        public final String[] COLUMNS;

        // TODO The ID field must be added in the case that the developer doesn't add it.

        public Contract(Class<?> cls, String tableName, int code) {
            this.TABLE_NAME = tableName;
            CONTENT_URI_STRING = "content://" + EasyProvider._AUTHORITY + "/" + TABLE_NAME;
            CONTENT_URI = Uri.parse(CONTENT_URI_STRING);
            CODE = code;

            // init column map & create statement
            Field[] fields = cls.getDeclaredFields();

            TreeMap<Integer, Pair<String, String>> columnMap = new TreeMap<>();
            for (int i = 0; i < fields.length; i++) {
                Pair<String, String> columnNameStatementPair = new Pair<>(null, null);
                Field field = fields[i];
                Id id = field.getAnnotation(Id.class);
                Column column = field.getAnnotation(Column.class);

                if (id == null && column == null) {
                    continue;
                }

                if (id != null) {
                    String columnName = BaseColumns._ID;
                    columnNameStatementPair.first = columnName;
                    columnNameStatementPair.second = columnName + " INTEGER PRIMARY KEY AUTOINCREMENT,";
                    columnMap.put(0, columnNameStatementPair);
                } else {
                    String fieldType = Utils.getDbTypeFromField(field);
                    // Use column data next
                    columnNameStatementPair.first = TextUtils.isEmpty(column.name()) ? field.getName() : column.name();
//                    columnsList.add(column.value(), columnName);
                    columnNameStatementPair.second = columnNameStatementPair.first + " " + fieldType + ",";

                    columnMap.put(column.value(), columnNameStatementPair);
                }
            }

            int columnCount = columnMap.size();
            if (columnCount == 0) {
                throw new RuntimeException("Attempting to create a table with no columns: " + tableName);
            }

            String sqlCreate = "CREATE TABLE " + tableName + " (";

            COLUMNS = new String[columnCount];
            Set<Integer> columnKeys = columnMap.keySet();
            Iterator<Integer> iterator = columnKeys.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                Pair<String, String> pair = columnMap.get(iterator.next());
                COLUMNS[i++] = pair.first;
                sqlCreate += pair.second;
            }

            sqlCreate = sqlCreate.substring(0, sqlCreate.length() - 1);
            sqlCreate += ");";
            SQL_CREATE = sqlCreate;

            Log.v("contracts", sqlCreate);
        }

        public Uri getContentUri(long id) {
            return Uri.parse(CONTENT_URI_STRING + "/" + id);
        }
    }
}
