package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.FileUpload;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.client.UploadStatus;
import com.mojang.realmsclient.dto.UploadInfo;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.util.UploadTokenCache;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPOutputStream;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsUploadScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsResetWorldScreen lastScreen;
    private final RealmsLevelSummary selectedLevel;
    private final long worldId;
    private final int slotId;
    private final UploadStatus uploadStatus;
    private final RateLimiter narrationRateLimiter;
    private volatile String errorMessage;
    private volatile String status;
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean uploadFinished;
    private volatile boolean showDots = true;
    private volatile boolean uploadStarted;
    private RealmsButton backButton;
    private RealmsButton cancelButton;
    private int animTick;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private int dotIndex;
    private Long previousWrittenBytes;
    private Long previousTimeSnapshot;
    private long bytesPersSecond;
    private static final ReentrantLock uploadLock = new ReentrantLock();

    public RealmsUploadScreen(long param0, int param1, RealmsResetWorldScreen param2, RealmsLevelSummary param3) {
        this.worldId = param0;
        this.slotId = param1;
        this.lastScreen = param2;
        this.selectedLevel = param3;
        this.uploadStatus = new UploadStatus();
        this.narrationRateLimiter = RateLimiter.create(0.1F);
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.backButton = new RealmsButton(1, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                RealmsUploadScreen.this.onBack();
            }
        };
        this.buttonsAdd(this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
            @Override
            public void onPress() {
                RealmsUploadScreen.this.onCancel();
            }
        });
        if (!this.uploadStarted) {
            if (this.lastScreen.slot == -1) {
                this.upload();
            } else {
                this.lastScreen.switchSlot(this);
            }
        }

    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        if (param0 && !this.uploadStarted) {
            this.uploadStarted = true;
            Realms.setScreen(this);
            this.upload();
        }

    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    private void onBack() {
        this.lastScreen.confirmResult(true, 0);
    }

    private void onCancel() {
        this.cancelled = true;
        Realms.setScreen(this.lastScreen);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            if (this.showDots) {
                this.onCancel();
            } else {
                this.onBack();
            }

            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
            this.status = getLocalizedString("mco.upload.verifying");
            this.cancelButton.active(false);
        }

        this.drawCenteredString(this.status, this.width() / 2, 50, 16777215);
        if (this.showDots) {
            this.drawDots();
        }

        if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar();
            this.drawUploadSpeed();
        }

        if (this.errorMessage != null) {
            String[] var0 = this.errorMessage.split("\\\\n");

            for(int var1 = 0; var1 < var0.length; ++var1) {
                this.drawCenteredString(var0[var1], this.width() / 2, 110 + 12 * var1, 16711680);
            }
        }

        super.render(param0, param1, param2);
    }

    private void drawDots() {
        int var0 = this.fontWidth(this.status);
        if (this.animTick % 10 == 0) {
            ++this.dotIndex;
        }

        this.drawString(DOTS[this.dotIndex % DOTS.length], this.width() / 2 + var0 / 2 + 5, 50, 16777215);
    }

    private void drawProgressBar() {
        double var0 = this.uploadStatus.bytesWritten.doubleValue() / this.uploadStatus.totalBytes.doubleValue() * 100.0;
        if (var0 > 100.0) {
            var0 = 100.0;
        }

        this.progress = String.format(Locale.ROOT, "%.1f", var0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        double var1 = (double)(this.width() / 2 - 100);
        double var2 = 0.5;
        Tezzelator var3 = Tezzelator.instance;
        var3.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
        var3.vertex(var1 - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var3.vertex(var1 + 200.0 * var0 / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var3.vertex(var1 + 200.0 * var0 / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var3.vertex(var1 - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var3.vertex(var1, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var3.vertex(var1 + 200.0 * var0 / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var3.vertex(var1 + 200.0 * var0 / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var3.vertex(var1, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var3.end();
        RenderSystem.enableTexture();
        this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
    }

    private void drawUploadSpeed() {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long var0 = System.currentTimeMillis() - this.previousTimeSnapshot;
                if (var0 == 0L) {
                    var0 = 1L;
                }

                this.bytesPersSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / var0;
                this.drawUploadSpeed0(this.bytesPersSecond);
            }

            this.previousWrittenBytes = this.uploadStatus.bytesWritten;
            this.previousTimeSnapshot = System.currentTimeMillis();
        } else {
            this.drawUploadSpeed0(this.bytesPersSecond);
        }

    }

    private void drawUploadSpeed0(long param0) {
        if (param0 > 0L) {
            int var0 = this.fontWidth(this.progress);
            String var1 = "(" + humanReadableByteCount(param0) + ")";
            this.drawString(var1, this.width() / 2 + var0 / 2 + 15, 84, 16777215);
        }

    }

    public static String humanReadableByteCount(long param0) {
        int var0 = 1024;
        if (param0 < 1024L) {
            return param0 + " B";
        } else {
            int var1 = (int)(Math.log((double)param0) / Math.log(1024.0));
            String var2 = "KMGTPE".charAt(var1 - 1) + "";
            return String.format(Locale.ROOT, "%.1f %sB/s", (double)param0 / Math.pow(1024.0, (double)var1), var2);
        }
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            List<String> var0 = Lists.newArrayList();
            var0.add(this.status);
            if (this.progress != null) {
                var0.add(this.progress + "%");
            }

            if (this.errorMessage != null) {
                var0.add(this.errorMessage);
            }

            Realms.narrateNow(String.join(System.lineSeparator(), var0));
        }

    }

    public static RealmsUploadScreen.Unit getLargestUnit(long param0) {
        if (param0 < 1024L) {
            return RealmsUploadScreen.Unit.B;
        } else {
            int var0 = (int)(Math.log((double)param0) / Math.log(1024.0));
            String var1 = "KMGTPE".charAt(var0 - 1) + "";

            try {
                return RealmsUploadScreen.Unit.valueOf(var1 + "B");
            } catch (Exception var5) {
                return RealmsUploadScreen.Unit.GB;
            }
        }
    }

    public static double convertToUnit(long param0, RealmsUploadScreen.Unit param1) {
        return param1.equals(RealmsUploadScreen.Unit.B) ? (double)param0 : (double)param0 / Math.pow(1024.0, (double)param1.ordinal());
    }

    public static String humanReadableSize(long param0, RealmsUploadScreen.Unit param1) {
        return String.format("%." + (param1.equals(RealmsUploadScreen.Unit.GB) ? "1" : "0") + "f %s", convertToUnit(param0, param1), param1.name());
    }

    private void upload() {
        this.uploadStarted = true;
        new Thread(
                () -> {
                    File var0 = null;
                    RealmsClient var1 = RealmsClient.createRealmsClient();
                    long var2 = this.worldId;
        
                    try {
                        UploadInfo var3;
                        try {
                            if (uploadLock.tryLock(1L, TimeUnit.SECONDS)) {
                                this.status = getLocalizedString("mco.upload.preparing");
                                var3 = null;
        
                                int var4;
                                for(var4 = 0; var4 < 20; ++var4) {
                                    try {
                                        if (this.cancelled) {
                                            this.uploadCancelled();
                                            return;
                                        }
        
                                        var3 = var1.upload(var2, UploadTokenCache.get(var2));
                                        break;
                                    } catch (RetryCallException var20) {
                                        Thread.sleep((long)(var20.delaySeconds * 1000));
                                    }
                                }
        
                                if (var3 == null) {
                                    this.status = getLocalizedString("mco.upload.close.failure");
                                    return;
                                }
        
                                UploadTokenCache.put(var2, var3.getToken());
                                if (!var3.isWorldClosed()) {
                                    this.status = getLocalizedString("mco.upload.close.failure");
                                    return;
                                }
        
                                if (this.cancelled) {
                                    this.uploadCancelled();
                                    return;
                                }
        
                                var4 = (int)(new File(Realms.getGameDirectoryPath(), "saves"));
                                var0 = this.tarGzipArchive(new File(var4, this.selectedLevel.getLevelId()));
                                if (this.cancelled) {
                                    this.uploadCancelled();
                                    return;
                                }
        
                                if (this.verify(var0)) {
                                    this.status = getLocalizedString("mco.upload.uploading", new Object[]{this.selectedLevel.getLevelName()});
                                    FileUpload var11 = new FileUpload(
                                        var0,
                                        this.worldId,
                                        this.slotId,
                                        var3,
                                        Realms.getSessionId(),
                                        Realms.getName(),
                                        Realms.getMinecraftVersionString(),
                                        this.uploadStatus
                                    );
                                    var11.upload(param1 -> {
                                        if (param1.statusCode >= 200 && param1.statusCode < 300) {
                                            this.uploadFinished = true;
                                            this.status = getLocalizedString("mco.upload.done");
                                            this.backButton.setMessage(getLocalizedString("gui.done"));
                                            UploadTokenCache.invalidate(var2);
                                        } else if (param1.statusCode == 400 && param1.errorMessage != null) {
                                            this.errorMessage = getLocalizedString("mco.upload.failed", new Object[]{param1.errorMessage});
                                        } else {
                                            this.errorMessage = getLocalizedString("mco.upload.failed", new Object[]{param1.statusCode});
                                        }
        
                                    });
        
                                    while(!var11.isFinished()) {
                                        if (this.cancelled) {
                                            var11.cancel();
                                            this.uploadCancelled();
                                            return;
                                        }
        
                                        try {
                                            Thread.sleep(500L);
                                        } catch (InterruptedException var19) {
                                            LOGGER.error("Failed to check Realms file upload status");
                                        }
                                    }
        
                                    return;
                                }
        
                                long var7 = var0.length();
                                RealmsUploadScreen.Unit var8 = getLargestUnit(var7);
                                RealmsUploadScreen.Unit var9 = getLargestUnit(5368709120L);
                                if (humanReadableSize(var7, var8).equals(humanReadableSize(5368709120L, var9)) && var8 != RealmsUploadScreen.Unit.B) {
                                    RealmsUploadScreen.Unit var10 = RealmsUploadScreen.Unit.values()[var8.ordinal() - 1];
                                    this.errorMessage = getLocalizedString("mco.upload.size.failure.line1", new Object[]{this.selectedLevel.getLevelName()})
                                        + "\\n"
                                        + getLocalizedString(
                                            "mco.upload.size.failure.line2",
                                            new Object[]{humanReadableSize(var7, var10), humanReadableSize(5368709120L, var10)}
                                        );
                                    return;
                                }
        
                                this.errorMessage = getLocalizedString("mco.upload.size.failure.line1", new Object[]{this.selectedLevel.getLevelName()})
                                    + "\\n"
                                    + getLocalizedString(
                                        "mco.upload.size.failure.line2", new Object[]{humanReadableSize(var7, var8), humanReadableSize(5368709120L, var9)}
                                    );
                                return;
                            }
                        } catch (IOException var21) {
                            var3 = var21;
                            this.errorMessage = getLocalizedString("mco.upload.failed", new Object[]{var21.getMessage()});
                            return;
                        } catch (RealmsServiceException var22) {
                            var3 = var22;
                            this.errorMessage = getLocalizedString("mco.upload.failed", new Object[]{var22.toString()});
                            return;
                        } catch (InterruptedException var23) {
                            var3 = var23;
                            LOGGER.error("Could not acquire upload lock");
                            return;
                        }
                    } finally {
                        this.uploadFinished = true;
                        if (uploadLock.isHeldByCurrentThread()) {
                            uploadLock.unlock();
                            this.showDots = false;
                            this.childrenClear();
                            this.buttonsAdd(this.backButton);
                            if (var0 != null) {
                                LOGGER.debug("Deleting file " + var0.getAbsolutePath());
                                var0.delete();
                            }
        
                        }
        
                        return;
                    }
        
                }
            )
            .start();
    }

    private void uploadCancelled() {
        this.status = getLocalizedString("mco.upload.cancelled");
        LOGGER.debug("Upload was cancelled");
    }

    private boolean verify(File param0) {
        return param0.length() < 5368709120L;
    }

    private File tarGzipArchive(File param0) throws IOException {
        TarArchiveOutputStream var0 = null;

        File var4;
        try {
            File var1 = File.createTempFile("realms-upload-file", ".tar.gz");
            var0 = new TarArchiveOutputStream(new GZIPOutputStream(new FileOutputStream(var1)));
            var0.setLongFileMode(3);
            this.addFileToTarGz(var0, param0.getAbsolutePath(), "world", true);
            var0.finish();
            var4 = var1;
        } finally {
            if (var0 != null) {
                var0.close();
            }

        }

        return var4;
    }

    private void addFileToTarGz(TarArchiveOutputStream param0, String param1, String param2, boolean param3) throws IOException {
        if (!this.cancelled) {
            File var0 = new File(param1);
            String var1 = param3 ? param2 : param2 + var0.getName();
            TarArchiveEntry var2 = new TarArchiveEntry(var0, var1);
            param0.putArchiveEntry(var2);
            if (var0.isFile()) {
                IOUtils.copy(new FileInputStream(var0), param0);
                param0.closeArchiveEntry();
            } else {
                param0.closeArchiveEntry();
                File[] var3 = var0.listFiles();
                if (var3 != null) {
                    for(File var4 : var3) {
                        this.addFileToTarGz(param0, var4.getAbsolutePath(), var1 + "/", false);
                    }
                }
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum Unit {
        B,
        KB,
        MB,
        GB;
    }
}
