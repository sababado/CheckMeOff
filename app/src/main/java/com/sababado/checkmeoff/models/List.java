package com.sababado.checkmeoff.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.sababado.checkmeoff.easyprovider.Column;
import com.sababado.checkmeoff.easyprovider.Id;
import com.sababado.checkmeoff.easyprovider.Table;

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

    public List(String title, long id) {
        this.title = title;
        this.id = id;
    }

    public List(Cursor cursor) {
        title = cursor.getString(1);
        id = cursor.getLong(0);
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
