package com.example.jerrychen.p3;

import android.content.Context;
import android.util.Log;

import java.util.Locale;

public class TextToSpeech {
    private boolean speakable;
    private android.speech.tts.TextToSpeech mTTS;
    public TextToSpeech(android.speech.tts.TextToSpeech mTTS){
        this.mTTS=mTTS;
    }
    public void initializeTextToSpeech(Context c) {
        mTTS=new android.speech.tts.TextToSpeech(c, new android.speech.tts.TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status== android.speech.tts.TextToSpeech.SUCCESS){
                    int result= mTTS.setLanguage(Locale.ENGLISH);
                    if (result== android.speech.tts.TextToSpeech.LANG_MISSING_DATA|| result== android.speech.tts.TextToSpeech.LANG_NOT_SUPPORTED){
                        speakable=false;
                        Log.d("TTS","not supported");
                    }else {
                        speakable=true;
                    }
                }else {
                    Log.d("TTS","Error");
                    speakable=false;
                }
            }
        });
    }
    public void speak(final String s){
        if (!mTTS.isSpeaking()) {
            mTTS.speak(s, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
