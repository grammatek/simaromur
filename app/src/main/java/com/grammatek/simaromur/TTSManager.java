package com.grammatek.simaromur;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class TTSManager extends Activity implements OnItemClickListener, TextToSpeech.OnInitListener {
    private final static String LOG_TAG = "Simaromur_Java_" + TTSManager.class.getSimpleName();
    private TextToSpeech mTtsClient;        // for querying TTS engine info
    private AlertDialog mWarningDialog;
    private final static int MY_DATA_CHECK_CODE = 1;

    static final LauncherIcon[] ICONS = {
        // @todo: we disable the FLite based choosers, we will replace local voice management
        //        completely
        // new LauncherIcon(R.drawable.custom_dialog_tts, "TTS Demo", TTSDemo.class),
        // new LauncherIcon(R.drawable.custom_dialog_manage, "FLite Voices", DownloadVoiceData.class),
        new LauncherIcon(R.drawable.simaromur_large, R.string.simaromur_voice_manager, VoiceManager.class),
        new LauncherIcon(R.drawable.custom_info_large, R.string.simaromur_info, InfoViewer.class),
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flitemanager);

        GridView gridview = (GridView) findViewById(R.id.dashboard_grid);
        gridview.setAdapter(new ImageAdapter(this));
        gridview.setOnItemClickListener(this);

        // Hack to disable GridView scrolling
        gridview.setOnTouchListener(new GridView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_MOVE;
            }
        });

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
    }

    /**
     * Method to fulfill TextToSpeech.OnInitListener interface.
     *
     * @param status status of TTS engine
     */
    @Override
    public void onInit(final int status) {
        Log.v(LOG_TAG, "onInit: status: " + status);
    }

    /**
     * Checks if our service is the TTS default service
     */
    private void checkDefaultEngine() {
        Log.v(LOG_TAG, "checkDefaultEngine()");
        if (mTtsClient == null) {
            Log.v(LOG_TAG, "No TTS connection ?!");
            return;
        }
        try {
            final String initEngine = mTtsClient.getDefaultEngine();
            if (initEngine != null) {
                Log.i(LOG_TAG, "Default engine: " + initEngine);
                // if Símarómur is not the default engine: engage user to change that
                if (!initEngine.equals(getApplicationContext().getPackageName())) {
                    showTtsEngineWarningDialog();
                }
            }
        } catch (final Exception e) {
            Log.e(LOG_TAG, "TTS engine default error" + e.getMessage());
        }
    }

    /**
     * Shows TTS Engine warning and refers user to Android TTS service settings.
     */
    private void showTtsEngineWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder
            .setMessage(R.string.tts_settings)
            .setTitle(R.string.choose_tts_engine)
            .setCancelable(false)
            .setPositiveButton(R.string.doit, (dialog, id) -> {
                openTtsSettings();
            })
            .setNegativeButton(R.string.not_yet, (dialog, id) -> {
                // nothing
            });
        if (mWarningDialog != null) {
            mWarningDialog.cancel();
            mWarningDialog = null;
        }
        mWarningDialog = builder.create();
        mWarningDialog.show();
    }

    @Override
    public void onResume() {
        Log.v(LOG_TAG, "onResume:");
        super.onResume();
        checkDefaultEngine();
    }

    @Override
    public void onStop() {
        Log.v(LOG_TAG, "onStop:");
        super.onStop();
        if (mWarningDialog != null) {
            mWarningDialog.cancel();
            mWarningDialog = null;
        }
    }

    @Override
    public void onDestroy() {
        Log.v(LOG_TAG, "onDestroy:");
        super.onDestroy();
        if (mTtsClient != null) {
            mTtsClient.shutdown();
            mTtsClient = null;
        }
    }

    // Open TTS engine preferences
    private void openTtsSettings() {
        Intent intent = new Intent();
        intent.setAction("com.android.settings.TTS_SETTINGS");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /**
     * This method is called, when the TTS system is bound to this activity. Before, onResume()
     * can be called.
     *
     * @param requestCode   The user-defined number provided when starting the activity
     * @param resultCode    Result code / outcome of the activity
     * @param data          optional data from the intent
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(LOG_TAG, "onActivityResult: resultCode: " + resultCode);
        if (requestCode == MY_DATA_CHECK_CODE) {
            // Create the TTS instance, this also pulls up our TTSService. Anywhere we use
            // this object, we must always check beforehand mTtsClient != null.
            mTtsClient = new TextToSpeech(this, this,
                    getApplicationContext().getPackageName());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        Intent intent = new Intent(this, ICONS[position].activity);
        startActivity(intent);
    }

    static class LauncherIcon {
        //final String text;
        final int textId;
        final int imgId;
        final Class activity;
        
        public LauncherIcon(int imgId, int textId, Class activity) {
            super();
            this.imgId = imgId;
            this.textId = textId;
            this.activity = activity;
        }
    }

    static class ImageAdapter extends BaseAdapter {
        private Context mContext;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        @Override
        public int getCount() {
            return ICONS.length;
        }

        @Override
        public LauncherIcon getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        static class ViewHolder {
            public ImageView icon;
            public TextView text;
        }

        // Create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ViewHolder holder;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);

                v = vi.inflate(R.layout.dashboard_icon, null);
                holder = new ViewHolder();
                holder.text = (TextView) v.findViewById(R.id.dashboard_icon_text);
                holder.icon = (ImageView) v.findViewById(R.id.dashboard_icon_img);
                v.setTag(holder);
            } else {
                holder = (ViewHolder) v.getTag();
            }
            String text = mContext.getString(ICONS[position].textId);
            holder.icon.setImageResource(ICONS[position].imgId);
            holder.text.setText(text);

            return v;
        }

    }
}
