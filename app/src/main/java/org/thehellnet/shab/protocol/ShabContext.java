package org.thehellnet.shab.protocol;

import org.thehellnet.shab.protocol.entity.Client;
import org.thehellnet.shab.protocol.entity.Hab;

import java.util.List;

/**
 * Created by sardylan on 14/08/16.
 */
public class ShabContext {

    private Client localClient = new Client();
    private Hab hab;
    private List<Client> remoteClients;

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
