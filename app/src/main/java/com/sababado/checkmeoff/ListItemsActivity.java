package com.sababado.checkmeoff;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.sababado.checkmeoff.easyprovider.Contracts;
import com.sababado.checkmeoff.models.List;

public class ListItemsActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    private static String ARG_LIST_ID_ON_LOAD = "arg_list_id_on_load";

    private static int listItemCount = 0;
    private long listIdOnLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_items);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            listIdOnLoad = savedInstanceState.getLong(ARG_LIST_ID_ON_LOAD);
        }

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ARG_LIST_ID_ON_LOAD, listIdOnLoad);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getSupportLoaderManager().destroyLoader(0);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.list_items, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_add_list) {
            handleNavAddList();
        } else if (id == R.id.nav_delete_list) {
            handleNavDeleteLists();
        } else {
            Toast.makeText(ListItemsActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
            handleListSelect(item.getItemId(), item.getTitle().toString());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleListSelect(long id, String title) {
        listIdOnLoad = id;
        ListItemFragment fragment = (ListItemFragment) getSupportFragmentManager().findFragmentByTag(ListItemFragment.TAG);
        if (fragment != null) {
            fragment.swapList(id, title);
        } else {
            fragment = ListItemFragment.newInstance(id, title);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, ListItemFragment.TAG)
                    .commit();
        }
    }

    private void handleNavAddList() {
        Contracts.Contract contract = Contracts.getContract(List.class);
        ContentValues values = (new List("List " + listItemCount).toContentValues());
        Uri insertUri = getContentResolver().insert(contract.CONTENT_URI, values);
        String id = String.valueOf(ContentUris.parseId(insertUri));
        Cursor cursor = getContentResolver().query(contract.CONTENT_URI,
                contract.COLUMNS,
                BaseColumns._ID + " = ?",
                new String[]{id}, null);
        cursor.moveToFirst();
        List newList = new List(cursor);
        listIdOnLoad = newList.getId();
        listItemCount++;
    }

    private void handleNavDeleteLists() {
        listIdOnLoad = -1;
        Contracts.Contract contract = Contracts.getContract(List.class);
        getContentResolver().delete(contract.CONTENT_URI, null, null);
        setTitle(R.string.app_name);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Contracts.Contract contract = Contracts.getContract(List.class);
        final Uri uri = contract.CONTENT_URI;
        final String[] projection = contract.COLUMNS;
        return new CursorLoader(this, uri, projection, null, null, null);
    }

    private Handler handler = new Handler();

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        SubMenu subMenu = navigationView.getMenu().getItem(0).getSubMenu();
        subMenu.clear();
        final List listToLoad = new List();
        if (data != null && data.moveToFirst()) {
            int count = data.getCount();
            listItemCount = count;
            for (int i = 0; i < count; i++) {
                List list = new List(data);
                if ((listIdOnLoad == 0 && i == 0) || (list.getId() == listIdOnLoad)) {
                    listToLoad.fromList(list);
                }
                subMenu.add(R.id.nav_list_group, (int) list.getId(), Menu.NONE, list.getTitle());
                data.moveToNext();
            }
        } else {
            listItemCount = 0;
        }

        if (listToLoad.getId() != -1) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    handleListSelect(listToLoad.getId(), listToLoad.getTitle());
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).getSubMenu().clear();
    }
}
