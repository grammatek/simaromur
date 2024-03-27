package com.grammatek.simaromur;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.grammatek.simaromur.db.NormDictEntry;
import com.grammatek.simaromur.db.Voice;

import java.util.List;

public class NormDictListAdapter extends RecyclerView.Adapter<NormDictListAdapter.NormDictViewHolder> {
    private final LayoutInflater mInflater;
    private List<NormDictEntry> mEntries; // Cached copy of norm dict entries
    private static NormDictListAdapter.ClickListener clickListener;

    public NormDictListAdapter(Context context) { mInflater = LayoutInflater.from(context); }

    static class NormDictViewHolder extends RecyclerView.ViewHolder {
        private final TextView termItemView;
        private final TextView replacementItemView;

        private NormDictViewHolder(View itemView) {
            super(itemView);
            termItemView = itemView.findViewById(R.id.textViewLeft);
            replacementItemView = itemView.findViewById(R.id.textViewRight);
            itemView.setOnClickListener(view -> clickListener.onItemClick(view, getAdapterPosition()));
        }
    }
    @NonNull
    @Override
    public NormDictViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.voice_manager_item, parent, false);
        return new NormDictListAdapter.NormDictViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(NormDictListAdapter.NormDictViewHolder holder, int position) {
        NormDictEntry current = mEntries.get(position);
        holder.termItemView.setText(current.term);
        holder.replacementItemView.setText(current.replacement);
    }

    // getItemCount() is called many times, and when it is first called,
    // mVoices has not been updated (means initially, it's null, and we can't return null).
    @Override
    public int getItemCount() {
        if (mEntries != null)
            return mEntries.size();
        else return 0;
    }

    void setEntries(List<NormDictEntry> entries){
        mEntries = entries;
        notifyDataSetChanged();
    }

    /**
     * Gets voice entry at given position.
     *
     * @param position Position of the voice in the View
     * @return Voice at the given position
     */
    public NormDictEntry getEntryAtPosition(int position) {
        return mEntries.get(position);
    }

    public interface ClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(NormDictListAdapter.ClickListener clickListener) {
        NormDictListAdapter.clickListener = clickListener;
    }
}
