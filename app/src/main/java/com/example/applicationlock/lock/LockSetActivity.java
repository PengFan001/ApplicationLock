package com.example.applicationlock.lock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.applicationlock.R;
import com.example.applicationlock.lock.widget.ImageLockView;
import com.example.applicationlock.lock.widget.LockEditText;
import com.example.applicationlock.lock.widget.MenuDialogFragment;
import com.example.applicationlock.provider.AppSettings;
import com.example.applicationlock.util.AppLockUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class LockSetActivity extends AppCompatActivity implements LockEditText.OnTextChangedListener,
        ImageLockView.OnGraphChangedListener, View.OnClickListener {
    private static final String TAG = "LockSetActivity";
    private static final int MSG_ID_RESET_LOCK_IMAGE_VIEW = 0;

    private TextView mTvTitle;
    private LockEditText mLetPw;
    private ImageLockView mIlvPw;
    private TextView mTvTips;
    private TextView mTvOther;
    private LinearLayout mLlBtnContent;
    private Button mBtnRedraw;
    private Button mBtnFinish;

    private int mType;
    private boolean ensurePw = false;
    private String firstPw = null;
    private final ArrayList<String> mLockTypeList = new ArrayList<String>();
    private InputMethodManager mInputMethodManager;
    private MyHandler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actiivty_lock_set);
        initView();
        initData();
    }

    private void initView() {
        mTvTitle = (TextView) findViewById(R.id.tv_set_pw_title);
        mTvOther = (TextView) findViewById(R.id.tv_other_lock_way);
        mTvTips = (TextView) findViewById(R.id.tv_tip_of_set_pw);
        mLetPw = (LockEditText) findViewById(R.id.let_number_pw_6);
        mIlvPw = (ImageLockView) findViewById(R.id.il_set_pw);
        mLlBtnContent = (LinearLayout) findViewById(R.id.btn_content);
        mBtnRedraw = (Button) findViewById(R.id.btn_redraw);
        mBtnFinish = (Button) findViewById(R.id.btn_ensure);

        mTvOther.setOnClickListener(this);
        mBtnRedraw.setOnClickListener(this);
        mBtnFinish.setOnClickListener(this);
        mLetPw.setOnTextChangedListener(this);
        mIlvPw.setOnGraphChangedListener(this);

        mIlvPw.setAlpha(125);
        mIlvPw.setDefaultColor(getResources().getColor(R.color.color_graphical_default_color));
        mIlvPw.setChooseColor(getResources().getColor(R.color.color_graphical_default_color));
    }


    private void initData() {
        mHandler = new MyHandler(this);
        initTypeList();
        mType = AppLockUtils.getAppLockMode(this);
        mInputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initTypeList() {
        mLockTypeList.clear();
        mLockTypeList.add(String.valueOf(AppSettings.LOCK_MODE_PASSWORD));
        mLockTypeList.add(String.valueOf(AppSettings.LOCK_MODE_GRAPHICAL));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUiLockType();
    }

    private void updateUiLockType() {
        if (mType == AppSettings.LOCK_MODE_PASSWORD) {
            mIlvPw.setVisibility(View.GONE);
            mLetPw.setVisibility(View.VISIBLE);
            if (ensurePw) {
                setLockTitle(LockConstants.TITLE_NUMBER_PW_ENSURE);
            } else {
                setLockTitle(LockConstants.TITLE_NUMBER_PW_6_DEFAULT);
            }
            mLetPw.setFocusable(true);
            mLetPw.setFocusableInTouchMode(true);
            mLetPw.requestFocus();
            autoShowInput();
        } else if (mType == AppSettings.LOCK_MODE_GRAPHICAL) {
            mLetPw.setVisibility(View.GONE);
            mIlvPw.setVisibility(View.VISIBLE);
            if (ensurePw) {
                setLockTitle(LockConstants.TITLE_GRAPHICAL_ENSURE);
            } else {
                setLockTitle(LockConstants.TITLE_GRAPHICAL_PW_DEFAULT);
            }
        }
        mTvTips.setText(getResources().getString(R.string.text_tip_of_set_pw));
        mTvTips.setVisibility(View.VISIBLE);
    }

    private void setLockTitle(int titleId) {
        switch (titleId) {
            case LockConstants.TITLE_NUMBER_PW_6_DEFAULT:
                mTvTitle.setText(getResources().getString(R.string.text_set_number_pw_6_title));
                break;

            case LockConstants.TITLE_NUMBER_PW_ENSURE:
                mTvTitle.setText(getResources().getString(R.string.title_set_number_pw_6_ensure));
                break;

            case LockConstants.TITLE_GRAPHICAL_PW_DEFAULT:
                mTvTitle.setText(getResources().getString(R.string.text_set_graphical_pw_title));
                break;

            case LockConstants.TITLE_GRAPHICAL_LESS_4_POINT:
                mTvTitle.setText(getResources().getString(R.string.title_set_graphical_less_4_point));
                break;

            case LockConstants.TITLE_GRAPHICAL_ENSURE:
                mTvTitle.setText(getResources().getString(R.string.title_set_graphical_ensure));
                break;

            case LockConstants.TITLE_YOUR_GRAPHICAL_PW:
                mTvTitle.setText(getResources().getString(R.string.title_your_graphical_pw));
                break;

            default:
                break;
        }
    }

    private void autoShowInput() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mInputMethodManager.showSoftInput(mLetPw, 0);
            }
        };
        timer.schedule(timerTask, 300);
    }

    @Override
    public void onTextChanged(String text) {
        if (text.length() == 6) {
            if (!ensurePw) {
                firstPw = text;
                ensurePw = true;
                mLetPw.clearText();
                mTvOther.setVisibility(View.GONE);
                setLockTitle(LockConstants.TITLE_NUMBER_PW_ENSURE);
            } else {
                if (!TextUtils.isEmpty(firstPw)) {
                    if (firstPw.equals(text)) {
                        AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK_OPEN,
                                "true");
                        AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK,
                                "false");
                        AppSettings.putInt(getContentResolver(), AppSettings.APP_LOCK_MODE,
                                AppSettings.LOCK_MODE_PASSWORD);
                        AppSettings.putString(getContentResolver(), AppSettings.APP_LOCK_PASSWORD,
                                text);
                        onBackPressed();
                    } else {
                        mTvTips.setText(getResources().getString(R.string.tips_pw_not_equal));
                        mLetPw.clearText();
                    }
                }
            }
        }
    }

    @Override
    public void onGraphFinish(String password) {
        if (password.length() < 4) {
            if (!ensurePw) {
                setLockTitle(LockConstants.TITLE_GRAPHICAL_LESS_4_POINT);
            } else {
                mTvTips.setText(getResources().getString(R.string.tips_graphical_pw_not_equal));
            }
            mIlvPw.setMatch(false);
            mHandler.sendEmptyMessageDelayed(MSG_ID_RESET_LOCK_IMAGE_VIEW, 800);
        } else {
            if (!ensurePw) {
                firstPw = password;
                ensurePw = true;
                mIlvPw.resetGraphicalPassword();
                mTvOther.setVisibility(View.GONE);
                mTvTips.setVisibility(View.INVISIBLE);
                setLockTitle(LockConstants.TITLE_GRAPHICAL_ENSURE);
                mLlBtnContent.setVisibility(View.VISIBLE);
            } else {
                if (!TextUtils.isEmpty(firstPw)) {
                    if (firstPw.equals(password)) {
                        setLockTitle(LockConstants.TITLE_YOUR_GRAPHICAL_PW);
                        mTvTips.setVisibility(View.INVISIBLE);
                        mBtnFinish.setEnabled(true);
                        mBtnFinish.setClickable(true);
                    } else {
                        mTvTips.setVisibility(View.VISIBLE);
                        mTvTips.setText(getResources().getString(R.string.tips_graphical_pw_not_equal));
                        mIlvPw.setMatch(false);
                        mHandler.sendEmptyMessageDelayed(MSG_ID_RESET_LOCK_IMAGE_VIEW, 800);
                    }
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_other_lock_way:
                showOtherLockWay();
                break;

            case R.id.btn_redraw:
                resetPwSet();
                updateUiLockType();
                mTvOther.setVisibility(View.VISIBLE);
                mLlBtnContent.setVisibility(View.GONE);
                mIlvPw.resetGraphicalPassword();
                break;

            case R.id.btn_ensure:
                AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK_OPEN,
                        "true");
                AppSettings.putString(getContentResolver(), AppSettings.IS_APP_LOCK, "false");
                AppSettings.putInt(getContentResolver(), AppSettings.APP_LOCK_MODE,
                        AppSettings.LOCK_MODE_GRAPHICAL);
                AppSettings.putString(getContentResolver(), AppSettings.APP_LOCK_PASSWORD,
                        mIlvPw.getGraphicalPassword());
                onBackPressed();
                break;

            default:
                break;
        }
    }

    private void showOtherLockWay() {
        ArrayList<String> otherTypeList = new ArrayList<String>();
        if (mType == AppSettings.LOCK_MODE_PASSWORD) {
            otherTypeList.add(getString(R.string.menu_item_graphical_pw));
        } else if (mType == AppSettings.LOCK_MODE_GRAPHICAL) {
            otherTypeList.add(getString(R.string.menu_item_6_number_pw));
        }

        MenuDialogFragment otherLockTypeDialog
                = MenuDialogFragment.newInstance(getString(R.string.text_other_pw), otherTypeList);
        otherLockTypeDialog.setMenuItemClick(new MenuDialogFragment.ItemClick() {
            @Override
            public void itemClick(int position, String item) {
                if (item.equals(getResources().getString(R.string.menu_item_6_number_pw))) {
                    mType = AppSettings.LOCK_MODE_PASSWORD;
                    startActivity(new Intent(getApplicationContext(), LockSetActivity.class));
                    resetPwSet();
                } else if (item.equals(getResources().getString(R.string.menu_item_graphical_pw))) {
                    mType = AppSettings.LOCK_MODE_GRAPHICAL;
                    startActivity(new Intent(getApplicationContext(), LockSetActivity.class));
                    resetPwSet();
                }
            }
        });
        otherLockTypeDialog.show(getFragmentManager(), TAG);

    }

    private void resetPwSet() {
        ensurePw = false;
        firstPw = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private static class MyHandler extends Handler {
        private WeakReference<LockSetActivity> mWeakReference;
        public MyHandler(LockSetActivity activity) {
            mWeakReference = new WeakReference<LockSetActivity>(activity);
        }

        @Override
        public void dispatchMessage(Message msg) {
            LockSetActivity activity = mWeakReference.get();
            if (activity != null) {
                switch (msg.what) {
                    case MSG_ID_RESET_LOCK_IMAGE_VIEW:
                        activity.mIlvPw.resetGraphicalPassword();
                        break;

                    default:
                        break;
                }
            }
        }
    }
}
