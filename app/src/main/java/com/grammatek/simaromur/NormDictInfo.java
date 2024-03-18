package com.grammatek.simaromur;

import static com.grammatek.simaromur.NormDictListView.EXTRA_DATA_DICT_ID;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.grammatek.simaromur.cache.CacheItem;
import com.grammatek.simaromur.db.NormDictEntry;
import com.grammatek.simaromur.device.TTSAudioControl;

import java.util.concurrent.atomic.AtomicBoolean;

public class NormDictInfo extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_" + NormDictInfo.class.getSimpleName();
    private int mEntryId;
    NormDictEntry mEntry;
    private EditText mTermText;
    private EditText mReplacementText;
    private boolean mTermTextEmpty = true;
    private boolean mReplacementTextEmpty = true;
    private FloatingActionButton mDeleteButton;
    private ImageButton mPlayButton1;
    private ImageButton mPlayButton2;
    private FloatingActionButton mSaveButton;
    private NormDictViewModel mViewModel;
    boolean mIsPlaying1 = false;
    boolean mIsPlaying2 = false;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_norm_dict_info);

        // create our instance of the view model
        ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
        mViewModel = new ViewModelProvider(this, factory).get(NormDictViewModel.class);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mEntryId = extras.getInt(EXTRA_DATA_DICT_ID);
            // query DB for given voice id. On completion set appropriate UI attributes
            // TODO XXX DS: is this necessary? The observer in NormDictListView already does this and
            //      we don't expect that this entry can change after it has been selected or is this
            //      a lazy async loading thing ?
            mViewModel.getEntries().observe(this, entries -> {
                Log.v(LOG_TAG, "onCreate::mViewModel.getEntries().observe - entries size: " + entries.size());
                mEntry = mViewModel.getEntryWithId(mEntryId);
                if (mEntry != null) {
                    mTermText.setText(mEntry.term);
                    mReplacementText.setText(mEntry.replacement);
                    // update the UI according to the entry
                    updateUi();
                }
            });
        }
        else {
            String msg = "No entry id provided, creating a new entry";
            Log.i(LOG_TAG, msg);
            mEntry = new NormDictEntry();
        }
        // layout doesn't work for landscape
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find all elements in the layout
        mTermText = findViewById(R.id.term_text);
        mReplacementText = findViewById(R.id.replacement_text);
        mDeleteButton = findViewById(R.id.delete_button_norm_dict);
        mPlayButton1 = findViewById(R.id.playButton1);
        mPlayButton2 = findViewById(R.id.playButton2);
        mSaveButton = findViewById(R.id.save_button);

        // set play button states
        mPlayButton1.setImageState(new int[]{R.attr.state_stopped}, true);
        mPlayButton2.setImageState(new int[]{R.attr.state_stopped}, true);

        // set callbacks for the buttons
        mDeleteButton.setOnClickListener(this::onDeleteClicked);
        mPlayButton1.setOnClickListener(this::onPlayCancelClicked);
        mPlayButton2.setOnClickListener(this::onPlayCancelClicked);
        mSaveButton.setOnClickListener(this::onSaveClicked);
        mTermText.addTextChangedListener(new EmptyTextWatcher() {
            @Override public void onEmptyField() { mTermTextEmpty = true; updateUi(); }
            @Override public void onFilledField() { mTermTextEmpty = false; updateUi(); }
        });
        mReplacementText.addTextChangedListener(new EmptyTextWatcher() {
            @Override public void onEmptyField() { mReplacementTextEmpty = true; updateUi(); }
            @Override public void onFilledField() { mReplacementTextEmpty = false; updateUi(); }
        });

        // update the rest of the UI
        updateUi();
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
        Log.v(LOG_TAG, "updateUi:");

        String termText = mTermText.getText().toString();
        String replacementText = mReplacementText.getText().toString();
        boolean saveEnabled = !termText.isEmpty() && !replacementText.isEmpty();
        if (saveEnabled && mEntry != null) {
            saveEnabled = !termText.equals(mEntry.term) || !replacementText.equals(mEntry.replacement);
        }
        // if the texts shown in mTermText and mReplacementText are not empty, then the play buttons
        // should be enabled, otherwise they should be disabled
        boolean playButton1Enabled = !mIsPlaying1 && !mTermTextEmpty;
        boolean playButton2Enabled = !mIsPlaying2 && !mReplacementTextEmpty;
        int playButton1Alpha = playButton1Enabled ? 0xFF : 0x3F;
        int playButton2Alpha = playButton2Enabled ? 0xFF : 0x3F;
        int saveButtonVisibility = saveEnabled ? View.VISIBLE : View.GONE;
        int deleteButtonVisibility = mEntryId > 0 ? View.VISIBLE : View.GONE;
        mPlayButton1.setEnabled(playButton1Enabled);
        mPlayButton1.setImageAlpha(playButton1Alpha);
        mPlayButton2.setEnabled(playButton2Enabled);
        mPlayButton2.setImageAlpha(playButton2Alpha);

        mSaveButton.setVisibility(saveButtonVisibility);
        mDeleteButton.setVisibility(deleteButtonVisibility);

        setTitle("Símarómur / " + getResources().getString(R.string.simaromur_norm_dictionary));
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy:");
        super.onDestroy();
        AppRepository appRepo = App.getAppRepository();
        mViewModel.stopSpeaking(appRepo.getCurrentVoice());
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume:");
        super.onResume();
        updateText();
        updateUi();
    }

    private void onDeleteClicked(View v) {
        Log.v(LOG_TAG, "onDeleteClicked");
        AtomicBoolean doCancel = new AtomicBoolean(true);
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.dict_entry_deletion_title)
                .setMessage(R.string.dict_entry_deletion_q)
                .setPositiveButton(R.string.doit, (dialog, id) -> {
                        doCancel.set(false);
                        Log.v(LOG_TAG, "onDeleteClicked: deleting entry " + mEntry.term);
                        mViewModel.delete(mEntry);
                        finish();
                    })
                .setNegativeButton(R.string.not_yet, (dialog, id) -> {
                    Log.v(LOG_TAG, "onDeleteClicked: canceled");
                    })
                .setCancelable(false)
                .create();
        d.show();

    }

    /**
     * Updates the text fields in the entry with the current text in the text fields. This
     * happens only, if the text fields have been updated.
     */
    private void updateText() {
        if (mEntry != null) {
            if (anyTextUpdated()) {
                mEntry.term = mTermText.getText().toString();
                mEntry.replacement = mReplacementText.getText().toString();
            }
        }
    }

    /**
     * Checks if any of the text fields have been updated
     *
     * @return true if any of the text fields have been updated, false otherwise
     */
    private boolean anyTextUpdated() {
        return !mTermText.getText().toString().equals(mEntry.term) ||
                !mReplacementText.getText().toString().equals(mEntry.replacement);
    }

    private void onSaveClicked(View v) {
        Log.v(LOG_TAG, "onSaveClicked");
        updateText();
        mViewModel.createOrUpdate(mEntry);
        finish();
    }

    // one of the speak buttons is pressed
    public void onPlayCancelClicked(View v) {
        Log.v(LOG_TAG, "onPlayCancelClicked");
        AppRepository appRepo = App.getAppRepository();
        if (appRepo.getCurrentVoice() == null) {
            Log.v(LOG_TAG, "onPlayCancelClicked: AppRepository.getCurrentVoice() is null ?! Not playing anything");
            return;
        }

        String text = "";

        // get text from view v which is a text field and disable the other button
        if (v.getId() == R.id.playButton1) {
            if (mIsPlaying1) {
                mPlayButton2.setEnabled(true);
                mPlayButton2.setImageAlpha(0xFF);
                mIsPlaying1 = false;
            } else {
                text = mTermText.getText().toString();
                mPlayButton2.setEnabled(false);
                mPlayButton2.setImageAlpha(0x3F);
                mIsPlaying1 = true;
            }
        } else if (v.getId() == R.id.playButton2) {
            if (mIsPlaying2) {
                mPlayButton1.setEnabled(true);
                mPlayButton1.setImageAlpha(0xFF);
                mIsPlaying2 = false;
            } else {
                text = mReplacementText.getText().toString();
                mPlayButton1.setEnabled(false);
                mPlayButton1.setImageAlpha(0x3F);
                mIsPlaying2 = true;
            }
        } else {
            Log.e(LOG_TAG, "onPlayCancelClicked: unknown view id: " + v.getId());
            return;
        }
        toggleSpeakButton(v);

        // execute frontend
        // TODO: we should do this in a separate thread
        // TODO don't cache ! We always want to speak the text as it is
        // TODO: do we need a special mode for the frontend to bypass any user dictionary ?
        if (mIsPlaying1 || mIsPlaying2) {
            CacheItem item = appRepo.getUtteranceCache().addUtterance(text);
            item = appRepo.executeFrontendAndSaveIntoCache(text, item, appRepo.getCurrentVoice(), true);
            if ((item.getUtterance().getPhonemesCount() == 0) ||
                    item.getUtterance().getPhonemesList().get(0).getSymbols().isEmpty()) {
                Log.w(LOG_TAG, "onPlayCancelClicked: Nothing to speak ?!");
                appRepo.getUtteranceCache().deleteCacheItem(item.getUuid());
                // reset the playing states
                mIsPlaying1 = false;
                mIsPlaying2 = false;
                // update UI
                toggleSpeakButton(v);
                updateUi();
                return;
            }
            // create TTS request
            TTSRequest ttsRequest = new TTSRequest(item.getUuid());
            appRepo.setCurrentTTSRequest(ttsRequest);

            Log.v(LOG_TAG, "Text to speak: " + item.getUtterance().getNormalized());
            mViewModel.startSpeaking(appRepo.getCurrentVoice(), item, 1.0f, 1.0f,
                    new NormDictInfo.AudioToggleObserver());
        } else {
            mViewModel.stopSpeaking(App.getAppRepository().getCurrentVoice());
        }
    }

    /**
     * Toggles Speak button to spinning if it was visible before, or the spinning wheel to the
     * play button back again otherwise.
     */
    private void toggleSpeakButton(View v) {
        boolean isPlaying = mIsPlaying1 || mIsPlaying2;
        ImageButton button = (ImageButton) v;
        if (isPlaying) {
            button.setImageState(new int[]{R.attr.state_playing}, true);
        } else {
            button.setImageState(new int[]{R.attr.state_stopped}, true);
        }
    }

    private void enableSpeakButtons() {
        mPlayButton1.setImageState(new int[]{R.attr.state_stopped}, true);
        mPlayButton2.setImageState(new int[]{R.attr.state_stopped}, true);
        mIsPlaying1 = false;
        mIsPlaying2 = false;
        updateUi();
    }

    /**
     * Implements an observer to toggle the "Play" button for showing a circular spinner and back
     * again in case audio playback is finished.
     */
    class AudioToggleObserver implements TTSAudioControl.AudioFinishedObserver {
        @Override
        public void hasFinished() {
            runOnUiThread(NormDictInfo.this::enableSpeakButtons);
        }
    }

    public abstract static class EmptyTextWatcher implements TextWatcher
    {
        private final static String LOG_TAG = "Simaromur_" + EmptyTextWatcher.class.getSimpleName();
        public abstract void onEmptyField();

        public abstract void onFilledField();

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            Log.v(LOG_TAG, "onTextChanged");
            if (s.toString().trim().length() == 0)
            {
                Log.v(LOG_TAG, "onEmptyField");
                onEmptyField();
            } else
            {
                Log.v(LOG_TAG, "onFilledField");
                onFilledField();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void afterTextChanged(Editable s)
        {
        }

    }


}
