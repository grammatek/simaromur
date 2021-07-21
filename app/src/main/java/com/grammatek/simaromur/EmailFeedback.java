package com.grammatek.simaromur;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class EmailFeedback extends Activity {
    private final static String LOG_TAG = "Simaromur_Java_" + EmailFeedback.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sendEmail();
    }

    protected void sendEmail() {
        Log.v(LOG_TAG, "Send email");
        String recipientEmail = "info@grammatek.com";
        String subject = getString(R.string.email_subject);
        String msg = getString(R.string.email_message);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:" + recipientEmail));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        String sendMsg = getString(R.string.send_message);
        try {
            startActivity(Intent.createChooser(emailIntent, sendMsg));
            finish();
            Log.i("Finished sending email...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(EmailFeedback.this, "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}
