package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.realmsclient.Unit;
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
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;
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
    private static final ReentrantLock UPLOAD_LOCK = new ReentrantLock();
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private final RealmsResetWorldScreen lastScreen;
    private final LevelSummary selectedLevel;
    private final long worldId;
    private final int slotId;
    private final UploadStatus uploadStatus;
    private final RateLimiter narrationRateLimiter;
    private volatile Component[] errorMessage;
    private volatile Component status;
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean uploadFinished;
    private volatile boolean showDots = true;
    private volatile boolean uploadStarted;
    private Button backButton;
    private Button cancelButton;
    private int tickCount;
    private Long previousWrittenBytes;
    private Long previousTimeSnapshot;
    private long bytesPersSecond;
    private final Runnable callback;

    public RealmsUploadScreen(long param0, int param1, RealmsResetWorldScreen param2, LevelSummary param3, Runnable param4) {
        this.worldId = param0;
        this.slotId = param1;
        this.lastScreen = param2;
        this.selectedLevel = param3;
        this.uploadStatus = new UploadStatus();
        this.narrationRateLimiter = RateLimiter.create(0.1F);
        this.callback = param4;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.backButton = new Button(this.width / 2 - 100, this.height - 42, 200, 20, CommonComponents.GUI_BACK, param0 -> this.onBack());
        this.cancelButton = this.addButton(new Button(this.width / 2 - 100, this.height - 42, 200, 20, CommonComponents.GUI_CANCEL, param0 -> this.onCancel()));
        if (!this.uploadStarted) {
            if (this.lastScreen.slot == -1) {
                this.upload();
            } else {
                this.lastScreen.switchSlot(() -> {
                    if (!this.uploadStarted) {
                        this.uploadStarted = true;
                        this.minecraft.setScreen(this);
                        this.upload();
                    }

                });
            }
        }

    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onBack() {
        this.callback.run();
    }

    private void onCancel() {
        this.cancelled = true;
        this.minecraft.setScreen(this.lastScreen);
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        if (!this.uploadFinished && this.uploadStatus.bytesWritten != 0L && this.uploadStatus.bytesWritten == this.uploadStatus.totalBytes) {
            this.status = new TranslatableComponent("mco.upload.verifying");
            this.cancelButton.active = false;
        }

        this.drawCenteredString(param0, this.font, this.status, this.width / 2, 50, 16777215);
        if (this.showDots) {
            this.drawDots(param0);
        }

        if (this.uploadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(param0);
            this.drawUploadSpeed(param0);
        }

        if (this.errorMessage != null) {
            for(int var0 = 0; var0 < this.errorMessage.length; ++var0) {
                this.drawCenteredString(param0, this.font, this.errorMessage[var0], this.width / 2, 110 + 12 * var0, 16711680);
            }
        }

        super.render(param0, param1, param2, param3);
    }

    private void drawDots(PoseStack param0) {
        int var0 = this.font.width(this.status);
        this.font.draw(param0, DOTS[this.tickCount / 10 % DOTS.length], (float)(this.width / 2 + var0 / 2 + 5), 50.0F, 16777215);
    }

    private void drawProgressBar(PoseStack param0) {
        double var0 = this.uploadStatus.bytesWritten.doubleValue() / this.uploadStatus.totalBytes.doubleValue() * 100.0;
        if (var0 > 100.0) {
            var0 = 100.0;
        }

        this.progress = String.format(Locale.ROOT, "%.1f", var0);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        double var1 = (double)(this.width / 2 - 100);
        double var2 = 0.5;
        Tesselator var3 = Tesselator.getInstance();
        BufferBuilder var4 = var3.getBuilder();
        var4.begin(7, DefaultVertexFormat.POSITION_COLOR);
        var4.vertex(var1 - 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var4.vertex(var1 + 200.0 * var0 / 100.0 + 0.5, 95.5, 0.0).color(217, 210, 210, 255).endVertex();
        var4.vertex(var1 + 200.0 * var0 / 100.0 + 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var4.vertex(var1 - 0.5, 79.5, 0.0).color(217, 210, 210, 255).endVertex();
        var4.vertex(var1, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var4.vertex(var1 + 200.0 * var0 / 100.0, 95.0, 0.0).color(128, 128, 128, 255).endVertex();
        var4.vertex(var1 + 200.0 * var0 / 100.0, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var4.vertex(var1, 80.0, 0.0).color(128, 128, 128, 255).endVertex();
        var3.end();
        RenderSystem.enableTexture();
        this.drawCenteredString(param0, this.font, this.progress + " %", this.width / 2, 84, 16777215);
    }

    private void drawUploadSpeed(PoseStack param0) {
        if (this.tickCount % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long var0 = Util.getMillis() - this.previousTimeSnapshot;
                if (var0 == 0L) {
                    var0 = 1L;
                }

                this.bytesPersSecond = 1000L * (this.uploadStatus.bytesWritten - this.previousWrittenBytes) / var0;
                this.drawUploadSpeed0(param0, this.bytesPersSecond);
            }

            this.previousWrittenBytes = this.uploadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMillis();
        } else {
            this.drawUploadSpeed0(param0, this.bytesPersSecond);
        }

    }

    private void drawUploadSpeed0(PoseStack param0, long param1) {
        if (param1 > 0L) {
            int var0 = this.font.width(this.progress);
            String var1 = "(" + Unit.humanReadable(param1) + "/s)";
            this.font.draw(param0, var1, (float)(this.width / 2 + var0 / 2 + 15), 84.0F, 16777215);
        }

    }

    @Override
    public void tick() {
        super.tick();
        ++this.tickCount;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            List<String> var0 = Lists.newArrayList();
            var0.add(this.status.getString());
            if (this.progress != null) {
                var0.add(this.progress + "%");
            }

            if (this.errorMessage != null) {
                Stream.of(this.errorMessage).map(Component::getString).forEach(var0::add);
            }

            NarrationHelper.now(String.join(System.lineSeparator(), var0));
        }

    }

    private void upload() {
        this.uploadStarted = true;
        new Thread(
                () -> {
                    File var0 = null;
                    RealmsClient var1 = RealmsClient.create();
                    long var2 = this.worldId;
        
                    try {
                        UploadInfo var3;
                        try {
                            if (UPLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                                this.status = new TranslatableComponent("mco.upload.preparing");
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
                                    this.status = new TranslatableComponent("mco.upload.close.failure");
                                    return;
                                }
        
                                UploadTokenCache.put(var2, var3.getToken());
                                if (!var3.isWorldClosed()) {
                                    this.status = new TranslatableComponent("mco.upload.close.failure");
                                    return;
                                }
        
                                if (this.cancelled) {
                                    this.uploadCancelled();
                                    return;
                                }
        
                                var4 = (int)(new File(this.minecraft.gameDirectory.getAbsolutePath(), "saves"));
                                var0 = this.tarGzipArchive(new File(var4, this.selectedLevel.getLevelId()));
                                if (this.cancelled) {
                                    this.uploadCancelled();
                                    return;
                                }
        
                                if (this.verify(var0)) {
                                    this.status = new TranslatableComponent("mco.upload.uploading", this.selectedLevel.getLevelName());
                                    FileUpload var11 = new FileUpload(
                                        var0,
                                        this.worldId,
                                        this.slotId,
                                        var3,
                                        this.minecraft.getUser(),
                                        SharedConstants.getCurrentVersion().getName(),
                                        this.uploadStatus
                                    );
                                    var11.upload(param1 -> {
                                        if (param1.statusCode >= 200 && param1.statusCode < 300) {
                                            this.uploadFinished = true;
                                            this.status = new TranslatableComponent("mco.upload.done");
                                            this.backButton.setMessage(CommonComponents.GUI_DONE);
                                            UploadTokenCache.invalidate(var2);
                                        } else if (param1.statusCode == 400 && param1.errorMessage != null) {
                                            this.setErrorMessage(new TranslatableComponent("mco.upload.failed", param1.errorMessage));
                                        } else {
                                            this.setErrorMessage(new TranslatableComponent("mco.upload.failed", param1.statusCode));
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
                                Unit var8 = Unit.getLargest(var7);
                                Unit var9 = Unit.getLargest(5368709120L);
                                if (Unit.humanReadable(var7, var8).equals(Unit.humanReadable(5368709120L, var9)) && var8 != Unit.B) {
                                    Unit var10 = Unit.values()[var8.ordinal() - 1];
                                    this.setErrorMessage(
                                        new TranslatableComponent("mco.upload.size.failure.line1", this.selectedLevel.getLevelName()),
                                        new TranslatableComponent(
                                            "mco.upload.size.failure.line2", Unit.humanReadable(var7, var10), Unit.humanReadable(5368709120L, var10)
                                        )
                                    );
                                    return;
                                }
        
                                this.setErrorMessage(
                                    new TranslatableComponent("mco.upload.size.failure.line1", this.selectedLevel.getLevelName()),
                                    new TranslatableComponent(
                                        "mco.upload.size.failure.line2", Unit.humanReadable(var7, var8), Unit.humanReadable(5368709120L, var9)
                                    )
                                );
                                return;
                            }
                        } catch (IOException var21) {
                            var3 = var21;
                            this.setErrorMessage(new TranslatableComponent("mco.upload.failed", var21.getMessage()));
                            return;
                        } catch (RealmsServiceException var22) {
                            var3 = var22;
                            this.setErrorMessage(new TranslatableComponent("mco.upload.failed", var22.toString()));
                            return;
                        } catch (InterruptedException var23) {
                            var3 = var23;
                            LOGGER.error("Could not acquire upload lock");
                            return;
                        }
                    } finally {
                        this.uploadFinished = true;
                        if (UPLOAD_LOCK.isHeldByCurrentThread()) {
                            UPLOAD_LOCK.unlock();
                            this.showDots = false;
                            this.children.clear();
                            this.addButton(this.backButton);
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

    private void setErrorMessage(Component... param0) {
        this.errorMessage = param0;
    }

    private void uploadCancelled() {
        this.status = new TranslatableComponent("mco.upload.cancelled");
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
}
