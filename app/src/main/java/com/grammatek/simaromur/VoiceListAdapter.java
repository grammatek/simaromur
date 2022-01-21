package com.grammatek.simaromur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.grammatek.simaromur.db.Voice;

import java.util.List;

public class VoiceListAdapter extends RecyclerView.Adapter<VoiceListAdapter.VoiceViewHolder> {
    private final LayoutInflater mInflater;
    private List<Voice> mVoices; // Cached copy of voices
    private static ClickListener clickListener;

    class VoiceViewHolder extends RecyclerView.ViewHolder {
        private final TextView voiceNameItemView;
        private final TextView voiceTypeItemView;

        private VoiceViewHolder(View itemView) {
            super(itemView);
            voiceNameItemView = itemView.findViewById(R.id.textViewLeft);
            voiceTypeItemView = itemView.findViewById(R.id.textViewRight);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClick(view, getAdapterPosition());
                }
            });
        }
    }

    VoiceListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    @Override
    public VoiceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.voice_manager_item, parent, false);
        return new VoiceViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VoiceViewHolder holder, int position) {
        Voice current = mVoices.get(position);
        holder.voiceNameItemView.setText(current.name);
        if (current.type.equals("tiro")) {
            holder.voiceTypeItemView.setText(App.getContext().getResources().getString(R.string.type_network));
        } else {
            holder.voiceTypeItemView.setText(App.getContext().getResources().getString(R.string.type_local));
        }
    }

    void setVoices(List<com.grammatek.simaromur.db.Voice> voices){
        mVoices = voices;
        notifyDataSetChanged();
    }

    // getItemCount() is called many times, and when it is first called,
    // mVoices has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mVoices != null)
            return mVoices.size();
        else return 0;
    }

    /**
     * Gets voice entry at given position.
     *
     * @param position Position of the voice in the View
     * @return Voice at the given position
     */
    public Voice getVoiceAtPosition(int position) {
        return mVoices.get(position);
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        VoiceListAdapter.clickListener = clickListener;
    }
}
