package org.thehellnet.shab.protocol.bean;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by sardylan on 19/07/16.
 */
public class Client implements Serializable {

    private String id;
    private String name;
    private DateTime lastContact = new DateTime();
    private Position position = new Position();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getLastContact() {
        return lastContact;
    }

    public void setLastContact(DateTime lastContact) {
        this.lastContact = lastContact;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setPosition(double latitude, double longitude, double altitude) {
        position.setLatitude(latitude);
        position.setLongitude(longitude);
        position.setAltitude(altitude);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Client)) return false;

        Client client = (Client) o;

        return getId().equals(client.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
