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
        final String recipientEmail = "info@grammatek.com";
        final String subject = getString(R.string.email_subject);
        final String msg = getString(R.string.email_message);
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {recipientEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, msg);

        final String sendMsg = getString(R.string.send_message);
        try {
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(emailIntent, sendMsg));
            } else {
                Toast.makeText(this, R.string.no_email_client, Toast.LENGTH_LONG).show();
            }
            Log.i("Finished sending email...", "");
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.no_email_client, Toast.LENGTH_LONG).show();
        }
        finish();
    }
}
