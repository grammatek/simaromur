/*************************************************************************/
/*                                                                       */
/*                  Language Technologies Institute                      */
/*                     Carnegie Mellon University                        */
/*                         Copyright (c) 2010                            */
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
/*               Date:  April 2010                                       */
/*************************************************************************/

package com.grammatek.simaromur;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.List;

/* Download user-requested voice data for Flite
 *
 */
public class DownloadVoiceData extends ListActivity {
	private static final int PERMISSION_REQUEST_CODE = 1;
	private final static String LOG_TAG = "Simaromur_Java_" + DownloadVoiceData.class.getSimpleName();
	private VoiceListAdapter mListAdapter;
	private Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		registerReceiver(onComplete,
				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

		mListAdapter = new VoiceListAdapter(this);
		setListAdapter(mListAdapter);
		mContext = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(onComplete);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.voice_list_update);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Only works for a single menu option.
		// User must have requested a refresh of the voice list.

		Toast toast = Toast.makeText(mContext, "Downloading Voice List", Toast.LENGTH_SHORT);
		toast.show();

		Thread thread = new Thread(() -> CheckFliteVoiceData.DownloadVoiceList(() -> runOnUiThread(() -> mListAdapter.refresh())));

		thread.start();
		return true;
	}

	private class VoiceListAdapter extends BaseAdapter {

		private final Context mContext;
		private List<Voice> mVoiceList;
		private final LayoutInflater mInflater;

		public VoiceListAdapter(Context context) {
			mContext = context;
			mInflater = LayoutInflater.from(mContext);

			// Get Information about voices
			mVoiceList = CheckFliteVoiceData.getVoices();

			if (mVoiceList.isEmpty()) {
				Intent intent = new Intent(mContext, CheckFliteVoiceData.class);
		        startActivity(intent);
			}
		}

		public void refresh() {
			mVoiceList = CheckFliteVoiceData.getVoices();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mVoiceList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.view_voice_manager, parent, false);
			}

			((TextView) convertView.findViewById(R.id.voice_manager_voice_language)).setText(mVoiceList.get(position).getDisplayLanguage());
			((TextView) convertView.findViewById(R.id.voice_manager_voice_variant)).setText(mVoiceList.get(position).getVariant());
			final ImageButton actionButton = convertView.findViewById(R.id.voice_manager_action_image);
			actionButton.setImageResource(mVoiceList.get(position).isAvailable() ?
					R.drawable.ic_action_delete : R.drawable.ic_action_download);
			actionButton.setVisibility(View.VISIBLE);

			actionButton.setOnClickListener(v -> {
				final Voice vox = mVoiceList.get(position);
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				if (!vox.isAvailable()) {
					builder.setMessage("Data Alert: Download Size up to 54MB.");
					builder.setCancelable(false);
					builder.setPositiveButton("Download Voice", (dialog, which) -> {
						boolean permission = checkPermission();
						if (permission) {
							Log.e("value", "Permission Granted, Now you can use local drive .");
						}
						else {
							Log.e("value", "Permission NOT granted! .");
						}
						// Create destination directory
						File f = new File
								(vox.getPath());
						f.mkdirs();
						f.delete();
	Log.d("DownloadVoiceData: vox.getPath():", vox.getPath());
						String url = Voice.getDownloadURLBasePath() + vox.getName() + ".flitevox";
						DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
						request.setDescription("Downloading Flite Voice: " + vox.getName());
						request.setTitle("Flite TTS Voice Download");
						File downLoadFile = new File(vox.getPath());
	Log.e("DownloadVoiceData: downLoadFile.getPath()", downLoadFile.getPath());
						request.setDestinationUri(Uri.fromFile(downLoadFile));

						DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
						manager.enqueue(request);
						Toast toast = Toast.makeText(mContext, "Download Started", Toast.LENGTH_SHORT);
						toast.show();
						actionButton.setVisibility(View.INVISIBLE);
					});
					builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
					AlertDialog alert = builder.create();
					alert.show();
				}
				else {
					builder.setMessage("Sure? Deleting " + vox.getDisplayName());
					builder.setCancelable(false);
					builder.setPositiveButton("Delete Voice", (dialog, which) -> {
						File f = new File(vox.getPath());
						if(f.delete()) {
							refresh();
							Toast toast = Toast.makeText(mContext, "Voice Deleted", Toast.LENGTH_SHORT);
							toast.show();
						}
					});
					builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
					AlertDialog alert = builder.create();
					alert.show();
				}
			});

			convertView.setOnClickListener(v -> actionButton.performClick());

			return convertView;
		}

	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == PERMISSION_REQUEST_CODE) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Log.e("value", "Permission Granted, Now you can use local drive .");
			} else {
				Log.e("value", "Permission Denied, You cannot use local drive .");
			}
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

	BroadcastReceiver onComplete=new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			Toast toast = Toast.makeText(ctxt, "Flite TTS Voice Data Downloaded!", Toast.LENGTH_SHORT);
			toast.show();
			mListAdapter.refresh();
		}
	};

}
