package org.thehellnet.shab.mobile.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.SHAB;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.protocol.ShabContext;
import org.thehellnet.shab.mobile.service.ShabService;
import org.thehellnet.shab.mobile.utility.Formatter;
import org.thehellnet.shab.mobile.utility.ImageManipulation;

/**
 * Created by sardylan on 17/07/16.
 */
public class MainFragment extends ShabFragment {

    private class CommandHabPositionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateInfos();
        }
    }

    private class CommandHabImageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateImageSlice();
        }
    }

    private class CommandHabTelemetryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateTelemetry();
        }
    }

    private static final String TAG = MainFragment.class.getSimpleName();

    private ShabContext shabContext = ShabContext.getInstance();

    private CommandHabPositionReceiver commandHabPositionReceiver;
    private CommandHabImageReceiver commandHabImageReceiver;
    private CommandHabTelemetryReceiver commandHabTelemetryReceiver;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView altitudeTextView;
    private TextView fixStatusTextView;
    private TextView intTempTextView;
    private TextView extTempTextView;
    private TextView extAltTextView;

    private ProgressBar imageProgressBar;
    private ImageView imageView;

    @Override
    protected int getLayout() {
        return R.layout.fragment_main;
    }

    @Override
    public void onResume() {
        super.onResume();

        initVars();
        initViews();

        if (SHAB.isServiceRunning(ShabService.class)) {
            updateViews();
        }

        commandHabPositionReceiver = new CommandHabPositionReceiver();
        getContext().registerReceiver(commandHabPositionReceiver, new IntentFilter(I.COMMAND_HAB_POSITION));

        commandHabImageReceiver = new CommandHabImageReceiver();
        getContext().registerReceiver(commandHabImageReceiver, new IntentFilter(I.COMMAND_HAB_IMAGE));

        commandHabTelemetryReceiver = new CommandHabTelemetryReceiver();
        getContext().registerReceiver(commandHabTelemetryReceiver, new IntentFilter(I.COMMAND_HAB_TELEMETRY));
    }

    @Override
    public void onPause() {
        if (commandHabImageReceiver != null) {
            getContext().unregisterReceiver(commandHabImageReceiver);
            commandHabImageReceiver = null;
        }

        if (commandHabPositionReceiver != null) {
            getContext().unregisterReceiver(commandHabPositionReceiver);
            commandHabPositionReceiver = null;
        }

        if (commandHabTelemetryReceiver != null) {
            getContext().unregisterReceiver(commandHabTelemetryReceiver);
            commandHabTelemetryReceiver = null;
        }

        super.onPause();
    }

    private void initVars() {
        latitudeTextView = (TextView) getActivity().findViewById(R.id.infos_latitude_value);
        longitudeTextView = (TextView) getActivity().findViewById(R.id.infos_longitude_value);
        altitudeTextView = (TextView) getActivity().findViewById(R.id.infos_altitude_value);
        fixStatusTextView = (TextView) getActivity().findViewById(R.id.infos_fixstatus_value);
        intTempTextView = (TextView) getActivity().findViewById(R.id.infos_int_temp_value);
        extTempTextView = (TextView) getActivity().findViewById(R.id.infos_ext_temp_value);
        extAltTextView = (TextView) getActivity().findViewById(R.id.infos_ext_alt_value);

        imageProgressBar = (ProgressBar) getActivity().findViewById(R.id.image_progress_value);
        imageView = (ImageView) getActivity().findViewById(R.id.infos_image);
    }

    private void initViews() {
//        String loadingString = getResources().getString(R.string.layout_loading);
        String loadingString = "";

        if (latitudeTextView != null)
            latitudeTextView.setText(loadingString);
        if (longitudeTextView != null)
            longitudeTextView.setText(loadingString);
        if (altitudeTextView != null)
            altitudeTextView.setText(loadingString);
        if (fixStatusTextView != null)
            fixStatusTextView.setText(loadingString);

        if (intTempTextView != null)
            intTempTextView.setText(loadingString);
        if (extTempTextView != null)
            extTempTextView.setText(loadingString);
        if (extAltTextView != null)
            extAltTextView.setText(loadingString);

        if (imageProgressBar != null) {
            imageProgressBar.setMax(1);
            imageProgressBar.setProgress(0);
        }

        if (imageView != null)
            imageView.setImageResource(R.drawable.shab_image_empty);
    }

    private void updateViews() {
        updateInfos();
        updateTelemetry();
        updateImageSlice();
    }

    private void updateInfos() {
        if (shabContext.getHab().getPosition() == null) {
            return;
        }

        if (latitudeTextView != null)
            latitudeTextView.setText(Formatter.latitudeToString(shabContext.getHab().getPosition().getLatitude()));
        if (longitudeTextView != null)
            longitudeTextView.setText(Formatter.longitudeToString(shabContext.getHab().getPosition().getLongitude()));
        if (altitudeTextView != null)
            altitudeTextView.setText(Formatter.altitudeToString(shabContext.getHab().getPosition().getAltitude()));
        if (fixStatusTextView != null)
            fixStatusTextView.setText(shabContext.getHab().getFixStatus().toString());
    }

    private void updateTelemetry() {
        if (intTempTextView != null)
            intTempTextView.setText(Formatter.temperatureToString(shabContext.getHab().getIntTemp()));
        if (extTempTextView != null)
            extTempTextView.setText(Formatter.temperatureToString(shabContext.getHab().getExtTemp()));
        if (extAltTextView != null)
            extAltTextView.setText(Formatter.altitudeToString(shabContext.getHab().getExtAlt()));
    }

    private void updateImageSlice() {
        if (imageProgressBar != null) {
            imageProgressBar.setMax(shabContext.getHab().getSliceTot());
            imageProgressBar.setProgress(shabContext.getHab().getSliceNum());
        }

        if (shabContext.getHab().getSliceNum() == shabContext.getHab().getSliceTot()
                && imageView != null) {
            byte[] imageData = shabContext.getHab().getImageData();
            imageView.setImageBitmap(ImageManipulation.createBitmap(imageData));
        }
    }
}
