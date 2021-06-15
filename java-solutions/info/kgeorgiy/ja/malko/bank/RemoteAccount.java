package info.kgeorgiy.ja.malko.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteAccount extends AbstractAccount {

    RemoteAccount(int id, int port) throws RemoteException {
        super(id);
        UnicastRemoteObject.exportObject(this, port);
    }
}
