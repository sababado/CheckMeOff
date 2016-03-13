package com.sababado.checkmeoff;

import android.app.Application;
import android.content.Context;

import com.sababado.ezprovider.EasyProvider;
import com.sababado.checkmeoff.models.List;
import com.sababado.checkmeoff.models.ListItem;
import com.sababado.checkmeoff.myprovider.DatabaseHelper;

/**
 * Created by robert on 2/28/16.
 */
public class MyApp extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        EasyProvider.init(this, DatabaseHelper.class, List.class, ListItem.class);
    }
}
