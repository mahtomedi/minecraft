package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilledProfileResults implements ProfileResults {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<String, Long> times;
    private final Map<String, Long> counts;
    private final long startTimeNano;
    private final int startTimeTicks;
    private final long endTimeNano;
    private final int endTimeTicks;
    private final int tickDuration;

    public FilledProfileResults(Map<String, Long> param0, Map<String, Long> param1, long param2, int param3, long param4, int param5) {
        this.times = param0;
        this.counts = param1;
        this.startTimeNano = param2;
        this.startTimeTicks = param3;
        this.endTimeNano = param4;
        this.endTimeTicks = param5;
        this.tickDuration = param5 - param3;
    }

    @Override
    public List<ResultField> getTimes(String param0) {
        String var0 = param0;
        long var1 = this.times.containsKey("root") ? this.times.get("root") : 0L;
        long var2 = this.times.getOrDefault(param0, -1L);
        long var3 = this.counts.getOrDefault(param0, 0L);
        List<ResultField> var4 = Lists.newArrayList();
        if (!param0.isEmpty()) {
            param0 = param0 + '\u001e';
        }

        long var5 = 0L;

        for(String var6 : this.times.keySet()) {
            if (var6.length() > param0.length() && var6.startsWith(param0) && var6.indexOf(30, param0.length() + 1) < 0) {
                var5 += this.times.get(var6);
            }
        }

        float var7 = (float)var5;
        if (var5 < var2) {
            var5 = var2;
        }

        if (var1 < var5) {
            var1 = var5;
        }

        Set<String> var8 = Sets.newHashSet(this.times.keySet());
        var8.addAll(this.counts.keySet());

        for(String var9 : var8) {
            if (var9.length() > param0.length() && var9.startsWith(param0) && var9.indexOf(30, param0.length() + 1) < 0) {
                long var10 = this.times.getOrDefault(var9, 0L);
                double var11 = (double)var10 * 100.0 / (double)var5;
                double var12 = (double)var10 * 100.0 / (double)var1;
                String var13 = var9.substring(param0.length());
                long var14 = this.counts.getOrDefault(var9, 0L);
                var4.add(new ResultField(var13, var11, var12, var14));
            }
        }

        for(String var15 : this.times.keySet()) {
            this.times.put(var15, this.times.get(var15) * 999L / 1000L);
        }

        if ((float)var5 > var7) {
            var4.add(
                new ResultField("unspecified", (double)((float)var5 - var7) * 100.0 / (double)var5, (double)((float)var5 - var7) * 100.0 / (double)var1, var3)
            );
        }

        Collections.sort(var4);
        var4.add(0, new ResultField(var0, 100.0, (double)var5 * 100.0 / (double)var1, var3));
        return var4;
    }

    @Override
    public long getStartTimeNano() {
        return this.startTimeNano;
    }

    @Override
    public int getStartTimeTicks() {
        return this.startTimeTicks;
    }

    @Override
    public long getEndTimeNano() {
        return this.endTimeNano;
    }

    @Override
    public int getEndTimeTicks() {
        return this.endTimeTicks;
    }

    @Override
    public boolean saveResults(File param0) {
        param0.getParentFile().mkdirs();
        Writer var0 = null;

        boolean var4;
        try {
            var0 = new OutputStreamWriter(new FileOutputStream(param0), StandardCharsets.UTF_8);
            var0.write(this.getProfilerResults(this.getNanoDuration(), this.getTickDuration()));
            return true;
        } catch (Throwable var8) {
            LOGGER.error("Could not save profiler results to {}", param0, var8);
            var4 = false;
        } finally {
            IOUtils.closeQuietly(var0);
        }

        return var4;
    }

    protected String getProfilerResults(long param0, int param1) {
        StringBuilder var0 = new StringBuilder();
        var0.append("---- Minecraft Profiler Results ----\n");
        var0.append("// ");
        var0.append(getComment());
        var0.append("\n\n");
        var0.append("Version: ").append(SharedConstants.getCurrentVersion().getId()).append('\n');
        var0.append("Time span: ").append(param0 / 1000000L).append(" ms\n");
        var0.append("Tick span: ").append(param1).append(" ticks\n");
        var0.append("// This is approximately ")
            .append(String.format(Locale.ROOT, "%.2f", (float)param1 / ((float)param0 / 1.0E9F)))
            .append(" ticks per second. It should be ")
            .append(20)
            .append(" ticks per second\n\n");
        var0.append("--- BEGIN PROFILE DUMP ---\n\n");
        this.appendProfilerResults(0, "root", var0);
        var0.append("--- END PROFILE DUMP ---\n\n");
        return var0.toString();
    }

    private void appendProfilerResults(int param0, String param1, StringBuilder param2) {
        List<ResultField> var0 = this.getTimes(param1);
        if (var0.size() >= 3) {
            for(int var1 = 1; var1 < var0.size(); ++var1) {
                ResultField var2 = var0.get(var1);
                param2.append(String.format("[%02d] ", param0));

                for(int var3 = 0; var3 < param0; ++var3) {
                    param2.append("|   ");
                }

                param2.append(var2.name)
                    .append('(')
                    .append(var2.count)
                    .append('/')
                    .append(String.format(Locale.ROOT, "%.0f", (float)var2.count / (float)this.tickDuration))
                    .append(')')
                    .append(" - ")
                    .append(String.format(Locale.ROOT, "%.2f", var2.percentage))
                    .append("%/")
                    .append(String.format(Locale.ROOT, "%.2f", var2.globalPercentage))
                    .append("%\n");
                if (!"unspecified".equals(var2.name)) {
                    try {
                        this.appendProfilerResults(param0 + 1, param1 + '\u001e' + var2.name, param2);
                    } catch (Exception var8) {
                        param2.append("[[ EXCEPTION ").append(var8).append(" ]]");
                    }
                }
            }

        }
    }

    private static String getComment() {
        String[] var0 = new String[]{
            "Shiny numbers!",
            "Am I not running fast enough? :(",
            "I'm working as hard as I can!",
            "Will I ever be good enough for you? :(",
            "Speedy. Zoooooom!",
            "Hello world",
            "40% better than a crash report.",
            "Now with extra numbers",
            "Now with less numbers",
            "Now with the same numbers",
            "You should add flames to things, it makes them go faster!",
            "Do you feel the need for... optimization?",
            "*cracks redstone whip*",
            "Maybe if you treated it better then it'll have more motivation to work faster! Poor server."
        };

        try {
            return var0[(int)(Util.getNanos() % (long)var0.length)];
        } catch (Throwable var2) {
            return "Witty comment unavailable :(";
        }
    }

    @Override
    public int getTickDuration() {
        return this.tickDuration;
    }
}
