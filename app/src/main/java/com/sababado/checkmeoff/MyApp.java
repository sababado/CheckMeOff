package com.sababado.checkmeoff;

import android.app.Application;
import android.util.Log;

import com.sababado.checkmeoff.easyprovider.EasyProvider;
import com.sababado.checkmeoff.models.ListItem;
import com.sababado.checkmeoff.models.List;
import com.sababado.checkmeoff.myprovider.DatabaseHelper;

/**
 * Created by robert on 2/28/16.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v("MyApp", "uhh...wtf");
        EasyProvider.init(this, DatabaseHelper.class, List.class, ListItem.class);
    }
}
