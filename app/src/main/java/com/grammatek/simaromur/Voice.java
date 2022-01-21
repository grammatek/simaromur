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
/*               Date:  July 2012                                        */
/*************************************************************************/

package com.grammatek.simaromur;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import android.util.Log;

public class Voice {
	private final static String LOG_TAG = "Simaromur_Java_" + Voice.class.getSimpleName();
	private final static String FLITE_VOICE_BASE_URL = "http://festvox.org/flite/voices/cg/voxdata-v2.0.0/";

	private String mVoiceName;
	private String mVoiceMD5;
	private String mVoiceLanguage;
	private String mVoiceCountry;
	private String mVoiceVariant;
	private final boolean mIsValidVoice;
	private String mVoicePath;
	private boolean mIsVoiceAvailable;

	/**
	 * @return base URL to download voices and other data
	 */
	public static String getDownloadURLBasePath() {
		return FLITE_VOICE_BASE_URL;
	}

	/**
	 * @param voiceInfoLine is the line that is found in "voices.list" file
	 * as downloaded on the server and cached. This line has text in the format:
	 * language-country-variant<TAB>MD5SUM
	 */
	Voice(String voiceInfoLine) {
		boolean parseSuccessful = false;
		String[] voiceInfo = voiceInfoLine.split("\t");
		if (voiceInfo.length != 2) {
			Log.e(LOG_TAG, "Voice line could not be read: " + voiceInfoLine);
		}
		else {
			mVoiceName = voiceInfo[0];
			mVoiceMD5 = voiceInfo[1];

			String[] voiceParams = mVoiceName.split("-");
			if(voiceParams.length != 3) {
				Log.e(LOG_TAG,"Incorrect voicename:" + mVoiceName);
			}
			else {
				mVoiceLanguage = voiceParams[0];
				mVoiceCountry = voiceParams[1];
				mVoiceVariant = voiceParams[2];
				parseSuccessful = true;
			}
		}

		if (parseSuccessful) {
			mIsValidVoice = true;
			mVoicePath = App.getDataPath() + "cg/" + mVoiceLanguage +
					"/" + mVoiceCountry + "/" + mVoiceVariant + ".cg.flitevox";
Log.d(LOG_TAG, "mVoicePath: " + mVoicePath);
			mIsVoiceAvailable = checkVoiceAvailability();
		}
		else {
			mIsValidVoice = false;
		}

	}

	/**
	 * Check validity of voice. Read file, calc MD5 checksum and throw exception in case voice file
	 * doesn't exist or is inconsistent.
	 *
	 * TODO(DS): this whole procedure is done every time for the whole voice list. This is very
	 * 			 inefficient and needs only to be done once at startup and can be done iteratively,
	 * 			 as soon as the voices are updated (i.e. deleted/downloaded). This inefficiency causes
	 * 			 the user to wait each time TTS demo is loaded
	 *
	 * @return	true in case voice is available, false otherwise
	 */
	private boolean checkVoiceAvailability() {
		boolean rv = false;

		String md5String = getMD5SumOfFile(mVoicePath);
		if ((md5String != null) && md5String.equals(mVoiceMD5)) {
			Log.v(LOG_TAG, "Voice is valid: " + mVoiceName);
			rv = true;
		}
		else {
			Log.e(LOG_TAG,"Voice MD5 sum incorrect. Found: " +
					md5String + ", expected: " + mVoiceMD5);
		}
		return rv;
	}

	/**
	 * Calculate MD5 message digest of given file and return it as string
	 */
	private static String getMD5SumOfFile(String filePath) {
		FileInputStream fis;
		try {
			fis = new FileInputStream(filePath);
		}
		catch (FileNotFoundException e) {
			Log.e(LOG_TAG, "File not found: " + filePath);
			return null;
		}

		byte[] dataBytes = new byte[1024];
		int nread;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG, "MD5 could not be computed");
			return null;
		}
		try {
			while ((nread = fis.read(dataBytes)) != -1) {
				md.update(dataBytes, 0, nread);
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "Could not read file: " + filePath);
			return null;
		}
		finally {
			try {
				fis.close();
			} catch (IOException e) {
				// Ignoring this exception.
			}
		}

		StringBuilder sb = new StringBuilder();
		byte[] mdBytes = md.digest();
		for (byte mdByte : mdBytes) {
			sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	public boolean isValid() {
		return mIsValidVoice;
	}

	public boolean isAvailable() {
		return mIsVoiceAvailable;
	}

	public String getName() {
		return mVoiceName;
	}

	public String getDisplayName() {
		Locale loc = new Locale(mVoiceLanguage, mVoiceCountry, mVoiceVariant);
		return loc.getDisplayLanguage() +
				"(" + loc.getDisplayCountry() + "," + loc.getVariant() + ")";
	}

	public String getVariant() {
		return mVoiceVariant;
	}

	public String getDisplayLanguage() {
		Locale loc = new Locale(mVoiceLanguage, mVoiceCountry, mVoiceVariant);
		return loc.getDisplayLanguage() +
				" (" + loc.getDisplayCountry() + ")";
	}

	public String getPath() {
		return mVoicePath;
	}

	public Locale getLocale() {
		return new Locale(mVoiceLanguage, mVoiceCountry, mVoiceVariant);
	}
}
