package net.minecraft;

import com.google.common.collect.Lists;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CrashReport {
    private static final Logger LOGGER = LogManager.getLogger();
    private final String title;
    private final Throwable exception;
    private final CrashReportCategory systemDetails = new CrashReportCategory(this, "System Details");
    private final List<CrashReportCategory> details = Lists.newArrayList();
    private File saveFile;
    private boolean trackingStackTrace = true;
    private StackTraceElement[] uncategorizedStackTrace = new StackTraceElement[0];

    public CrashReport(String param0, Throwable param1) {
        this.title = param0;
        this.exception = param1;
        this.initDetails();
    }

    private void initDetails() {
        this.systemDetails.setDetail("Minecraft Version", () -> SharedConstants.getCurrentVersion().getName());
        this.systemDetails.setDetail("Minecraft Version ID", () -> SharedConstants.getCurrentVersion().getId());
        this.systemDetails
            .setDetail(
                "Operating System",
                () -> System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ") version " + System.getProperty("os.version")
            );
        this.systemDetails.setDetail("Java Version", () -> System.getProperty("java.version") + ", " + System.getProperty("java.vendor"));
        this.systemDetails
            .setDetail(
                "Java VM Version",
                () -> System.getProperty("java.vm.name") + " (" + System.getProperty("java.vm.info") + "), " + System.getProperty("java.vm.vendor")
            );
        this.systemDetails.setDetail("Memory", () -> {
            Runtime var0 = Runtime.getRuntime();
            long var1 = var0.maxMemory();
            long var2 = var0.totalMemory();
            long var3 = var0.freeMemory();
            long var4 = var1 / 1024L / 1024L;
            long var5 = var2 / 1024L / 1024L;
            long var6 = var3 / 1024L / 1024L;
            return var3 + " bytes (" + var6 + " MB) / " + var2 + " bytes (" + var5 + " MB) up to " + var1 + " bytes (" + var4 + " MB)";
        });
        this.systemDetails.setDetail("CPUs", Runtime.getRuntime().availableProcessors());
        this.systemDetails.setDetail("JVM Flags", () -> {
            List<String> var0 = Util.getVmArguments().collect(Collectors.toList());
            return String.format("%d total; %s", var0.size(), var0.stream().collect(Collectors.joining(" ")));
        });
    }

    public String getTitle() {
        return this.title;
    }

    public Throwable getException() {
        return this.exception;
    }

    public void getDetails(StringBuilder param0) {
        if ((this.uncategorizedStackTrace == null || this.uncategorizedStackTrace.length <= 0) && !this.details.isEmpty()) {
            this.uncategorizedStackTrace = ArrayUtils.subarray(this.details.get(0).getStacktrace(), 0, 1);
        }

        if (this.uncategorizedStackTrace != null && this.uncategorizedStackTrace.length > 0) {
            param0.append("-- Head --\n");
            param0.append("Thread: ").append(Thread.currentThread().getName()).append("\n");
            param0.append("Stacktrace:\n");

            for(StackTraceElement var0 : this.uncategorizedStackTrace) {
                param0.append("\t").append("at ").append(var0);
                param0.append("\n");
            }

            param0.append("\n");
        }

        for(CrashReportCategory var1 : this.details) {
            var1.getDetails(param0);
            param0.append("\n\n");
        }

        this.systemDetails.getDetails(param0);
    }

    public String getExceptionMessage() {
        StringWriter var0 = null;
        PrintWriter var1 = null;
        Throwable var2 = this.exception;
        if (var2.getMessage() == null) {
            if (var2 instanceof NullPointerException) {
                var2 = new NullPointerException(this.title);
            } else if (var2 instanceof StackOverflowError) {
                var2 = new StackOverflowError(this.title);
            } else if (var2 instanceof OutOfMemoryError) {
                var2 = new OutOfMemoryError(this.title);
            }

            var2.setStackTrace(this.exception.getStackTrace());
        }

        String var4;
        try {
            var0 = new StringWriter();
            var1 = new PrintWriter(var0);
            var2.printStackTrace(var1);
            var4 = var0.toString();
        } finally {
            IOUtils.closeQuietly((Writer)var0);
            IOUtils.closeQuietly((Writer)var1);
        }

        return var4;
    }

    public String getFriendlyReport() {
        StringBuilder var0 = new StringBuilder();
        var0.append("---- Minecraft Crash Report ----\n");
        var0.append("// ");
        var0.append(getErrorComment());
        var0.append("\n\n");
        var0.append("Time: ");
        var0.append(new SimpleDateFormat().format(new Date()));
        var0.append("\n");
        var0.append("Description: ");
        var0.append(this.title);
        var0.append("\n\n");
        var0.append(this.getExceptionMessage());
        var0.append("\n\nA detailed walkthrough of the error, its code path and all known details is as follows:\n");

        for(int var1 = 0; var1 < 87; ++var1) {
            var0.append("-");
        }

        var0.append("\n\n");
        this.getDetails(var0);
        return var0.toString();
    }

    @OnlyIn(Dist.CLIENT)
    public File getSaveFile() {
        return this.saveFile;
    }

    public boolean saveToFile(File param0) {
        if (this.saveFile != null) {
            return false;
        } else {
            if (param0.getParentFile() != null) {
                param0.getParentFile().mkdirs();
            }

            Writer var0 = null;

            boolean var4;
            try {
                var0 = new OutputStreamWriter(new FileOutputStream(param0), StandardCharsets.UTF_8);
                var0.write(this.getFriendlyReport());
                this.saveFile = param0;
                return true;
            } catch (Throwable var8) {
                LOGGER.error("Could not save crash report to {}", param0, var8);
                var4 = false;
            } finally {
                IOUtils.closeQuietly(var0);
            }

            return var4;
        }
    }

    public CrashReportCategory getSystemDetails() {
        return this.systemDetails;
    }

    public CrashReportCategory addCategory(String param0) {
        return this.addCategory(param0, 1);
    }

    public CrashReportCategory addCategory(String param0, int param1) {
        CrashReportCategory var0 = new CrashReportCategory(this, param0);
        if (this.trackingStackTrace) {
            int var1 = var0.fillInStackTrace(param1);
            StackTraceElement[] var2 = this.exception.getStackTrace();
            StackTraceElement var3 = null;
            StackTraceElement var4 = null;
            int var5 = var2.length - var1;
            if (var5 < 0) {
                System.out.println("Negative index in crash report handler (" + var2.length + "/" + var1 + ")");
            }

            if (var2 != null && 0 <= var5 && var5 < var2.length) {
                var3 = var2[var5];
                if (var2.length + 1 - var1 < var2.length) {
                    var4 = var2[var2.length + 1 - var1];
                }
            }

            this.trackingStackTrace = var0.validateStackTrace(var3, var4);
            if (var1 > 0 && !this.details.isEmpty()) {
                CrashReportCategory var6 = this.details.get(this.details.size() - 1);
                var6.trimStacktrace(var1);
            } else if (var2 != null && var2.length >= var1 && 0 <= var5 && var5 < var2.length) {
                this.uncategorizedStackTrace = new StackTraceElement[var5];
                System.arraycopy(var2, 0, this.uncategorizedStackTrace, 0, this.uncategorizedStackTrace.length);
            } else {
                this.trackingStackTrace = false;
            }
        }

        this.details.add(var0);
        return var0;
    }

    private static String getErrorComment() {
        String[] var0 = new String[]{
            "Who set us up the TNT?",
            "Everything's going to plan. No, really, that was supposed to happen.",
            "Uh... Did I do that?",
            "Oops.",
            "Why did you do that?",
            "I feel sad now :(",
            "My bad.",
            "I'm sorry, Dave.",
            "I let you down. Sorry :(",
            "On the bright side, I bought you a teddy bear!",
            "Daisy, daisy...",
            "Oh - I know what I did wrong!",
            "Hey, that tickles! Hehehe!",
            "I blame Dinnerbone.",
            "You should try our sister game, Minceraft!",
            "Don't be sad. I'll do better next time, I promise!",
            "Don't be sad, have a hug! <3",
            "I just don't know what went wrong :(",
            "Shall we play a game?",
            "Quite honestly, I wouldn't worry myself about that.",
            "I bet Cylons wouldn't have this problem.",
            "Sorry :(",
            "Surprise! Haha. Well, this is awkward.",
            "Would you like a cupcake?",
            "Hi. I'm Minecraft, and I'm a crashaholic.",
            "Ooh. Shiny.",
            "This doesn't make any sense!",
            "Why is it breaking :(",
            "Don't do that.",
            "Ouch. That hurt :(",
            "You're mean.",
            "This is a token for 1 free hug. Redeem at your nearest Mojangsta: [~~HUG~~]",
            "There are four lights!",
            "But it works on my machine."
        };

        try {
            return var0[(int)(Util.getNanos() % (long)var0.length)];
        } catch (Throwable var2) {
            return "Witty comment unavailable :(";
        }
    }

    public static CrashReport forThrowable(Throwable param0, String param1) {
        while(param0 instanceof CompletionException && param0.getCause() != null) {
            param0 = param0.getCause();
        }

        CrashReport var0;
        if (param0 instanceof ReportedException) {
            var0 = ((ReportedException)param0).getReport();
        } else {
            var0 = new CrashReport(param1, param0);
        }

        return var0;
    }
}
