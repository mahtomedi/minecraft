package net.minecraft.server.packs;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.eventlog.JsonEventLog;
import net.minecraft.util.thread.ProcessorMailbox;
import org.slf4j.Logger;

public class DownloadQueue implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path cacheDir;
    private final JsonEventLog<DownloadQueue.LogEntry> eventLog;
    private final ProcessorMailbox<Runnable> tasks = ProcessorMailbox.create(Util.nonCriticalIoPool(), "download-queue");

    public DownloadQueue(Path param0) throws IOException {
        this.cacheDir = param0;
        FileUtil.createDirectoriesSafe(param0);
        this.eventLog = JsonEventLog.open(DownloadQueue.LogEntry.CODEC, param0.resolve("log.json"));
    }

    private DownloadQueue.BatchResult runDownload(DownloadQueue.BatchConfig param0, Map<UUID, DownloadQueue.DownloadRequest> param1) {
        DownloadQueue.BatchResult var0 = new DownloadQueue.BatchResult();
        param1.forEach(
            (param2, param3) -> {
                Path var0x = this.cacheDir.resolve(param2.toString());
                Path var1x = null;
    
                try {
                    var1x = HttpUtil.downloadFile(
                        var0x, param3.url, param0.headers, param0.hashFunction, param3.hash, param0.maxSize, param0.proxy, param0.listener
                    );
                    var0.downloaded.put(param2, var1x);
                } catch (Exception var9) {
                    LOGGER.error("Failed to download {}", param3.url, var9);
                    var0.failed.add(param2);
                }
    
                try {
                    this.eventLog
                        .write(
                            new DownloadQueue.LogEntry(
                                param2,
                                param3.url.toString(),
                                Instant.now(),
                                Optional.ofNullable(param3.hash).map(HashCode::toString),
                                var1x != null ? this.getFileInfo(var1x) : Either.left("download_failed")
                            )
                        );
                } catch (Exception var8) {
                    LOGGER.error("Failed to log download of {}", param3.url, var8);
                }
    
            }
        );
        return var0;
    }

    private Either<String, DownloadQueue.FileInfoEntry> getFileInfo(Path param0) {
        try {
            long var0 = Files.size(param0);
            Path var1 = this.cacheDir.relativize(param0);
            return Either.right(new DownloadQueue.FileInfoEntry(var1.toString(), var0));
        } catch (IOException var5) {
            LOGGER.error("Failed to get file size of {}", param0, var5);
            return Either.left("no_access");
        }
    }

    public CompletableFuture<DownloadQueue.BatchResult> downloadBatch(DownloadQueue.BatchConfig param0, Map<UUID, DownloadQueue.DownloadRequest> param1) {
        return CompletableFuture.supplyAsync(() -> this.runDownload(param0, param1), this.tasks::tell);
    }

    @Override
    public void close() throws IOException {
        this.tasks.close();
        this.eventLog.close();
    }

    public static record BatchConfig(
        HashFunction hashFunction, int maxSize, Map<String, String> headers, Proxy proxy, HttpUtil.DownloadProgressListener listener
    ) {
    }

    public static record BatchResult(Map<UUID, Path> downloaded, Set<UUID> failed) {
        public BatchResult() {
            this(new HashMap<>(), new HashSet<>());
        }
    }

    public static record DownloadRequest(URL url, @Nullable HashCode hash) {
    }

    static record FileInfoEntry(String name, long size) {
        public static final Codec<DownloadQueue.FileInfoEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        Codec.STRING.fieldOf("name").forGetter(DownloadQueue.FileInfoEntry::name),
                        Codec.LONG.fieldOf("size").forGetter(DownloadQueue.FileInfoEntry::size)
                    )
                    .apply(param0, DownloadQueue.FileInfoEntry::new)
        );
    }

    static record LogEntry(UUID id, String url, Instant time, Optional<String> hash, Either<String, DownloadQueue.FileInfoEntry> errorOrFileInfo) {
        public static final Codec<DownloadQueue.LogEntry> CODEC = RecordCodecBuilder.create(
            param0 -> param0.group(
                        UUIDUtil.STRING_CODEC.fieldOf("id").forGetter(DownloadQueue.LogEntry::id),
                        Codec.STRING.fieldOf("url").forGetter(DownloadQueue.LogEntry::url),
                        ExtraCodecs.INSTANT_ISO8601.fieldOf("time").forGetter(DownloadQueue.LogEntry::time),
                        Codec.STRING.optionalFieldOf("hash").forGetter(DownloadQueue.LogEntry::hash),
                        Codec.mapEither(Codec.STRING.fieldOf("error"), DownloadQueue.FileInfoEntry.CODEC.fieldOf("file"))
                            .forGetter(DownloadQueue.LogEntry::errorOrFileInfo)
                    )
                    .apply(param0, DownloadQueue.LogEntry::new)
        );
    }
}
