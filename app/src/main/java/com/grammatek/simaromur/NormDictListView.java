package com.grammatek.simaromur;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grammatek.simaromur.db.NormDictEntry;

public class NormDictListView extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_" + NormDictListView.class.getSimpleName();
    public static final String EXTRA_DATA_DICT_ID = "EXTRA_DATA_DICT_ID";
    NormDictListAdapter mAdapter = null;
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_norm_dict_list_view);
        mRecyclerView = findViewById(R.id.normDictRecyclerview);

        if (mAdapter == null) {
            Log.v(LOG_TAG, "onCreate - creating new NormDictListAdapter");
            mAdapter = new NormDictListAdapter(this);
            mAdapter.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY);
        }

        // Get new/existing ViewModel from ViewModelProvider.
        ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
        NormDictViewModel normDictViewModel = new ViewModelProvider(this, factory).get(NormDictViewModel.class);
        normDictViewModel.getEntries().observe(this, entries -> {
            Log.v(LOG_TAG, "normDictViewModel.getEntries().observe - entries: " + entries.size());
            mAdapter.setEntries(entries);
            if (mRecyclerView.getAdapter() != mAdapter) {
                Log.v(LOG_TAG, "normDictViewModel.getEntries().observe setting adapter");
                mRecyclerView.setAdapter(mAdapter);
            }
        });

        mAdapter.setOnItemClickListener((v, position) -> {
            NormDictEntry entry = mAdapter.getEntryAtPosition(position);
            Log.v(LOG_TAG, "onItemClick - selected: " + entry.term + " - " + entry.replacement);
            launchNormDictEntryActivity(entry);
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener( view -> {
            Intent intent = new Intent(this, NormDictInfo.class);
            startActivity(intent);
        });

        setTitle("Símarómur / " + getResources().getString(R.string.simaromur_norm_dictionary));
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "onDestroy");
    }

    public void launchNormDictEntryActivity(NormDictEntry entry) {
        Log.v(LOG_TAG, "launchNormDictEntryActivity for entry: " + entry);
        Intent intent = new Intent(this, NormDictInfo.class);
        intent.putExtra(EXTRA_DATA_DICT_ID, entry.id);
        startActivity(intent);
    }
}
