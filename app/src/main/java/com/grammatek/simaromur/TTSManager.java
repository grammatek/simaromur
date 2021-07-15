package com.grammatek.simaromur;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

public class TTSManager extends Activity implements OnItemClickListener {
    private final static String LOG_TAG = "Simaromur_Java_" + TTSManager.class.getSimpleName();

    static final LauncherIcon[] ICONS = {
        // @todo: we disable the FLite based choosers, we will replace local voice management
        // completely
        // new LauncherIcon(R.drawable.custom_dialog_tts, "TTS Demo", TTSDemo.class),
        // new LauncherIcon(R.drawable.custom_dialog_manage, "FLite Voices", DownloadVoiceData.class),
        new LauncherIcon(R.drawable.ic_launcher_app_logo, "SIM Voices", VoiceManager.class),
        new LauncherIcon(R.drawable.custom_dialog_info, "About Símarómur", InfoViewer.class),
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
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

        Intent intent = new Intent(this, ICONS[position].activity);
        startActivity(intent);

    }

    static class LauncherIcon {
        final String text;
        final int imgId;
        final Class activity;

        public LauncherIcon(int imgId, String text, Class activity) {
            super();
            this.imgId = imgId;
            this.text = text;
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

            holder.icon.setImageResource(ICONS[position].imgId);
            holder.text.setText(ICONS[position].text);

            return v;
        }

    }
}
