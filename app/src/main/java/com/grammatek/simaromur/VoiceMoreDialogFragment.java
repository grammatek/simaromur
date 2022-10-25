package com.grammatek.simaromur;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;

public class VoiceMoreDialogFragment extends DialogFragment {
    private final static String LOG_TAG = "Simaromur_" + VoiceMoreDialogFragment.class.getSimpleName();
    // the listener is passed via onAttach()
    NoticeDialogListener m_listener;
    int m_itemId = -1;
    Button m_positiveButton;
    // TODO: preparation for updatable voices
    boolean m_isUpdateAvailable = false;

    VoiceMoreDialogFragment(boolean isUpdateAvailable) {
        m_isUpdateAvailable = isUpdateAvailable;
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog, int itemId);
        public void onDialogNegativeClick(DialogFragment dialog);
    }


    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            m_listener = (NoticeDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(LOG_TAG + ": Activity must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int checkedItem = -1;
        int arrayId = m_isUpdateAvailable ? R.array.voice_more_options_array : R.array.voice_more_options_wo_update;
        builder.setTitle(R.string.voice_more_title)
                .setCancelable(false)
                .setSingleChoiceItems(arrayId, checkedItem, (dialog, which) -> {
                    // 'which' contains the index position of selected item
                    Log.v(LOG_TAG, "which: " + which);
                    switch (which) {
                        case 0:     // FALLTHROUGH
                        case 1:
                            m_itemId = which;
                            m_positiveButton.setEnabled(true);
                            break;
                        default:
                            Log.w(LOG_TAG, "Unknown option: " + which);
                            break;
                    }
                })
                .setPositiveButton(R.string.ok, (dialog, id) -> {
                    // Send the positive button event back to the host activity
                    m_listener.onDialogPositiveClick(VoiceMoreDialogFragment.this, m_itemId);
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> {
                    // Ok, user cancelled the dialog
                    m_listener.onDialogNegativeClick(VoiceMoreDialogFragment.this);
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();
        AlertDialog d = (AlertDialog) getDialog();
        assert d != null;
        m_positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
        m_positiveButton.setEnabled(false);

        ListView listView = d.getListView();
        Log.v(LOG_TAG, "dialog adapter: " + listView.getAdapter());

        final ListAdapter items = d.getListView().getAdapter();
        if (items.getCount() != 0) {
            Log.w(LOG_TAG, "Items: " + items.getCount());
        } else {
            Log.w(LOG_TAG, "No items in list");
        }
    }

}
