package info.kgeorgiy.ja.malko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper parallelMapper;


    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, R> R launch(final int threads, final List<T> values,
                            final Function<Stream<T>, R> getResult, final Function<Stream<R>, R> mergeResult) throws InterruptedException {

        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be more or equal 1");
        }

        final List<R> resultList = launchThreads(threads, values, getResult);
        return mergeResult.apply(resultList.stream());
    }

    private <T> T launch(final int threads, final List<T> values,
                         final Function<Stream<T>, T> getResult) throws InterruptedException {
        return launch(threads, values, getResult, getResult);
    }

    private <T, R> R reduceLaunch(final int threads, final List<T> values,
                                  final Function<T, R> map, final BinaryOperator<R> reduceOperator)
        throws InterruptedException {
        return launch(threads, values, getResultProvider(map, reduceOperator),
            getResultProvider(UnaryOperator.identity(), reduceOperator));
    }

    private <T, R> Function<Stream<T>, R> getResultProvider(final Function<T, R> map,
                                                            final BinaryOperator<R> reduceOperator) {
        return (a) -> a.map(map).reduce(reduceOperator).orElse(null);
    }

    private void joinThreads(final List<Thread> threadList) throws ParallelismInterruptedException {
        final List<InterruptedException> exceptions = new ArrayList<>();
        for (int i = 0; i < threadList.size(); i++) {
            try {
                threadList.get(i).join();
            } catch (final InterruptedException e) {
                exceptions.add(e);
                for (int j = i; j < threadList.size(); j++) {
                    threadList.get(j).interrupt();
                }
                for (int j = i; j < threadList.size(); j++) {
                    try {
                        threadList.get(j).join();
                    } catch (final InterruptedException exc) {
                        j--;
                        exceptions.add(exc);
                    }
                }
            }
        }
        if (!exceptions.isEmpty()) {
            // :NOTE: addSupressed
            throw new ParallelismInterruptedException(exceptions);
        }
    }

    private <T, R> List<R> launchThreads(final int threads, final List<T> values,
                                         final Function<Stream<T>, R> getResult)
        throws InterruptedException {
        final List<Stream<T>> streams = split(threads, values);
        return parallelMapper == null ? mapLocal(getResult, streams) : parallelMapper.map(getResult, streams);
    }

    private static <T> List<Stream<T>> split(final int threads, final List<T> values) {
        final int valuesPerThread = values.size() / threads;
        final int valuesLeft = values.size() % threads;

        int valuesPos = 0;
        final List<Stream<T>> streams = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            int valuesNumber = valuesPerThread;
            if (i < valuesLeft) {
                valuesNumber++;
            }
            if (valuesNumber == 0) {
                break;
            }
            streams.add(values.subList(valuesPos, valuesPos + valuesNumber).stream());
            valuesPos += valuesNumber;
        }
        return streams;
    }

    private <T, R> List<R> mapLocal(final Function<Stream<T>, R> getResult, final List<Stream<T>> streams) throws ParallelismInterruptedException {
        final List<Thread> threadList = new ArrayList<>();
        final List<R> resultList;
        resultList = new ArrayList<>(Collections.nCopies(streams.size(), null));
        // :NOTE: IntStream
        for (int i = 0; i < streams.size(); i++) {
            final Thread thread = launchThread(i, resultList, streams.get(i), getResult);
            threadList.add(thread);
        }
        joinThreads(threadList);
        return resultList;
    }

    private static <T, R> Thread launchThread(final int i, final List<R> resultList, final Stream<T> stream,
                                              final Function<Stream<T>, R> getResult) {
        final Thread thread = new Thread(() ->
            resultList.set(i,
            getResult.apply(stream)));
        thread.start();
        return thread;
    }

    private <T, R> Stream<R> streamLaunch(final int threads, final List<T> values, final Function<T, Stream<R>> getResult)
        throws InterruptedException {
        return reduceLaunch(threads, values, getResult, Stream::concat);
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
        throws InterruptedException {
        return launch(threads, values, a -> a.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
        throws InterruptedException {
        return launch(threads, values, a -> a.min(comparator).orElse(null));
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
        throws InterruptedException {
        // :NOTE: Метод
        return reduceLaunch(threads, values, predicate::test, (a, b) -> a & b);
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
        throws InterruptedException {
        return reduceLaunch(threads, values, predicate::test, (a, b) -> a | b);
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return streamLaunch(threads, values, (a) -> Stream.of(a.toString()))
            .collect(Collectors.joining());
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values,
                              final Predicate<? super T> predicate) throws InterruptedException {
        // :NOTE: Обобщить
        return streamLaunch(threads, values, (a) -> Stream.of(a).filter(predicate))
            .collect(Collectors.toList());
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values,
                              final Function<? super T, ? extends U> f) throws InterruptedException {
        return streamLaunch(threads, values, (a) -> Stream.of(a).map(f)).collect(Collectors.toList());
    }

    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid)
        throws InterruptedException {
        return reduceLaunch(threads, values, UnaryOperator.identity(), monoid.getOperator());
    }

    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift,
                              final Monoid<R> monoid) throws InterruptedException {
        return reduceLaunch(threads, values, lift, monoid.getOperator());
    }
}