package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Locale;

public class CurrencyAnalyzer extends NumberAnalyzer {
    public CurrencyAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        super(text, textLocale, bundle, "currency", writer);
    }

    @Override
    protected Number parse(ParsePosition pos) {
        NumberFormat parser = NumberFormat.getCurrencyInstance(textLocale);
        return parser.parse(text, pos);
    }
}
