package com.grammatek.simaromur;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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

        // create our instance of the view model
        ViewModelProvider.Factory factory =
                ViewModelProvider.AndroidViewModelFactory.getInstance(App.getApplication());
        mVoiceViewModel = new ViewModelProvider(this, factory).get(VoiceViewModel.class);

        // fill in the default user text
        mUserText = (EditText) findViewById(R.id.speakable_text);

        TextView nameTextView = (TextView) findViewById(R.id.textViewName);
        TextView langTextView = (TextView) findViewById(R.id.textViewLanguage);
        TextView genderTextView = (TextView) findViewById(R.id.textViewGender);

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
                if (mVoice.gender.toLowerCase().equals("Male".toLowerCase())) {
                    genderTextView.setText(getResources().getString(R.string.male));
                } else {
                    genderTextView.setText(getResources().getString(R.string.female));
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
        if (ConnectionCheck.isNetworkConnected()) {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
        }
        else {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
            App.getAppRepository().showTtsBackendWarningDialog(this);
        }
    }

    // speak_button is pressed
    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onClick");
        if (!(ConnectionCheck.isNetworkConnected() && ConnectionCheck.isTTSServiceReachable())) {
            mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_unavailable_solid);
            App.getAppRepository().showTtsBackendWarningDialog(this);
            return;
        }
        mNetworkAvailabilityIcon.setImageResource(R.drawable.ic_cloud_checked_solid);
        String text = mUserText.getText().toString();
        NormalizationManager normalizationManager = App.getApplication().getNormalizationManager();
        String normalizedText = normalizationManager.process(text);
        Log.v(LOG_TAG, "Text to speak: " + normalizedText);
        mVoiceViewModel.startSpeaking(mVoice, normalizedText, 1.0f, 1.0f);
    }
}
