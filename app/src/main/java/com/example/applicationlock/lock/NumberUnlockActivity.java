package com.example.applicationlock.lock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.applicationlock.MyApplication;
import com.example.applicationlock.R;
import com.example.applicationlock.lock.widget.LockEditText;
import com.example.applicationlock.provider.AppSettings;
import com.example.applicationlock.util.AppLockUtils;
import com.example.applicationlock.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class NumberUnlockActivity extends AppCompatActivity implements View.OnClickListener,
        LockEditText.OnTextChangedListener {
    private static final String TAG = "NumberUnlockActivity";

    private static final int MSG_PW_ERROR = 0;
    private static final int MSG_UPDATE_TITLE = 1;

    private LockEditText mLetPassword;
    private TextView mTvTitle;

    /**
     * controls in tl_digit_keyboard
     */
    private LinearLayout mBtn0;
    private LinearLayout mBtn1;
    private LinearLayout mBtn2;
    private LinearLayout mBtn3;
    private LinearLayout mBtn4;
    private LinearLayout mBtn5;
    private LinearLayout mBtn6;
    private LinearLayout mBtn7;
    private LinearLayout mBtn8;
    private LinearLayout mBtn9;

    private TextView mTvCancelOrDel;
    private Timer timer;
    private TimerTask timerTask;
    private long restTime;
    private String mAction;
    private MyHandler mHandler;

    private static class MyHandler extends Handler {
        private WeakReference<NumberUnlockActivity> mWeakReference;

        public MyHandler(NumberUnlockActivity activity) {
            mWeakReference = new WeakReference<NumberUnlockActivity>(activity);
        }

        @Override
        public void dispatchMessage(Message msg) {
            NumberUnlockActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_PW_ERROR:
                        int count = AppSettings.getInt(activity.getContentResolver(),
                                AppSettings.REMAIN_UNLOCK_COUNT, 5);
                        if (count > 3) {
                            activity.mTvTitle.setText(activity.getString(R.string.text_pw_error));
                        } else if (count > 0){
                            String text = activity.getString(R.string.text_input_lock) + count
                                    + activity.getString(R.string.text_input_lock_suffix);
                            activity.mTvTitle.setText(text);
                        } else {
                            AppSettings.putString(activity.getContentResolver(),
                                    AppSettings.APP_LOCK_ENABLE, "false");
                            AppSettings.putLong(activity.getContentResolver(),
                                    AppSettings.APP_LOCK_BANED_TIME,
                                    System.currentTimeMillis() / 1000);
                            activity.checkBanUnLockStatus();
                        }
                        break;

                    case MSG_UPDATE_TITLE:
                        activity.restTime--;
                        if (activity.restTime > 0) {
                            String title;
                            if (activity.restTime > 59) {
                                title = (activity.restTime / 60 + 1)
                                        + activity.getString(R.string.text_timer_title_minute);
                            } else {
                                title = activity.restTime
                                        + activity.getString(R.string.text_timer_title_second);
                            }
                            activity.mTvTitle.setText(title);
                        } else {
                            activity.resetTimer();
                            AppSettings.putString(activity.getContentResolver(),
                                    AppSettings.APP_LOCK_ENABLE, "true");
                            activity.mTvTitle.setText(activity.getString(R.string.text_input_pw));
                        }
                        break;

                    default:
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_number_unlock);
        initView();
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBanUnLockStatus();
    }

    private void initData(){
        mHandler = new MyHandler(this);
        Intent intent = getIntent();
        if (intent != null) {
            mAction = intent.getAction();
        }
    }

    private void initView() {
        mLetPassword = (LockEditText) findViewById(R.id.let_password);
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mBtn0 = (LinearLayout) findViewById(R.id.dial_btn_0);
        mBtn1 = (LinearLayout) findViewById(R.id.dial_btn_1);
        mBtn2 = (LinearLayout) findViewById(R.id.dial_btn_2);
        mBtn3 = (LinearLayout) findViewById(R.id.dial_btn_3);
        mBtn4 = (LinearLayout) findViewById(R.id.dial_btn_4);
        mBtn5 = (LinearLayout) findViewById(R.id.dial_btn_5);
        mBtn6 = (LinearLayout) findViewById(R.id.dial_btn_6);
        mBtn7 = (LinearLayout) findViewById(R.id.dial_btn_7);
        mBtn8 = (LinearLayout) findViewById(R.id.dial_btn_8);
        mBtn9 = (LinearLayout) findViewById(R.id.dial_btn_9);
        mTvCancelOrDel = (TextView) findViewById(R.id.tv_cancel_or_del);

        mBtn0.setOnClickListener(this);
        mBtn1.setOnClickListener(this);
        mBtn2.setOnClickListener(this);
        mBtn3.setOnClickListener(this);
        mBtn4.setOnClickListener(this);
        mBtn5.setOnClickListener(this);
        mBtn6.setOnClickListener(this);
        mBtn7.setOnClickListener(this);
        mBtn8.setOnClickListener(this);
        mBtn9.setOnClickListener(this);
        mLetPassword.setOnTextChangedListener(this);
        mLetPassword.setColor(Color.WHITE);
        mTvCancelOrDel.setOnClickListener(this);
        mLetPassword.setFocusableInTouchMode(false);
    }

    private void checkBanUnLockStatus() {
        if (!AppLockUtils.isAppLockEnable(this)) {
            long currentTime = System.currentTimeMillis() / 1000;
            long banStartTime = AppSettings.getLong(getContentResolver(),
                    AppSettings.APP_LOCK_BANED_TIME, 0);
            long banTime = 0;
            int banCount = AppSettings.getInt(getContentResolver(),
                    AppSettings.REMAIN_UNLOCK_COUNT, 5);
            LogUtil.d(TAG, "checkBanUnLockStatus: banCount = " + banCount);
            if (banCount == 0) {
                banTime = 30;
            } else if (banCount == -1) {
                banTime = 60;
            } else if (banCount == -2) {
                banTime = 5 * 60;
            } else if (banCount == -3) {
                banTime = 30 * 60;
            } else if (banCount <= -4) {
                banTime = 60 * 60;
            }

            LogUtil.d(TAG, "checkBanUnLockStatus: currentTime = " + currentTime
                    + "   banStartTime = " + banStartTime + "   banTime = " + banTime
                    + "    endTime = " + (banStartTime + banTime));
            if (currentTime >= (banStartTime + banTime)) {
                AppSettings.putString(getContentResolver(),
                        AppSettings.APP_LOCK_ENABLE, "true");
                mTvTitle.setText(getResources().getString(R.string.text_input_pw));
            } else {
                restTime = (banStartTime + banTime) - currentTime;
                startTimer();
            }
        }
    }

    private void startTimer() {
        resetTimer();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mHandler != null) {
                    mHandler.sendEmptyMessage(MSG_UPDATE_TITLE);
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
    }

    private void resetTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dial_btn_0:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("0");
                }
                break;
            case R.id.dial_btn_1:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("1");
                }
                break;
            case R.id.dial_btn_2:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("2");
                }
                break;
            case R.id.dial_btn_3:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("3");
                }
                break;
            case R.id.dial_btn_4:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("4");
                }
                break;
            case R.id.dial_btn_5:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("5");
                }
                break;
            case R.id.dial_btn_6:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("6");
                }
                break;
            case R.id.dial_btn_7:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("7");
                }
                break;
            case R.id.dial_btn_8:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("8");
                }
                break;
            case R.id.dial_btn_9:
                if (AppLockUtils.isAppLockEnable(this)) {
                    appendPhoneNumber("9");
                }
                break;

            case R.id.tv_cancel_or_del:
                if (mTvCancelOrDel.getText().equals(getResources().getString(R.string.cancel))) {
                    Log.d(TAG, "onClick: action = " + mAction);
                    if (mAction.equals(AppLockUtils.ACTION_UNLOCK_APP_LOCK)) {
                        MyApplication.getInstance().exit();
                    } else {
                        onBackPressed();
                    }
                } else if (mTvCancelOrDel.getText().equals(getResources()
                        .getString(R.string.delete))) {
                    deletePhoneNumber();
                }
                break;
        }
    }

    private void appendPhoneNumber(String string) {
        if (TextUtils.isEmpty(string)) {
            return;
        }
        String lastInput = mLetPassword.getText().toString().trim();
        if (lastInput.length() < 6) {
            StringBuilder builder = new StringBuilder(lastInput);
            builder.append(string);
            mLetPassword.setText(builder.toString());
            mLetPassword.requestFocus();
            mLetPassword.setSelection(mLetPassword.getText().toString().length());
        }
    }

    private void deletePhoneNumber() {
        String lastInput = mLetPassword.getText().toString().trim();
        if (!TextUtils.isEmpty(lastInput)) {
            StringBuilder builder = new StringBuilder(lastInput);
            builder.delete(lastInput.length() - 1, lastInput.length());
            mLetPassword.setText(builder.toString());
        }
        mLetPassword.requestFocus();
        mLetPassword.setSelection(mLetPassword.getText().toString().length());
    }

    private void handlerPwCorrect() {
        if (!TextUtils.isEmpty(mAction)) {
            switch (mAction) {
                case AppLockUtils.ACTION_UNLOCK_APP_LOCK:
                    AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK,
                            "false");
                    AppSettings.putInt(getContentResolver(), AppSettings.REMAIN_UNLOCK_COUNT,
                            5);
                    onBackPressed();
                    break;

                case AppLockUtils.ACTION_MODIFY_LOCK_PASSWORD:
                    AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK,
                            "false");
                    AppSettings.putInt(getContentResolver(), AppSettings.REMAIN_UNLOCK_COUNT,
                            5);
                    startActivity(new Intent(NumberUnlockActivity.this,
                            LockSetActivity.class));
                    finish();
                    break;

                case AppLockUtils.ACTION_CLOSE_APP_LOCK:
                    AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK_OPEN,
                            "false");
                    AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK,
                            "false");
                    AppSettings.putInt(getContentResolver(), AppSettings.REMAIN_UNLOCK_COUNT,
                            5);
                    onBackPressed();
                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (AppLockUtils.isAppLock(this)) {
            MyApplication.getInstance().exit();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtil.d(TAG, "onDestroy: -----");
        resetTimer();
    }

    @Override
    public void onTextChanged(String text) {
        if (text.length() > 0) {
            mTvCancelOrDel.setText(getResources().getString(R.string.delete));
            if (text.length() == 6) {
                String password = AppSettings.getString(getContentResolver(),
                        AppSettings.APP_LOCK_PASSWORD);
                if (text.equals(password)) {
                    handlerPwCorrect();
                } else {
                    mLetPassword.clearText();
                    int count = AppSettings.getInt(getContentResolver(),
                            AppSettings.REMAIN_UNLOCK_COUNT, 5);
                    LogUtil.d(TAG, "onTextChange: count = " + count);
                    AppSettings.putInt(getContentResolver(),
                            AppSettings.REMAIN_UNLOCK_COUNT, count - 1);
                    mHandler.sendEmptyMessage(MSG_PW_ERROR);
                }
            }
        } else {
            mTvCancelOrDel.setText(getResources().getString(R.string.cancel));
        }
    }
}
