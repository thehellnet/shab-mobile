package org.thehellnet.shab.mobile.config;

import com.google.android.gms.maps.GoogleMap;

/**
 * Created by sardylan on 17/07/16.
 */
public final class Prefs {

    public static final class Default {

        public static final String DEVICE_ID = "";

        public static final boolean PERMISSIONS_ASK = false;

        public static final boolean MAP_AUTOBOUNDS = true;
        public static final int MAP_TYPE = GoogleMap.MAP_TYPE_SATELLITE;
        public static final float MAP_ZOOMLEVEL = 15;

        public static final String SERVER_ADDRESS = "caronte.thehellnet.org";
        public static final int SOCKET_PORT = 12345;
        public static final String NAME = "device_name";
    }

    public static final String DEVICE_ID = "device_id";

    public static final String PERMISSIONS_ASK = "permissions_ask";

    public static final String MAP_AUTOBOUNDS = "map_autobounds";
    public static final String MAP_TYPE = "map_type";
    public static final String MAP_ZOOMLEVEL = "map_zoomlevel";

    public static final String SERVER_ADDRESS = "socket_address";
    public static final String SOCKET_PORT = "socket_port";
    public static final String NAME = "device_name";
}
