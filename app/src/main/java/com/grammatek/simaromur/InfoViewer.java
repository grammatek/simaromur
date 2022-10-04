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
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;

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

    private void populateInformation() {
        // These 4 lists below, `cardTitle`, `cardText`, `cardUrl` and `cardSwitch`
        // need to be structured as following:
        // cardTitle:  emptyString when card contains url, otherwise string
        // cardText:   emptyString when section large section title, otherwise string
        // cardUrl:    emptyString if card doesn't contain url, otherwise a valid url (string)
        // cardSwitch: emptyString if card doesn't contain a switch, otherwise string
        //
        // For example if you want to create a card containing a title and text you still
        // need to add an emptyString to `cardUrl` and `cardSwitch`
        final List<String> cardTitle = new ArrayList<>() {
            {
                add(getString(R.string.app_name));
                add(getString(R.string.info_app_version));
                add(emptyString);
                add(emptyString);
                add("Device Information");
                add(getString(R.string.info_android_version));
                add(getString(R.string.info_phone_model));
                add("Other");
                add(emptyString);
                add(emptyString);
            }
        };
        final List<String> cardText = new ArrayList<>() {
            {
                add(emptyString);
                add(getAppVersion());
                add("github");
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
                add("https://www.grammatek.com");
                add(getString(R.string.info_privacy_notice_url));
            }
        };
        final List<String> cardSwitch = new ArrayList<>() {
            {
                add(emptyString);
                add(emptyString);
                add(emptyString);
                add("Bæta Símaróm");
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
                    if (aSwitch.length() > 0 ) {
                        view = this.getSwitchView(inflater, parent, position);
                    } else if (text == null || text.length() == 0) {
                        view = this.getTitleView(inflater, parent, position);
                    } else {
                        String url = this.url[position];
                        view = url.length() > 0 ? this.getClickableCardView(inflater, parent, position) : this.getCardView(inflater, parent, position);
                    }

                    cView = view;
                }
            }
            return cView;
        }

        private View getTitleView(LayoutInflater inflater, ViewGroup parent, int position) {
            View cView1 = inflater.inflate(R.layout.about_list_title, parent, false);
            TextView title = cView1.findViewById(R.id.title);
            title.setText(this.title[position]);
            return cView1;
        }

        private View getCardView(LayoutInflater inflater, ViewGroup parent, int position) {
            View cView1 = inflater.inflate(R.layout.about_list_item, parent, false);
            TextView infoDetail = cView1.findViewById(R.id.info_title);
            TextView infoType = cView1.findViewById(R.id.info_text);
            infoDetail.setText(this.title[position]);
            infoType.setText(this.text[position]);
            return cView1;
        }

        private View getClickableCardView(LayoutInflater inflater, ViewGroup parent, final int position) {
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

        // This is kind of hardcoded since it only allows the switch to work for crash analytics consent.
        // But is useful for dynamically adding to the listview
        private View getSwitchView(LayoutInflater inflater, ViewGroup parent, final int position) {
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
