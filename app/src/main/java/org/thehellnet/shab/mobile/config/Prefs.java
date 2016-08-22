package org.thehellnet.shab.mobile.config;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by sardylan on 17/07/16.
 */
public final class Prefs {

    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_ID_DEFAULT = "";

    public static final String MAP_AUTOBOUNDS = "map_autobounds";
    public static final boolean MAP_AUTOBOUNDS_DEFAULT = true;
    public static final String MAP_TYPE = "map_type";
    public static final int MAP_TYPE_DEFAULT = GoogleMap.MAP_TYPE_SATELLITE;

    public static final String SERVER_ADDRESS = "socket_address";
    public static final String SERVER_ADDRESS_DEFAULT = "caronte.thehellnet.org";
    public static final String SOCKET_PORT = "socket_port";
    public static final int SOCKET_PORT_DEFAULT = 12345;
    public static final String NAME = "device_name";
    public static final String NAME_DEFAULT = "device_name";
}
