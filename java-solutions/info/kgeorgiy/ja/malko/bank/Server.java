package info.kgeorgiy.ja.malko.bank;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public final class Server {
    private final static int DEFAULT_PORT = 8888;

    public static void main(final String... args) {
        launchBank();
    }

    public static void launchBank() {
        final int port = DEFAULT_PORT;
        final Bank bank;
        try {
            bank = new RemoteBank(port);

            Registry r = LocateRegistry.createRegistry(port);
            r.rebind("bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
