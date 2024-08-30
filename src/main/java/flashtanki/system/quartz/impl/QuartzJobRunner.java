/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.system.quartz.impl;

import flashtanki.system.quartz.QuartzJob;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzJobRunner
implements Job {
    public static String jobRunKey = "runnable";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        QuartzJob run = (QuartzJob)context.getJobDetail().getJobDataMap().get(jobRunKey);
        run.run(context);
    }
}

