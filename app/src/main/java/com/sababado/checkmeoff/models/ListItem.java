package com.sababado.checkmeoff.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.sababado.checkmeoff.easyprovider.Column;
import com.sababado.checkmeoff.easyprovider.Id;
import com.sababado.checkmeoff.easyprovider.Table;

/**
 * Created by robert on 2/28/16.
 */
@Table(name = "listItem", code = 2)
public class ListItem {
    @Id
    long id;

    @Column(1)
    long listId;
    @Column(2)
    String label;
    @Column(3)
    boolean checked;

    public ListItem(Cursor cursor) {
        id = cursor.getLong(0);
        listId = cursor.getLong(1);
        label = cursor.getString(2);
        checked = cursor.getInt(3) == 1;
    }

    private ListItem() {
        checked = false;
    }

    public ListItem(long listId) {
        this();
        this.listId = listId;
    }

    public ListItem(long listId, String label) {
        this();
        this.listId = listId;
        this.label = label;
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues(3);
        values.put("listId", listId);
        values.put("label", label);
        values.put("checked", checked);
        return values;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
