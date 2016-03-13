package com.sababado.checkmeoff.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.sababado.ezprovider.Column;
import com.sababado.ezprovider.Id;
import com.sababado.ezprovider.Table;

/**
 * Created by robert on 2/28/16.
 */
@Table(name = "list", code = 1)
public class List {

    @Column(1)
    private String title;

    @Id
    private long id;

    private int testIgnoreMe;

    public List() {
        title = null;
        id = -1;
    }

    public List(String title) {
        this.title = title;
    }

    public List(Cursor cursor) {
        title = cursor.getString(1);
        id = cursor.getLong(0);
    }

    public void fromList(List list) {
        title = list.title;
        id = list.id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(1);
        values.put("title", title);
        return values;
    }
}
