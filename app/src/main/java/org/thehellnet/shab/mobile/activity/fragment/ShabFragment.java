package org.thehellnet.shab.mobile.activity.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sardylan on 17/07/16.
 */
public abstract class ShabFragment extends Fragment {

    protected View view;
    protected SharedPreferences prefs;

    public static ShabFragment getNewFragment(Fragments item) {
        switch (item) {
            case MAIN:
                return new MainFragment();
            case MAP:
                return new MapFragment();
            case RAWLOG:
                return new RawLogFragment();
            case ABOUT:
                return new AboutFragment();
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(getLayout(), container, false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        initElements();
        setOnClickListeners();
    }

    public Fragments getBackFragment() {
        return null;
    }

    protected void initElements() {

    }

    protected void setOnClickListeners() {

    }

    protected abstract int getLayout();
}
