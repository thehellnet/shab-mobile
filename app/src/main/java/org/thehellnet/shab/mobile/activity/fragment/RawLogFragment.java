package org.thehellnet.shab.mobile.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
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
            if (rawlogText != null
                    && scrollView != null
                    && line != null) {
                appendLogLine(line);
            }
        }
    }

    private static final String TAG = RawLogFragment.class.getSimpleName();
    private static final int RAWLOG_MAX_LINES = 150;

    private NewLineReceiver newLineReceiver;

    private TextView rawlogText;
    private ScrollView scrollView;

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
        scrollView = (ScrollView) getActivity().findViewById(R.id.rawlog_scroll_container);
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

    private void appendLogLine(Line line) {
        rawlogText.append(String.format("%s - %s\n", DateTime.now(), line.serialize()));

        int linesToRemove = rawlogText.getLineCount() - RAWLOG_MAX_LINES;
        if (linesToRemove > 0) {
            for (int i = 0; i < linesToRemove; i++) {
                Editable text = rawlogText.getEditableText();
                int lineStart = rawlogText.getLayout().getLineStart(0);
                int lineEnd = rawlogText.getLayout().getLineEnd(0);
                text.delete(lineStart, lineEnd);
            }
        }

        Log.d(TAG, String.valueOf(rawlogText.getLineCount()));

        scrollView.fullScroll(View.FOCUS_DOWN);
    }
}
