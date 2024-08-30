package gtanks.system.quartz.impl;

import gtanks.system.quartz.QuartzJob;
import gtanks.system.quartz.QuartzService;
import gtanks.system.quartz.TimeType;
import gtanks.system.quartz.impl.QuartzJobRunner;
import java.util.Date;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class QuartzServiceImpl
implements QuartzService {
    private static QuartzServiceImpl instance;
    private Scheduler scheduler;

    public static QuartzServiceImpl getInstance() {
        if (instance == null) {
            instance = new QuartzServiceImpl();
        }
        return instance;
    }

    private QuartzServiceImpl() {
        StdSchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.start();
        }
        catch (SchedulerException schedulerException) {
            // empty catch block
        }
    }

    private JobDetail createJob(String name, String group, QuartzJob object) {
        JobDetail job = JobBuilder.newJob(QuartzJobRunner.class)
                .withIdentity(name, group)
                .build();

        job.getJobDataMap().put(QuartzJobRunner.jobRunKey, object);
        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group, QuartzJob object, TimeType type, long interval, int repeatCount) {
        JobDetail job = this.createJob(name, group, object);
        try {

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                            .withIntervalInMilliseconds((int) interval)
                            .withRepeatCount(repeatCount))
                    .build();

            this.scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException schedulerException) {
            // empty catch block
        }
        return job;
    }

    @Override
    public JobDetail addJobInterval(String name, String group, QuartzJob object, TimeType type, long interval) {
        return this.addJobInterval(name, group, object, type, interval, -1);
    }

    @Override
    public JobDetail addJob(String name, String group, QuartzJob object, TimeType type, long time) {
        JobDetail job = this.createJob(name, group, object);
        try {
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(name, group)
                    .startAt( new Date(System.currentTimeMillis() + type.time(time)))
                    .build();

            this.scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException schedulerException) {
            // empty catch block
        }
        return job;
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    @Override
    public void deleteJob(String name, String group) {
        try {
            this.scheduler.deleteJob(new JobKey(name, group));
        }
        catch (SchedulerException schedulerException) {
            // empty catch block
        }
    }
}