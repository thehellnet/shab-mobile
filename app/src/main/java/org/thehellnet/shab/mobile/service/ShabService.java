package org.thehellnet.shab.mobile.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.thehellnet.shab.mobile.config.C;
import org.thehellnet.shab.mobile.protocol.ShabSocket;
import org.thehellnet.shab.mobile.protocol.ShabSocketCallback;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabService extends Service implements ShabSocketCallback {

    private static final String TAG = ShabService.class.getSimpleName();

    private final Object SYNC_START = new Object();

    private ShabSocket shabSocket;
    private boolean alreadyStarted = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        synchronized (SYNC_START) {
            if (!alreadyStarted) {
                start();
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public void connected() {
        shabSocket.send("C");
    }

    @Override
    public void newLine(String line) {
        Log.i(TAG, String.format("New line from socket: %s", line));
    }

    @Override
    public void disconnected() {
        shabSocket.stop();
        shabSocket = null;

        alreadyStarted = false;
    }

    private void start() {
        shabSocket = new ShabSocket(this);
        shabSocket.start(C.SOCKET_ADDRESS, C.SOCKET_PORT);

        alreadyStarted = true;
    }
}
