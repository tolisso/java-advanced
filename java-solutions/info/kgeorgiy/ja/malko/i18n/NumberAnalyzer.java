package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class NumberAnalyzer extends AbstractTokenAnalyzer<Number> {

    public NumberAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        this(text, textLocale, bundle, "number", writer);
    }

    public NumberAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, String bindingType, BufferedWriter writer) {
        super(text, textLocale, bundle, bindingType, writer,
            Comparator.comparingDouble(Number::doubleValue));
    }

    @Override
    protected Number parse(ParsePosition pos) {
        NumberFormat parser = NumberFormat.getInstance(textLocale);
        return parser.parse(text, pos);
    }

    @Override
    protected Object getAverage(List<Number> sentences) {
        double sum = sentences
            .stream()
            .map(Number::doubleValue)
            .reduce(0d, Double::sum);
        return sum / sentences.size();
    }
}
