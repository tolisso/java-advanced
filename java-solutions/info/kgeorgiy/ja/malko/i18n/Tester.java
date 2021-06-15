package info.kgeorgiy.ja.malko.i18n;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class Tester {

    @Test
    public void test01_empty_ru() {
        test(1,"ru", "ru");
    }

    @Test
    public void test02_empty_en() {
        test(2,"ru", "en");
    }

    @Test
    public void test03_full_en() {
        test(3,"en", "en");
    }

    @Test
    public void test04_full_ru() {
        test(4,"en", "ru");
    }

    @Test
    public void test05_arabic() {
        test(5,"ar", "ru");
    }

    @Test
    public void test06_simple() {
        test(6,"en", "ru");
    }

    @Test
    public void test07_ar_ru_full() {
        test(7,"ar", "ru");
    }

    @Test
    public void test08_ru_en_full() {
        test(8,"ru", "en");
    }

    String readFile(int num, String name) {
        StringBuilder text = new StringBuilder();
        try (BufferedReader bf = Files.newBufferedReader(Path.of(getTestFile(num, name)))) {
            char[] buffer = new char[2000];
            int length;
            while ((length = bf.read(buffer)) != -1) {
                text.append(String.valueOf(buffer, 0, length));
            }
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred while reading: " + e.getMessage());
        }
        return text.toString();
    }

    private void test(int num, String in, String out) {
        MainAnalyzer.main(
            in, out,
            getTestFile(num, "input.txt"),
            getTestFile(num, "current.txt"));
        Assert.assertEquals(
            readFile(num, "output.txt"),
            readFile(num, "current.txt"));
        try {
            Files.deleteIfExists(Path.of(getTestFile(num, "current.txt")));
        } catch (IOException e) {
            System.err.print("Can't delete out");
        }
    }

    private String getTestFile(int num, String s) {
        return Path.of("test", num + "", s).toString();
    }

    public static void main(String[] args) {
        JUnitCore junit = new JUnitCore();
        junit.addListener(new TextListener(System.out));
        System.exit(junit.run(Tester.class).wasSuccessful() ? 0 : 1);
    }
}