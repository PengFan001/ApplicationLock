package com.example.applicationlock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.applicationlock.lock.GraphicalUnLockActivity;
import com.example.applicationlock.lock.LockSetActivity;
import com.example.applicationlock.lock.NumberUnlockActivity;
import com.example.applicationlock.provider.AppSettings;
import com.example.applicationlock.util.AppLockUtils;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "MainActivity";
    private TextView mTvSetLock;
    private TextView mTvCloseLock;
    private LinearLayout mLlCloseLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAppLock();
        updateUI();
    }

    private void checkAppLock() {
        if (AppLockUtils.isAppLockOpen(this)) {
            if (AppLockUtils.isAppLock(this)) {
                switch (AppLockUtils.getAppLockMode(this)) {
                    case AppSettings.LOCK_MODE_PASSWORD:
                        startActivity(new Intent(this, NumberUnlockActivity.class)
                                .setAction(AppLockUtils.ACTION_UNLOCK_APP_LOCK));
                        break;

                    case AppSettings.LOCK_MODE_GRAPHICAL:
                        startActivity(new Intent(this, GraphicalUnLockActivity.class)
                                .setAction(AppLockUtils.ACTION_UNLOCK_APP_LOCK));
                        break;
                }
            }
        }
    }

    private void updateUI() {
        if (AppLockUtils.isAppLockOpen(this)) {
            mLlCloseLock.setVisibility(View.VISIBLE);
            mTvSetLock.setText(getResources().getString(R.string.text_modify_app_lock_pw));
        } else {
            mLlCloseLock.setVisibility(View.GONE);
            mTvSetLock.setText(getResources().getString(R.string.text_app_lock_pw));
        }
    }

    private void initView() {
        mTvSetLock = (TextView) findViewById(R.id.tv_set_lock_pw);
        mTvCloseLock = (TextView) findViewById(R.id.tv_close_app_lock);
        mLlCloseLock = (LinearLayout) findViewById(R.id.ll_close_app_lock);

        mTvCloseLock.setOnClickListener(this);
        mTvSetLock.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv_set_lock_pw:
                String text = mTvSetLock.getText().toString();
                if (text.equals(getString(R.string.text_app_lock_pw))) {
                    intent = new Intent(MainActivity.this, LockSetActivity.class);
                } else if (text.equals(getString(R.string.text_modify_app_lock_pw))) {
                    int type = AppLockUtils.getAppLockMode(MainActivity.this);
                    if (type == AppSettings.LOCK_MODE_PASSWORD) {
                        intent = new Intent(MainActivity.this,
                                NumberUnlockActivity.class);
                        intent.setAction(AppLockUtils.ACTION_MODIFY_LOCK_PASSWORD);
                    } else if (type == AppSettings.LOCK_MODE_GRAPHICAL) {
                        intent = new Intent(MainActivity.this,
                                GraphicalUnLockActivity.class);
                        intent.setAction(AppLockUtils.ACTION_MODIFY_LOCK_PASSWORD);
                    }
                }
                if (intent != null) {
                    startActivity(intent);
                }
                break;

            case R.id.tv_close_app_lock:
                int type = AppLockUtils.getAppLockMode(MainActivity.this);
                if (type == AppSettings.LOCK_MODE_PASSWORD) {
                    intent = new Intent(MainActivity.this,
                            NumberUnlockActivity.class);
                    intent.setAction(AppLockUtils.ACTION_CLOSE_APP_LOCK);
                } else if (type == AppSettings.LOCK_MODE_GRAPHICAL) {
                    intent = new Intent(MainActivity.this,
                            GraphicalUnLockActivity.class);
                    intent.setAction(AppLockUtils.ACTION_CLOSE_APP_LOCK);
                }
                if (intent != null) {
                    startActivity(intent);
                }
                break;

            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}