/*************************************************************************/
/*                                                                       */
/*                  Language Technologies Institute                      */
/*                     Carnegie Mellon University                        */
/*                         Copyright (c) 2012                            */
/*                        All Rights Reserved.                           */
/*                                                                       */
/*  Permission is hereby granted, free of charge, to use and distribute  */
/*  this software and its documentation without restriction, including   */
/*  without limitation the rights to use, copy, modify, merge, publish,  */
/*  distribute, sublicense, and/or sell copies of this work, and to      */
/*  permit persons to whom this work is furnished to do so, subject to   */
/*  the following conditions:                                            */
/*   1. The code must retain the above copyright notice, this list of    */
/*      conditions and the following disclaimer.                         */
/*   2. Any modifications must be clearly marked as such.                */
/*   3. Original authors' names are not deleted.                         */
/*   4. The authors' names are not used to endorse or promote products   */
/*      derived from this software without specific prior written        */
/*      permission.                                                      */
/*                                                                       */
/*  CARNEGIE MELLON UNIVERSITY AND THE CONTRIBUTORS TO THIS WORK         */
/*  DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE, INCLUDING      */
/*  ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO EVENT   */
/*  SHALL CARNEGIE MELLON UNIVERSITY NOR THE CONTRIBUTORS BE LIABLE      */
/*  FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES    */
/*  WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN   */
/*  AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION,          */
/*  ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF       */
/*  THIS SOFTWARE.                                                       */
/*                                                                       */
/*************************************************************************/
/*             Author:  Alok Parlikar (aup@cs.cmu.edu)                   */
/*               Date:  July 2012                                        */
/*************************************************************************/
package com.grammatek.simaromur;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;

public class TTSDemo extends ListActivity implements OnClickListener, OnKeyListener, OnInitListener {
	private final static String LOG_TAG = "Simaromur_Java_" + TTSDemo.class.getSimpleName();

	private EditText mUserText;
	private ImageButton mSendButton;
	private ArrayAdapter<String> mAdapter;
	private ArrayAdapter<String> mVoiceAdapter;
    private ArrayAdapter<String> mRateAdapter;
	private List<Voice> mVoices;
	private ArrayList<String> mStrings = new ArrayList<>();
    private List<String> mRates = new ArrayList<>();
	private Spinner mVoiceSpinner;
    private Spinner mRateSpinner;
	private TextToSpeech mTts;
	private int mSelectedVoice;

	private static final int PERMISSION_REQUEST_CODE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// we need these properties for openNLP
		System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

		if (checkPermission())
		{
				// Code for above or equal 23 API Oriented Device
				// Your Permission granted already .Do next code
		} else {
			requestPermission(); // Code for permission
		}

		List<Voice> allVoices = CheckFliteVoiceData.getVoices();
		mVoices = new ArrayList<>();
		for(Voice vox:allVoices) {
			if (vox.isAvailable()) {
				mVoices.add(vox);
			}
		}

		if (mVoices.isEmpty()) {
			// We can't demo anything if there are no voices installed.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Flite voices not installed. Please add voices in order to run the demo");
			builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
		else {
			if (mTts == null) {
				Log.d("mTts", "new TextToSpeech");
				mTts = new TextToSpeech(this, this);
			}
			mSelectedVoice = -1;
		}
	}

	private boolean checkPermission() {
		int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
		if (result == PackageManager.PERMISSION_GRANTED) {
			Log.e("value", "PERMISSION_GRANTED is true.");
			return true;
		} else {
			Log.e("value", "PERMISSION_GRANTED is false.");
			return false;
		}
	}

