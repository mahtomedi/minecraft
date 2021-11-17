package net.minecraft.data;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.server.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DataGenerator {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Collection<Path> inputFolders;
    private final Path outputFolder;
    private final List<DataProvider> providers = Lists.newArrayList();

    public DataGenerator(Path param0, Collection<Path> param1) {
        this.outputFolder = param0;
        this.inputFolders = param1;
    }

    public Collection<Path> getInputFolders() {
        return this.inputFolders;
    }

    public Path getOutputFolder() {
        return this.outputFolder;
    }

    public void run() throws IOException {
        HashCache var0 = new HashCache(this.outputFolder, "cache");
        var0.keep(this.getOutputFolder().resolve("version.json"));
        Stopwatch var1 = Stopwatch.createStarted();
        Stopwatch var2 = Stopwatch.createUnstarted();

        for(DataProvider var3 : this.providers) {
            LOGGER.info("Starting provider: {}", var3.getName());
            var2.start();
            var3.run(var0);
            var2.stop();
            LOGGER.info("{} finished after {} ms", var3.getName(), var2.elapsed(TimeUnit.MILLISECONDS));
            var2.reset();
        }

        LOGGER.info("All providers took: {} ms", var1.elapsed(TimeUnit.MILLISECONDS));
        var0.purgeStaleAndWrite();
    }

    public void addProvider(DataProvider param0) {
        this.providers.add(param0);
    }

    static {
        Bootstrap.bootStrap();
    }
}
