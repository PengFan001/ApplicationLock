package com.example.applicationlock;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Process;

import com.example.applicationlock.provider.AppSettings;
import com.example.applicationlock.util.AppLockUtils;
import com.example.applicationlock.util.LogUtil;

import java.util.ArrayList;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private static MyApplication mInstance;
    private Context mContext;
    private ArrayList<Activity> mActivityList;

    private ActivityLifecycleCallbacks mActivityLifecycleCallbacks
            = new ActivityLifecycleCallbacks() {
        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
            mActivityList.add(activity);
        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {
            mActivityList.remove(activity);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG, "onCreate: -----");
        mInstance = this;
        mContext = getApplicationContext();
        mActivityList = new ArrayList<Activity>();
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
        checkAppLock();
    }

    private void checkAppLock() {
        if (AppLockUtils.isAppLockOpen(mContext)) {
            AppSettings.putBoolean(getContentResolver(), AppSettings.IS_APP_LOCK, true);
        }
    }

    public static MyApplication getInstance() {
        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }


    public void exit() {
        for (Activity activity : mActivityList) {
            activity.finish();
        }
        Process.killProcess(Process.myPid());
    }
}
