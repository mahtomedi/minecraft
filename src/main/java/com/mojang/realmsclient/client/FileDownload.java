package com.mojang.realmsclient.client;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.gui.screens.RealmsDownloadLatestWorldScreen;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class FileDownload {
    static final Logger LOGGER = LogUtils.getLogger();
    volatile boolean cancelled;
    volatile boolean finished;
    volatile boolean error;
    volatile boolean extracting;
    @Nullable
    private volatile File tempFile;
    volatile File resourcePackPath;
    @Nullable
    private volatile HttpGet request;
    @Nullable
    private Thread currentThread;
    private final RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(120000).setConnectTimeout(120000).build();
    private static final String[] INVALID_FILE_NAMES = new String[]{
        "CON",
        "COM",
        "PRN",
        "AUX",
        "CLOCK$",
        "NUL",
        "COM1",
        "COM2",
        "COM3",
        "COM4",
        "COM5",
        "COM6",
        "COM7",
        "COM8",
        "COM9",
        "LPT1",
        "LPT2",
        "LPT3",
        "LPT4",
        "LPT5",
        "LPT6",
        "LPT7",
        "LPT8",
        "LPT9"
    };

    public long contentLength(String param0) {
        CloseableHttpClient var0 = null;
        HttpGet var1 = null;

        long var5;
        try {
            var1 = new HttpGet(param0);
            var0 = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
            CloseableHttpResponse var2 = var0.execute(var1);
            return Long.parseLong(var2.getFirstHeader("Content-Length").getValue());
        } catch (Throwable var16) {
            LOGGER.error("Unable to get content length for download");
            var5 = 0L;
        } finally {
            if (var1 != null) {
                var1.releaseConnection();
            }

            if (var0 != null) {
                try {
                    var0.close();
                } catch (IOException var15) {
                    LOGGER.error("Could not close http client", (Throwable)var15);
                }
            }

        }

        return var5;
    }

    public void download(WorldDownload param0, String param1, RealmsDownloadLatestWorldScreen.DownloadStatus param2, LevelStorageSource param3) {
        if (this.currentThread == null) {
            this.currentThread = new Thread(() -> {
                CloseableHttpClient var0 = null;

                try {
                    this.tempFile = File.createTempFile("backup", ".tar.gz");
                    this.request = new HttpGet(param0.downloadLink);
                    var0 = HttpClientBuilder.create().setDefaultRequestConfig(this.requestConfig).build();
                    HttpResponse var1 = var0.execute(this.request);
                    param2.totalBytes = Long.parseLong(var1.getFirstHeader("Content-Length").getValue());
                    if (var1.getStatusLine().getStatusCode() == 200) {
                        OutputStream var8 = new FileOutputStream(this.tempFile);
                        FileDownload.ProgressListener var9 = new FileDownload.ProgressListener(param1.trim(), this.tempFile, param3, param2);
                        FileDownload.DownloadCountingOutputStream var10 = new FileDownload.DownloadCountingOutputStream(var8);
                        var10.setListener(var9);
                        IOUtils.copy(var1.getEntity().getContent(), var10);
                        return;
                    }

                    this.error = true;
                    this.request.abort();
                } catch (Exception var93) {
                    LOGGER.error("Caught exception while downloading: {}", var93.getMessage());
                    this.error = true;
                    return;
                } finally {
                    this.request.releaseConnection();
                    if (this.tempFile != null) {
                        this.tempFile.delete();
                    }

                    if (!this.error) {
                        if (!param0.resourcePackUrl.isEmpty() && !param0.resourcePackHash.isEmpty()) {
                            try {
                                this.tempFile = File.createTempFile("resources", ".tar.gz");
                                this.request = new HttpGet(param0.resourcePackUrl);
                                HttpResponse var24 = var0.execute(this.request);
                                param2.totalBytes = Long.parseLong(var24.getFirstHeader("Content-Length").getValue());
                                if (var24.getStatusLine().getStatusCode() != 200) {
                                    this.error = true;
                                    this.request.abort();
                                    return;
                                }

                                OutputStream var25 = new FileOutputStream(this.tempFile);
                                FileDownload.ResourcePackProgressListener var26 = new FileDownload.ResourcePackProgressListener(this.tempFile, param2, param0);
                                FileDownload.DownloadCountingOutputStream var27 = new FileDownload.DownloadCountingOutputStream(var25);
                                var27.setListener(var26);
                                IOUtils.copy(var24.getEntity().getContent(), var27);
                            } catch (Exception var91) {
                                LOGGER.error("Caught exception while downloading: {}", var91.getMessage());
                                this.error = true;
                            } finally {
                                this.request.releaseConnection();
                                if (this.tempFile != null) {
                                    this.tempFile.delete();
                                }

                            }
                        } else {
                            this.finished = true;
                        }
                    }

                    if (var0 != null) {
                        try {
                            var0.close();
                        } catch (IOException var90) {
                            LOGGER.error("Failed to close Realms download client");
                        }
                    }

                }

            });
            this.currentThread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
            this.currentThread.start();
        }
    }

    public void cancel() {
        if (this.request != null) {
            this.request.abort();
        }

        if (this.tempFile != null) {
            this.tempFile.delete();
        }

        this.cancelled = true;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isError() {
        return this.error;
    }

    public boolean isExtracting() {
        return this.extracting;
    }

    public static String findAvailableFolderName(String param0) {
        param0 = param0.replaceAll("[\\./\"]", "_");

        for(String var0 : INVALID_FILE_NAMES) {
            if (param0.equalsIgnoreCase(var0)) {
                param0 = "_" + param0 + "_";
            }
        }

        return param0;
    }

    void untarGzipArchive(String param0, @Nullable File param1, LevelStorageSource param2) throws IOException {
        Pattern var0 = Pattern.compile(".*-([0-9]+)$");
        int var1 = 1;

        for(char var2 : SharedConstants.ILLEGAL_FILE_CHARACTERS) {
            param0 = param0.replace(var2, '_');
        }

        if (StringUtils.isEmpty(param0)) {
            param0 = "Realm";
        }

        param0 = findAvailableFolderName(param0);

        try {
            for(LevelStorageSource.LevelDirectory var3 : param2.findLevelCandidates()) {
                String var4 = var3.directoryName();
                if (var4.toLowerCase(Locale.ROOT).startsWith(param0.toLowerCase(Locale.ROOT))) {
                    Matcher var5 = var0.matcher(var4);
                    if (var5.matches()) {
                        int var6 = Integer.parseInt(var5.group(1));
                        if (var6 > var1) {
                            var1 = var6;
                        }
                    } else {
                        ++var1;
                    }
                }
            }
        } catch (Exception var43) {
            LOGGER.error("Error getting level list", (Throwable)var43);
            this.error = true;
            return;
        }

        String var10;
        if (param2.isNewLevelIdAcceptable(param0) && var1 <= 1) {
            var10 = param0;
        } else {
            var10 = param0 + (var1 == 1 ? "" : "-" + var1);
            if (!param2.isNewLevelIdAcceptable(var10)) {
                boolean var9 = false;

                while(!var9) {
                    ++var1;
                    var10 = param0 + (var1 == 1 ? "" : "-" + var1);
                    if (param2.isNewLevelIdAcceptable(var10)) {
                        var9 = true;
                    }
                }
            }
        }

        TarArchiveInputStream var11 = null;
        File var12 = new File(Minecraft.getInstance().gameDirectory.getAbsolutePath(), "saves");

        try {
            var12.mkdir();
            var11 = new TarArchiveInputStream(new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(param1))));

            for(TarArchiveEntry var13 = var11.getNextTarEntry(); var13 != null; var13 = var11.getNextTarEntry()) {
                File var14 = new File(var12, var13.getName().replace("world", var10));
                if (var13.isDirectory()) {
                    var14.mkdirs();
                } else {
                    var14.createNewFile();

                    try (FileOutputStream var15 = new FileOutputStream(var14)) {
                        IOUtils.copy(var11, var15);
                    }
                }
            }
        } catch (Exception var41) {
            LOGGER.error("Error extracting world", (Throwable)var41);
            this.error = true;
        } finally {
            if (var11 != null) {
                var11.close();
            }

            if (param1 != null) {
                param1.delete();
            }

            try (LevelStorageSource.LevelStorageAccess var23 = param2.validateAndCreateAccess(var10)) {
                var23.renameAndDropPlayer(var10);
            } catch (NbtException | ReportedNbtException | IOException var39) {
                LOGGER.error("Failed to modify unpacked realms level {}", var10, var39);
            } catch (ContentValidationException var40) {
                LOGGER.warn("{}", var40.getMessage());
            }

            this.resourcePackPath = new File(var12, var10 + File.separator + "resources.zip");
        }

    }

    @OnlyIn(Dist.CLIENT)
    static class DownloadCountingOutputStream extends CountingOutputStream {
        @Nullable
        private ActionListener listener;

        public DownloadCountingOutputStream(OutputStream param0) {
            super(param0);
        }

        public void setListener(ActionListener param0) {
            this.listener = param0;
        }

        @Override
        protected void afterWrite(int param0) throws IOException {
            super.afterWrite(param0);
            if (this.listener != null) {
                this.listener.actionPerformed(new ActionEvent(this, 0, null));
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class ProgressListener implements ActionListener {
        private final String worldName;
        private final File tempFile;
        private final LevelStorageSource levelStorageSource;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;

        ProgressListener(String param0, File param1, LevelStorageSource param2, RealmsDownloadLatestWorldScreen.DownloadStatus param3) {
            this.worldName = param0;
            this.tempFile = param1;
            this.levelStorageSource = param2;
            this.downloadStatus = param3;
        }

        @Override
        public void actionPerformed(ActionEvent param0) {
            this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)param0.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled && !FileDownload.this.error) {
                try {
                    FileDownload.this.extracting = true;
                    FileDownload.this.untarGzipArchive(this.worldName, this.tempFile, this.levelStorageSource);
                } catch (IOException var3) {
                    FileDownload.LOGGER.error("Error extracting archive", (Throwable)var3);
                    FileDownload.this.error = true;
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class ResourcePackProgressListener implements ActionListener {
        private final File tempFile;
        private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
        private final WorldDownload worldDownload;

        ResourcePackProgressListener(File param0, RealmsDownloadLatestWorldScreen.DownloadStatus param1, WorldDownload param2) {
            this.tempFile = param0;
            this.downloadStatus = param1;
            this.worldDownload = param2;
        }

        @Override
        public void actionPerformed(ActionEvent param0) {
            this.downloadStatus.bytesWritten = ((FileDownload.DownloadCountingOutputStream)param0.getSource()).getByteCount();
            if (this.downloadStatus.bytesWritten >= this.downloadStatus.totalBytes && !FileDownload.this.cancelled) {
                try {
                    String var0 = Hashing.sha1().hashBytes(Files.toByteArray(this.tempFile)).toString();
                    if (var0.equals(this.worldDownload.resourcePackHash)) {
                        FileUtils.copyFile(this.tempFile, FileDownload.this.resourcePackPath);
                        FileDownload.this.finished = true;
                    } else {
                        FileDownload.LOGGER
                            .error("Resourcepack had wrong hash (expected {}, found {}). Deleting it.", this.worldDownload.resourcePackHash, var0);
                        FileUtils.deleteQuietly(this.tempFile);
                        FileDownload.this.error = true;
                    }
                } catch (IOException var3) {
                    FileDownload.LOGGER.error("Error copying resourcepack file: {}", var3.getMessage());
                    FileDownload.this.error = true;
                }
            }

        }
    }
}
