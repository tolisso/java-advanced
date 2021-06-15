package info.kgeorgiy.ja.malko.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemotePerson extends AbstractPerson {

    public RemotePerson(String firstName, String secondName, String passport, int port)
        throws RemoteException {
        super(firstName, secondName, passport);
        UnicastRemoteObject.exportObject(this, port);
    }
}
