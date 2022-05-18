package net.minecraft.data;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.WorldVersion;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<DataProvider, HashCache.ProviderCache> existingCaches;
    private final Map<DataProvider, HashCache.CacheUpdater> cachesToWrite = new HashMap<>();
    private final Set<Path> cachePaths = new HashSet<>();
    private final int initialCount;

    private Path getProviderCachePath(DataProvider param0) {
        return this.cacheDir.resolve(Hashing.sha1().hashString(param0.getName(), StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path param0, List<DataProvider> param1, WorldVersion param2) throws IOException {
        this.versionId = param2.getName();
        this.rootDir = param0;
        this.cacheDir = param0.resolve(".cache");
        Files.createDirectories(this.cacheDir);
        Map<DataProvider, HashCache.ProviderCache> var0 = new HashMap<>();
        int var1 = 0;

        for(DataProvider var2 : param1) {
            Path var3 = this.getProviderCachePath(var2);
            this.cachePaths.add(var3);
            HashCache.ProviderCache var4 = readCache(param0, var3);
            var0.put(var2, var4);
            var1 += var4.count();
        }

        this.existingCaches = var0;
        this.initialCount = var1;
    }

    private static HashCache.ProviderCache readCache(Path param0, Path param1) {
        if (Files.isReadable(param1)) {
            try {
                return HashCache.ProviderCache.load(param0, param1);
            } catch (Exception var3) {
                LOGGER.warn("Failed to parse cache {}, discarding", param1, var3);
            }
        }

        return new HashCache.ProviderCache("unknown");
    }

    public boolean shouldRunInThisVersion(DataProvider param0) {
        HashCache.ProviderCache var0 = this.existingCaches.get(param0);
        return var0 == null || !var0.version.equals(this.versionId);
    }

    public CachedOutput getUpdater(DataProvider param0) {
        return this.cachesToWrite.computeIfAbsent(param0, param0x -> {
            HashCache.ProviderCache var0 = this.existingCaches.get(param0x);
            if (var0 == null) {
                throw new IllegalStateException("Provider not registered: " + param0x.getName());
            } else {
                HashCache.CacheUpdater var1x = new HashCache.CacheUpdater(this.versionId, var0);
                this.existingCaches.put(param0x, var1x.newCache);
                return var1x;
            }
        });
    }

    public void purgeStaleAndWrite() throws IOException {
        MutableInt var0 = new MutableInt();
        this.cachesToWrite.forEach((param1, param2) -> {
            Path var0x = this.getProviderCachePath(param1);
            param2.newCache.save(this.rootDir, var0x, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + param1.getName());
            var0.add(param2.writes);
        });
        Set<Path> var1 = new HashSet<>();
        this.existingCaches.values().forEach(param1 -> var1.addAll(param1.data().keySet()));
        var1.add(this.rootDir.resolve("version.json"));
        MutableInt var2 = new MutableInt();
        MutableInt var3 = new MutableInt();

        try (Stream<Path> var4 = Files.walk(this.rootDir)) {
            var4.forEach(param3 -> {
                if (!Files.isDirectory(param3)) {
                    if (!this.cachePaths.contains(param3)) {
                        var2.increment();
                        if (!var1.contains(param3)) {
                            try {
                                Files.delete(param3);
                            } catch (IOException var6) {
                                LOGGER.warn("Failed to delete file {}", param3, var6);
                            }

                            var3.increment();
                        }
                    }
                }
            });
        }

        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", var2, this.initialCount, var1.size(), var3, var0);
    }

    static class CacheUpdater implements CachedOutput {
        private final HashCache.ProviderCache oldCache;
        final HashCache.ProviderCache newCache;
        int writes;

        CacheUpdater(String param0, HashCache.ProviderCache param1) {
            this.oldCache = param1;
            this.newCache = new HashCache.ProviderCache(param0);
        }

        private boolean shouldWrite(Path param0, HashCode param1) {
            return !Objects.equals(this.oldCache.get(param0), param1) || !Files.exists(param0);
        }

        @Override
        public void writeIfNeeded(Path param0, byte[] param1, HashCode param2) throws IOException {
            if (this.shouldWrite(param0, param2)) {
                ++this.writes;
                Files.createDirectories(param0.getParent());
                Files.write(param0, param1);
            }

            this.newCache.put(param0, param2);
        }
    }

    static record ProviderCache(String version, Map<Path, HashCode> data) {
        ProviderCache(String param0) {
            this(param0, new HashMap<>());
        }

        @Nullable
        public HashCode get(Path param0) {
            return this.data.get(param0);
        }

        public void put(Path param0, HashCode param1) {
            this.data.put(param0, param1);
        }

        public int count() {
            return this.data.size();
        }

        public static HashCache.ProviderCache load(Path param0, Path param1) throws IOException {
            HashCache.ProviderCache var7;
            try (BufferedReader var0 = Files.newBufferedReader(param1, StandardCharsets.UTF_8)) {
                String var1 = var0.readLine();
                if (!var1.startsWith("// ")) {
                    throw new IllegalStateException("Missing cache file header");
                }

                String[] var2 = var1.substring("// ".length()).split("\t", 2);
                String var3 = var2[0];
                Map<Path, HashCode> var4 = new HashMap<>();
                var0.lines().forEach(param2 -> {
                    int var0x = param2.indexOf(32);
                    var4.put(param0.resolve(param2.substring(var0x + 1)), HashCode.fromString(param2.substring(0, var0x)));
                });
                var7 = new HashCache.ProviderCache(var3, Map.copyOf(var4));
            }

            return var7;
        }

        public void save(Path param0, Path param1, String param2) {
            try (BufferedWriter var0 = Files.newBufferedWriter(param1, StandardCharsets.UTF_8)) {
                var0.write("// ");
                var0.write(this.version);
                var0.write(9);
                var0.write(param2);
                var0.newLine();

                for(Entry<Path, HashCode> var1 : this.data.entrySet()) {
                    var0.write(var1.getValue().toString());
                    var0.write(32);
                    var0.write(param0.relativize(var1.getKey()).toString());
                    var0.newLine();
                }
            } catch (IOException var9) {
                HashCache.LOGGER.warn("Unable write cachefile {}: {}", param1, var9);
            }

        }
    }
}
