package info.kgeorgiy.ja.malko.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Account extends Remote {
    void addMoney(int toAdd) throws RemoteException;
    int getMoney() throws RemoteException;
    int getId() throws RemoteException;
}
