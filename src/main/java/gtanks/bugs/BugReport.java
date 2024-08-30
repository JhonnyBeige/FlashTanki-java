/*
 * Decompiled with CFR 0.150.
 */
package gtanks.bugs;

import gtanks.StringUtils;
import gtanks.bugs.BugInfo;
import gtanks.bugs.screenshots.BufferScreenshotTransfer;
import gtanks.users.User;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class BugReport {
    private static String URL_BUGS_FILE = "bugs/bugs.data";
    private static File bugsFile;

    public static void bugReport(User sender, BufferScreenshotTransfer screenshot) {
    }

    @SneakyThrows
    public static void bugReport(User sender, BugInfo bug) {
        if (bugsFile == null) {
            bugsFile = new File(URL_BUGS_FILE);
            try {
                bugsFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        Throwable e = null;
        Object var3_6 = null;
        try (FileWriter writer = new FileWriter(bugsFile, true);){
            writer.append(BugReport.getFormatedData(sender, bug));
        }

    }

    private static String getFormatedData(User sender, BugInfo bug) {
        return StringUtils.concatStrings("----", new Date().toString(), "----\n", "  User: ", sender.getNickname(), "\n", bug.toString(), "---------------------------------------\n");
    }
}

