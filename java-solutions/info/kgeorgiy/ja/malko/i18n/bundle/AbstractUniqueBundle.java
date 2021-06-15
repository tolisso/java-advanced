package info.kgeorgiy.ja.malko.i18n.bundle;

import java.util.ListResourceBundle;

public abstract class AbstractUniqueBundle extends ListResourceBundle {
    private final Object[][] entity;

    public AbstractUniqueBundle(Object[][] entity) {
        this.entity = entity;
    }

    @Override
    protected Object[][] getContents() {
        return entity;
    }

    public abstract String getUnique(int val);
}
