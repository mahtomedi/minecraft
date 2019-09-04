package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsDefaultVertexFormat;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsScreen lastScreen;
    private final WorldDownload worldDownload;
    private final String downloadTitle;
    private final RateLimiter narrationRateLimiter;
    private RealmsButton cancelButton;
    private final String worldName;
    private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
    private volatile String errorMessage;
    private volatile String status;
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean showDots = true;
    private volatile boolean finished;
    private volatile boolean extracting;
    private Long previousWrittenBytes;
    private Long previousTimeSnapshot;
    private long bytesPersSecond;
    private int animTick;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private int dotIndex;
    private final int WARNING_ID = 100;
    private int confirmationId = -1;
    private boolean checked;
    private static final ReentrantLock downloadLock = new ReentrantLock();

    public RealmsDownloadLatestWorldScreen(RealmsScreen param0, WorldDownload param1, String param2) {
        this.lastScreen = param0;
        this.worldName = param2;
        this.worldDownload = param1;
        this.downloadStatus = new RealmsDownloadLatestWorldScreen.DownloadStatus();
        this.downloadTitle = getLocalizedString("mco.download.title");
        this.narrationRateLimiter = RateLimiter.create(0.1F);
    }

    public void setConfirmationId(int param0) {
        this.confirmationId = param0;
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(this.cancelButton = new RealmsButton(0, this.width() / 2 - 100, this.height() - 42, 200, 20, getLocalizedString("gui.cancel")) {
            @Override
            public void onPress() {
                RealmsDownloadLatestWorldScreen.this.cancelled = true;
                RealmsDownloadLatestWorldScreen.this.backButtonClicked();
            }
        });
        this.checkDownloadSize();
    }

    private void checkDownloadSize() {
        if (!this.finished) {
            if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
                String var0 = getLocalizedString("mco.download.confirmation.line1", new Object[]{humanReadableSize(5368709120L)});
                String var1 = getLocalizedString("mco.download.confirmation.line2");
                Realms.setScreen(new RealmsLongConfirmationScreen(this, RealmsLongConfirmationScreen.Type.Warning, var0, var1, false, 100));
            } else {
                this.downloadSave();
            }

        }
    }

    @Override
    public void confirmResult(boolean param0, int param1) {
        this.checked = true;
        Realms.setScreen(this);
        this.downloadSave();
    }

    private long getContentLength(String param0) {
        FileDownload var0 = new FileDownload();
        return var0.contentLength(param0);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            List<String> var0 = Lists.newArrayList();
            var0.add(this.downloadTitle);
            var0.add(this.status);
            if (this.progress != null) {
                var0.add(this.progress + "%");
                var0.add(humanReadableSpeed(this.bytesPersSecond));
            }

            if (this.errorMessage != null) {
                var0.add(this.errorMessage);
            }

            String var1 = String.join(System.lineSeparator(), var0);
            Realms.narrateNow(var1);
        }

    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.cancelled = true;
            this.backButtonClicked();
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private void backButtonClicked() {
        if (this.finished && this.confirmationId != -1 && this.errorMessage == null) {
            this.lastScreen.confirmResult(true, this.confirmationId);
        }

        Realms.setScreen(this.lastScreen);
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        if (this.extracting && !this.finished) {
            this.status = getLocalizedString("mco.download.extracting");
        }

        this.drawCenteredString(this.downloadTitle, this.width() / 2, 20, 16777215);
        this.drawCenteredString(this.status, this.width() / 2, 50, 16777215);
        if (this.showDots) {
            this.drawDots();
        }

        if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar();
            this.drawDownloadSpeed();
        }

        if (this.errorMessage != null) {
            this.drawCenteredString(this.errorMessage, this.width() / 2, 110, 16711680);
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
        double var0 = this.downloadStatus.bytesWritten.doubleValue() / this.downloadStatus.totalBytes.doubleValue() * 100.0;
        this.progress = String.format(Locale.ROOT, "%.1f", var0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        Tezzelator var1 = Tezzelator.instance;
        var1.begin(7, RealmsDefaultVertexFormat.POSITION_COLOR);
        double var2 = (double)(this.width() / 2 - 100);
        double var3 = 0.5;
        var1.vertex(var2 - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var1.vertex(var2 + 200.0 * var0 / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var1.vertex(var2 + 200.0 * var0 / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var1.vertex(var2 - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var1.vertex(var2, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var1.vertex(var2 + 200.0 * var0 / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var1.vertex(var2 + 200.0 * var0 / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var1.vertex(var2, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var1.end();
        RenderSystem.enableTexture();
        this.drawCenteredString(this.progress + " %", this.width() / 2, 84, 16777215);
    }

    private void drawDownloadSpeed() {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long var0 = System.currentTimeMillis() - this.previousTimeSnapshot;
                if (var0 == 0L) {
                    var0 = 1L;
                }

                this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / var0;
                this.drawDownloadSpeed0(this.bytesPersSecond);
            }

            this.previousWrittenBytes = this.downloadStatus.bytesWritten;
            this.previousTimeSnapshot = System.currentTimeMillis();
        } else {
            this.drawDownloadSpeed0(this.bytesPersSecond);
        }

    }

    private void drawDownloadSpeed0(long param0) {
        if (param0 > 0L) {
            int var0 = this.fontWidth(this.progress);
            String var1 = "(" + humanReadableSpeed(param0) + ")";
            this.drawString(var1, this.width() / 2 + var0 / 2 + 15, 84, 16777215);
        }

    }

    public static String humanReadableSpeed(long param0) {
        int var0 = 1024;
        if (param0 < 1024L) {
            return param0 + " B/s";
        } else {
            int var1 = (int)(Math.log((double)param0) / Math.log(1024.0));
            String var2 = "KMGTPE".charAt(var1 - 1) + "";
            return String.format(Locale.ROOT, "%.1f %sB/s", (double)param0 / Math.pow(1024.0, (double)var1), var2);
        }
    }

    public static String humanReadableSize(long param0) {
        int var0 = 1024;
        if (param0 < 1024L) {
            return param0 + " B";
        } else {
            int var1 = (int)(Math.log((double)param0) / Math.log(1024.0));
            String var2 = "KMGTPE".charAt(var1 - 1) + "";
            return String.format(Locale.ROOT, "%.0f %sB", (double)param0 / Math.pow(1024.0, (double)var1), var2);
        }
    }

    private void downloadSave() {
        new Thread(() -> {
            try {
                if (downloadLock.tryLock(1L, TimeUnit.SECONDS)) {
                    this.status = getLocalizedString("mco.download.preparing");
                    if (this.cancelled) {
                        this.downloadCancelled();
                        return;
                    }

                    this.status = getLocalizedString("mco.download.downloading", new Object[]{this.worldName});
                    FileDownload var0 = new FileDownload();
                    var0.contentLength(this.worldDownload.downloadLink);
                    var0.download(this.worldDownload, this.worldName, this.downloadStatus, this.getLevelStorageSource());

                    while(!var0.isFinished()) {
                        if (var0.isError()) {
                            var0.cancel();
                            this.errorMessage = getLocalizedString("mco.download.failed");
                            this.cancelButton.setMessage(getLocalizedString("gui.done"));
                            return;
                        }

                        if (var0.isExtracting()) {
                            this.extracting = true;
                        }

                        if (this.cancelled) {
                            var0.cancel();
                            this.downloadCancelled();
                            return;
                        }

                        try {
                            Thread.sleep(500L);
                        } catch (InterruptedException var8) {
                            LOGGER.error("Failed to check Realms backup download status");
                        }
                    }

                    this.finished = true;
                    this.status = getLocalizedString("mco.download.done");
                    this.cancelButton.setMessage(getLocalizedString("gui.done"));
                    return;
                }
            } catch (InterruptedException var9) {
                LOGGER.error("Could not acquire upload lock");
                return;
            } catch (Exception var10) {
                this.errorMessage = getLocalizedString("mco.download.failed");
                var10.printStackTrace();
                return;
            } finally {
                if (!downloadLock.isHeldByCurrentThread()) {
                    return;
                }

                downloadLock.unlock();
                this.showDots = false;
                this.finished = true;
            }

        }).start();
    }

    private void downloadCancelled() {
        this.status = getLocalizedString("mco.download.cancelled");
    }

    @OnlyIn(Dist.CLIENT)
    public class DownloadStatus {
        public volatile Long bytesWritten = 0L;
        public volatile Long totalBytes = 0L;
    }
}
