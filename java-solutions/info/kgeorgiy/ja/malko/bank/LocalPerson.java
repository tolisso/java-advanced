package info.kgeorgiy.ja.malko.bank;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LocalPerson extends AbstractPerson {
    private Map<Integer, Account> accounts;

    public LocalPerson(String firstName, String secondName, String passport,
        Map<Integer, Account> accounts) {
        super(firstName, secondName, passport);
        this.accounts = accounts;
    }

    public Map<Integer, Account> getAccounts() {
        return accounts;
    }
}
