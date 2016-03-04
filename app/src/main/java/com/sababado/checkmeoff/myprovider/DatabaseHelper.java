package com.sababado.checkmeoff.myprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sababado.checkmeoff.easyprovider.Contracts;
import com.sababado.checkmeoff.models.List;
import com.sababado.checkmeoff.models.ListItem;

/**
 * Database helper.
 * Created by robert on 3/1/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "idme.db";
    public static final int VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Contracts.getContract(List.class).SQL_CREATE);
        db.execSQL(Contracts.getContract(ListItem.class).SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Contracts.getContract(List.class).TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + Contracts.getContract(ListItem.class).TABLE_NAME);
        onCreate(db);
    }
}
