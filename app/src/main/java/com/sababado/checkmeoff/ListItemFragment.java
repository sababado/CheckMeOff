package com.sababado.checkmeoff;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.sababado.checkmeoff.easyprovider.Contracts;
import com.sababado.checkmeoff.models.List;
import com.sababado.checkmeoff.models.ListItem;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ListItemFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String TAG = ListItemFragment.class.getSimpleName();
    private static final String ARG_LIST_ID = "arg_list_id";
    private static final String ARG_LIST_TITLE = "arg_list_title";
    private ListItemAdapter adapter;
    private static final long NO_ID = -1;
    private long listId = NO_ID;
    private Callbacks callbacks;

    public static ListItemFragment newInstance(long listId, String title) {
        Bundle args = new Bundle();
        args.putLong(ARG_LIST_ID, listId);
        args.putString(ARG_LIST_TITLE, title);
        ListItemFragment fragment = new ListItemFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new ListItemAdapter(getActivity(), null);
        listId = getArguments().getLong(ARG_LIST_ID);
        getLoaderManager().initLoader(1, null, this);
        getActivity().setTitle(getArguments().getString(ARG_LIST_TITLE));
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getLoaderManager().destroyLoader(1);
    }

    public void swapList(long listId, String listName) {
        this.listId = listId;
        getLoaderManager().restartLoader(1, null, this);
        getActivity().setTitle(listName);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_items, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.action_delete).setVisible(listId != NO_ID);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {
            handleDeleteList();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleDeleteList() {
        Contracts.Contract contract = Contracts.getContract(List.class);
        getActivity().getContentResolver().delete(contract.CONTENT_URI,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(listId)});
        callbacks.onListDeleted(listId);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] selectionArgs = new String[]{
                String.valueOf(listId)};
        Contracts.Contract contract = Contracts.getContract(ListItem.class);
        return new CursorLoader(getActivity(), contract.CONTENT_URI, contract.COLUMNS,
                "listId = ?", selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (adapter != null) {
            adapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.swapCursor(null);
        }
    }

    private class ListItemAdapter extends CursorAdapter {
        private LayoutInflater inflater;

        public ListItemAdapter(Context context, Cursor c) {
            super(context, c, 0);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = inflater.inflate(R.layout.fragment_listitem, parent, false);
            ViewHolder vh = new ViewHolder();
            vh.text1 = (TextView) view.findViewById(R.id.text1);
            vh.text2 = (TextView) view.findViewById(R.id.text2);
            view.setTag(vh);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ListItem listItem = new ListItem(cursor);
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.text1.setText(listItem.getLabel());
            vh.text2.setText(String.valueOf(listItem.getId()));
            int checkedColor = getResources().getColor(android.R.color.holo_orange_light);
            int normalColor = getResources().getColor(android.R.color.white);
            view.setBackgroundColor(listItem.isChecked() ? checkedColor : normalColor);
        }

        private class ViewHolder {
            TextView text1;
            TextView text2;
        }
    }

    public interface Callbacks {
        void onListDeleted(long listId);
    }
}
