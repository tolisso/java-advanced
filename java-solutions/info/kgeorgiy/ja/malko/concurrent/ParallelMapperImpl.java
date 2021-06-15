package info.kgeorgiy.ja.malko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Thread.interrupted;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threadsList;
    private final SynchronizedQueue tasks = new SynchronizedQueue();

    public ParallelMapperImpl(final int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be more or equal 1");
        }

        final Runnable action = () -> {
            while (!interrupted()) {
                try {
                    tasks.poll().run();
                } catch (final InterruptedException ignored) {
                    break;
                }
            }
        };

        threadsList = Stream.generate(() -> new Thread(action)).limit(threads).collect(Collectors.toList());
        threadsList.forEach(Thread::start);
    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args)
        throws InterruptedException
    {
        final TaskResult taskResult = new TaskResult(args.size());
        final List<R> resultList = new ArrayList<>(Collections.nCopies(args.size(), null));

        // :NOTE: Вытеснили

        IntStream.range(0, args.size()).mapToObj(i -> new Task(() -> {
            try {
                final R result = f.apply(args.get(i));

                // :NOTE: Повторная синхронизация
                synchronized (resultList) {
                    resultList.set(i, result);
                }
                taskResult.decrement();
            } catch (final RuntimeException exc) {
                taskResult.setException(exc);
            }
        }, taskResult)).forEach(tasks::add);
        taskResult.waitResult();

        return resultList;
    }

    @Override
    public void close() {
        for (final Thread tr : threadsList) {
            tr.interrupt();
        }

        for (final Thread tr : threadsList) {
            while (true) {
                // :NOTE: Дождались не всех
		// исправить
                try {
                    tr.join();
                    break;
                } catch (final InterruptedException ignored) {
                    // nothing
                }
            }
        }
        tasks.endAll();
    }


    private static class Task implements Runnable {
        private final Runnable body;
        private final TaskResult taskResult;

        public Task(final Runnable body, final TaskResult taskResult) {
            this.body = body;
            this.taskResult = taskResult;
        }

        @Override
        public void run() {
            body.run();
        }

        public void continueCaller() {
            taskResult.clearTasks();
            // :NOTE: notify без блокировки
            taskResult.notify();
        }
    }

    private static class SynchronizedQueue {
        private final Queue<Task> tasks = new ArrayDeque<>();

        private synchronized Task poll() throws InterruptedException {
            while (tasks.isEmpty()) {
                wait();
            }
            return tasks.poll();
        }

        private synchronized void add(final Task task) {
            tasks.add(task);
            notify();
        }

        private void endAll() {
            tasks.forEach(Task::continueCaller);
        }
    }

    private static class TaskResult {
        private int tasksRemain;
        private RuntimeException exception;

        private TaskResult(final int tasks) {
            tasksRemain = tasks;
        }

        private synchronized void decrement() {
            tasksRemain--;
            if (tasksRemain == 0) {
                notify();
            }
        }

        private synchronized void clearTasks() {
            tasksRemain = 0;
            notify();
        }

        private synchronized void setException(final RuntimeException exc) {
            if (exception == null) {
                this.exception = exc;
            }
            notify();
        }

        private synchronized void waitResult() throws InterruptedException {
            while (exception == null && tasksRemain != 0) {
                wait();
            }
            if (exception != null) {
                throw exception;
            }
        }
    }
}
