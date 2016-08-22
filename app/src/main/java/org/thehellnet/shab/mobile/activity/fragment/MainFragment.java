package org.thehellnet.shab.mobile.activity.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.thehellnet.shab.mobile.R;
import org.thehellnet.shab.mobile.config.I;
import org.thehellnet.shab.mobile.utility.Formatter;
import org.thehellnet.shab.mobile.utility.ImageManipulation;
import org.thehellnet.shab.mobile.protocol.ShabContext;

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

    private static final String TAG = MainFragment.class.getSimpleName();

    private ShabContext shabContext = ShabContext.getInstance();

    private CommandHabPositionReceiver commandHabPositionReceiver;
    private CommandHabImageReceiver commandHabImageReceiver;

    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView altitudeTextView;
    private TextView fixStatusTextView;
    private TextView speedTextView;
    private TextView angleTextView;

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

        commandHabPositionReceiver = new CommandHabPositionReceiver();
        getContext().registerReceiver(commandHabPositionReceiver, new IntentFilter(I.COMMAND_HAB_POSITION));

        commandHabImageReceiver = new CommandHabImageReceiver();
        getContext().registerReceiver(commandHabImageReceiver, new IntentFilter(I.COMMAND_HAB_IMAGE));
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

        super.onPause();
    }

    private void initVars() {
        latitudeTextView = (TextView) getActivity().findViewById(R.id.infos_latitude_value);
        longitudeTextView = (TextView) getActivity().findViewById(R.id.infos_longitude_value);
        altitudeTextView = (TextView) getActivity().findViewById(R.id.infos_altitude_value);
        fixStatusTextView = (TextView) getActivity().findViewById(R.id.infos_fixstatus_value);
        speedTextView = (TextView) getActivity().findViewById(R.id.infos_speed_value);
        angleTextView = (TextView) getActivity().findViewById(R.id.infos_angle_value);

        imageProgressBar = (ProgressBar) getActivity().findViewById(R.id.image_progress_value);
        imageView = (ImageView) getActivity().findViewById(R.id.infos_image);
    }

    private void updateInfos() {
        if (latitudeTextView != null)
            latitudeTextView.setText(Formatter.coordinateToString(shabContext.getHab().getPosition().getLatitude()));
        if (longitudeTextView != null)
            longitudeTextView.setText(Formatter.coordinateToString(shabContext.getHab().getPosition().getLongitude()));
        if (altitudeTextView != null)
            altitudeTextView.setText(Formatter.coordinateToString(shabContext.getHab().getPosition().getAltitude()));
        if (fixStatusTextView != null)
            fixStatusTextView.setText(shabContext.getHab().getFixStatus().toString());
    }

    private void updateImageSlice() {
        if (imageProgressBar != null) {
            imageProgressBar.setMax(shabContext.getHab().getSliceTot());
            imageProgressBar.setProgress(shabContext.getHab().getSliceNum());
        }

        if (shabContext.getHab().getSliceNum() == shabContext.getHab().getSliceTot()
                && imageView != null) {
            byte[] imageData = shabContext.getHab().getImageData();
            Log.d(TAG, String.format("Image header and footer: %02X%02X - %02X%02X",
                    imageData[0], imageData[1],
                    imageData[imageData.length - 2], imageData[imageData.length - 1]));
            imageView.setImageBitmap(ImageManipulation.createBitmap(imageData));
        }
    }
}
