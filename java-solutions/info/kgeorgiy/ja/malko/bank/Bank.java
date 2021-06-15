package info.kgeorgiy.ja.malko.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Bank extends Remote {
    Person getLocalPerson(String passport) throws RemoteException;
    Person getRemotePerson(String passport) throws RemoteException;
    boolean addPerson(String firstName, String secondName, String passport)
        throws RemoteException;
    Account getAccount(Person person, int id) throws RemoteException;
    boolean addAccount(Person person, int id) throws RemoteException;
}
