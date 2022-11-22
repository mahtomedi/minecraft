package net.minecraft.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
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
    private final Map<String, HashCache.ProviderCache> caches;
    private final Set<String> cachesToWrite = new HashSet<>();
    private final Set<Path> cachePaths = new HashSet<>();
    private final int initialCount;
    private int writes;

    private Path getProviderCachePath(String param0) {
        return this.cacheDir.resolve(Hashing.sha1().hashString(param0, StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path param0, Collection<String> param1, WorldVersion param2) throws IOException {
        this.versionId = param2.getName();
        this.rootDir = param0;
        this.cacheDir = param0.resolve(".cache");
        Files.createDirectories(this.cacheDir);
        Map<String, HashCache.ProviderCache> var0 = new HashMap<>();
        int var1 = 0;

        for(String var2 : param1) {
            Path var3 = this.getProviderCachePath(var2);
            this.cachePaths.add(var3);
            HashCache.ProviderCache var4 = readCache(param0, var3);
            var0.put(var2, var4);
            var1 += var4.count();
        }

        this.caches = var0;
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

        return new HashCache.ProviderCache("unknown", ImmutableMap.of());
    }

    public boolean shouldRunInThisVersion(String param0) {
        HashCache.ProviderCache var0 = (HashCache.ProviderCache)this.caches.get(param0);
        return var0 == null || !var0.version.equals(this.versionId);
    }

    public CompletableFuture<HashCache.UpdateResult> generateUpdate(String param0, HashCache.UpdateFunction param1) {
        HashCache.ProviderCache var0 = (HashCache.ProviderCache)this.caches.get(param0);
        if (var0 == null) {
            throw new IllegalStateException("Provider not registered: " + param0);
        } else {
            HashCache.CacheUpdater var1 = new HashCache.CacheUpdater(param0, this.versionId, var0);
            return param1.update(var1).thenApply(param1x -> var1.close());
        }
    }

    public void applyUpdate(HashCache.UpdateResult param0) {
        this.caches.put(param0.providerId(), param0.cache());
        this.cachesToWrite.add(param0.providerId());
        this.writes += param0.writes();
    }

    public void purgeStaleAndWrite() throws IOException {
        Set<Path> var0 = new HashSet<>();
        this.caches.forEach((param1, param2) -> {
            if (this.cachesToWrite.contains(param1)) {
                Path var0x = this.getProviderCachePath(param1);
                param2.save(this.rootDir, var0x, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + param1);
            }

            var0.addAll(param2.data().keySet());
        });
        var0.add(this.rootDir.resolve("version.json"));
        MutableInt var1 = new MutableInt();
        MutableInt var2 = new MutableInt();

        try (Stream<Path> var3 = Files.walk(this.rootDir)) {
            var3.forEach(param3 -> {
                if (!Files.isDirectory(param3)) {
                    if (!this.cachePaths.contains(param3)) {
                        var1.increment();
                        if (!var0.contains(param3)) {
                            try {
                                Files.delete(param3);
                            } catch (IOException var6) {
                                LOGGER.warn("Failed to delete file {}", param3, var6);
                            }

                            var2.increment();
                        }
                    }
                }
            });
        }

        LOGGER.info(
            "Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", var1, this.initialCount, var0.size(), var2, this.writes
        );
    }

    class CacheUpdater implements CachedOutput {
        private final String provider;
        private final HashCache.ProviderCache oldCache;
        private final HashCache.ProviderCacheBuilder newCache;
        private final AtomicInteger writes = new AtomicInteger();
        private volatile boolean closed;

        CacheUpdater(String param0, String param1, HashCache.ProviderCache param2) {
            this.provider = param0;
            this.oldCache = param2;
            this.newCache = new HashCache.ProviderCacheBuilder(param1);
        }

        private boolean shouldWrite(Path param0, HashCode param1) {
            return !Objects.equals(this.oldCache.get(param0), param1) || !Files.exists(param0);
        }

        @Override
        public void writeIfNeeded(Path param0, byte[] param1, HashCode param2) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("Cannot write to cache as it has already been closed");
            } else {
                if (this.shouldWrite(param0, param2)) {
                    this.writes.incrementAndGet();
                    Files.createDirectories(param0.getParent());
                    Files.write(param0, param1);
                }

                this.newCache.put(param0, param2);
            }
        }

        public HashCache.UpdateResult close() {
            this.closed = true;
            return new HashCache.UpdateResult(this.provider, this.newCache.build(), this.writes.get());
        }
    }

    static record ProviderCache(String version, ImmutableMap<Path, HashCode> data) {
        @Nullable
        public HashCode get(Path param0) {
            return this.data.get(param0);
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
                Builder<Path, HashCode> var4 = ImmutableMap.builder();
                var0.lines().forEach(param2 -> {
                    int var0x = param2.indexOf(32);
                    var4.put(param0.resolve(param2.substring(var0x + 1)), HashCode.fromString(param2.substring(0, var0x)));
                });
                var7 = new HashCache.ProviderCache(var3, var4.build());
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

    static record ProviderCacheBuilder(String version, ConcurrentMap<Path, HashCode> data) {
        ProviderCacheBuilder(String param0) {
            this(param0, new ConcurrentHashMap<>());
        }

        public void put(Path param0, HashCode param1) {
            this.data.put(param0, param1);
        }

        public HashCache.ProviderCache build() {
            return new HashCache.ProviderCache(this.version, ImmutableMap.copyOf(this.data));
        }
    }

    @FunctionalInterface
    public interface UpdateFunction {
        CompletableFuture<?> update(CachedOutput var1);
    }

    public static record UpdateResult(String providerId, HashCache.ProviderCache cache, int writes) {
    }
}
