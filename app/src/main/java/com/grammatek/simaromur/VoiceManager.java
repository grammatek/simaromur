package com.grammatek.simaromur;

import androidx.lifecycle.ViewModelProvider ;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.grammatek.simaromur.db.Voice;

/**
 * This activity is started, when one presses on "Símarómur raddir" inside TTSManager.
 */
public class VoiceManager extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_" + VoiceManager.class.getSimpleName();
    public static final String EXTRA_DATA_VOICE_ID = "voice_model_id";

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
        VoiceViewModel voiceViewModel = new ViewModelProvider(this, factory).get(VoiceViewModel.class);

        // Add an observer on the LiveData returned by getAllVoices.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        voiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "VoiceManager::onCreate::voiceViewModel.getAllVoices().observe - voices size: " + voices.size());
            // sort voices by their type and then their internal name
            voices.sort((v1, v2) -> {
                if (v1.type.equals(v2.type)) {
                    return v1.internalName.compareTo(v2.internalName);
                } else {
                    // we want to have the type sorted in the order of
                    // the following list:
                    // 1. "vits"
                    // 3. "network"
                    if (v1.type.equals("vits")) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            // Update cached voices
            adapter.setVoices(voices);
        });

        adapter.setOnItemClickListener((v, position) -> {
            Voice voice = adapter.getVoiceAtPosition(position);
            Log.v(LOG_TAG, "onItemClick - Selected Voice: " + voice.name);
            launchVoiceInfoActivity(voice);
        });
        setTitle("Símarómur / " + getResources().getString(R.string.simaromur_voice_manager));
    }

    public void launchVoiceInfoActivity( Voice voice) {
        Log.v(LOG_TAG, "launchVoiceInfoActivity for voice: " + voice);
        Intent intent = new Intent(this, VoiceInfo.class);
        intent.putExtra(EXTRA_DATA_VOICE_ID, voice.voiceId);
        startActivity(intent);
    }
}
