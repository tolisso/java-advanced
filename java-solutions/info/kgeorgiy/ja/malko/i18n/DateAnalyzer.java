package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DateAnalyzer extends AbstractTokenAnalyzer<Date> {

    public DateAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        super(text, textLocale, bundle, "date", writer,
            Comparator.naturalOrder());
    }

    @Override
    protected Date parse(ParsePosition pos) {
        DateFormat[] formats = new DateFormat[]{
            DateFormat.getDateInstance(DateFormat.FULL, textLocale),
            DateFormat.getDateInstance(DateFormat.LONG, textLocale),
            DateFormat.getDateInstance(DateFormat.MEDIUM, textLocale),
            DateFormat.getDateInstance(DateFormat.SHORT, textLocale)
        };
        Date res = null;
        for (DateFormat format : formats) {
            res = format.parse(text, pos);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    @Override
    protected Object getAverage(List<Date> sentences) {
        long time = sentences
            .stream()
            .map(Date::getTime)
            .reduce(0L, Long::sum);

        return new Date(time / sentences.size());
    }
}
