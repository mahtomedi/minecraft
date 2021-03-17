package net.minecraft.util.profiling;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilledProfileResults implements ProfileResults {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ProfilerPathEntry EMPTY = new ProfilerPathEntry() {
        @Override
        public long getDuration() {
            return 0L;
        }

        @Override
        public long getCount() {
            return 0L;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.emptyMap();
        }
    };
    private static final Splitter SPLITTER = Splitter.on('\u001e');
    private static final Comparator<Entry<String, FilledProfileResults.CounterCollector>> COUNTER_ENTRY_COMPARATOR = Entry.<String, FilledProfileResults.CounterCollector>comparingByValue(
            Comparator.comparingLong(param0 -> param0.totalValue)
        )
        .reversed();
    private final Map<String, ? extends ProfilerPathEntry> entries;
    private final long startTimeNano;
    private final int startTimeTicks;
    private final long endTimeNano;
    private final int endTimeTicks;
    private final int tickDuration;

    public FilledProfileResults(Map<String, ? extends ProfilerPathEntry> param0, long param1, int param2, long param3, int param4) {
        this.entries = param0;
        this.startTimeNano = param1;
        this.startTimeTicks = param2;
        this.endTimeNano = param3;
        this.endTimeTicks = param4;
        this.tickDuration = param4 - param2;
    }

    private ProfilerPathEntry getEntry(String param0) {
        ProfilerPathEntry var0 = this.entries.get(param0);
        return var0 != null ? var0 : EMPTY;
    }

    @Override
    public List<ResultField> getTimes(String param0) {
        String var0 = param0;
        ProfilerPathEntry var1 = this.getEntry("root");
        long var2 = var1.getDuration();
        ProfilerPathEntry var3 = this.getEntry(param0);
        long var4 = var3.getDuration();
        long var5 = var3.getCount();
        List<ResultField> var6 = Lists.newArrayList();
        if (!param0.isEmpty()) {
            param0 = param0 + '\u001e';
        }

        long var7 = 0L;

        for(String var8 : this.entries.keySet()) {
            if (isDirectChild(param0, var8)) {
                var7 += this.getEntry(var8).getDuration();
            }
        }

        float var9 = (float)var7;
        if (var7 < var4) {
            var7 = var4;
        }

        if (var2 < var7) {
            var2 = var7;
        }

        for(String var10 : this.entries.keySet()) {
            if (isDirectChild(param0, var10)) {
                ProfilerPathEntry var11 = this.getEntry(var10);
                long var12 = var11.getDuration();
                double var13 = (double)var12 * 100.0 / (double)var7;
                double var14 = (double)var12 * 100.0 / (double)var2;
                String var15 = var10.substring(param0.length());
                var6.add(new ResultField(var15, var13, var14, var11.getCount()));
            }
        }

        if ((float)var7 > var9) {
            var6.add(
                new ResultField("unspecified", (double)((float)var7 - var9) * 100.0 / (double)var7, (double)((float)var7 - var9) * 100.0 / (double)var2, var5)
            );
        }

        Collections.sort(var6);
        var6.add(0, new ResultField(var0, 100.0, (double)var7 * 100.0 / (double)var2, var5));
        return var6;
    }

    private static boolean isDirectChild(String param0, String param1) {
        return param1.length() > param0.length() && param1.startsWith(param0) && param1.indexOf(30, param0.length() + 1) < 0;
    }

    private Map<String, FilledProfileResults.CounterCollector> getCounterValues() {
        Map<String, FilledProfileResults.CounterCollector> var0 = Maps.newTreeMap();
        this.entries
            .forEach(
                (param1, param2) -> {
                    Object2LongMap<String> var0x = param2.getCounters();
                    if (!var0x.isEmpty()) {
                        List<String> var1x = SPLITTER.splitToList(param1);
                        var0x.forEach(
                            (param2x, param3) -> var0.computeIfAbsent(param2x, param0x -> new FilledProfileResults.CounterCollector())
                                    .addValue(var1x.iterator(), param3)
                        );
                    }
        
                }
            );
        return var0;
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
    public boolean saveResults(Path param0) {
        Writer var0 = null;

        boolean var4;
        try {
            Files.createDirectories(param0.getParent());
            var0 = Files.newBufferedWriter(param0, StandardCharsets.UTF_8);
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
        Map<String, FilledProfileResults.CounterCollector> var1 = this.getCounterValues();
        if (!var1.isEmpty()) {
            var0.append("--- BEGIN COUNTER DUMP ---\n\n");
            this.appendCounters(var1, var0, param1);
            var0.append("--- END COUNTER DUMP ---\n\n");
        }

        return var0.toString();
    }

    private static StringBuilder indentLine(StringBuilder param0, int param1) {
        param0.append(String.format("[%02d] ", param1));

        for(int var0 = 0; var0 < param1; ++var0) {
            param0.append("|   ");
        }

        return param0;
    }

    private void appendProfilerResults(int param0, String param1, StringBuilder param2) {
        List<ResultField> var0 = this.getTimes(param1);
        Object2LongMap<String> var1 = ObjectUtils.firstNonNull(this.entries.get(param1), EMPTY).getCounters();
        var1.forEach(
            (param2x, param3) -> indentLine(param2, param0)
                    .append('#')
                    .append(param2x)
                    .append(' ')
                    .append(param3)
                    .append('/')
                    .append(param3 / (long)this.tickDuration)
                    .append('\n')
        );
        if (var0.size() >= 3) {
            for(int var2 = 1; var2 < var0.size(); ++var2) {
                ResultField var3 = var0.get(var2);
                indentLine(param2, param0)
                    .append(var3.name)
                    .append('(')
                    .append(var3.count)
                    .append('/')
                    .append(String.format(Locale.ROOT, "%.0f", (float)var3.count / (float)this.tickDuration))
                    .append(')')
                    .append(" - ")
                    .append(String.format(Locale.ROOT, "%.2f", var3.percentage))
                    .append("%/")
                    .append(String.format(Locale.ROOT, "%.2f", var3.globalPercentage))
                    .append("%\n");
                if (!"unspecified".equals(var3.name)) {
                    try {
                        this.appendProfilerResults(param0 + 1, param1 + '\u001e' + var3.name, param2);
                    } catch (Exception var9) {
                        param2.append("[[ EXCEPTION ").append(var9).append(" ]]");
                    }
                }
            }

        }
    }

    private void appendCounterResults(int param0, String param1, FilledProfileResults.CounterCollector param2, int param3, StringBuilder param4) {
        indentLine(param4, param0)
            .append(param1)
            .append(" total:")
            .append(param2.selfValue)
            .append('/')
            .append(param2.totalValue)
            .append(" average: ")
            .append(param2.selfValue / (long)param3)
            .append('/')
            .append(param2.totalValue / (long)param3)
            .append('\n');
        param2.children
            .entrySet()
            .stream()
            .sorted(COUNTER_ENTRY_COMPARATOR)
            .forEach(param3x -> this.appendCounterResults(param0 + 1, param3x.getKey(), param3x.getValue(), param3, param4));
    }

    private void appendCounters(Map<String, FilledProfileResults.CounterCollector> param0, StringBuilder param1, int param2) {
        param0.forEach((param2x, param3) -> {
            param1.append("-- Counter: ").append(param2x).append(" --\n");
            this.appendCounterResults(0, "root", param3.children.get("root"), param2, param1);
            param1.append("\n\n");
        });
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

    static class CounterCollector {
        private long selfValue;
        private long totalValue;
        private final Map<String, FilledProfileResults.CounterCollector> children = Maps.newHashMap();

        private CounterCollector() {
        }

        public void addValue(Iterator<String> param0, long param1) {
            this.totalValue += param1;
            if (!param0.hasNext()) {
                this.selfValue += param1;
            } else {
                this.children.computeIfAbsent(param0.next(), param0x -> new FilledProfileResults.CounterCollector()).addValue(param0, param1);
            }

        }
    }
}
