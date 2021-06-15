package info.kgeorgiy.ja.malko.concurrent;

import info.kgeorgiy.java.advanced.concurrent.AdvancedIP;
import info.kgeorgiy.java.advanced.mapper.ParallelMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class IterativeParallelism implements AdvancedIP {
    private final ParallelMapper parallelMapper;
    
    public IterativeParallelism() {
        this(null);
    }

    public IterativeParallelism(final ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, R> R launch(final int threads, final List<T> values,
        final Function<Stream<T>, R> getResult, final Function<Stream<R>, R> mergeResult) throws InterruptedException {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be more or equal 1");
        }

        final List<Stream<T>> streams = split(threads, values);
        final List<R> resultList = parallelMapper == null ? mapLocal(getResult, streams) : parallelMapper.map(getResult, streams);
        return mergeResult.apply(resultList.stream());
    }

    private <T, R> T launch(final int threads, final List<T> values,
        final Function<Stream<T>, T> mergeResult) throws InterruptedException {
        return launch(threads, values, mergeResult, mergeResult);
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

    private static <T, R> List<R> mapLocal(final Function<T, R> getResult, final List<T> streams) throws InterruptedException {
        final List<R> results = new ArrayList<>(Collections.nCopies(streams.size(), null));
        final List<Thread> threads = IntStream.range(0, streams.size())
                .mapToObj(i -> new Thread(() -> results.set(i, getResult.apply(streams.get(i)))))
                .collect(Collectors.toList());
        threads.forEach(Thread::start);

        joinThreads(threads);
        return results;
    }

    private static void joinThreads(final List<Thread> threadList) throws InterruptedException {
        final List<InterruptedException> exceptions = new ArrayList<>();
        // :NOTE: Квадрат
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
            final var excToThrow = new InterruptedException();
            exceptions.forEach(excToThrow::addSuppressed);
            throw excToThrow;
        }
    }

    private <T, R> List<R> flatMapLaunch(final int threads, final List<? extends T> values,
        final Function<Stream<? extends T>, Stream<? extends R>> getResult) throws InterruptedException {
        return launch(threads, values,
            stream -> getResult.apply(stream).collect(Collectors.toList()),
            stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    @Override
    public <T> T maximum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
        throws InterruptedException {
        return launch(threads, values, s -> s.max(comparator).orElse(null));
    }

    @Override
    public <T> T minimum(final int threads, final List<? extends T> values, final Comparator<? super T> comparator)
        throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
        throws InterruptedException {
        return launch(threads, values, s -> s.allMatch(predicate),
            s -> s.reduce(Boolean::logicalAnd).orElse(null));
    }

    @Override
    public <T> boolean any(final int threads, final List<? extends T> values, final Predicate<? super T> predicate)
        throws InterruptedException {
        return !all(threads, values, predicate.negate());
    }

    private <T, R> R launchMap(final int threads, final List<T> values,
                               final Function<Stream<R>, R> mergeResult, final Function<T, R> toMap)
        throws InterruptedException {
        return launch(threads, values,
            stream -> mergeResult.apply(stream.map(toMap)), mergeResult);
    }

    @Override
    public String join(final int threads, final List<?> values) throws InterruptedException {
        return launchMap(threads, values,
            stream -> stream.collect(Collectors.joining()),
            Object::toString);
    }

    @Override
    public <T> List<T> filter(final int threads, final List<? extends T> values,
                              final Predicate<? super T> predicate) throws InterruptedException {
        return flatMapLaunch(threads, values, stream -> stream.filter(predicate));
    }

    @Override
    public <T, U> List<U> map(final int threads, final List<? extends T> values,
                              final Function<? super T, ? extends U> f) throws InterruptedException {
        return flatMapLaunch(threads, values, stream -> stream.map(f));
    }

    @Override
    public <T> T reduce(final int threads, final List<T> values, final Monoid<T> monoid) throws InterruptedException {
        return mapReduce(threads, values, Function.identity(), monoid);
    }

    @Override
    public <T, R> R mapReduce(final int threads, final List<T> values, final Function<T, R> lift,
                              final Monoid<R> monoid) throws InterruptedException {
        return launchMap(threads, values,
            stream -> stream.reduce(monoid.getOperator()).orElse(monoid.getIdentity()), lift);
    }
}
