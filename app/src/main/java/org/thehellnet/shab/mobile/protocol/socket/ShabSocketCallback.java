package org.thehellnet.shab.mobile.protocol.socket;

/**
 * Created by sardylan on 19/07/16.
 */
public interface ShabSocketCallback {

    void connected();

    void newLine(String rawLine);

    void disconnected();
}
