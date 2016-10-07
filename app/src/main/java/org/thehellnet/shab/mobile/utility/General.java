package org.thehellnet.shab.mobile.utility;

/**
 * Created by sardylan on 26/09/16.
 */

public final class General {

    public static void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ignored) {
        }
    }
}
