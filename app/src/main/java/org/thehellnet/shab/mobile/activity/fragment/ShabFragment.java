package org.thehellnet.shab.mobile.activity.fragment;


import android.support.v4.app.Fragment;

/**
 * Created by sardylan on 17/07/16.
 */
public abstract class ShabFragment extends Fragment {

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
