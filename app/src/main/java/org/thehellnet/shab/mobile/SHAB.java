package org.thehellnet.shab.mobile;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by sardylan on 16/07/16.
 */
public class SHAB extends Application {

    public static final String[] PERMISSIONS = new String[]{
            android.Manifest.permission.INTERNET,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
            android.Manifest.permission.READ_PHONE_STATE
    };

    private static SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        ContextKeeper.getInstance().setContext(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    public static Context getAppContext() {
        return ContextKeeper.getInstance().getContext();
    }

    public static SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) ContextKeeper.getInstance()
                .getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo
                : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void showToast(int resId) {
        showToast(ContextKeeper.getInstance().getContext().getString(resId));
    }

    public static void showToast(String message) {
        Toast.makeText(ContextKeeper.getInstance().getContext(), message, Toast.LENGTH_LONG).show();
    }
}
