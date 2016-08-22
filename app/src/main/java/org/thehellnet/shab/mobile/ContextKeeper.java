package org.thehellnet.shab.mobile;

import android.content.Context;

/**
 * Created by sardylan on 22/08/16.
 */
public final class ContextKeeper {

    private static ContextKeeper instance = new ContextKeeper();

    private Context context;

    private ContextKeeper() {

    }

    public static ContextKeeper getInstance() {
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
