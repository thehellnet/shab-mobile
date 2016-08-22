package org.thehellnet.shab.mobile.activity.fragment;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.TextView;

import org.thehellnet.shab.mobile.R;

import java.util.Locale;

/**
 * Created by sardylan on 18/07/16.
 */
public class AboutFragment extends ShabFragment {

    private TextView versionText;

    @Override
    protected int getLayout() {
        return R.layout.fragment_about;
    }

    @Override
    public Fragments getBackFragment() {
        return Fragments.MAIN;
    }

    @Override
    protected void initElements() {
        super.initElements();
        versionText = (TextView) getActivity().findViewById(R.id.about_version);
        if (versionText != null) {
            try {
                PackageInfo packageInfo = getActivity()
                        .getPackageManager()
                        .getPackageInfo(getActivity().getPackageName(), 0);
                versionText.setText(String.format(Locale.US,
                        "Version %s - Code %d",
                        packageInfo.versionName,
                        packageInfo.versionCode));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
