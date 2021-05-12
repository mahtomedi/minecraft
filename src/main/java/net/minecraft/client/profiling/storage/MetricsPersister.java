package net.minecraft.client.profiling.storage;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.profiling.metric.FpsSpikeRecording;
import net.minecraft.client.profiling.metric.MetricSampler;
import net.minecraft.client.profiling.metric.SamplerCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CsvOutput;
import net.minecraft.util.profiling.ContinuousProfiler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class MetricsPersister {
    public static final Path PROFILING_RESULTS_DIR = Paths.get("debug/profiling");
    public static final String METRICS_DIR_NAME = "metrics";
    public static final String DEVIATIONS_DIR_NAME = "deviations";
    public static final String PROFILING_RESULT_FILENAME = "profiling.txt";
    private static final Logger LOGGER = LogManager.getLogger();
    public static final FileSystemProvider ZIP_FILE_SYSTEM_PROVIDER = FileSystemProvider.installedProviders()
        .stream()
        .filter(param0 -> param0.getScheme().equalsIgnoreCase("jar"))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No jar file system provider found"));

    public Path saveReports(List<SamplerCategory> param0, List<FpsSpikeRecording> param1, ContinuousProfiler param2) {
        try {
            Files.createDirectories(PROFILING_RESULTS_DIR);
        } catch (IOException var11) {
            throw new UncheckedIOException(var11);
        }

        Path var1 = PROFILING_RESULTS_DIR.resolve(new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + ".tmp");

        try (FileSystem var2 = ZIP_FILE_SYSTEM_PROVIDER.newFileSystem(var1, ImmutableMap.of("create", "true"))) {
            Files.createDirectories(PROFILING_RESULTS_DIR);
            Path var3 = var2.getPath("/");
            Path var4 = var3.resolve("metrics");

            for(SamplerCategory var5 : param0) {
                this.saveMetrics(var5, var4);
            }

            if (!param1.isEmpty()) {
                this.saveSpikeLogs(param1, var3.resolve("deviations"));
            }

            this.saveProfilingTaskExecutionResult(param2, var3);
        } catch (IOException var13) {
            throw new UncheckedIOException(var13);
        }

        return this.renameZipFile(var1);
    }

    private void saveMetrics(SamplerCategory param0, Path param1) {
        String var0 = param0.getName();
        List<MetricSampler> var1 = param0.getMetricSamplers();
        if (var1.isEmpty()) {
            throw new IllegalArgumentException("Expected at least one sampler for category: " + var0);
        } else {
            IntSummaryStatistics var2 = var1.stream().collect(Collectors.summarizingInt(MetricSampler::numberOfValues));
            if (var2.getMax() != var2.getMin()) {
                throw new IllegalStateException(
                    String.format("Expected all samples within category %s to contain same amount of samples, got %s", param0, var2)
                );
            } else {
                Path var3 = param1.resolve(Util.sanitizeName(var0, ResourceLocation::validPathChar) + ".csv");
                Writer var4 = null;

                try {
                    Files.createDirectories(var3.getParent());
                    var4 = Files.newBufferedWriter(var3, StandardCharsets.UTF_8);
                    CsvOutput.Builder var5 = CsvOutput.builder();

                    for(MetricSampler var6 : var1) {
                        var5.addColumn(var6.getMetric().getName());
                    }

                    CsvOutput var7 = var5.build(var4);

                    while(var1.get(0).hasMoreValues()) {
                        Double[] var8 = var1.stream().map(MetricSampler::readNextValue).toArray(param0x -> new Double[param0x]);
                        var7.writeRow(var8);
                    }

                    LOGGER.info("Flushed metrics to {}", var3);
                } catch (Exception var14) {
                    LOGGER.error("Could not save profiler results to {}", var3, var14);
                } finally {
                    IOUtils.closeQuietly(var4);
                }

            }
        }
    }

    private void saveSpikeLogs(List<FpsSpikeRecording> param0, Path param1) {
        SimpleDateFormat var0 = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS");

        for(FpsSpikeRecording var1 : param0) {
            String var2 = var0.format(var1.timestamp);
            Path var3 = param1.resolve(String.format("%d@%s.txt", var1.tick, var2));
            var1.profilerResultForSpikeFrame.saveResults(var3);
        }

    }

    private void saveProfilingTaskExecutionResult(ContinuousProfiler param0, Path param1) {
        param0.getResults().saveResults(param1.resolve("profiling.txt"));
    }

    private Path renameZipFile(Path param0) {
        try {
            return Files.move(param0, param0.resolveSibling(StringUtils.substringBefore(param0.getFileName().toString(), ".tmp") + ".zip"));
        } catch (IOException var3) {
            throw new UncheckedIOException(var3);
        }
    }
}
