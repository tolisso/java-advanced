package info.kgeorgiy.ja.malko.concurrent;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

import static java.lang.Thread.interrupted;

public class ParallelMapperImpl implements ParallelMapper {
    // :NOTE: Доступ?
    final List<Thread> threadsList = new ArrayList<>();
    final SynchronizedQueue tasks = new SynchronizedQueue();

    public ParallelMapperImpl(final int threads) {
        if (threads < 1) {
            throw new IllegalArgumentException("Number of threads should be more or equal 1");
        }

        // :NOTE: Stream
        for (int i = 0; i < threads; i++) {
            final Thread thread = new Thread(() -> {
                while (!interrupted()) {
                    try {
                        tasks.poll().run();
                    } catch (final InterruptedException ignored) {
                        break;
                    }
                }
            });

            threadsList.add(thread);
            thread.start();
        }
    }

    private static class SynchronizedQueue {
        // :NOTE: ??
        final Queue<Runnable> tasks = new ArrayDeque<>();

        private synchronized Runnable poll() throws InterruptedException {
            while (tasks.isEmpty()) {
                wait();
            }
            return tasks.poll();
        }

        private synchronized void add(final Runnable r) {
            tasks.add(r);
            notify();
        }
    }

    private static class TaskCounter {
        private int tasksRemain;

        public TaskCounter(final int tasks) {
            tasksRemain = tasks;
        }

        public synchronized void decrement() {
            tasksRemain--;
            if (ifNoRemain()) {
                notify();
            }
        }

        public synchronized boolean ifNoRemain() {
            return tasksRemain == 0;
        }
    }

    private static class TaskException {
        private RuntimeException exception;

        private synchronized void setException(final RuntimeException exc) {
            if (!hasException()) {
                this.exception = exc;
            }
        }

        private synchronized RuntimeException getException() {
            return exception;
        }

        public synchronized boolean hasException() {
            return exception != null;
        }

    }

    @Override
    public <T, R> List<R> map(final Function<? super T, ? extends R> f, final List<? extends T> args)
        throws InterruptedException {

        final TaskCounter taskCnt = new TaskCounter(args.size());
        final TaskException taskExc = new TaskException();
        final List<R> resultList = new ArrayList<>(Collections.nCopies(args.size(), null));

        // :NOTE: IntStream
        for (int i = 0; i < args.size(); i++) {
            final int fi = i;

            final Runnable task = () -> {
                try {
                    final R result = f.apply(args.get(fi));

                    synchronized (resultList) {
                        resultList.set(fi, result);
                    }
                    taskCnt.decrement();
                } catch (final RuntimeException exc) {
                    taskExc.setException(exc);
                    taskCnt.notify();
                }
            };

            tasks.add(task);
        }
        waitResult(taskCnt, taskExc);

        if (taskExc.hasException()) {
            throw taskExc.getException();
        }
        return resultList;
    }

    @Override
    public void close() {
        for (final Thread tr : threadsList) {
            tr.interrupt();
        }
        
        for (final Thread tr : threadsList) {
            try {
                tr.join();
            } catch (final InterruptedException ignored) {
                assert false;
            }
        }
    }

    private void waitResult(final TaskCounter taskCnt, final TaskException taskExc) throws InterruptedException {
        synchronized (taskCnt) {
            while (!taskCnt.ifNoRemain() && !taskExc.hasException()) {
                // :NOTE: Бесконечное ожидание
                taskCnt.wait();
            }
        }
    }
}
