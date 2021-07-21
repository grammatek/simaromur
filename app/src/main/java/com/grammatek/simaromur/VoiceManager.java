package com.grammatek.simaromur;

import androidx.lifecycle.ViewModelProvider ;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.grammatek.simaromur.db.Voice;

public class VoiceManager extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_" + VoiceManager.class.getSimpleName();
    public static final String EXTRA_DATA_VOICE_ID = "voice_model_id";

    private VoiceViewModel mVoiceViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_manager);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final VoiceListAdapter adapter = new VoiceListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
        mVoiceViewModel = new ViewModelProvider(this, factory).get(VoiceViewModel.class);

        // Add an observer on the LiveData returned by getAllVoices.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mVoiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            // Update cached voices
            adapter.setVoices(voices);
        });

        adapter.setOnItemClickListener((v, position) -> {
            Voice voice = adapter.getVoiceAtPosition(position);
            Log.v(LOG_TAG, "onItemClick - Selected Voice: " + voice.name);
            launchVoiceInfoActivity(voice);
        });
    }

    public void launchVoiceInfoActivity( Voice voice) {
        Log.v(LOG_TAG, "launchVoiceInfoActivity for voice: " + voice);
        Intent intent = new Intent(this, VoiceInfo.class);
        intent.putExtra(EXTRA_DATA_VOICE_ID, voice.voiceId);
        startActivity(intent);
    }
}
