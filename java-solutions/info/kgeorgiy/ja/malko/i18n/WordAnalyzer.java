package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WordAnalyzer extends AbstractStringAnalyzer {

    public WordAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, BufferedWriter writer) {
        super(text, textLocale, bundle, "word", writer);
    }

    protected List<String> getTokens() {
        BreakIterator it = BreakIterator.getWordInstance(textLocale);
        List<String> sentences = getAllTokens(it);

        sentences = sentences
            .stream()
            .filter(s -> {
                for (int i = 0; i < s.length(); i++) {
                    if (Character.isLetter(s.charAt(i))) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
        return sentences;
    }
}
