package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.Collator;
import java.util.Locale;

public abstract class TextContainer {
    protected final String text;
    protected final Locale textLocale;
    protected final AbstractUniqueBundle bundle;
    protected final Collator collator;
    protected final BufferedWriter writer;

    public TextContainer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        this.text = text;
        this.textLocale = textLocale;
        this.bundle = bundle;
        collator = Collator.getInstance(textLocale);
        this.writer = writer;
    }

    public abstract void getStatistics() throws IOException;
    public abstract int getTokensAmount();
}
