package info.kgeorgiy.ja.malko.bank;

import java.io.Serializable;

public abstract class AbstractAccount implements Account, Serializable {
    protected final int id;
    protected int money;

    public AbstractAccount(int id) {
        this.id = id;
    }

    @Override
    public synchronized void addMoney(int toAdd) {
        money += toAdd;
    }

    @Override
    public synchronized int getMoney() {
        return money;
    }

    @Override
    public int getId() {
        return id;
    }
}
