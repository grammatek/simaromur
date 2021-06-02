package com.grammatek.simaromur;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_GENDER;
import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_ID;
import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_LANG;
import static com.grammatek.simaromur.VoiceManager.EXTRA_DATA_VOICE_NAME;

/**
 * This class displays an info screen for a voice.
 */
public class VoiceInfo  extends AppCompatActivity implements View.OnClickListener {
    private final static String LOG_TAG = "Simaromur_" + VoiceInfo.class.getSimpleName();

    private String mName;
    private String mLanguage;
    private String mGender;
    private long mVoiceId;
    private EditText mUserText;
    private com.grammatek.simaromur.db.Voice mVoice;
    private VoiceViewModel mVoiceViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_info);

        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mName = extras.getString(EXTRA_DATA_VOICE_NAME, "N/A");
            mLanguage = extras.getString(EXTRA_DATA_VOICE_LANG, "N/A");
            mGender = extras.getString(EXTRA_DATA_VOICE_GENDER, "N/A");
            mVoiceId = extras.getLong(EXTRA_DATA_VOICE_ID);
        }
        else {
            String errMsg = new String("No data has been passed to activity ?!");
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
        final Button button = findViewById(R.id.speak_button);
        button.setEnabled(false);
        button.setOnClickListener(this);

        // query DB for given voice id
        mVoiceViewModel.getAllVoices();
        mVoice = mVoiceViewModel.getVoiceWithId(mVoiceId);
        if (mVoice != null)
        {
            nameTextView.setText(mVoice.name);
            langTextView.setText(mVoice.languageName);
            genderTextView.setText(mVoice.gender);
        }
    }

    // Called, when speak_button is pressed
    @Override
    public void onClick(View v) {
        Log.v(LOG_TAG, "onCreate");
        String text = mUserText.getText().toString();
        Log.v(LOG_TAG, "Text to speak: " + text);
    }
}
