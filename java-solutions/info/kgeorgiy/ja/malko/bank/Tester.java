package info.kgeorgiy.ja.malko.bank;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Tester {

    @BeforeClass
    public static void createBank() {
        Server.launchBank();
    }

    private Bank getBank() {
        try {
            return (Bank) LocateRegistry.getRegistry(8888).lookup("bank");
        } catch (final NotBoundException | RemoteException e) {
            fail("Bank can't be received: " + e.getMessage());
            throw new RuntimeException("This can't be reached");
        }
    }

    private Person getPerson(int i, boolean remote) {
        try {
            String passport = "passport" + i;
            getBank().addPerson("name" + i, "second-name" + i, passport);
            if (remote) {
                return getBank().getRemotePerson(passport);
            } else {
                return getBank().getLocalPerson(passport);
            }
        } catch (RemoteException e) {
            fail("Can't get person: " + e.getMessage());
            throw new RuntimeException("This can't be reached");
        }
    }

    private Account getAccount(Person person, int accountId) {
        try {
            getBank().addAccount(person, accountId);
            return getBank().getAccount(person, accountId);
        } catch (RemoteException e) {
            fail("Can't get account: " + e.getMessage());
            throw new RuntimeException("This can't be reached");
        }
    }

    private int addMoney(Account account) {
        try {
            account.addMoney(100);
            return account.getMoney();
        } catch (RemoteException e) {
            fail("Can't increase money: " + e.getMessage());
            return -1;
        }
    }

    private void fail(String message) {
        System.out.println(message);
        Assert.fail();
    }

    @Test
    public void test01_basicFunctionality() {
        Person remotePerson = getPerson(1, true);
        Account remoteAccount = getAccount(remotePerson, 1);
        int money = 0;
        for (int i = 0; i < 5; i++) {
            money = addMoney(remoteAccount);
        }
        if (money != 500) {
            fail("Money on remote not match 500: " + money);
        }
        Person localPerson = getPerson(1, false);
        Account localAccount = getAccount(localPerson, 1);
        for (int i = 0; i < 5; i++) {
            money = addMoney(localAccount);
        }
        if (money != 1000) {
            fail("Money on local not match 1000: " + money);
        }
        money = addMoney(remoteAccount);
        if (money != 600) {
            fail("Local person affects on remote. Money on remote not match 600: " + money);
        }
    }

    private int addMoney(Person person, int id) {
        return addMoney(getAccount(person, id));
    }

    @Test
    public void test02_differentPeople() {
        Person person1_1 = getPerson(2, true);
        Person person1_2 = getPerson(2, true);
        Person person2 = getPerson(3, true);

        addMoney(person1_1, 1);
        addMoney(person1_2, 1);
        Assert.assertEquals(300, addMoney(person1_1, 1));
        Assert.assertEquals(100, addMoney(person2, 1));
        Assert.assertEquals(100, addMoney(person1_1, 2));
        Assert.assertEquals(200, addMoney(person1_2, 2));
    }

    @Test
    public void test02_parallelism() {
        int personId = 4;
        int numberOfStreams = 100;
        IntStream
            .range(0, numberOfStreams)
            .parallel()
            .mapToObj(i -> getAccount(getPerson(personId, true), 1))
            .forEach(this::addMoney);

        Assert.assertEquals((numberOfStreams + 1) * 100,
            addMoney(getAccount(getPerson(personId, true), 1)));
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        System.exit(junit.run(Tester.class).wasSuccessful() ? 0 : 1);
    }
}
