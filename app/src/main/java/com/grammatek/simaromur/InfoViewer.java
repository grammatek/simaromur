package com.grammatek.simaromur;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity shows information about the application.
 */
public class InfoViewer extends ListActivity {
    private final static String LOG_TAG = "Simaromur_Java_" + InfoViewer.class.getSimpleName();
    private NativeFliteTTS mFliteEngine;
    private float mBenchmark = -1;
    static final boolean mEnableBenchmark = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProgressDialog progress = new ProgressDialog(this);
        if (mEnableBenchmark) {
            progress.setMessage("Benchmarking Símarómur. Wait a few seconds");
            mFliteEngine = new NativeFliteTTS(this, null);
            mFliteEngine.setLanguage("eng", "USA","");
        }
        progress.setCancelable(false);
        new GetInformation(progress).execute();
    }

    private class GetInformation extends AsyncTask<Void, Void, Void> {

        private ProgressDialog progress;

        public GetInformation(ProgressDialog progress) {
            this.progress = progress;
        }

        @Override
        public void onPreExecute() {
            progress.show();
        }

        @Override
        public Void doInBackground(Void... arg0) {
            populateInformation();
            return null;
        }

        @Override
        public void onPostExecute(Void unused) {
            progress.dismiss();
        }
    }

    private void populateInformation() {
        if (mEnableBenchmark && mBenchmark < 0) {
            mBenchmark = mFliteEngine.getNativeBenchmark();
        }
        final List<String> Info = new ArrayList<String>() {
            {
                add(getString(R.string.info_app_version));
                add(getString(R.string.info_url));
                add(getString(R.string.info_copyright));
                add(getString(R.string.info_runtime_header));
                add(getString(R.string.info_android_version));
                add(getString(R.string.info_supported_abis));
                add(getString(R.string.info_phone_model));
            }
        };

        final List<String> Data = new ArrayList<String>() {
            {
            try {
                PackageInfo pInfo = getApplicationContext().getPackageManager()
                        .getPackageInfo(getApplicationContext().getPackageName(), 0);
                String version = pInfo.versionName;
                add(version);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                add(getString(R.string.info_version_error));
            }
            add(getString(R.string.info_repo_url));
            add(getString(R.string.info_about));
            add("");
            add(android.os.Build.VERSION.RELEASE);
            add(String.join(", ", android.os.Build.SUPPORTED_ABIS));
            add(android.os.Build.MODEL);
            }
        };

        if (mEnableBenchmark) {
            Info.add(getString(R.string.info_benchmark));
            Data.add("mBenchmark + \" " + getString(R.string.info_benchmark_value) + " \"");
        }

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String[] dataArray = new String[Data.size()];
                Data.toArray(dataArray);
                String[] infoArray = new String[Info.size()];
                Info.toArray(infoArray);
                setListAdapter(new SettingsArrayAdapter(InfoViewer.this,
                        infoArray, dataArray));
            }
        });

    }

    private class SettingsArrayAdapter extends ArrayAdapter<String> {
        private final Context context;
        private final String[] values;
        private final String[] data;

        public SettingsArrayAdapter(Context context, String[] values, String[] data) {
            super(context, R.layout.flite_info, values);
            this.context = context;
            this.values = values;
            this.data = data;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (values[position].equals(getString(R.string.info_runtime_header))) {
                return 0;
            }
            else return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (convertView == null) {
                convertView = inflater.inflate(R.layout.flite_info, parent, false);
            }

            TextView infoType = (TextView) convertView.findViewById(R.id.infotitle);
            TextView infoDetail = (TextView) convertView.findViewById(R.id.infodetail);

            if (values[position].equals(getString(R.string.info_runtime_header))) {
                infoType.setText(getString(R.string.info_runtime));
                infoType.setClickable(false);

                infoType.setTextColor(getResources().getColor(R.color.themeblue));
                infoType.setPadding(0,20,0,5);
                infoDetail.setVisibility(View.GONE);
            }
            else {
                infoType.setText(values[position]);
                infoDetail.setText(data[position]);
            }

            return convertView;
        }

    }

}
