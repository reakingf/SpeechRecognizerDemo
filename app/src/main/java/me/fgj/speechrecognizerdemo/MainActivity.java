package me.fgj.speechrecognizerdemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.VoiceRecognitionService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by FangGengjia on 2016/6/2
 * 运用Android提供的识别器结合百度提供的语音识别服务定制自己的语音识别功能，
 * 可以实现自己的事件处理逻辑，减少第三方对事件处理的限制
 */
public class MainActivity extends Activity implements View.OnClickListener {
    ListView listView;
    private List<String> resultList;
    private ArrayAdapter<String> adapter;
    Button startButton;//启动识别按钮
    Button settingButton;//设置语音类型
    private SpeechRecognizer speechRecognizer;//识别器
    private MyDialog myDialog;//自定义对话框
    private TextView dialogTip;//对话框中的语音状态提示
    ImageView dialogVolume;//对话框中的音量状态
    Handler dialogHandler;//用于处理语音识别对话框的音量显示视图
    Button finishButton;//对话框中的按钮控制
    RecognizerConfig config;//设置语音输入类型
    Intent recognizerIntent;//用于设置语音识别器
    private boolean isRecognizing = false;
    private static final int DELAY_TIME = 200;//音量调用延迟时间
    private AlertDialog.Builder builder;

    /**
     * 将语音识别器的活动与对话框事件关联起来
     */
    class MyDialog extends Dialog {

        public MyDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        public void show() {
            super.show();
            if (speechRecognizer != null) {
                recognizerStart();
            }
            dialogTip.setText(R.string.wait);
            finishButton.setBackgroundResource(R.drawable.bg_dialog_bt_normal);
        }

        @Override
        public void dismiss() {
            super.dismiss();
            if (speechRecognizer != null) {
                speechRecognizer.cancel();
                isRecognizing = false;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialogHandler = new Handler();
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this,
                VoiceRecognitionService.class));//接入百度语音服务类
        config = new RecognizerConfig();
        recognizerIntent = config.getRecognizerIntent();
        speechRecognizer.setRecognitionListener(mRecognitionListener);
        initView();
    }
    private void initView() {
        listView = (ListView) findViewById(R.id.results);
        resultList = new ArrayList<>();
        adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, resultList);
        listView.setAdapter(adapter);
        startButton = (Button) findViewById(R.id.start);
        settingButton = (Button) findViewById(R.id.setting);
        myDialog = new MyDialog(this, R.style.Theme_RecognitionDialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.recognizer_dialog, null);
        myDialog.setContentView(view);
        dialogTip = (TextView) myDialog.findViewById(R.id.speak_tips);
        dialogVolume = (ImageView) myDialog.findViewById(R.id.volume);
        finishButton = (Button) myDialog.findViewById(R.id.speak_finish);
        startButton.setOnClickListener(this);
        settingButton.setOnClickListener(this);
        finishButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                myDialog.show();
                break;
            case R.id.setting:
                setLanguage();
                break;
            case R.id.speak_finish:
                speechRecognizer.stopListening();
                finishButton.setBackgroundResource(R.drawable.bg_dialog_bt_selected);
                break;
        }
    }

    //启动语音识别
    private void recognizerStart() {
        recognizerIntent.putExtra("language", config.getCurrentLanguage());
        speechRecognizer.startListening(recognizerIntent);
    }

    public void setLanguage() {
        builder = new AlertDialog.Builder(this);
        builder.setTitle("设置语音输入类型");
        final String[] languageTypes = {"普通话(中国)", "English(United State)", "粤语（广东话）", "四川话"};
        builder.setItems(languageTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                config.setCurrentLanguage(which);
                Toast.makeText(getApplicationContext(), "当前语言为" +
                        languageTypes[which], Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    //语音转文本后的回调接口
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_OK) {
            mRecognitionListener.onResults(data.getExtras());
        }
    }

    //自定义语音识别监听器
    RecognitionListener mRecognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            dialogTip.setText(R.string.speak_please);
        }

        @Override
        public void onBeginningOfSpeech() {
            dialogTip.setText(R.string.listening);
            isRecognizing = true;
            dialogHandler.removeCallbacks(dialogVolumeRunnable);
            dialogHandler.postDelayed(dialogVolumeRunnable, DELAY_TIME);
        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {
        }

        @Override
        public void onEndOfSpeech() {
            isRecognizing = false;
            dialogHandler.removeCallbacks(dialogVolumeRunnable);
            dialogHandler.postDelayed(dialogVolumeRunnable, DELAY_TIME);
        }

        //当识别出现错误时，如识别不到声音，网络不好等
        @Override
        public void onError(int error) {
            dialogHandler.removeCallbacks(dialogVolumeRunnable);
            dialogHandler.postDelayed(dialogVolumeRunnable, DELAY_TIME);
            if (myDialog != null && myDialog.isShowing()) {
                myDialog.dismiss();
            }
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> rs = results != null ? results.getStringArrayList(SpeechRecognizer.
                    RESULTS_RECOGNITION) : null;
            if (rs != null && rs.size() > 0) {
                resultList.add(0, rs.get(0));
                adapter.notifyDataSetChanged();
            }
            if (myDialog.isShowing()) {
                myDialog.dismiss();
            }
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> rs = partialResults != null ? partialResults.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION) : null;
            if (rs != null && rs.size() > 0) {
                dialogTip.setText(rs.get(0));
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
        }

    };

    /**
     * 语音识别对话框音量显示任务
     * 目前只是个简单的动态图，并没有真的与音量变化挂钩
     */
    Runnable dialogVolumeRunnable = new Runnable() {
        int i = 0;
        @Override
        public void run() {
            if (isRecognizing) {
                if (i < 7) {
                    i++;
                } else {
                    i = 0;
                }
                if (myDialog != null && myDialog.isShowing()) {
                    int resId = getResources().getIdentifier("v" + i, "drawable", getPackageName());
                    dialogVolume.setImageResource(resId);
                }
            } else {
                int resId = getResources().getIdentifier("v" + 1, "drawable", getPackageName());
                dialogVolume.setImageResource(resId);
            }
            dialogHandler.removeCallbacks(dialogVolumeRunnable);
            dialogHandler.postDelayed(dialogVolumeRunnable, DELAY_TIME);
        }
    };

    @Override
    protected void onDestroy() {
        if (myDialog.isShowing()) {
            myDialog.dismiss();
            speechRecognizer.stopListening();
        }
        speechRecognizer.destroy();
        super.onDestroy();
    }
}
