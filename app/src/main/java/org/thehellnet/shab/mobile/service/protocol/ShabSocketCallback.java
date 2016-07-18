package org.thehellnet.shab.mobile.service.protocol;

/**
 * Created by sardylan on 16/07/16.
 */
public interface ShabSocketCallback {

    void connected();

    void newLine(String line);

    void disconnected();
}
