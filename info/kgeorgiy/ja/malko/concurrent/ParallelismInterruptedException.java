package info.kgeorgiy.ja.malko.concurrent;

import java.util.List;

public class ParallelismInterruptedException extends InterruptedException {
    public final List<InterruptedException> exceptions;

    public ParallelismInterruptedException(List<InterruptedException> exceptions) {
        this.exceptions = exceptions;
    }
}
