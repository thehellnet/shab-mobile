package org.thehellnet.shab.mobile.activity.fragment;

import org.thehellnet.shab.mobile.R;

/**
 * Created by sardylan on 18/07/16.
 */
public class AboutFragment extends ShabFragment {

    @Override
    protected int getLayout() {
        return R.layout.fragment_about;
    }

    @Override
    public Fragments getBackFragment() {
        return Fragments.MAIN;
    }
}
