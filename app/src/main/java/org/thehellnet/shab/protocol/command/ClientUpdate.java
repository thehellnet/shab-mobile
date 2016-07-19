package org.thehellnet.shab.protocol.command;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.thehellnet.shab.protocol.bean.Client;
import org.thehellnet.shab.protocol.bean.Position;

import java.util.Locale;

/**
 * Created by sardylan on 19/07/16.
 */
public class ClientUpdate {

    public static final String COMMAND = "C";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyyMMddHHmmss");

    public static Client parse(String rawData) {
        return parse(null, rawData);
    }

    public static Client parse(Client client, String rawData) {
        if (rawData == null || rawData.length() == 0) {
            return null;
        }
        if (client == null) {
            client = new Client();
        }

        String[] items = rawData.split("\\|");
        if (items.length != 7 || !items[0].equals(COMMAND)) {
            return null;
        }
        client.setId(items[2]);
        client.setName(items[3]);
        client.setLastContact(DATE_TIME_FORMATTER.parseDateTime(items[1]));

        Position position = new Position();
        position.setLatitude(Double.parseDouble(items[4]));
        position.setLongitude(Double.parseDouble(items[5]));
        position.setAltitude(Double.parseDouble(items[6]));
        client.setPosition(position);

        return client;
    }

    public static String serialize(Client client) {
        return serialize(new DateTime(), client);
    }

    public static String serialize(DateTime dateTime, Client client) {
        return String.format(Locale.US, "%s|%s|%s|%s|%.06f|%.06f|%.06f",
                COMMAND,
                DATE_TIME_FORMATTER.print(dateTime),
                client.getId(),
                client.getName(),
                client.getPosition().getLatitude(),
                client.getPosition().getLongitude(),
                client.getPosition().getAltitude());
    }
}
