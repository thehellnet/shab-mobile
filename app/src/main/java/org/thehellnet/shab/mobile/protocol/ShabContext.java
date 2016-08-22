package org.thehellnet.shab.mobile.protocol;

import org.thehellnet.shab.protocol.entity.Client;
import org.thehellnet.shab.protocol.entity.Hab;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sardylan on 14/08/16.
 */
public final class ShabContext {

    private static ShabContext instance;

    private Hab hab = new Hab();
    private Client localClient = new Client();
    private List<Client> remoteClients = new ArrayList<>();

    private ShabContext() {
    }

    public static ShabContext getInstance() {
        if (instance == null) {
            instance = new ShabContext();
        }
        return instance;
    }

    public void clear() {
        localClient.setPosition(null);
        hab.setPosition(null);
        remoteClients.clear();
    }

    public Client findRemoteClientById(String id) {
        for (Client client : remoteClients) {
            if (client.getId().equals(id)) {
                return client;
            }
        }
        return null;
    }

    public Client getLocalClient() {
        return localClient;
    }

    public void setLocalClient(Client localClient) {
        this.localClient = localClient;
    }

    public Hab getHab() {
        return hab;
    }

    public void setHab(Hab hab) {
        this.hab = hab;
    }

    public List<Client> getRemoteClients() {
        return remoteClients;
    }

    public void setRemoteClients(List<Client> remoteClients) {
        this.remoteClients = remoteClients;
    }
}
