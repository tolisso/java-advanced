package info.kgeorgiy.ja.malko.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RemoteBank extends UnicastRemoteObject implements Bank {
    private int port;
    private final ConcurrentHashMap<String, Person> passportToPerson
        = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Map<Integer, Account>> passportToAccount
        = new ConcurrentHashMap<>();

    public RemoteBank(int port) throws RemoteException {
        super(port);
        this.port = port;
    }

    @Override
    public Person getLocalPerson(String passport) throws RemoteException {
        Person p = passportToPerson.getOrDefault(passport, null);
        if (p == null) {
            return null;
        }
        String firstName = p.getFirstName();
        String secondName = p.getSecondName();

        Map<Integer, Account> accounts = new HashMap<>();
        for (Account a : passportToAccount.getOrDefault(passport, new HashMap<>()).values()) {
            accounts.put(a.getId(), new LocalAccount(a.getId(), a.getMoney()));
        }

        return new LocalPerson(firstName, secondName, passport, accounts);
    }

    @Override
    public Person getRemotePerson(String passport) {
        return passportToPerson.getOrDefault(passport, null);
    }

    @Override
    public boolean addPerson(String firstName, String secondName, String passport)
        throws RemoteException {
        Person p = new RemotePerson(firstName, secondName, passport, port);
        if (passportToPerson.putIfAbsent(passport, p) == null) {
            passportToAccount.putIfAbsent(passport, new ConcurrentHashMap<>());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Account getAccount(Person person, int id) throws RemoteException {
        if (person instanceof LocalPerson) {
            return ((LocalPerson) person).getAccounts().getOrDefault(id, null);
        }
        var map = passportToAccount.getOrDefault(person.getPassport(), null);
        if (map == null) {
            return null;
        }
        return map.getOrDefault(id, null);
    }

    @Override
    public boolean addAccount(Person person, int id) throws RemoteException {
        if (person instanceof LocalPerson) {
            LocalPerson localPerson = ((LocalPerson) person);
            return localPerson
                .getAccounts()
                .putIfAbsent(id, new LocalAccount(id, 0)) == null;
        } else {
            var map = passportToAccount.getOrDefault(person.getPassport(), null);
            if (map == null) {
                return false;
            }
            return map.putIfAbsent(id, new RemoteAccount(id, port)) == null;
        }
    }
}
