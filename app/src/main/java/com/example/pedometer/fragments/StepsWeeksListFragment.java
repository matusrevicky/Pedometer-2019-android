package com.example.pedometer.fragments;

import android.content.AsyncQueryHandler;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.example.pedometer.Defaults;
import com.example.pedometer.R;
import com.example.pedometer.helper.CustomAdapter;
import com.example.pedometer.helper.CustomAdapterWeeks;
import com.example.pedometer.provider.PedometerContentProvider;
import com.example.pedometer.provider.Provider;


public class StepsWeeksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, AbsListView.MultiChoiceModeListener {

    private GridView stepsGridView;
    private CustomAdapterWeeks gridViewAdapter;

    public StepsWeeksListFragment() {
    // required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);
        getActivity().setTitle("Stats");
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        /////////// loads data into grid - start
        String[] from = {"stepsCount", "weekNumber"};
        int[] to = {R.id.StepItemTextView1, R.id.StepItemTextView2};

        stepsGridView = v.findViewById(R.id.notesGridView);
        gridViewAdapter = new CustomAdapterWeeks(getContext(),
                R.layout.pedometer_week_item, Defaults.NO_CURSOR, from, to, Defaults.NO_FLAGS);
        stepsGridView.setAdapter(gridViewAdapter);

       // stepsGridView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
       // stepsGridView.setMultiChoiceModeListener(this);
        getActivity().getSupportLoaderManager().initLoader(6, Bundle.EMPTY, this);
        /////////// loads data into grid - end

        return v;
    }


    ///////////////////////////////// LoaderManager - start
    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        CursorLoader loader = new CursorLoader(getContext());
        loader.setUri(PedometerContentProvider.CONTENT_URI_WEEKS);

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        gridViewAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        gridViewAdapter.swapCursor(Defaults.NO_CURSOR);
    }
    ///////////////////////////////// LoaderManager - end

    /////////////////////////////////  Listener multichoice - start
    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        int count = stepsGridView.getCheckedItemCount();
        actionMode.setSubtitle("Selected items " + count);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.note_cab, menu);
        mode.setTitle("Items selection");

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
//        if (menuItem.getItemId() == R.id.deleteNoteMenuItem) {
//            for (long id : stepsGridView.getCheckedItemIds()) {
//                AsyncQueryHandler handler = new AsyncQueryHandler(getActivity().getContentResolver()) {};
//                handler.startDelete(0, null,PedometerContentProvider.CONTENT_URI, "_ID = ?", new String[]{id+""});
//            }
//            return true;
//        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }
    /////////////////////////////////  Listener multichoice - end
}
