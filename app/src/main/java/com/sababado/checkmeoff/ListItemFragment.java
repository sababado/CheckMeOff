package com.sababado.checkmeoff;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sababado.ezprovider.Contracts;
import com.sababado.checkmeoff.models.List;
import com.sababado.checkmeoff.models.ListItem;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 */
public class ListItemFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        RecognitionListener {
    public static final String TAG = ListItemFragment.class.getSimpleName();
    private static final String ARG_LIST_ID = "arg_list_id";
    private static final String ARG_LIST_TITLE = "arg_list_title";
    private ListItemAdapter adapter;
    private static final long NO_ID = -1;
    private long listId = NO_ID;
    private Callbacks callbacks;
    FloatingActionButton fab;
    AlertDialog addItemDialog;
    EditText addItemEditText;
    ProgressBar voiceProgressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;

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

        speech = SpeechRecognizer.createSpeechRecognizer(getActivity());
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                getActivity().getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        fab = new FloatingActionButton(getActivity());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM | Gravity.END;
        fab.setLayoutParams(params);
        fab.setImageResource(R.drawable.ic_add);

        view.addView(fab);

        voiceProgressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
        voiceProgressBar.setVisibility(View.GONE);
        view.addView(voiceProgressBar);

        addItemEditText = new EditText(getActivity());
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(adapter);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addItemEditText.setText(null);
                if (addItemDialog == null) {
                    addItemDialog = new AlertDialog.Builder(getContext())
                            .setNegativeButton(android.R.string.cancel, null)
                            .setPositiveButton(android.R.string.ok, onAddItemClick)
                            .setTitle(R.string.new_item)
                            .setView(addItemEditText)
                            .create();
                }
                addItemDialog.show();
            }
        });
    }

    private DialogInterface.OnClickListener onAddItemClick = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ListItem listItem = new ListItem(listId, addItemEditText.getText().toString().trim());
            ContentValues values = listItem.toContentValues();
            Contracts.Contract contract = Contracts.getContract(ListItem.class);
            getActivity().getContentResolver().insert(contract.CONTENT_URI, values);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
        }
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
        if (id == R.id.action_start_voice) {
            boolean starting = voiceProgressBar.getVisibility() == View.GONE;
            voiceProgressBar.setVisibility(starting ? View.VISIBLE : View.GONE);
            if (starting) {
                speech.startListening(recognizerIntent);
            } else {
                speech.stopListening();
            }
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

    // <editor-fold desc="List Item Click and View">
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Contracts.Contract contract = Contracts.getContract(ListItem.class);

        ContentValues values = new ContentValues(1);
        ListItemAdapter.ViewHolder vh = (ListItemAdapter.ViewHolder) v.getTag();
        values.put("checked", !vh.checked);

        getActivity().getContentResolver().update(contract.CONTENT_URI,
                values,
                BaseColumns._ID + " = ?", new String[]{String.valueOf(id)});
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
            view.setTag(vh);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ListItem listItem = new ListItem(cursor);
            ViewHolder vh = (ViewHolder) view.getTag();
            vh.text1.setText(listItem.getLabel());
            int checkedColor = getResources().getColor(android.R.color.holo_orange_light);
            int normalColor = getResources().getColor(android.R.color.white);
            view.setBackgroundColor(listItem.isChecked() ? checkedColor : normalColor);
            vh.checked = listItem.isChecked();
        }

        private class ViewHolder {
            TextView text1;
            boolean checked;
        }
    }
// </editor-fold>

    public interface Callbacks {
        void onListDeleted(long listId);
    }

    // <editor-fold desc="Speech Recognition">
    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {
        voiceProgressBar.setIndeterminate(false);
        voiceProgressBar.setMax(10);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        voiceProgressBar.setProgress((int) rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        voiceProgressBar.setIndeterminate(true);
        voiceProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError(int error) {
        Toast.makeText(getActivity(), getErrorText(error), Toast.LENGTH_SHORT).show();
        onEndOfSpeech();
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> results = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        Contracts.Contract contract = Contracts.getContract(ListItem.class);
        ContentValues values = new ContentValues(1);
        values.put("checked", true);


        String[] args = new String[2];
        args[0] = String.valueOf(listId);
        for (int i = 1; i < args.length; i++) {
            args[1] = "%" + results.get(i - 1) + "%";

            getActivity().getContentResolver().update(contract.CONTENT_URI,
                    values,
                    "listId = ? AND label LIKE '?'", args);
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
    // </editor-fold>
}
