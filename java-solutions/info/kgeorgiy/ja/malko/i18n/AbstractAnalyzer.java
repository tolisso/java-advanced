package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public abstract class AbstractAnalyzer<T> extends TextContainer {

    protected static final String padding = "\t";

    protected final String bindingType;

    public AbstractAnalyzer(String text, Locale textLocale,
        AbstractUniqueBundle bundle, String bindingType, BufferedWriter writer) {
        super(text, textLocale, bundle, writer);
        this.bindingType = bindingType;
    }

    protected void printWithFormat(String patternName, Object ... args) throws IOException {
        String pattern = bundle.getString(patternName);
        if (pattern.contains("<!>")) {
            String type = getWithBindingType(patternName + "-type");

            pattern = pattern
                .replace("<!>", type);
        }


        String res = new MessageFormat(pattern, bundle.getLocale()).format(args);
        writer.write(res);
        writer.newLine();
    }

    protected String getWithBindingType(String val) {
        return bundle.getString(bindingType + "-" + val);
    }

    protected void printHat() throws IOException {
        printWithFormat("hat",
            getWithBindingType("hat"));
    }

    protected void printTokensNumber(List<T> sentences) throws IOException {
        int unique = new HashSet<>(sentences).size();
        writer.write(padding);
        printWithFormat("number",
            getWithBindingType("number"),
            sentences.size(),
            unique,
            bundle.getUnique(unique));
    }

    protected String getMinMax(boolean max, String prefix) {
        return max ? bundle.getString(prefix + "Max") :
            bundle.getString(prefix + "Min");
    }

    protected void printFirstByComparator(List<T> sentences,
        Comparator<? super T> comparator, boolean max) throws IOException {
        T sentence = getMax(sentences, comparator);
        String prefix = getMinMax(max, "comparator");
        writer.write(padding);
        printWithFormat("byComparator",
            prefix,
            getWithBindingType("byComparator"),
            sentence);
    }

    protected T getMax(List<T> sentences, Comparator<? super T> comparator) {
        return sentences
            .stream()
            .max(comparator)
            .orElseThrow(() ->
                new RuntimeException("Unexpected exception: can not find max"));
    }

    protected abstract List<T> getTokens();

    public int getTokensAmount() {
        return getTokens().size();
    }

    protected void printAverage(List<T> sentences) throws IOException {

        Object average = getAverage(sentences);
        writer.write(padding);
        printWithFormat("average",
            getWithBindingType("average"),
            average);
    }

    protected abstract Object getAverage(List<T> sentences);
}
