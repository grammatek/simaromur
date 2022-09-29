package com.grammatek.simaromur;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.DownloadVoiceManager;
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
    private ProgressDialog mProgressDialog;

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

        // set callbacks for the buttons and spinner
        Button playButton = findViewById(R.id.speak_button);
        playButton.setOnClickListener(this::onPlayClicked);
        Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setOnClickListener(this::onDownloadClicked);
        ProgressBar pg = findViewById(R.id.progressBarPlay);
        pg.setOnClickListener(this::onSpinnerClicked);

        mNetworkAvailabilityIcon = findViewById(R.id.imageStatus);
        // update the rest of the UI
        updateUi();

        // query DB for given voice id. On completion set appropriate UI attributes
        mVoiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            mVoice = mVoiceViewModel.getVoiceWithId(mVoiceId);
            if (mVoice != null) {
                // update the UI according to the voice
                updateUi();
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        Log.v(LOG_TAG, "onWindowFocusChanged:");
        super.onWindowFocusChanged(hasFocus);
        // query DB for given voice id. On completion set appropriate UI attributes
        mVoiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            mVoice = mVoiceViewModel.getVoiceWithId(mVoiceId);
            if (mVoice != null) {
                // update the UI according to the voice
                updateUi();
            }
        });
    }

    private void updateUi() {
        TextView nameTextView = findViewById(R.id.textViewName);
        TextView langTextView = findViewById(R.id.textViewLanguage);
        TextView genderTextView = findViewById(R.id.textViewGender);
        TextView typeTextView = findViewById(R.id.textViewType);
        TextView speakableTextView = mUserText;

        // setup buttons / spinner
        Button playButton = findViewById(R.id.speak_button);
        playButton.setEnabled(true);
        Button downloadButton = findViewById(R.id.download_button);
        downloadButton.setEnabled(false);
        downloadButton.setVisibility(View.GONE);

        ProgressBar pg = findViewById(R.id.progressBarPlay);
        pg.setVisibility(View.INVISIBLE);
        if (mVoice != null) {
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
                // change voice availability icon according to network availability and type
                if (mVoice.needsNetwork()) {
                    if (ConnectionCheck.isNetworkConnected()) {
                        mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
                    } else {
                        mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                        App.getAppRepository().showTtsBackendWarningDialog(this);
                    }
                }
            } else if (mVoice.needsDownload()){
                Log.v(LOG_TAG, "updateUi: voice needs download");
                // set text view and play button
                typeTextView.setText(getResources().getString(R.string.type_local_downloaded));
                speakableTextView.setText(getResources().getString(R.string.voice_test_text_on_device));
                speakableTextView.setVisibility(View.INVISIBLE);
                playButton.setVisibility(View.INVISIBLE);
                // change color of textViewType
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_download);
                mNetworkAvailabilityIcon.setColorFilter(Color.YELLOW);
                // set download button
                downloadButton.setText(getResources().getString(R.string.do_download));
                downloadButton.setEnabled(true);
                downloadButton.setVisibility(View.VISIBLE);
            } else {
                Log.v(LOG_TAG, "updateUi: voice is on device");
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_download);
                mNetworkAvailabilityIcon.setColorFilter(Color.GREEN);
                typeTextView.setText(getResources().getString(R.string.type_local));
                if (mVoice.isFast()) {
                    speakableTextView.setText(getResources().getString(R.string.voice_test_text_on_device_fast));
                } else {
                    speakableTextView.setText(getResources().getString(R.string.voice_test_text_on_device));
                }
                speakableTextView.setVisibility(View.VISIBLE);
                playButton.setVisibility(View.VISIBLE);
            }
            setTitle("Símarómur / " + getResources().getString(R.string.simaromur_voice_manager)
                    + " / " + mVoice.name);
        }
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy:");
        super.onDestroy();
        mVoiceViewModel.stopSpeaking(mVoice);
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume:");
        super.onResume();
        mVoiceViewModel.getAllVoices().observe(this, voices -> {
            Log.v(LOG_TAG, "onChanged - voices size: " + voices.size());
            mVoice = mVoiceViewModel.getVoiceWithId(mVoiceId);
            if (mVoice != null) {
                // update the UI according to the voice
                updateUi();
            }
        });
    }

    /**
     * Implements an observer to toggle the "Play" button for showing a circular spinner and back
     * again in case audio playback is finished.
     */
    class AudioToggleObserver implements TTSAudioControl.AudioFinishedObserver {
        @Override
        public void hasFinished() {
            runOnUiThread(VoiceInfo.this::toggleSpeakButton);
        }
    }

    /**
     * Toggles the "Download button to show a horizontal progress bar and back again in case
     * download is finished.
     */
    class DownloadObserver implements DownloadVoiceManager.DownloadObserver {
        ProgressDialog mProgressDialog;
        DownloadObserver(ProgressDialog dialog) {
            mProgressDialog = dialog;
        }

        @Override
        public void hasFinished(boolean success) {
            runOnUiThread(VoiceInfo.this::toggleDownloadButton);
            mProgressDialog.dismiss();
        }
        @Override
        public void updateProgress(int progress) {
            runOnUiThread(() -> {
                mProgressDialog.setProgress(progress);
            });
        }
        @Override
        public void hasError(String error) {
            runOnUiThread(() -> {
                Toast.makeText(VoiceInfo.this, error, Toast.LENGTH_LONG).show();
                toggleDownloadButton();
                mProgressDialog.dismiss();
            });
        }
    }

    // speak_button is pressed
    public void onPlayClicked(View v) {
        Log.v(LOG_TAG, "onPlayClicked");
        String text = mUserText.getText().toString();
        AppRepository appRepo = App.getAppRepository();

        if (mVoice == null) {
            Log.v(LOG_TAG, "onPlayClicked: mVoice is null ?! Not playing anything");
            // TODO: this happens, e.g. if the activity is opened, but the async DB update destroys
            //       all relevant entries and repopulates the DB again. We should actually
            //       hinder this from happening by setting a mutex whenever any DB update must not
            //       be interrupted. Maybe we shoul change the approach completely and never update
            //       the DB after it has been populated once at app start. Then it would need to be
            //       restarted to get new voices. OR: we could only start the DB update if the
            //       Voice Manager is opened. This would be the best solution, I think. The voice
            //       manager could also get a refresh button. But we need to look out for each
            //       mVoice variable to be null-checked and invalidated if the DB is updated.
            //       Another solution is to never delete entries from the DB that haven't changed
            //       we should especially make sure that the currently used voice is not
            //       deleted from the DB.
            return;
        }

        // execute frontend, generate new TTS request
        CacheItem item = appRepo.getUtteranceCache().addUtterance(text);
        item = appRepo.executeFrontendAndSaveIntoCache(text, item);
        TTSRequest ttsRequest = new TTSRequest(item.getUuid());
        appRepo.setCurrentTTSRequest(ttsRequest);

        if (mVoice.type.equals(Voice.TYPE_TIRO)) {
            if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                appRepo.showTtsBackendWarningDialog(this);
                return;
            }
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
            toggleSpeakButton();
        } else if (mVoice.type.equals(Voice.TYPE_TORCH)) {
            toggleSpeakButton();
        } else {
            toggleSpeakButton();
        }
        Log.v(LOG_TAG, "Text to speak: " + item.getUtterance().getNormalized());
        mVoiceViewModel.startSpeaking(mVoice, item, 1.0f, 1.0f, new AudioToggleObserver());
    }

    // circle spinner is pressed
    public void onSpinnerClicked(View v) {
        Log.v(LOG_TAG, "onSpinnerClicked");
        mVoiceViewModel.stopSpeaking(mVoice);
        App.getAppRepository().setCurrentTTSRequest(null);
        toggleSpeakButton();
    }

    public void onDownloadClicked(View v) {
        Log.v(LOG_TAG, "onDownloadClicked");
        toggleDownloadButton();
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMax(100); // Progress Dialog Max Value
        mProgressDialog.setMessage(getResources().getString(R.string.do_download)); // Setting Message
        mProgressDialog.setTitle(mVoice.name); // Setting Title
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); // Progress Dialog Style Horizontal
        mProgressDialog.show(); // Display Progress Dialog
        mProgressDialog.setCancelable(true);
        mProgressDialog.setOnCancelListener(dialog -> {
            Log.v(LOG_TAG, "onDownloadClicked: download cancelled");
            App.getAppRepository().cancelDownloadVoice();
            toggleDownloadButton();
        });
        App.getAppRepository().downloadVoiceAsync(mVoice, new DownloadObserver(mProgressDialog));
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

    /**
     * Toggles Speak button to spinning if it was visible before, or the spinning wheel to the
     * play button back again otherwise.
     */
    private void toggleDownloadButton() {
        Button button = findViewById(R.id.download_button);
        if (button.getVisibility() == View.VISIBLE) {
            button.setVisibility(View.INVISIBLE);
        } else {

            if (! mVoice.needsDownload()) {
                // voice download finished, so we can start playing
                button.setVisibility(View.VISIBLE);
                button.setText(R.string.speak_voice_title);
                mUserText.setVisibility(View.VISIBLE);
            }
        }
    }
}
