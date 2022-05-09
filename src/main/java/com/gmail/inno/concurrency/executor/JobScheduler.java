package com.gmail.inno.concurrency.executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class JobScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ScheduledExecutorService executorService;

    JobScheduler(int size) {
        this.executorService = Executors.newScheduledThreadPool(size);
    }

    public static JobScheduler createJobScheduler(int size) {
        return new JobScheduler(size);
    }

    public Job scheduleOneTimeRunJob(final String jobType,
                                     final Runnable task) {

        final Job job = new Job(jobType);
        LOGGER.info("Job with id: {}, type {} scheduled", job.getJobId(), job.getJobType());
        ScheduledFuture<?> scheduledFuture = executorService.schedule(modifyTask(job, task), 0, TimeUnit.SECONDS);
        job.setScheduledFuture(scheduledFuture);

        return job;
    }

    public Job schedulePeriodicJob(final String jobType,
                                   final Runnable task,
                                   final long period,
                                   final TimeUnit timeUnit) {

        final Job job = new Job(jobType);
        LOGGER.info("Job with id: {}, type {} scheduled", job.getJobId(), job.getJobType());
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(modifyTask(job, task), 0, period, timeUnit);
        job.setScheduledFuture(scheduledFuture);

        return job;
    }

    public void shutdown() {
        executorService.shutdown();
    }

    private Runnable modifyTask(final Job job,
                                final Runnable task) {

        return () -> {
            try {
                job.setState(Job.State.IN_PROGRESS);
                LOGGER.info("Thread with id: {}, type {} started", job.getJobId(), job.getJobType());
                task.run();
                job.setState(Job.State.FINISHED);
                LOGGER.info("Thread with id: {}, type {} finished", job.getJobId(), job.getJobType());
            } catch (Exception ex) {
                job.setState(Job.State.FAILED);
                LOGGER.error("Thread with id: {}, type {} failed: {}", job.getJobId(), job.getJobType(), ex.getMessage());
            }
        };
    }
}
