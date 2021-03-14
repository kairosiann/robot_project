package com.samples.flironecamera.view.chatbot;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.util.Locale;

public class TTSViewModel {
    private TextToSpeech mTTS1;

    public TTSViewModel(Context context) {
        mTTS1 = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = mTTS1.setLanguage(Locale.US); //change to English
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                }

                mTTS1.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                        Log.d("TTSViewModel", "onStart: ");
                    }

                    @Override
                    public void onDone(String utteranceId) {
                        Log.d("TTSViewModel", "onDone: ");
                    }

                    @Override
                    public void onError(String utteranceId) {

                    }
                });
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    public void speak(String text) {
        if (mTTS1.isSpeaking() == false) {
            mTTS1.setSpeechRate(1.6f);
            mTTS1.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public boolean isSpeaking() {
        return mTTS1.isSpeaking();
    }
}
