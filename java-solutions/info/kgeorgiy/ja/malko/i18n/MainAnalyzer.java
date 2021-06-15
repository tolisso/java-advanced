package info.kgeorgiy.ja.malko.i18n;

import info.kgeorgiy.ja.malko.i18n.bundle.AbstractUniqueBundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainAnalyzer extends TextContainer {
    private static final String BUNDLE_PREFIX =
        "info.kgeorgiy.ja.malko.i18n.bundle.UniqueBundle";

    public MainAnalyzer(String text, Locale textLocale, AbstractUniqueBundle bundle,
        BufferedWriter writer) {
        super(text, textLocale, bundle, writer);
    }

    public void getStatistics() throws IOException {
        TextContainer[] arr = new TextContainer[] {
            new SentenceAnalyzer(text, textLocale, bundle, writer),
            new WordAnalyzer(text, textLocale, bundle, writer),
            new NumberAnalyzer(text, textLocale, bundle, writer),
            new CurrencyAnalyzer(text, textLocale, bundle, writer),
            new DateAnalyzer(text, textLocale, bundle, writer)
        };
        printMainStatistics(arr);
        for (TextContainer textContainer : arr) {
            textContainer.getStatistics();
        }

    }

    private void printMainStatistics(TextContainer[] arr) throws IOException {
        MessageFormat mf = new MessageFormat(bundle.getString("main"), bundle.getLocale());
        writer.write(mf.format(
            Arrays.stream(arr)
                .map(TextContainer::getTokensAmount)
                .toArray()));
        writer.newLine();
    }

    public static void main(String ... args) {
        try {
            checkArgs(args);
            StringBuilder text = new StringBuilder();
            Locale locale = getLocale(args[0]);
            try (BufferedReader bf = Files.newBufferedReader(getPath(args[2]))) {
                char[] buffer = new char[2000];
                int length;
                while ((length = bf.read(buffer)) != -1) {
                    text.append(String.valueOf(buffer, 0, length));
                }
            } catch (IOException e) {
                throw new AnalyzerException(
                    "IOException occurred while reading: " + e.getMessage());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(getPath(args[3]))) {
                MainAnalyzer analyzer = new MainAnalyzer(text.toString(),
                    locale, getBundle(args[1]), writer);
                analyzer.getStatistics();
            } catch (IOException e) {
                throw new AnalyzerException("Can not write: " + e.getMessage());
            }
        } catch (AnalyzerException exc) {
            System.err.println(exc.getMessage());
        }
    }

    private static AbstractUniqueBundle getBundle(String locale) {
        try {
            return (AbstractUniqueBundle) ResourceBundle
                .getBundle(BUNDLE_PREFIX, getLocale(locale));
        } catch (MissingResourceException exc) {
            throw new AnalyzerException(locale + " locale is not supporting");
        }
    }

    private static Locale getLocale(String localeStr) {
        Locale locale = new Locale(localeStr);
        return locale;
    }

    private static Path getPath(String path) {
        try {
            return Path.of(path);
        } catch (InvalidPathException exc) {
            throw new AnalyzerException("Invalid path: " + path);
        }
    }

    private static void checkArgs(String[] args) {
        if (args == null || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            throw new AnalyzerException("you must provide 4 non null arguments");
        }
    }

    @Override
    public int getTokensAmount() {
        throw new UnsupportedOperationException();
    }
}
