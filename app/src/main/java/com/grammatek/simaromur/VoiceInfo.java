package com.grammatek.simaromur;

import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_ID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.device.DownloadVoiceManager;
import com.grammatek.simaromur.device.TTSAudioControl;
import com.grammatek.simaromur.network.ConnectionCheck;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class displays an info screen for a voice.
 */
public class VoiceInfo extends AppCompatActivity
        implements VoiceMoreDialogFragment.NoticeDialogListener {
    private final static String LOG_TAG = "Simaromur_" + VoiceInfo.class.getSimpleName();

    private long mVoiceId;
    private EditText mUserText;
    private com.grammatek.simaromur.db.Voice mVoice;
    private VoiceViewModel mVoiceViewModel;
    private ImageView mNetworkAvailabilityIcon;
    private ProgressBar mProgressBar;
    private TextView mProgressBarPercentage;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_info);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBarPercentage = findViewById(R.id.pbPercentage);

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
                (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
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
            Log.v(LOG_TAG, "onCreate::mVoiceViewModel.getAllVoices().observe - voices size: " + voices.size());
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
        if (hasFocus) {
            updateUi();
        }
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

        // more options (3 dots ...)
        CardView moreOptions = findViewById(R.id.moreOptionsBackground);
        moreOptions.setOnClickListener(view -> {
            // TODO: update not yet implemented
            boolean isUpdateAvailable = false;
            DialogFragment newFragment = new VoiceMoreDialogFragment(isUpdateAvailable);
            newFragment.show(getSupportFragmentManager(), "update voice");
        });

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
                Log.v(LOG_TAG, "updateUi: voice needs network");
                typeTextView.setText(getResources().getString(R.string.type_network));
                speakableTextView.setText(getResources().getString(R.string.voice_test_text_network));
                moreOptions.setVisibility(View.GONE);
                moreOptions.setEnabled(false);
                // change voice availability icon according to network availability and type
                if (mVoice.needsNetwork()) {
                    if (ConnectionCheck.isNetworkConnected()) {
                        mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
                    } else {
                        mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
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
                mNetworkAvailabilityIcon.setColorFilter(App.getContext().getResources()
                        .getColor(android.R.color.holo_orange_light, null));
                // set download button
                downloadButton.setText(getResources().getString(R.string.do_download));
                downloadButton.setEnabled(true);
                downloadButton.setVisibility(View.VISIBLE);
                moreOptions.setVisibility(View.GONE);
                moreOptions.setEnabled(false);
            } else {
                Log.v(LOG_TAG, "updateUi: voice is on device");
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_download);
                mNetworkAvailabilityIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_light, null));
                typeTextView.setText(getResources().getString(R.string.type_local));
                if (mVoice.isFast()) {
                    moreOptions.setVisibility(View.VISIBLE);
                    moreOptions.setEnabled(true);
                    speakableTextView.setText(getResources().getString(R.string.voice_test_text_on_device_fast));
                } else {
                    moreOptions.setVisibility(View.INVISIBLE);
                    moreOptions.setEnabled(false);
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
        App.getAppRepository().cancelDownloadVoice();
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume:");
        super.onResume();
        updateUi();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, int itemId) {
        Log.v(LOG_TAG, "onDialogPositiveClick, index:" + itemId);
        // user touched the dialog's positive button
        onDeleteClicked();
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Log.v(LOG_TAG, "onDialogNegativeClick:");
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
     * Implements an observer for updating UI when voice is downloaded
     */
    class DownloadObserver implements DownloadVoiceManager.Observer {
        ProgressBar mProgressBar;
        DownloadObserver(ProgressBar progressBar) {
            mProgressBar = progressBar;
            mProgressBar.setProgress(0);
            mProgressBarPercentage.setText("0%");
        }

        @Override
        public void hasFinished(boolean success) {

            // hide from the UI whether the download is a success or not.
            findViewById(R.id.ccProgressBar).setVisibility(View.GONE);
            if (!success) {
                runOnUiThread(() -> {
                    mProgressBar.setProgress(0);
                    mProgressBarPercentage.setText("0%");
                    findViewById(R.id.llProgressBar).setVisibility(View.GONE);
                });
            }
        }
        @Override
        public void updateProgress(int progress) {
                runOnUiThread(() -> {
                    mProgressBar.setProgress(progress);
                    String progress_in_percentage = progress + "%";
                    mProgressBarPercentage.setText(progress_in_percentage);
                });
                if (progress == 100) {
                    Log.d(LOG_TAG, "Finished downloading file.. unzipping..");
                    runOnUiThread(() -> {
                        // swap view to indeterminate progress bar to allow the voice to unzip peacefully
                        findViewById(R.id.llProgressBar).setVisibility(View.GONE);
                        // spinner needs to have its text updated, as it is used for different purposes
                        final View mSpinner = findViewById(R.id.ccProgressBar);
                        final TextView spinnerText = mSpinner.findViewById(R.id.pbText);
                        spinnerText.setText(R.string.unzipping_voice);
                        mSpinner.setVisibility(View.VISIBLE);
                    });
            }
        }
        @Override
        public void hasError(String error) {
            // Tell user something went wrong unless it's from him cancelling the download.
            if (!error.matches("(?i).*cancel.*")) {
                runOnUiThread(() -> {
                    findViewById(R.id.llProgressBar).setVisibility(View.GONE);
                    AlertDialog.Builder b = new AlertDialog.Builder(VoiceInfo.this);
                    b.setMessage(R.string.sth_failed_try_again);
                    b.setCancelable(true);
                    b.create().show();
                });
            }
        }
    }

    /**
     * Implements an observer for updating UI when voice is deleted
     */
    public class DeleteVoiceObserver implements DownloadVoiceManager.Observer {
        final static String LOG_TAG = "DeleteVoiceObserver";
        final View mSpinner = findViewById(R.id.ccProgressBar);
        DeleteVoiceObserver() {
            runOnUiThread(() -> {
                final TextView spinnerText = mSpinner.findViewById(R.id.pbText);
                spinnerText.setText(R.string.voice_deletion_title);
            });
        }

        @Override
        public void hasFinished(boolean success) {
            runOnUiThread(() -> mSpinner.setVisibility(View.GONE));
        }
        @Override
        public void updateProgress(int progress) {
            Log.d(LOG_TAG, "updateProgress: " + progress);
            if (progress == 0) {
                Log.d(LOG_TAG, "updateProgress: show Spinner");
                runOnUiThread(() -> mSpinner.setVisibility(View.VISIBLE));
            }
        }
        @Override
        public void hasError(String error) {
            Log.e(LOG_TAG, "hasError: " + error);
            // Tell user something went wrong unless it's from him cancelling the download.
            if (!error.matches("(?i).*cancel.*")) {
                runOnUiThread(() -> {
                    mSpinner.setVisibility(View.GONE);
                    AlertDialog.Builder b = new AlertDialog.Builder(VoiceInfo.this);
                    b.setMessage(R.string.sth_failed_try_again);
                    b.setCancelable(true);
                    b.create().show();
                });
            }
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

        if (mVoice.type.equals(Voice.TYPE_NETWORK)) {
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

    public void onDeleteClicked() {
        Log.v(LOG_TAG, "onDeleteClicked");

        AtomicBoolean doCancel = new AtomicBoolean(true);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.voice_deletion_title)
                .setMessage(R.string.voice_deletion_q)
                .setPositiveButton(R.string.doit, (dialog, id) -> {doCancel.set(false);
                    if (App.getAppRepository().isCurrentVoice(mVoice)) {
                        Log.v(LOG_TAG, "onDeleteClicked: voice cannot be deleted ");
                        AlertDialog.Builder b = new AlertDialog.Builder(VoiceInfo.this)
                                .setMessage(R.string.voice_deletion_not_possible)
                                .setNegativeButton(R.string.ok, (aDialog, anId) -> doCancel.set(true))
                                .setCancelable(true);
                        b.create().show();
                    } else {
                        Log.v(LOG_TAG, "onDeleteClicked: deleting voice " + mVoice.name);
                        App.getAppRepository().deleteVoiceAsync(mVoice, new DeleteVoiceObserver());
                    }})
                .setNegativeButton(R.string.not_yet, (dialog, id) -> Log.v(LOG_TAG, "onDeleteClicked: canceled"))
                .setCancelable(false)
                .create();
        d.show();
    }

    public void onDownloadClicked(View v) {
        Log.v(LOG_TAG, "onDownloadClicked");
        // check internet connectivity
        if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
            App.getAppRepository().showTtsBackendWarningDialog(this);
            return;
        }
        toggleDownloadButton();

        findViewById(R.id.llProgressBar).setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(view -> {
            Log.v(LOG_TAG, "onDownloadClicked: download cancelled");
            App.getAppRepository().cancelDownloadVoice();
            findViewById(R.id.download_button).setVisibility(View.VISIBLE);
            findViewById(R.id.llProgressBar).setVisibility(View.GONE);
        });
        App.getAppRepository().downloadVoiceAsync(mVoice, new DownloadObserver(mProgressBar));
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
     * Toggles the download button off if it was visible, if it's invisible and the voice is
     * downloaded the button text is made visible again and it's text changed to "play"
     */
    private void toggleDownloadButton() {
        Log.d(LOG_TAG, "toggling");
        Button button = findViewById(R.id.download_button);
        if (button.getVisibility() == View.VISIBLE) {
            if(ConnectionCheck.isNetworkConnected()) {
                button.setVisibility(View.INVISIBLE);
            } else {
                App.getAppRepository().showTtsBackendWarningDialog(this);
            }
        } else if (! mVoice.needsDownload()) {
            // voice download finished, so we can start playing
            button.setVisibility(View.VISIBLE);
            button.setText(R.string.speak_voice_title);
            mUserText.setVisibility(View.VISIBLE);
        }
    }
}
