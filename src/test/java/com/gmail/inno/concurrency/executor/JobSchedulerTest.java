package com.gmail.inno.concurrency.executor;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class JobSchedulerTest {

    @Test
    void shouldScheduleOneTimeRunJob() throws InterruptedException {
        JobScheduler jobScheduler = JobScheduler.createJobScheduler(1);
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
             Job job = jobScheduler.scheduleOneTimeRunJob("testJob", () -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {

                }
            });
            jobs.add(job);
        }

        TimeUnit.SECONDS.sleep(5);
        assertEquals(1, jobs.stream()
                .filter(j -> j.getState() == Job.State.IN_PROGRESS)
                .count());
        assertEquals(2, jobs.stream()
                .filter(j -> j.getState() == Job.State.NEW)
                .count());

        TimeUnit.SECONDS.sleep(10);
        assertEquals(1, jobs.stream()
                .filter(j -> j.getState() == Job.State.IN_PROGRESS)
                .count());
        assertEquals(1, jobs.stream()
                .filter(j -> j.getState() == Job.State.NEW)
                .count());
        assertEquals(1, jobs.stream()
                .filter(j -> j.getState() == Job.State.FINISHED)
                .count());

        TimeUnit.SECONDS.sleep(10);
        assertEquals(1, jobs.stream()
                .filter(j -> j.getState() == Job.State.IN_PROGRESS)
                .count());
        assertEquals(2, jobs.stream()
                .filter(j -> j.getState() == Job.State.FINISHED)
                .count());

        TimeUnit.SECONDS.sleep(10);
        assertEquals(3, jobs.stream()
                .filter(j -> j.getState() == Job.State.FINISHED)
                .count());

        jobScheduler.shutdown();
    }

    @Test
    void shouldSchedulePeriodicJob() throws InterruptedException{
        JobScheduler jobScheduler = JobScheduler.createJobScheduler(1);
            Job job = jobScheduler.schedulePeriodicJob("testJob", () -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {

                }
            },
                    1L, TimeUnit.SECONDS);

        assertEquals(Job.State.NEW, job.getState());

        TimeUnit.SECONDS.sleep(5);
        job.setState(Job.State.FINISHED);
        assertEquals(Job.State.FINISHED, job.getState());

        jobScheduler.shutdown();
    }

    @Test
    void shouldReturnStateFailed() throws InterruptedException{
        JobScheduler jobScheduler = JobScheduler.createJobScheduler(1);
        Job job = jobScheduler.schedulePeriodicJob("testJob", () -> {throw new RuntimeException();},
                1L, TimeUnit.SECONDS);

        TimeUnit.SECONDS.sleep(5);
        assertEquals(Job.State.FAILED, job.getState());

        jobScheduler.shutdown();
    }

    @Test
    void shouldReturnStateStopped() throws InterruptedException{
        JobScheduler jobScheduler = JobScheduler.createJobScheduler(1);
        Job job = jobScheduler.schedulePeriodicJob("testJob", () -> {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {

                    }
                },
                1L, TimeUnit.SECONDS);

        assertEquals(Job.State.NEW, job.getState());

        TimeUnit.SECONDS.sleep(5);
        job.stop();
        assertEquals(Job.State.STOPPED, job.getState());

        jobScheduler.shutdown();
    }
}
