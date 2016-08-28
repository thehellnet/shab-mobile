package org.thehellnet.shab.mobile.protocol.socket;

import android.util.Log;

import org.thehellnet.shab.protocol.line.Line;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by sardylan on 19/07/16.
 */
public class ShabSocket {

    private static final String TAG = ShabSocket.class.getSimpleName();
    private static final int SOCKET_CONNECT_TIMEOUT = 10000;

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
                Log.d(TAG, "Thread started");

                while (keepRunning) {
                    Log.d(TAG, "Connecting");
                    socket = new Socket();
                    try {
                        socket.connect(new InetSocketAddress(address, port), SOCKET_CONNECT_TIMEOUT);
                        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    } catch (IOException e) {
                        Log.d(TAG, "Error connecting");
                        if (!sleep()) break;
                        continue;
                    }

                    Log.d(TAG, "Connected");
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
                    } catch (IOException ignored) {
                    }

                    Log.d(TAG, "Disconnected");
                    callback.disconnected();
                    socket = null;

                    if (!keepRunning) break;
                    if (!sleep()) break;

                    Log.d(TAG, "Reconnecting");
                    callback.reconnecting();
                }
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
        } catch (IOException ignored) {
        }

        if (thread != null) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void send(Line line) {
        send(line.serialize());
    }

    public void send(String line) {
        if (line == null || line.length() == 0) return;
        if (socket == null || !socket.isConnected() || writer == null) return;
        if (lastLine != null && lastLine.equals(line)) return;

        lastLine = line;
        writer.println(lastLine);
        writer.flush();
    }

    private boolean sleep() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }
}
