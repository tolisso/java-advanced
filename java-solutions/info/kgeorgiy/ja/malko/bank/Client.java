package info.kgeorgiy.ja.malko.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.Arrays;
import java.util.Objects;

public final class Client {

    public static void main(final String... args) {
        check(args);
        try {
            final Bank bank;
            try {
                bank = (Bank) LocateRegistry.getRegistry(8888).lookup("bank");
            } catch (final NotBoundException e) {
                System.out.println("Bank is not bound");
                return;
            }
            String name = args[0];
            String secondName = args[1];
            String passport = args[2];
            int id = Integer.parseInt(args[3]);
            int money = Integer.parseInt(args[4]);
            if (bank.addPerson(name, secondName, passport)) {
                System.out.println("Person created: " + name + " " + secondName + " " + passport);
            }
            Person person = bank.getRemotePerson(passport);
            if (bank.addAccount(person, id)) {
                System.out.println("Account with id " + id + " on passport " + passport + " created");
            }
            Account account = bank.getAccount(person, id);
            System.out.println("Adding money to account with id " + id + " on passport " + passport);
            account.addMoney(money);
            System.out.println("Money: " + account.getMoney());
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void check(String[] args) {
        if (args == null || Arrays.stream(args).anyMatch(Objects::isNull)) {
            writeException("arguments must not be null");
        }
        assert args != null;
        if (args.length != 5) {
            writeException("you must provide exactly 5 arguments:\n"
                + "<name> <second-name> <passport> <account-id> <money-delta>");
        }
        try {
            Integer.parseInt(args[3]);
        } catch (NumberFormatException exc) {
            writeException("can't parse <account-id>");
        }
        try {
            Integer.parseInt(args[4]);
        } catch (NumberFormatException exc) {
            writeException("can't parse <money-delta>");
        }
    }
    private static void writeException(String str) {
        System.err.println(str);
        System.exit(1);
    }
}

