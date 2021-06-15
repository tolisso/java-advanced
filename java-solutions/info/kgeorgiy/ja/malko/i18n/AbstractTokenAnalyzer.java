package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class AbstractTokenAnalyzer<T> extends AbstractAnalyzer<T> {

    private final Comparator<? super T> comparator;

    public AbstractTokenAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, String bindingType, BufferedWriter writer, Comparator<? super T> comparator) {
        super(text, textLocale, bundle, bindingType, writer);
        this.comparator = comparator;
    }

    public void getStatistics() throws IOException {
        List<T> sentences = getTokens();
        printHat();
        printTokensNumber(sentences);
        if (sentences.isEmpty()) {
            return;
        }
        printFirstByComparator(sentences, comparator.reversed(), false);
        printFirstByComparator(sentences, comparator, true);
        printAverage(sentences);
    }

    @Override
    protected List<T> getTokens() {
        BreakIterator it = BreakIterator.getWordInstance(textLocale);
        it.setText(text);
        List<T> sentences = new ArrayList<>();

        int skipOver = -1;
        for (int cur = it.first(); cur != BreakIterator.DONE; cur = it.next()) {
            if (skipOver >= cur) {
                continue;
            }
            ParsePosition pos = new ParsePosition(cur);
            T val = parse(pos);
            if (val != null) {
                sentences.add(val);
                skipOver = pos.getIndex();
            }
        }
        return sentences;
    }

    protected static final int NOT_PARSED = -1;
    protected abstract T parse(ParsePosition pos);
}
