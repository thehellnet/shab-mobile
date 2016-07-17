package org.thehellnet.shab.mobile.utility;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.thehellnet.shab.mobile.BuildConfig;
import org.thehellnet.shab.mobile.SHAB;

import java.util.UUID;

/**
 * Created by sardylan on 05/05/15.
 */
public final class DeviceIdentifier {

    public static String getDeviceId() {
        String id;

        if (ActivityCompat.checkSelfPermission(SHAB.getAppContext(), Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) SHAB.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            id = telephonyManager.getDeviceId();
        } else {
            id = UUID.randomUUID().toString();
        }


        return new String(Hex.encodeHex(DigestUtils.sha256(id)));
    }

    public static String getDeviceName() {
        return android.os.Build.MODEL;
    }

    public static String getDensityName() {
        float density = SHAB.getAppContext().getResources().getDisplayMetrics().density;

        if (density >= 4.0) {
            return "xxxhdpi";
        }
        if (density >= 3.0) {
            return "xxhdpi";
        }
        if (density >= 2.0) {
            return "xhdpi";
        }
        if (density >= 1.5) {
            return "hdpi";
        }
        if (density >= 1.0) {
            return "mdpi";
        }
        return "ldpi";
    }

    public static String getAppVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public static String getCodeVersion() {
        return String.valueOf(BuildConfig.VERSION_CODE);
    }
}
