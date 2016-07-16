package org.thehellnet.shab.mobile.protocol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabSocket {

    private static final String TAG = ShabSocket.class.getSimpleName();

    private ShabSocketCallback callback;

    private Socket socket;
    private Thread thread;
    private boolean keepRunning;

    private BufferedReader reader;
    private PrintWriter writer;
    private String lastLine = "";

    public ShabSocket(ShabSocketCallback callback) {
        this.callback = callback;
    }

    public void start(String address, int port) {
        try {
            socket = new Socket(address, port);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
            return;
        }

        callback.connected();

        keepRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (keepRunning && socket.isConnected()) {
                        lastLine = reader.readLine();
                        if (lastLine == null) {
                            socket.close();
                            break;
                        }
                        if (lastLine.length() > 0) {
                            callback.newLine(lastLine);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                callback.disconnected();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        keepRunning = false;
        writer.close();
        try {
            reader.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void send(String line) {
        if (lastLine.equals(line) || lastLine.length() == 0) {
            return;
        }
        lastLine = line;
        writer.print(line);
        writer.print("\n");
        writer.flush();
    }
}
