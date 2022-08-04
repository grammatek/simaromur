package com.grammatek.simaromur;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.network.ConnectionCheck;

import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_ID;

/**
 * This class displays an info screen for a voice.
 */
public class VoiceInfo extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_" + VoiceInfo.class.getSimpleName();

    private long mVoiceId;
    private EditText mUserText;
    private com.grammatek.simaromur.db.Voice mVoice;
    private VoiceViewModel mVoiceViewModel;
    private ImageView mNetworkAvailabilityIcon;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_info);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mVoiceId = extras.getLong(EXTRA_DATA_VOICE_ID);
        }
        else {
            String errMsg = "No data has been passed to activity ?!";
            Log.e(LOG_TAG, errMsg);
            throw new AssertionError(errMsg);
        }
        // layout doesn't work for landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // create our instance of the view model
        ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
        mVoiceViewModel = new ViewModelProvider(this, factory).get(VoiceViewModel.class);

        // fill in the default user text
        mUserText = findViewById(R.id.speakable_text);

        TextView nameTextView = findViewById(R.id.textViewName);
        TextView langTextView = findViewById(R.id.textViewLanguage);
        TextView genderTextView = findViewById(R.id.textViewGender);
        TextView typeTextView = findViewById(R.id.textViewType);
        TextView speakableTextView = mUserText;

        // setup button / spinner
        Button button = findViewById(R.id.speak_button);
        button.setEnabled(true);
        button.setOnClickListener(this::onPlayClicked);
        ProgressBar pg = findViewById(R.id.progressBarPlay);
        pg.setVisibility(View.INVISIBLE);
        pg.setOnClickListener(this::onSpinnerClicked);

        mNetworkAvailabilityIcon = findViewById(R.id.imageStatus);

        // query DB for given voice id. On completion set appropriate UI attributes
        mVoiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            mVoice = mVoiceViewModel.getVoiceWithId(mVoiceId);
            if (mVoice != null)
            {
                // change english voices default text
                if (mVoice.languageCode.startsWith("en")) {
                    mUserText.setText(getResources().getString(R.string.eng_sample));
                }
                nameTextView.setText(mVoice.name);
                langTextView.setText(mVoice.getLocale().getDisplayLanguage().toLowerCase());
                if (mVoice.gender.equalsIgnoreCase("male")) {
                    genderTextView.setText(getResources().getString(R.string.male));
                } else {
                    genderTextView.setText(getResources().getString(R.string.female));
                }
                if (mVoice.needsNetwork()) {
                    typeTextView.setText(getResources().getString(R.string.type_network));
                    speakableTextView.setText(getResources().getString(R.string.voice_test_text_network));
                } else {
                    typeTextView.setText(getResources().getString(R.string.type_local));
                    speakableTextView.setText(getResources().getString(R.string.voice_test_text_on_device));
                }
                setTitle("Símarómur / " + getResources().getString(R.string.simaromur_voice_manager)
                        + " / " + mVoice.name);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVoiceViewModel.stopSpeaking(mVoice);
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume:");
        super.onResume();
        if (mVoice.needsNetwork()) {
            if (ConnectionCheck.isNetworkConnected()) {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
            }
            else {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                App.getAppRepository().showTtsBackendWarningDialog(this);
            }
        } else {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_download);
        }
    }

    /**
     * Implements an observer to toggle the "Play" button for showing a circular spinner and back
     * again in case audio playback is finished.
     */
    class AudioToggleObserver implements TTSAudioControl.AudioFinishedObserver {
        @Override
        public void update() {
            runOnUiThread(VoiceInfo.this::toggleSpeakButton);
        }
    }

    // speak_button is pressed
    public void onPlayClicked(View v) {
        Log.v(LOG_TAG, "onPlayClicked");
        String text = mUserText.getText().toString();
        AppRepository appRepo = App.getAppRepository();

        CacheItem item = appRepo.getUtteranceCache().addUtterance(text);
        item = appRepo.doNormalizationAndG2PAndSaveIntoCache(text, item);
        appRepo.setCurrentUtterance(item);

        if (mVoice.type.equals(Voice.TYPE_TIRO)) {
            if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                appRepo.showTtsBackendWarningDialog(this);
                return;
            }
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
        } else if (mVoice.type.equals(Voice.TYPE_TORCH)) {
        } else {
            Log.w(LOG_TAG, "Selected voice type " + mVoice.type + " not yet supported !");
            return;
        }
        toggleSpeakButton();
        Log.v(LOG_TAG, "Text to speak: " + item.getUtterance().getNormalized());
        mVoiceViewModel.startSpeaking(mVoice, item, 1.0f, 1.0f, new AudioToggleObserver());
    }

    // circle spinner is pressed
    public void onSpinnerClicked(View v) {
        Log.v(LOG_TAG, "onSpinnerClicked");
        mVoiceViewModel.stopSpeaking(mVoice);
        App.getAppRepository().setCurrentUtterance(null);
        toggleSpeakButton();
    }

    /**
     * Toggles Speak button to spinning if it was visible before, or the spinning wheel to the
     * play button back again otherwise.
     */
    private void toggleSpeakButton() {
        Button button = findViewById(R.id.speak_button);
        ProgressBar pg = findViewById(R.id.progressBarPlay);
        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.INVISIBLE);
            pg.setVisibility(View.VISIBLE);
        } else {
            pg.setVisibility(View.INVISIBLE);
            button.setVisibility(View.VISIBLE);
        }
    }
}
