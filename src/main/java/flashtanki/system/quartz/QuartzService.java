/*
 * Decompiled with CFR 0.150.
 */
package flashtanki.system.quartz;

import org.quartz.JobDetail;

public interface QuartzService {
    public JobDetail addJobInterval(String var1, String var2, QuartzJob var3, TimeType var4, long var5);

    public JobDetail addJobInterval(String var1, String var2, QuartzJob var3, TimeType var4, long var5, int var7);

    public JobDetail addJob(String var1, String var2, QuartzJob var3, TimeType var4, long var5);

    public void deleteJob(String var1, String var2);
}

