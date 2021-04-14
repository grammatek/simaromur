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
/*               Date:  June 2012                                        */
/*************************************************************************/

package com.grammatek.simaromur;

import com.grammatek.simaromur.NativeFliteTTS.SynthReadyCallback;
import com.grammatek.simaromur.frontend.FrontendManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeechService;
import android.util.Log;

/**
 * Implements the Flite Engine as a TextToSpeechService
 *
 */
public class FliteTtsService extends TextToSpeechService {
	private final static String LOG_TAG = "Simaromur_Java_" + FliteTtsService.class.getSimpleName();
	private NativeFliteTTS mEngine;
	private NativeG2P mG2P;

	private FrontendManager mFrontendManager;

	private static final String DEFAULT_LANGUAGE = "eng";
	private static final String DEFAULT_COUNTRY = "USA";
	private static final String DEFAULT_VARIANT = "male,rms";

	private String mCountry = DEFAULT_COUNTRY;
	private String mLanguage = DEFAULT_LANGUAGE;
	private String mVariant = DEFAULT_VARIANT;
	private Object mAvailableVoices;
	private SynthesisCallback mCallback;

	@Override
	public void onCreate() {
		initializeFliteEngine();
		initializeFrontendManager(this);
		// This calls onIsLanguageAvailable() and must run after Initialization
		super.onCreate();
	}

	private void initializeFliteEngine() {
		if (mEngine != null) {
			mEngine.stop();
			mEngine = null;
		}
		mEngine = new NativeFliteTTS(this, mSynthCallback);
	}

	private void initializeFrontendManager(Context context) {
		if (mFrontendManager == null)
			mFrontendManager = new FrontendManager(context);
	}

	@Override
	protected String[] onGetLanguage() {
		Log.v(LOG_TAG, "onGetLanguage");
		return new String[] {
				mLanguage, mCountry, mVariant
		};
	}

	@Override
	protected int onIsLanguageAvailable(String language, String country, String variant) {
		Log.v(LOG_TAG, "onIsLanguageAvailable");
		return mEngine.isLanguageAvailable(language, country, variant);
	}

	@Override
	protected int onLoadLanguage(String language, String country, String variant) {
		Log.v(LOG_TAG, "onLoadLanguage");
		return mEngine.isLanguageAvailable(language, country, variant);
	}

	@Override
	protected void onStop() {
		Log.v(LOG_TAG, "onStop");
		mEngine.stop();
	}

	@Override
	protected synchronized void onSynthesizeText(
			SynthesisRequest request, SynthesisCallback callback) {
		Log.v(LOG_TAG, "onSynthesize");

		String language = request.getLanguage();
		String country = request.getCountry();
		String variant = request.getVariant();
		String text = request.getCharSequenceText().toString();

		boolean result = true;

		// this string, engineInput, is unused at the moment, will become the input to our engine
		// when that interface is defined
		String engineInput = mFrontendManager.process(text);
		Log.i(LOG_TAG, text + " => " + engineInput);
		
		if (! ((mLanguage.equals(language)) &&
				(mCountry.equals(country)) &&
				(mVariant.equals(variant)))) {
			result = mEngine.setLanguage(language, country, variant);
			mLanguage = language;
			mCountry = country;
			mVariant = variant;
		}

		if (!result) {
			Log.e(LOG_TAG, "Could not set language for synthesis");
			return;
		}

		int speechrate = request.getSpeechRate();
		mEngine.setSpeechRate(speechrate);
		
		mCallback = callback;
        Integer rate = mEngine.getSampleRate();
        Log.e(LOG_TAG, rate.toString());
		mCallback.start(mEngine.getSampleRate(), AudioFormat.ENCODING_PCM_16BIT, 1);
		mEngine.synthesize(text);
	}

	private final NativeFliteTTS.SynthReadyCallback mSynthCallback = new SynthReadyCallback() {
        @Override
        public void onSynthDataReady(byte[] audioData) {
            if ((audioData == null) || (audioData.length == 0)) {
                onSynthDataComplete();
                return;
            }

            final int maxBytesToCopy = mCallback.getMaxBufferSize();

            int offset = 0;

            while (offset < audioData.length) {
                final int bytesToWrite = Math.min(maxBytesToCopy, (audioData.length - offset));
                mCallback.audioAvailable(audioData, offset, bytesToWrite);
                offset += bytesToWrite;
            }
        }

        @Override
        public void onSynthDataComplete() {
            mCallback.done();
        }
	};

	/**
	 * Listens for language update broadcasts and initializes the flite engine.
	 */
	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initializeFliteEngine();
		}
	};
}
