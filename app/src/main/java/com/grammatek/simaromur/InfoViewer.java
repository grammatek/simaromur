package com.grammatek.simaromur;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

import com.grammatek.simaromur.cache.UtteranceCacheManager;
import com.grammatek.simaromur.db.AppData;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Activity shows information about the application.
 */
public class InfoViewer extends AppCompatActivity {
    private final static String LOG_TAG = "Simaromur_Java_" + InfoViewer.class.getSimpleName();
    private final String emptyString = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        // use our non-default layout
        setContentView(R.layout.activity_info);
        setTitle("Símarómur / " + getResources().getString(R.string.simaromur_info));
        populateInformation();
    }


    /**
     * This method contains 4 lists that are used to inflate a view with "card-like boxes" that
     * display information. There are 5 types of cards that can be created and they are done so
     * dynamically based on the information in these 4 lists. What card to inflate is dependant on
     * information from the 4 lists at given index.
     *
     *  These 4 lists below, `cardTitle`, `cardText`, `cardUrl` and `cardSwitch`
     *  need to be structured as following:
     *  cardTitle:  emptyString when card contains url, otherwise string
     *  cardText:   emptyString when large title instead of card, otherwise string
     *  cardUrl:    emptyString if card doesn't contain url, otherwise a valid url (string)
     *  cardSwitch: emptyString if card doesn't contain a switch, otherwise string
     *
     *  In addition, if all lists contain an emptyString for the same index, a cache card is
     *  created.
     *
     *  For example if you want to create a card containing a title and text you still
     *  need to add an emptyString to `cardUrl` and `cardSwitch`
     *
     * The Views that are created based on the list data.
     * {@link SettingsArrayAdapter#getTitleView(LayoutInflater, ViewGroup, int)}
     * {@link SettingsArrayAdapter#getBasicCardView(LayoutInflater, ViewGroup, int)}
     * {@link SettingsArrayAdapter#getUrlCardView(LayoutInflater, ViewGroup, int)}
     * {@link SettingsArrayAdapter#getCrashAnalyticConsentCardView(LayoutInflater, ViewGroup, int)}
     * {@link SettingsArrayAdapter#getBasicCardView(LayoutInflater, ViewGroup, int)}
     */
    private void populateInformation() {
        final List<String> cardTitle = new ArrayList<>() {
            {
                add(getString(R.string.app_name));
                add(getString(R.string.info_app_version));
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(getString(R.string.info_about_device_title));
                add(getString(R.string.info_android_version));
                add(getString(R.string.info_phone_model));
                add(getString(R.string.info_other));
                add(emptyString);
                add(emptyString);
            }
        };
        final List<String> cardText = new ArrayList<>() {
            {
                add(emptyString);
                add(getAppVersion());
                add(getString(R.string.wiki_github));
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(android.os.Build.VERSION.RELEASE);
                add(android.os.Build.MODEL);
                add(emptyString);
                add(getString(R.string.info_copyright));
                add(getString(R.string.info_privacy_notice));
            }
        };
        final List<String> cardUrl = new ArrayList<>() {
            {
                add(emptyString);
                add(emptyString);
                add(getString(R.string.info_repo_url));
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(getString(R.string.info_copyright_url));
                add(getString(R.string.info_privacy_notice_url));
            }
        };
        final List<String> cardSwitch = new ArrayList<>() {
            {
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(getString(R.string.crashlytics_title));
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add(emptyString);
            }
        };

        String[] titleArray = new String[cardTitle.size()];
        cardTitle.toArray(titleArray);
        String[] textArray = new String[cardText.size()];
        cardText.toArray(textArray);
        String[] urlArray = new String[cardUrl.size()];
        cardUrl.toArray(urlArray);
        String[] switchArray = new String[cardSwitch.size()];
        cardSwitch.toArray(switchArray);

        ListView infoView = findViewById(R.id.infoListView);
        infoView.setAdapter(new SettingsArrayAdapter(this, textArray, titleArray, urlArray, switchArray));
    }

    private String getAppVersion() {
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager()
                    .getPackageInfo(getApplicationContext().getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return getString(R.string.info_version_error);
        }
    }

    private static class SettingsArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] text;
        private final String[] title;
        private final String[] url;
        private final String[] aSwitch;

        public SettingsArrayAdapter(Context context, String[] text, String[] title, String[] url, String[] aSwitch) {
            super(context, R.layout.activity_info, text);
            this.context = context;
            this.text = text;
            this.title = title;
            this.url = url;
            this.aSwitch = aSwitch;
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NotNull ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View cView = convertView;

            if (inflater == null) {
                throw new NullPointerException("null cannot be cast to non-null type android.view.LayoutInflater");
            } else {
                if (convertView == null) {
                    View view;
                    CharSequence text = this.text[position];
                    CharSequence aSwitch = this.aSwitch[position];
                    String url = this.url[position];
                    CharSequence title = this.title[position];

                    // if everything is empty
                    if ((text.length() + aSwitch.length() + url.length() + title.length()) == 0) {
                        view = this.getCacheCardView(inflater, parent);
                    } else if (aSwitch.length() > 0 ) {
                        view = this.getCrashAnalyticConsentCardView(inflater, parent, position);
                    } else if (text.length() == 0) {
                        view = this.getTitleView(inflater, parent, position);
                    } else {
                        view = url.length() > 0
                                ? this.getUrlCardView(inflater, parent, position)
                                : this.getBasicCardView(inflater, parent, position);
                    }

                    cView = view;
                }
            }
            return cView;
        }

        private View getCacheCardView(LayoutInflater inflater, ViewGroup parent) {
            UtteranceCacheManager mCacheManager = App.getAppRepository().getUtteranceCache();

            View cView1 = inflater.inflate(R.layout.about_list_item_cache, parent, false);
            TextView title = cView1.findViewById(R.id.info_title);
            title.setText(R.string.cache);

            ImageView button = cView1.findViewById(R.id.trash_bin);
            button.setOnClickListener(it -> {
                mCacheManager.clearCache();
                if (context != null) {
                    Toast.makeText(context, R.string.cache_cleared, Toast.LENGTH_LONG).show();
                }
            });

            double cacheUsed = mCacheManager.getAudioFileSize() / (1024.0 * 1024.0);
            double cacheMax = mCacheManager.getCacheSizeHighWatermark() / (1024.0 * 1024.0);
            TextView text = cView1.findViewById(R.id.info_text);
            text.setText(context.getString(R.string.cache_size_mb, (int) cacheMax));

            int progress_percentage = (int) ((cacheUsed / cacheMax) * 100);
            TextView progress = cView1.findViewById(R.id.progress_bar_percentage);
            progress.setText(context.getString(R.string.cache_progress_percent, progress_percentage));

            ProgressBar progressBar = cView1.findViewById(R.id.cache_storage_bar);
            progressBar.setProgress(progress_percentage);

            return cView1;
        }

        private View getTitleView(LayoutInflater inflater, ViewGroup parent, int position) {
            View cView1 = inflater.inflate(R.layout.about_list_title, parent, false);
            TextView title = cView1.findViewById(R.id.title);
            title.setText(this.title[position]);
            return cView1;
        }

        private View getBasicCardView(LayoutInflater inflater, ViewGroup parent, int position) {
            View cView1 = inflater.inflate(R.layout.about_list_item, parent, false);
            TextView title = cView1.findViewById(R.id.info_title);
            TextView text = cView1.findViewById(R.id.info_text);
            title.setText(this.title[position]);
            text.setText(this.text[position]);
            return cView1;
        }

        private View getUrlCardView(LayoutInflater inflater, ViewGroup parent, final int position) {
            View cView1 = inflater.inflate(R.layout.about_list_item_clickable, parent, false);
            TextView title = cView1.findViewById(R.id.info_title);
            CardView card = cView1.findViewById(R.id.cardView);
            card.setOnClickListener((it -> {
                Uri uri = Uri.parse(SettingsArrayAdapter.this.url[position]);
                Intent intent = new Intent("android.intent.action.VIEW", uri);
                SettingsArrayAdapter.this.context.startActivity(intent);
            }));
            title.setText(this.text[position]);
            return cView1;
        }

        private View getCrashAnalyticConsentCardView(LayoutInflater inflater, ViewGroup parent, final int position) {
            View cView1 = inflater.inflate(R.layout.about_list_item_switch, parent, false);
            TextView title = cView1.findViewById(R.id.info_title);
            title.setText(SettingsArrayAdapter.this.aSwitch[position]);

            AppData app = App.getAppRepository().getCachedAppData();
            SwitchCompat sc = cView1.findViewById(R.id.switch_improve_simaromur);
            if (app != null) {
                // Sync switch with saved consent
                boolean consent = app.crashLyticsUserConsentGiven;
                sc.setChecked(consent);
            }

            sc.setOnClickListener(view -> {
                boolean isChecked = ((SwitchCompat) view).isChecked();
                App.getAppRepository().doGiveCrashLyticsUserConsent(isChecked);
            });
            return cView1;
        }

    }
}
