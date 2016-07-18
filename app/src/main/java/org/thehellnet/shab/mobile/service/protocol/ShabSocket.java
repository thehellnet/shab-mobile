package org.thehellnet.shab.mobile.service.protocol;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by sardylan on 16/07/16.
 */
public class ShabSocket {

    private static final String TAG = ShabSocket.class.getSimpleName();
    private static final int SOCKET_TIMEOUT = 5000;

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

    public void start(final String address, final int port) {
        keepRunning = true;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(address, port), SOCKET_TIMEOUT);
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                    callback.disconnected();
                    return;
                }

                callback.connected();

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
                    Log.w(TAG, e.getMessage());
                }

                callback.disconnected();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void stop() {
        keepRunning = false;
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Log.w(TAG, "Thread interrupted");
            }
        }
    }

    public void send(String line) {
        if (socket == null || !socket.isConnected() || writer == null) {
            return;
        }
        if (lastLine.equals(line) || line.length() == 0) {
            return;
        }
        lastLine = line;
        writer.println(lastLine);
        writer.flush();
    }
}
