package net.minecraft.data;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HashCache {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path path;
    private final Path cachePath;
    private int hits;
    private final Map<Path, String> oldCache = Maps.newHashMap();
    private final Map<Path, String> newCache = Maps.newHashMap();
    private final Set<Path> keep = Sets.newHashSet();

    public HashCache(Path param0, String param1) throws IOException {
        this.path = param0;
        Path var0 = param0.resolve(".cache");
        Files.createDirectories(var0);
        this.cachePath = var0.resolve(param1);
        this.walkOutputFiles().forEach(param0x -> this.oldCache.put(param0x, ""));
        if (Files.isReadable(this.cachePath)) {
            IOUtils.readLines(Files.newInputStream(this.cachePath), Charsets.UTF_8).forEach(param1x -> {
                int var0x = param1x.indexOf(32);
                this.oldCache.put(param0.resolve(param1x.substring(var0x + 1)), param1x.substring(0, var0x));
            });
        }

    }

    public void purgeStaleAndWrite() throws IOException {
        this.removeStale();

        Writer var0;
        try {
            var0 = Files.newBufferedWriter(this.cachePath);
        } catch (IOException var3) {
            LOGGER.warn("Unable write cachefile {}: {}", this.cachePath, var3.toString());
            return;
        }

        IOUtils.writeLines(
            this.newCache
                .entrySet()
                .stream()
                .map(param0 -> (String)param0.getValue() + " " + this.path.relativize(param0.getKey()))
                .collect(Collectors.toList()),
            System.lineSeparator(),
            var0
        );
        var0.close();
        LOGGER.debug("Caching: cache hits: {}, created: {} removed: {}", this.hits, this.newCache.size() - this.hits, this.oldCache.size());
    }

    @Nullable
    public String getHash(Path param0) {
        return this.oldCache.get(param0);
    }

    public void putNew(Path param0, String param1) {
        this.newCache.put(param0, param1);
        if (Objects.equals(this.oldCache.remove(param0), param1)) {
            ++this.hits;
        }

    }

    public boolean had(Path param0) {
        return this.oldCache.containsKey(param0);
    }

    public void keep(Path param0) {
        this.keep.add(param0);
    }

    private void removeStale() throws IOException {
        this.walkOutputFiles().forEach(param0 -> {
            if (this.had(param0) && !this.keep.contains(param0)) {
                try {
                    Files.delete(param0);
                } catch (IOException var3) {
                    LOGGER.debug("Unable to delete: {} ({})", param0, var3.toString());
                }
            }

        });
    }

    private Stream<Path> walkOutputFiles() throws IOException {
        return Files.walk(this.path).filter(param0 -> !Objects.equals(this.cachePath, param0) && !Files.isDirectory(param0));
    }
}