	private void requestPermission() {

		if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
		} else {
			ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.e("value", "Permission Granted, Now you can use local drive .");
			} else {
				Log.e("value", "Permission Denied, You cannot use local drive .");
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTts != null)
		{
			mTts.stop();
			mTts.shutdown();
			mTts = null;
			Log.e("mTts", "delete TextToSpeech");
		}

	}

	private void buildUI() {

		List<String> voiceNames = new ArrayList<>();

		for (Voice vox: mVoices) {
		    voiceNames.add(vox.getDisplayName()); // vox.getVariant());
		}

		mVoiceAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item,
				voiceNames);


		setContentView(R.layout.activity_tts_demo);
		mStrings.add("Click an item here to synthesize, or enter your own text below!");
		mStrings.add("A whole joy was reaping, but they've gone south, go fetch azure mike!");
		mStrings.add("?????????????????? ??????????????????????????? ????????? ?????? ???????????? ?????? ??????????????? ????????????????????? ?????? ???????????? ?????? ???????????? ???????????? ???????????? ?????? ???????????? ???????????? ???????????? ???????????? ?????????");
		mStrings.add("Innanlandssmiti?? sem greindist utan s??ttkv??ar ?? g??r s??nir a?? veiran er ekki horfin ??r ??slensku samf??lagi");
		mStrings.add("Innanlandssmiti?? sem greindist utan s??ttkv??ar ?? g??r s??nir a?? veiran er ekki horfin ??r ??slensku samf??lagi." +
				"Ekki er b??i?? a?? rekja smiti?? og ???? liggur ra??greining ekki fyrir.??etta kom fram ?? m??li ????r??lfs Gu??nasonar," +
				"s??ttvarnal??knis, ?? uppl??singafundi almannavarna og landl??knis ?? dag. Hann sag??ist vona a?? h??ps??kingin sem kom" +
				"upp fyrir tveimur vikum v??ri yfirsta??in en t??k fram a?? ekki v??ri h??gt a?? ??tiloka eitthva?? samf??lagslegt smit" +
				"??t fr?? smitinu sem greindist ?? g??r. ????r??lfur sag??i smitrakningu standa yfir og ???? b??st hann vi?? ni??urst????u ??r" +
				"ra??greiningu ?? kv??ld. ???? sag??i hann a?? ??eir sta??ir sem s?? smita??i var ?? v??ru nokku?? afmarka??ir og ekki mj??g margir." +
				"L??klegast yr??i h??gt a?? hafa samband vi?? alla ???? sem voru ?? ??eim st????um upp ?? s??ttkv?? og s??nat??ku a?? gera." +
				" ??a?? er ??st????a til a?? hafa ??hyggjur af smitinu sem greindist ?? g??r utan s??ttkv??ar en vi?? erum ?? fullu a?? setja f??lk" +
				" ?? s??ttkv?? og skima ?? kringum ??ennan einstakling, sag??i ????r??lfur. ???? hvatti hann almenning til a?? g??ta ??fram a??" +
				"  ??trustu s??ttv??rnum. Tilfelli?? sem greindist ?? g??r s??nir ??a?? a?? veiran er ekki horfin ??r ??slensku samf??lagi og ef" +
				"  vi?? p??ssum okkur ekki getum vi?? fengi?? a??ra bylgju ?? baki??, sag??i ????r??lfur.");
		mStrings.add("Hello World, ?????????????????????, ?????????????????????, ????????????????????????");

		mAdapter = new InputHistoryAdapter(this, R.layout.list_tts_history, mStrings);

		setListAdapter(mAdapter);

		mRates.add("Very Slow");
		mRates.add("Slow");
		mRates.add("Normal");
		mRates.add("Fast");
		mRates.add("Very Fast");

		mRateAdapter = new ArrayAdapter<String>(this,
							android.R.layout.simple_spinner_dropdown_item,
							mRates);


		mUserText = (EditText) findViewById(R.id.userText);
		mSendButton = (ImageButton) findViewById(R.id.sendButton);

		mVoiceSpinner = (Spinner) findViewById(R.id.voice);
		mVoiceSpinner.setAdapter(mVoiceAdapter);

		mRateSpinner = (Spinner) findViewById(R.id.speechrate);
		mRateSpinner.setAdapter(mRateAdapter);
		mRateSpinner.setSelection(2);

		mUserText.setOnClickListener(this);
		mSendButton.setOnClickListener(this);
		mUserText.setOnKeyListener(this);
	}

	public void onClick(View v) {
		sendText();
	}

	private void sendText() {
		String text = mUserText.getText().toString();
		if (text.isEmpty())
			return;
		mAdapter.add(text);
		mUserText.setText(null);
		sayText(text);
	}

	private void sayText(String text) {
		Log.v(LOG_TAG, "Speaking: " + text);
		int currentVoiceID = mVoiceSpinner.getSelectedItemPosition();
		if (currentVoiceID != mSelectedVoice) {
			mSelectedVoice = currentVoiceID;
			Voice v = mVoices.get(currentVoiceID);
			mTts.setLanguage(v.getLocale());
		}

		int currentRate = mRateSpinner.getSelectedItemPosition();
		mTts.setSpeechRate((float)(currentRate + 1)/3);

		mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
			case KeyEvent.KEYCODE_ENTER:
				sendText();
				return true;
			}
		}
		return false;
	}

	private class InputHistoryAdapter extends ArrayAdapter<String> {
		private ArrayList<String> items;

		public InputHistoryAdapter(Context context,
				int textViewResourceId, ArrayList<String> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = vi.inflate(R.layout.list_tts_history, null);
			}
			String s = items.get(position);
			TextView tt = (TextView) convertView.findViewById(R.id.inputText);
			tt.setText(s);
			return convertView;
		}

	}

	@SuppressWarnings("deprecation")
	// onInit() can also be called in case of an Error in new TextToSpeech()
	@Override
	public void onInit(int status) {
		// mTts is only set in case status != TextToSpeech.ERROR !
		boolean success = false;
		if ((status != TextToSpeech.ERROR) &&
				(mTts.setEngineByPackageName("com.grammatek.simaromur") != TextToSpeech.ERROR)) {
			// REALLY check that it is flite engine that has been initialized
			// This is done using a hack, for now, since for API < 14
			// there seems to be no way to check which engine is being used.
			Locale locale = new Locale("eng", "USA", "is_flite_available");
			if (mTts.isLanguageAvailable(locale) == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
				success = true;
			}
		}

		if (success) {
			buildUI();
		}
		else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Flite TTS Engine could not be initialized. Check that Flite is enabled on your phone!. In some cases, you may have to select flite as the default engine.");
			builder.setNegativeButton("Open TTS Settings", (dialog, which) -> {
				dialog.cancel();
				//Open Android Text-To-Speech Settings
				Intent intent = new Intent();
				intent.setAction("com.android.settings.TTS_SETTINGS");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
				finish();
			});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Override
	public void onListItemClick(ListView parent, View view, int position, long id) {
		String text = (String) parent.getItemAtPosition(position);
		sayText(text);

	}
}
