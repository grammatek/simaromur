package com.grammatek.simaromur;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.grammatek.simaromur.db.Voice;
import com.grammatek.simaromur.frontend.NormalizationManager;
import com.grammatek.simaromur.network.ConnectionCheck;

import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_ID;

/**
 * This class displays an info screen for a voice.
 */
public class VoiceInfo  extends AppCompatActivity implements View.OnClickListener {
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

        // setup button
        Button mButton = findViewById(R.id.speak_button);
        mButton.setEnabled(true);
        mButton.setOnClickListener(this);

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
                if (mVoice.type.equalsIgnoreCase("tiro")) {
                    typeTextView.setText(getResources().getString(R.string.type_network));
                } else {
                    typeTextView.setText(getResources().getString(R.string.type_local));
                }
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
        if (mVoice.type.equals(Voice.TYPE_TIRO)) {
            if (ConnectionCheck.isNetworkConnected()) {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
            }
            else {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                App.getAppRepository().showTtsBackendWarningDialog(this);
            }
        } else if (mVoice.type.equals(Voice.TYPE_TORCH)) {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_send);
        }
    }

    // speak_button is pressed
    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onClick");
        String normalizedText;
        String text = mUserText.getText().toString();
        if (mVoice.type.equals(Voice.TYPE_TIRO)) {
            if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
                mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
                App.getAppRepository().showTtsBackendWarningDialog(this);
                return;
            }
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
            NormalizationManager normalizationManager = App.getApplication().getNormalizationManager();
            normalizedText = normalizationManager.process(text);
        } else if (mVoice.type.equals(Voice.TYPE_TORCH)) {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_action_send);
            // normalization is done in the Engine itself
            normalizedText = text;
        } else {
            Log.w(LOG_TAG, "Selected voice type " + mVoice.type + " not yet supported !");
            return;
        }
        Log.v(LOG_TAG, "Text to speak: " + normalizedText);
        mVoiceViewModel.startSpeaking(mVoice, normalizedText, 1.0f, 1.0f);
    }
}
