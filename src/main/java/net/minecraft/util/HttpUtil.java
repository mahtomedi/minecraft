package net.minecraft.util;

import com.google.common.hash.Funnels;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.OptionalLong;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class HttpUtil {
    private static final Logger LOGGER = LogUtils.getLogger();

    private HttpUtil() {
    }

    public static Path downloadFile(
        Path param0,
        URL param1,
        Map<String, String> param2,
        HashFunction param3,
        @Nullable HashCode param4,
        int param5,
        Proxy param6,
        HttpUtil.DownloadProgressListener param7
    ) {
        HttpURLConnection var0 = null;
        InputStream var1 = null;
        param7.requestStart();
        Path var2;
        if (param4 != null) {
            var2 = cachedFilePath(param0, param4);

            try {
                if (checkExistingFile(var2, param3, param4)) {
                    LOGGER.info("Returning cached file since actual hash matches requested");
                    param7.requestFinished();
                    return var2;
                }
            } catch (IOException var34) {
                LOGGER.warn("Failed to check cached file {}", var2, var34);
            }

            try {
                LOGGER.warn("Existing file {} not found or had mismatched hash", var2);
                Files.deleteIfExists(var2);
            } catch (IOException var33) {
                param7.requestFinished();
                throw new UncheckedIOException("Failed to remove existing file " + var2, var33);
            }
        } else {
            var2 = null;
        }

        Path var10;
        try {
            var0 = (HttpURLConnection)param1.openConnection(param6);
            var0.setInstanceFollowRedirects(true);
            param2.forEach(var0::setRequestProperty);
            var1 = var0.getInputStream();
            long var6 = var0.getContentLengthLong();
            OptionalLong var7 = var6 != -1L ? OptionalLong.of(var6) : OptionalLong.empty();
            FileUtil.createDirectoriesSafe(param0);
            param7.downloadStart(var7);
            if (var7.isPresent() && var7.getAsLong() > (long)param5) {
                throw new IOException("Filesize is bigger than maximum allowed (file is " + var7 + ", limit is " + param5 + ")");
            }

            if (var2 == null) {
                Path var9 = Files.createTempFile(param0, "download", ".tmp");

                try {
                    HashCode var10 = downloadAndHash(param3, param5, param7, var1, var9);
                    Path var11 = cachedFilePath(param0, var10);
                    if (!checkExistingFile(var11, param3, var10)) {
                        Files.move(var9, var11, StandardCopyOption.REPLACE_EXISTING);
                    }

                    return var11;
                } finally {
                    Files.deleteIfExists(var9);
                }
            }

            HashCode var8 = downloadAndHash(param3, param5, param7, var1, var2);
            if (!var8.equals(param4)) {
                throw new IOException("Hash of downloaded file (" + var8 + ") did not match requested (" + param4 + ")");
            }

            var10 = var2;
        } catch (Throwable var36) {
            if (var0 != null) {
                InputStream var13 = var0.getErrorStream();
                if (var13 != null) {
                    try {
                        LOGGER.error("HTTP response error: {}", IOUtils.toString(var13, StandardCharsets.UTF_8));
                    } catch (Exception var32) {
                        LOGGER.error("Failed to read response from server");
                    }
                }
            }

            throw new IllegalStateException("Failed to download file " + param1, var36);
        } finally {
            param7.requestFinished();
            IOUtils.closeQuietly(var1);
        }

        return var10;
    }

    private static HashCode hashFile(Path param0, HashFunction param1) throws IOException {
        Hasher var0 = param1.newHasher();

        try (
            OutputStream var1 = Funnels.asOutputStream(var0);
            InputStream var2 = Files.newInputStream(param0);
        ) {
            var2.transferTo(var1);
        }

        return var0.hash();
    }

    private static boolean checkExistingFile(Path param0, HashFunction param1, HashCode param2) throws IOException {
        if (Files.exists(param0)) {
            HashCode var0 = hashFile(param0, param1);
            if (var0.equals(param2)) {
                return true;
            }

            LOGGER.warn("Mismatched hash of file {}, expected {} but found {}", param0, param2, var0);
        }

        return false;
    }

    private static Path cachedFilePath(Path param0, HashCode param1) {
        return param0.resolve(param1.toString());
    }

    private static HashCode downloadAndHash(HashFunction param0, int param1, HttpUtil.DownloadProgressListener param2, InputStream param3, Path param4) throws IOException {
        HashCode var11;
        try (OutputStream var0 = Files.newOutputStream(param4, StandardOpenOption.CREATE)) {
            Hasher var1 = param0.newHasher();
            byte[] var2 = new byte[8196];
            long var3 = 0L;

            int var4;
            while((var4 = param3.read(var2)) >= 0) {
                var3 += (long)var4;
                param2.downloadedBytes(var3);
                if (var3 > (long)param1) {
                    throw new IOException("Filesize was bigger than maximum allowed (got >= " + var3 + ", limit was " + param1 + ")");
                }

                if (Thread.interrupted()) {
                    LOGGER.error("INTERRUPTED");
                    throw new IOException("Download interrupted");
                }

                var0.write(var2, 0, var4);
                var1.putBytes(var2, 0, var4);
            }

            var11 = var1.hash();
        }

        return var11;
    }

    public static int getAvailablePort() {
        try {
            int var11;
            try (ServerSocket var0 = new ServerSocket(0)) {
                var11 = var0.getLocalPort();
            }

            return var11;
        } catch (IOException var5) {
            return 25564;
        }
    }

    public static boolean isPortAvailable(int param0) {
        if (param0 >= 0 && param0 <= 65535) {
            try {
                boolean var2;
                try (ServerSocket var0 = new ServerSocket(param0)) {
                    var2 = var0.getLocalPort() == param0;
                }

                return var2;
            } catch (IOException var6) {
                return false;
            }
        } else {
            return false;
        }
    }

    public interface DownloadProgressListener {
        void requestStart();

        void downloadStart(OptionalLong var1);

        void downloadedBytes(long var1);

        void requestFinished();
    }
}
