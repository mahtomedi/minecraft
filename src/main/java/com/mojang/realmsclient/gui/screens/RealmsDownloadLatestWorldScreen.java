package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsDownloadLatestWorldScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
    private final Screen lastScreen;
    private final WorldDownload worldDownload;
    private final Component downloadTitle;
    private final RateLimiter narrationRateLimiter;
    private Button cancelButton;
    private final String worldName;
    private final RealmsDownloadLatestWorldScreen.DownloadStatus downloadStatus;
    private volatile Component errorMessage;
    private volatile Component status = new TranslatableComponent("mco.download.preparing");
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
    private boolean checked;
    private final BooleanConsumer callback;

    public RealmsDownloadLatestWorldScreen(Screen param0, WorldDownload param1, String param2, BooleanConsumer param3) {
        this.callback = param3;
        this.lastScreen = param0;
        this.worldName = param2;
        this.worldDownload = param1;
        this.downloadStatus = new RealmsDownloadLatestWorldScreen.DownloadStatus();
        this.downloadTitle = new TranslatableComponent("mco.download.title");
        this.narrationRateLimiter = RateLimiter.create(0.1F);
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.cancelButton = this.addButton(new Button(this.width / 2 - 100, this.height - 42, 200, 20, CommonComponents.GUI_CANCEL, param0 -> {
            this.cancelled = true;
            this.backButtonClicked();
        }));
        this.checkDownloadSize();
    }

    private void checkDownloadSize() {
        if (!this.finished) {
            if (!this.checked && this.getContentLength(this.worldDownload.downloadLink) >= 5368709120L) {
                Component var0 = new TranslatableComponent("mco.download.confirmation.line1", Unit.humanReadable(5368709120L));
                Component var1 = new TranslatableComponent("mco.download.confirmation.line2");
                this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0 -> {
                    this.checked = true;
                    this.minecraft.setScreen(this);
                    this.downloadSave();
                }, RealmsLongConfirmationScreen.Type.Warning, var0, var1, false));
            } else {
                this.downloadSave();
            }

        }
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
            List<Component> var0 = Lists.newArrayList();
            var0.add(this.downloadTitle);
            var0.add(this.status);
            if (this.progress != null) {
                var0.add(new TextComponent(this.progress + "%"));
                var0.add(new TextComponent(Unit.humanReadable(this.bytesPersSecond) + "/s"));
            }

            if (this.errorMessage != null) {
                var0.add(this.errorMessage);
            }

            String var1 = var0.stream().map(Component::getString).collect(Collectors.joining("\n"));
            NarrationHelper.now(var1);
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
        if (this.finished && this.callback != null && this.errorMessage == null) {
            this.callback.accept(true);
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.downloadTitle, this.width / 2, 20, 16777215);
        drawCenteredString(param0, this.font, this.status, this.width / 2, 50, 16777215);
        if (this.showDots) {
            this.drawDots(param0);
        }

        if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(param0);
            this.drawDownloadSpeed(param0);
        }

        if (this.errorMessage != null) {
            drawCenteredString(param0, this.font, this.errorMessage, this.width / 2, 110, 16711680);
        }

        super.render(param0, param1, param2, param3);
    }

    private void drawDots(PoseStack param0) {
        int var0 = this.font.width(this.status);
        if (this.animTick % 10 == 0) {
            ++this.dotIndex;
        }

        this.font.draw(param0, DOTS[this.dotIndex % DOTS.length], (float)(this.width / 2 + var0 / 2 + 5), 50.0F, 16777215);
    }

    private void drawProgressBar(PoseStack param0) {
        double var0 = Math.min((double)this.downloadStatus.bytesWritten / (double)this.downloadStatus.totalBytes, 1.0);
        this.progress = String.format(Locale.ROOT, "%.1f", var0 * 100.0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        Tesselator var1 = Tesselator.getInstance();
        BufferBuilder var2 = var1.getBuilder();
        var2.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        double var3 = (double)(this.width / 2 - 100);
        double var4 = 0.5;
        var2.vertex(var3 - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var2.vertex(var3 + 200.0 * var0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var2.vertex(var3 + 200.0 * var0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var2.vertex(var3 - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var2.vertex(var3, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var2.vertex(var3 + 200.0 * var0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var2.vertex(var3 + 200.0 * var0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var2.vertex(var3, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var1.end();
        RenderSystem.enableTexture();
        drawCenteredString(param0, this.font, this.progress + " %", this.width / 2, 84, 16777215);
    }

    private void drawDownloadSpeed(PoseStack param0) {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long var0 = Util.getMillis() - this.previousTimeSnapshot;
                if (var0 == 0L) {
                    var0 = 1L;
                }

                this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / var0;
                this.drawDownloadSpeed0(param0, this.bytesPersSecond);
            }

            this.previousWrittenBytes = this.downloadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMillis();
        } else {
            this.drawDownloadSpeed0(param0, this.bytesPersSecond);
        }

    }

    private void drawDownloadSpeed0(PoseStack param0, long param1) {
        if (param1 > 0L) {
            int var0 = this.font.width(this.progress);
            String var1 = "(" + Unit.humanReadable(param1) + "/s)";
            this.font.draw(param0, var1, (float)(this.width / 2 + var0 / 2 + 15), 84.0F, 16777215);
        }

    }

    private void downloadSave() {
        new Thread(() -> {
            try {
                if (DOWNLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                    if (this.cancelled) {
                        this.downloadCancelled();
                        return;
                    }

                    this.status = new TranslatableComponent("mco.download.downloading", this.worldName);
                    FileDownload var0 = new FileDownload();
                    var0.contentLength(this.worldDownload.downloadLink);
                    var0.download(this.worldDownload, this.worldName, this.downloadStatus, this.minecraft.getLevelSource());

                    while(!var0.isFinished()) {
                        if (var0.isError()) {
                            var0.cancel();
                            this.errorMessage = new TranslatableComponent("mco.download.failed");
                            this.cancelButton.setMessage(CommonComponents.GUI_DONE);
                            return;
                        }

                        if (var0.isExtracting()) {
                            if (!this.extracting) {
                                this.status = new TranslatableComponent("mco.download.extracting");
                            }

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
                    this.status = new TranslatableComponent("mco.download.done");
                    this.cancelButton.setMessage(CommonComponents.GUI_DONE);
                    return;
                }

                this.status = new TranslatableComponent("mco.download.failed");
            } catch (InterruptedException var9) {
                LOGGER.error("Could not acquire upload lock");
                return;
            } catch (Exception var10) {
                this.errorMessage = new TranslatableComponent("mco.download.failed");
                var10.printStackTrace();
                return;
            } finally {
                if (!DOWNLOAD_LOCK.isHeldByCurrentThread()) {
                    return;
                }

                DOWNLOAD_LOCK.unlock();
                this.showDots = false;
                this.finished = true;
            }

        }).start();
    }

    private void downloadCancelled() {
        this.status = new TranslatableComponent("mco.download.cancelled");
    }

    @OnlyIn(Dist.CLIENT)
    public class DownloadStatus {
        public volatile long bytesWritten;
        public volatile long totalBytes;
    }
}
