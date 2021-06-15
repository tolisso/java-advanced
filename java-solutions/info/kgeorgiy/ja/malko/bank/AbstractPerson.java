package info.kgeorgiy.ja.malko.bank;

import java.io.Serializable;

public class AbstractPerson implements Person, Serializable {
    protected final String firstName;
    protected final String secondName;
    protected final String passport;

    public AbstractPerson(String firstName, String secondName, String passport) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.passport = passport;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getSecondName() {
        return secondName;
    }

    @Override
    public String getPassport() {
        return passport;
    }
}
