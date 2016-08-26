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

    private Hab hab;
    private Client localClient;
    private List<Client> remoteClients;

    private ShabContext() {
        clear();
    }

    public static ShabContext getInstance() {
        if (instance == null) {
            instance = new ShabContext();
        }
        return instance;
    }

    public void clear() {
        hab = new Hab();
        localClient = new Client();
        remoteClients = new ArrayList<>();
    }

    public Client findRemoteClientById(String id) {
        for (Client client : remoteClients) {
            if (client.getId().equals(id)) {
                return client;
            }
        }
        return null;
    }

    public Hab getHab() {
        return hab;
    }

    public void setHab(Hab hab) {
        this.hab = hab;
    }

    public Client getLocalClient() {
        return localClient;
    }

    public void setLocalClient(Client localClient) {
        this.localClient = localClient;
    }

    public List<Client> getRemoteClients() {
        return remoteClients;
    }

    public void setRemoteClients(List<Client> remoteClients) {
        this.remoteClients = remoteClients;
    }
}
