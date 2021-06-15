package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public abstract class AbstractStringAnalyzer extends AbstractAnalyzer<String> {

    public AbstractStringAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, String bindingType, BufferedWriter writer) {
        super(text, textLocale, bundle, bindingType, writer);
    }

    public void getStatistics() throws IOException {
        List<String> sentences = getTokens();
        printHat();
        printTokensNumber(sentences);
        if (sentences.isEmpty()) {
            return;
        }
        printFirstByComparator(sentences, collator.reversed(), false);
        printFirstByComparator(sentences, collator, true);
        Comparator<String> lengthCmp = Comparator.comparingInt(String::length);
        printFirstByLength(sentences, lengthCmp.reversed(), false);
        printFirstByLength(sentences, lengthCmp, true);
        printAverage(sentences);
    }

    private void printFirstByLength(List<String> sentences,
        Comparator<String> lengthCmp, boolean max) throws IOException {
        String sentence = getMax(sentences, lengthCmp);
        String prefix = getMinMax(max, "length");
        if (sentence != null) {
            writer.write(padding);
            printWithFormat("byLength",
                prefix, getWithBindingType("byLength"),
                sentence.length(), sentence);
        }
    }

    protected Object getAverage(List<String> sentences) {
        int sum = sentences
            .stream()
            .map(String::length)
            .reduce(0, Integer::sum);
        return ((double)sum) / sentences.size();
    }

    protected List<String> getAllTokens(BreakIterator it) {
        it.setText(text);
        List<String> sentences = new ArrayList<>();

        it.first();
        for (int cur = it.next(), prev = 0; cur != BreakIterator.DONE; prev = cur, cur = it.next()) {
            sentences.add(text.substring(prev, cur));
        }
        return sentences;
    }
}
