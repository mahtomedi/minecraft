package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.WorldVersion;
import net.minecraft.server.Bootstrap;
import org.slf4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path rootOutputFolder;
    private final PackOutput vanillaPackOutput;
    private final List<DataProvider> allProviders = Lists.newArrayList();
    private final List<DataProvider> providersToRun = Lists.newArrayList();
    private final WorldVersion version;
    private final boolean alwaysGenerate;

    public DataGenerator(Path param0, WorldVersion param1, boolean param2) {
        this.rootOutputFolder = param0;
        this.vanillaPackOutput = new PackOutput(this.rootOutputFolder);
        this.version = param1;
        this.alwaysGenerate = param2;
    }

    public void run() throws IOException {
        HashCache var0 = new HashCache(this.rootOutputFolder, this.allProviders, this.version);
        Stopwatch var1 = Stopwatch.createStarted();
        Stopwatch var2 = Stopwatch.createUnstarted();

        for(DataProvider var3 : this.providersToRun) {
            if (!this.alwaysGenerate && !var0.shouldRunInThisVersion(var3)) {
                LOGGER.debug("Generator {} already run for version {}", var3.getName(), this.version.getName());
            } else {
                LOGGER.info("Starting provider: {}", var3.getName());
                var2.start();
                var3.run(var0.getUpdater(var3));
                var2.stop();
                LOGGER.info("{} finished after {} ms", var3.getName(), var2.elapsed(TimeUnit.MILLISECONDS));
                var2.reset();
            }
        }

        LOGGER.info("All providers took: {} ms", var1.elapsed(TimeUnit.MILLISECONDS));
        var0.purgeStaleAndWrite();
    }

    public void addProvider(boolean param0, DataProvider param1) {
        if (param0) {
            this.providersToRun.add(param1);
        }

        this.allProviders.add(param1);
    }

    public PackOutput getVanillaPackOutput() {
        return this.vanillaPackOutput;
    }

    public PackOutput createBuiltinDatapackOutput(String param0) {
        Path var0 = this.vanillaPackOutput.getOutputFolder(PackOutput.Target.DATA_PACK).resolve("minecraft").resolve("datapacks").resolve(param0);
        return new PackOutput(var0);
    }

    static {
        Bootstrap.bootStrap();
    }
}
