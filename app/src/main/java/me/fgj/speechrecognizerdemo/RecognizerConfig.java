package me.fgj.speechrecognizerdemo;

import android.content.Intent;

/**
 * Created by FangGengjia on 2016/6/2
 */

public class RecognizerConfig {
    private Intent recognizerIntent = new Intent();

    private String currentLanguage = Constants.LANGUAGE_CHINESE;//默认为中文输入

    public void setCurrentLanguage(int index){
        switch (index){
            case 1:
                currentLanguage = Constants.LANGUAGE_ENGLISH;
                break;
            case 2:
                currentLanguage = Constants.LANGUAGE_CANTONESE;
                break;
            case 3:
                currentLanguage = Constants.LANGUAGE_SICHUAN;
                break;
            default:
                currentLanguage = Constants.LANGUAGE_CHINESE;
                break;
        }
    }

    public String getCurrentLanguage(){
        return currentLanguage;
    }

    //设置识别状态提示音
    private void initRecognizer(){
        recognizerIntent.putExtra(Constants.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
        recognizerIntent.putExtra(Constants.EXTRA_SOUND_SUCCESS,R.raw.bdspeech_recognition_success);
        recognizerIntent.putExtra(Constants.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
        recognizerIntent.putExtra(Constants.EXTRA_SOUND_ERROR,R.raw.bdspeech_recognition_error);
        recognizerIntent.putExtra(Constants.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
    }

    public Intent getRecognizerIntent(){
        initRecognizer();
        return recognizerIntent;
    }
}