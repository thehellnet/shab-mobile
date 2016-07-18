package org.thehellnet.shab.mobile.activity.fragment;

import org.thehellnet.shab.mobile.R;

/**
 * Created by sardylan on 18/07/16.
 */
public class RawLogFragment extends ShabFragment {

    @Override
    protected int getLayout() {
        return R.layout.fragment_rawlog;
    }

    @Override
    public Fragments getBackFragment() {
        return Fragments.MAIN;
    }
}
