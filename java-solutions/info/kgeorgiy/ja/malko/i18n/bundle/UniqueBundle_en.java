package info.kgeorgiy.ja.malko.i18n.bundle;

public class UniqueBundle_en extends AbstractUniqueBundle {

    public UniqueBundle_en() {
        super(new Object[][] {
            {"main", "Overall statistics\n"
                + "\tNumber of sentences: {0,number}.\n"
                + "\tNumber of words: {1,number}.\n"
                + "\tNumber of numbers: {2,number}.\n"
                + "\tNumber of currencies: {3,number}.\n"
                + "\tNumber of dates: {4,number}."},
            {"hat", "{0} statistics"},
            {"number", "Number of {0}: {1,number,integer} ({2,number,integer} {3})."},
            {"byComparator", "{0} value of {1}: <!>."},
            {"byLength", "{0} length of {1}: {2,number,integer} (\"{3}\")."},
            {"average", "{0}: <!>."},
            {"comparatorMin", "Minimum"},
            {"comparatorMax", "Maximum"},
            {"lengthMin", "Minimum"},
            {"lengthMax", "Maximum"},

            {"sentence-hat", "Sentences"},
            {"sentence-number", "sentences"},
            {"sentence-byComparator", "sentence"},
            {"sentence-byLength", "sentence"},
            {"sentence-average", "Average length of sentence"},
            {"sentence-byComparator-type", "\"{2}\""},
            {"sentence-average-type", "{1,number}"},

            {"word-hat", "Words"},
            {"word-number", "words"},
            {"word-byComparator", "word"},
            {"word-byLength", "word"},
            {"word-average", "Average length of word"},
            {"word-byComparator-type", "\"{2}\""},
            {"word-average-type", "{1,number}"},

            {"number-hat", "Numbers"},
            {"number-number", "numbers"},
            {"number-byComparator", "number"},
            {"number-average", "Average value of number"},
            {"number-byComparator-type", "{2,number}"},
            {"number-average-type", "{1,number}"},

            {"currency-hat", "Currencies"},
            {"currency-number", "currencies"},
            {"currency-byComparator", "currency"},
            {"currency-average", "Average currency"},
            {"currency-byComparator-type", "{2,number,currency}"},
            {"currency-average-type", "{1,number,currency}"},

            {"date-hat", "Dates"},
            {"date-number", "dates"},
            {"date-byComparator", "date"},
            {"date-average", "Average value of date"},
            {"date-byComparator-type", "{2,date,long}"},
            {"date-average-type", "{1,date,long}"}
        });
    }

    @Override
    public String getUnique(int val) {
        val %= 100;
        if (val % 10 == 1 && val != 11) {
            return "unique value";
        }
        return "unique values";
    }
}
