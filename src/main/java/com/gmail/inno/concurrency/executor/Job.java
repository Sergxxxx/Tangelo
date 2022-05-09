package com.gmail.inno.concurrency.executor;

import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Job {

    private final UUID jobId;
    private final String jobType;
    private final Lock lock;
    private ScheduledFuture<?> scheduledFuture;
    private State state = State.NEW;

    Job(String jobType) {
        this.jobId = UUID.randomUUID();
        this.jobType = jobType;
        this.lock = new ReentrantLock();
        setState(State.NEW);
    }

    public UUID getJobId() {
        return jobId;
    }

    public String getJobType() {
        return jobType;
    }

    public State getState() {
        lock.lock();
        try {
            return state;
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            setState(State.STOPPED);
        }
    }

    void setState(State state) {
        lock.lock();
        try {
            this.state = state;
        } finally {
            lock.unlock();
        }
    }

    void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
        this.scheduledFuture = scheduledFuture;
    }

    public enum State {
        NEW,
        IN_PROGRESS,
        STOPPED,
        FAILED,
        FINISHED
    }
}
