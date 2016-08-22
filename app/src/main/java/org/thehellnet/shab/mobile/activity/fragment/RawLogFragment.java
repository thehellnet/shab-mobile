package org.thehellnet.shab.mobile.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.protocol.line.Line;

/**
 * Created by sardylan on 18/07/16.
 */
public class RawLogFragment extends ShabFragment {

    private class NewLineReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Line line = (Line) intent.getSerializableExtra("line");
            if (rawlogText != null && line != null) {
                rawlogText.append(String.format("%s - %s\n", DateTime.now(), line.serialize()));
            }
        }
    }

    private NewLineReceiver newLineReceiver;

    private TextView rawlogText;

    @Override
    protected int getLayout() {
        return R.layout.fragment_rawlog;
    }

    @Override
    public Fragments getBackFragment() {
        return Fragments.MAIN;
    }

    @Override
    protected void initElements() {
        super.initElements();

        rawlogText = (TextView) getActivity().findViewById(R.id.rawlog_text);
    }

    @Override
    public void onResume() {
        super.onResume();

        newLineReceiver = new NewLineReceiver();
        getActivity().registerReceiver(newLineReceiver, new IntentFilter(I.UPDATE_NEWLINE));
    }

    @Override
    public void onPause() {
        if (newLineReceiver != null) {
            getActivity().unregisterReceiver(newLineReceiver);
            newLineReceiver = null;
        }

        super.onPause();
    }
}
