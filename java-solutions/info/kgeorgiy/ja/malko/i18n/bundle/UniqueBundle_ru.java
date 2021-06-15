package info.kgeorgiy.ja.malko.i18n.bundle;

import java.text.ChoiceFormat;

public class UniqueBundle_ru extends AbstractUniqueBundle {

    private final ChoiceFormat format;

    public UniqueBundle_ru() {
        super(new Object[][] {
            {"main", "Сводная статистика\n"
                + "\tЧисло предложений: {0,number}.\n"
                + "\tЧисло слов: {1,number}.\n"
                + "\tЧисло чисел: {2,number}.\n"
                + "\tЧисло сумм: {3,number}.\n"
                + "\tЧисло дат: {4,number}."},
            {"hat", "Статистика по {0}"},
            {"number", "Число {0}: {1,number,integer} ({2,number,integer} {3})."},
            {"byComparator", "{0} значение {1}: <!>."},
            {"byLength", "{0} длина {1}: {2,number,integer} (\"{3}\")."},
            {"average", "{0}: <!>."},
            {"comparatorMin", "Минимальное"},
            {"comparatorMax", "Максимальное"},
            {"lengthMin", "Минимальная"},
            {"lengthMax", "Максимальная"},

            {"sentence-hat", "предложениям"},
            {"sentence-number", "предложений"},
            {"sentence-byComparator", "предложения"},
            {"sentence-byLength", "предложения"},
            {"sentence-average", "Средняя длина предложения"},
            {"sentence-byComparator-type", "\"{2}\""},
            {"sentence-average-type", "{1,number}"},

            {"word-hat", "словам"},
            {"word-number", "слов"},
            {"word-byComparator", "слова"},
            {"word-byLength", "слова"},
            {"word-average", "Средняя длина слова"},
            {"word-byComparator-type", "\"{2}\""},
            {"word-average-type", "{1,number}"},

            {"number-hat", "числам"},
            {"number-number", "чисел"},
            {"number-byComparator", "числа"},
            {"number-average", "Среднее число"},
            {"number-byComparator-type", "{2,number}"},
            {"number-average-type", "{1,number}"},

            {"currency-hat", "суммам денег"},
            {"currency-number", "сумм"},
            {"currency-byComparator", "суммы"},
            {"currency-average", "Средняя сумма"},
            {"currency-byComparator-type", "{2,number,currency}"},
            {"currency-average-type", "{1,number,currency}"},

            {"date-hat", "датам"},
            {"date-number", "дат"},
            {"date-byComparator", "даты"},
            {"date-average", "Средняя дата"},
            {"date-byComparator-type", "{2,date,long}"},
            {"date-average-type", "{1,date,long}"}
        });

        double[] limits = {0, 1, 2, 5, 21};
        String[] values = {
            "уникальных значений",
            "уникальное значение",
            "уникальных значения",
            "уникальных значений",
            "уникальных значений"
        };

        format = new ChoiceFormat(limits, values);
    }

    @Override
    public String getUnique(int val) {
        val %= 100;
        if (val > 20) {
            val %= 10;
        }
        return format.format(val);
    }
}
