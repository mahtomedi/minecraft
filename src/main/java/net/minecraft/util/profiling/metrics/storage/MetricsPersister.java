package net.minecraft.util.profiling.metrics.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.metrics.MetricCategory;
import net.minecraft.util.profiling.metrics.MetricSampler;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MetricsPersister {
    public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
    public static final String METRICS_DIR_NAME = "metrics";
    public static final String DEVIATIONS_DIR_NAME = "deviations";
    public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
    private static final Logger LOGGER = LogManager.getLogger();
    private final String rootFolderName;

    public MetricsPersister(String param0) {
        this.rootFolderName = param0;
    }

    public Path saveReports(Set<MetricSampler> param0, Map<MetricSampler, List<RecordedDeviation>> param1, ProfileResults param2) {
        try {
            Files.createDirectories(PROFILING_RESULTS_DIR);
        } catch (IOException var8) {
            throw new UncheckedIOException(var8);
        }

        try {
            Path var1 = Files.createTempDirectory("minecraft-profiling");
            var1.toFile().deleteOnExit();
            Files.createDirectories(PROFILING_RESULTS_DIR);
            Path var2 = var1.resolve(this.rootFolderName);
            Path var3 = var2.resolve("metrics");
            this.saveMetrics(param0, var3);
            if (!param1.isEmpty()) {
                this.saveDeviations(param1, var2.resolve("deviations"));
            }

            this.saveProfilingTaskExecutionResult(param2, var2);
            return var1;
        } catch (IOException var7) {
            throw new UncheckedIOException(var7);
        }
    }

    private void saveMetrics(Set<MetricSampler> param0, Path param1) {
        if (param0.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one sampler to persist");
        } else {
            Map<MetricCategory, List<MetricSampler>> var0 = param0.stream().collect(Collectors.groupingBy(MetricSampler::getCategory));
            var0.forEach((param1x, param2) -> this.saveCategory(param1x, param2, param1));
        }
    }

    private void saveCategory(MetricCategory param0, List<MetricSampler> param1, Path param2) {
        Path var0 = param2.resolve(Util.sanitizeName(param0.getDescription(), ResourceLocation::validPathChar) + ".csv");
        Writer var1 = null;

        try {
            Files.createDirectories(var0.getParent());
            var1 = Files.newBufferedWriter(var0, StandardCharsets.UTF_8);
            CsvOutput.Builder var2 = CsvOutput.builder();
            var2.addColumn("@tick");

            for(MetricSampler var3 : param1) {
                var2.addColumn(var3.getName());
            }

            CsvOutput var4 = var2.build(var1);
            List<MetricSampler.SamplerResult> var5 = param1.stream().map(MetricSampler::result).collect(Collectors.toList());
            int var6 = var5.stream().mapToInt(MetricSampler.SamplerResult::getFirstTick).summaryStatistics().getMin();
            int var7 = var5.stream().mapToInt(MetricSampler.SamplerResult::getLastTick).summaryStatistics().getMax();

            for(int var8 = var6; var8 <= var7; ++var8) {
                int var9 = var8;
                Stream<String> var10 = var5.stream().map(param1x -> String.valueOf(param1x.valueAtTick(var9)));
                Object[] var11 = Stream.concat(Stream.of(String.valueOf(var8)), var10).toArray(param0x -> new String[param0x]);
                var4.writeRow(var11);
            }

            LOGGER.info("Flushed metrics to {}", var0);
        } catch (Exception var18) {
            LOGGER.error("Could not save profiler results to {}", var0, var18);
        } finally {
            IOUtils.closeQuietly(var1);
        }

    }

    private void saveDeviations(Map<MetricSampler, List<RecordedDeviation>> param0, Path param1) {
        DateTimeFormatter var0 = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss.SSS", Locale.UK).withZone(ZoneId.systemDefault());
        param0.forEach(
            (param2, param3) -> param3.forEach(
                    param3x -> {
                        String var0x = var0.format(param3x.timestamp);
                        Path var1x = param1.resolve(Util.sanitizeName(param2.getName(), ResourceLocation::validPathChar))
                            .resolve(String.format(Locale.ROOT, "%d@%s.txt", param3x.tick, var0x));
                        param3x.profilerResultAtTick.saveResults(var1x);
                    }
                )
        );
    }

    private void saveProfilingTaskExecutionResult(ProfileResults param0, Path param1) {
        param0.saveResults(param1.resolve("profiling.txt"));
    }
}
