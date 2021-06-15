package info.kgeorgiy.ja.malko.bank;

import java.io.Serializable;

public class LocalAccount extends AbstractAccount {

    public LocalAccount(int id, int money) {
        super(id);
        this.money = money;
    }
}
