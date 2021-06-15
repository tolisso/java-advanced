package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;

public class SentenceAnalyzer extends AbstractStringAnalyzer {

    public SentenceAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        super(text, textLocale, bundle, "sentence", writer);
    }

    protected List<String> getTokens() {
        BreakIterator it = BreakIterator.getSentenceInstance(textLocale);

        return getAllTokens(it);
    }
}
