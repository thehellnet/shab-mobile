package org.thehellnet.shab.protocol;

import org.thehellnet.shab.protocol.bean.Client;
import org.thehellnet.shab.protocol.bean.HAB;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sardylan on 19/07/16.
 */
public class DataKeeper {

    private Client client;
    private HAB hab;
    private Set<Client> clients = new HashSet<>();

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public HAB getHab() {
        return hab;
    }

    public void setHab(HAB hab) {
        this.hab = hab;
    }

    public Set<Client> getClients() {
        return clients;
    }

    public void setClients(Set<Client> clients) {
        this.clients = clients;
    }
}
