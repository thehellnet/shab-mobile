package org.thehellnet.shab.mobile.activity.fragment;


import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by sardylan on 17/07/16.
 */
public abstract class ShabFragment extends Fragment {

    protected View view;

    public static ShabFragment getNewFragment(Fragments item) {
        switch (item) {
            case MAIN:
                return new MainFragment();
            case MAP:
                return new MapFragment();
            default:
                return null;
        }
    }
}
