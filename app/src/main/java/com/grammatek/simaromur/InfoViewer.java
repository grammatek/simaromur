package com.grammatek.simaromur;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import java.util.ArrayList;

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
        final ArrayList<String> Info = new ArrayList<String>() {
            {
                add("Copyright");
                add("URL");
                add("RUNTIME_HEADER");
                add("Android Version");
                add("Supported ABIs");
                add("Phone Model");
            }
        };

        final ArrayList<String> Data = new ArrayList<String>() {
            {
            add("© (2021) Grammatek ehf\nBased on previous work from Carnegie Mellon University © (1999-2012)");
            add("https://github.com/grammatek/simaromur");
            add("");
            add(android.os.Build.VERSION.RELEASE);
            add(String.join(", ", android.os.Build.SUPPORTED_ABIS));
            add(android.os.Build.MODEL);
            }
        };

        if (mEnableBenchmark) {
            Info.add("Benchmark");
            Data.add("mBenchmark + \" times faster than real time\"");
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
            if (values[position].equals("RUNTIME_HEADER")) {
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

            if (values[position].equals("RUNTIME_HEADER")) {
                infoType.setText("Runtime Information");
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
