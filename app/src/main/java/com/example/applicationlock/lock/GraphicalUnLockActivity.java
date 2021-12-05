package com.example.applicationlock.lock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.example.applicationlock.MyApplication;
import com.example.applicationlock.R;
import com.example.applicationlock.lock.widget.ImageLockView;
import com.example.applicationlock.provider.AppSettings;
import com.example.applicationlock.util.AppLockUtils;
import com.example.applicationlock.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class GraphicalUnLockActivity extends AppCompatActivity implements View.OnClickListener,
        ImageLockView.OnGraphChangedListener {
    private static final String TAG = "GraphicalUnLockActivity";
    private static final int MSG_PW_ERROR = 0;
    private static final int MSG_UPDATE_TITLE = 1;
    private ImageLockView mIlvPw;
    private TextView mTvCancel;
    private TextView mTvTitle;

    private Timer timer;
    private TimerTask timerTask;
    private long restTime;
    private String mAction;
    private MyHandler mHandler;


    private static class MyHandler extends Handler {
        private WeakReference<GraphicalUnLockActivity> mWeakReference;
        public MyHandler(GraphicalUnLockActivity activity) {
            mWeakReference = new WeakReference<GraphicalUnLockActivity>(activity);
        }

        @Override
        public void dispatchMessage(Message msg) {
            GraphicalUnLockActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_PW_ERROR:
                        activity.mIlvPw.resetGraphicalPassword();
                        int count = AppSettings.getInt(activity.getContentResolver(),
                                AppSettings.REMAIN_UNLOCK_COUNT, 5);
                        if (count > 3) {
                            activity.mTvTitle.setText(
                                    activity.getString(R.string.text_graphical_pw_error));
                        } else if (count > 0){
                            String text = activity.getString(R.string.text_graphical_input_lock)
                                    + count
                                    + activity.getString(R.string.text_graphical_input_lock_suffix);
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
                            activity.mIlvPw.setEnable(true);
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
        setContentView(R.layout.activity_graphical_unlock);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkBanUnLockStatus();
        initData();
    }

    private void initView() {
        mTvTitle = (TextView) findViewById(R.id.tv_title);
        mIlvPw = (ImageLockView) findViewById(R.id.il_graphical_pw);
        mTvCancel = (TextView) findViewById(R.id.tv_cancel_unlock);

        mTvCancel.setOnClickListener(this);
        mIlvPw.setOnGraphChangedListener(this);
        mIlvPw.setAlpha(70);
        mIlvPw.setDefaultColor(getResources().getColor(R.color.color_graphical_default_color));
        mIlvPw.setChooseColor(getResources().getColor(R.color.color_graphical_choose_color));
    }

    private void initData(){
        mHandler = new MyHandler(this);
        Intent intent = getIntent();
        if (intent != null) {
            mAction = intent.getAction();
        }
    }

    private void checkBanUnLockStatus() {
        if (!AppLockUtils.isAppLockEnable(this)) {
            mIlvPw.setEnable(false);
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
                AppSettings.putString(getContentResolver(), AppSettings.APP_LOCK_ENABLE,
                        "true");
                mIlvPw.setEnable(true);
                mTvTitle.setText(getResources().getString(R.string.text_input_pw));
            } else {
                restTime = (banStartTime + banTime) - currentTime;
                startTimer();
            }
        } else {
            mIlvPw.setEnable(true);
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
            case R.id.tv_cancel_unlock:
                if (mAction.equals(AppLockUtils.ACTION_UNLOCK_APP_LOCK)) {
                    MyApplication.getInstance().exit();
                } else {
                    onBackPressed();
                }
                break;

            default:
                break;
        }
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
                    startActivity(new Intent(GraphicalUnLockActivity.this,
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
    public void onGraphFinish(String password) {
        LogUtil.d(TAG, "onPointTouchUp: password = " + password);
        if (!TextUtils.isEmpty(password)) {
            String pw = AppSettings.getString(getContentResolver(), AppSettings.APP_LOCK_PASSWORD);
            if (password.equals(pw)) {
                handlerPwCorrect();
            } else {
                int count = AppSettings.getInt(getContentResolver(),
                        AppSettings.REMAIN_UNLOCK_COUNT, 5);
                AppSettings.putInt(getContentResolver(),
                        AppSettings.REMAIN_UNLOCK_COUNT, count - 1);
                mIlvPw.setMatch(false);
                mHandler.sendEmptyMessageDelayed(MSG_PW_ERROR, 1000);
            }
        }
    }
}
