package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
    static final Logger LOGGER = LogManager.getLogger();
    static final ResourceLocation PLUS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/plus_icon.png");
    static final ResourceLocation RESTORE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/restore_icon.png");
    static final Component RESTORE_TOOLTIP = new TranslatableComponent("mco.backup.button.restore");
    static final Component HAS_CHANGES_TOOLTIP = new TranslatableComponent("mco.backup.changes.tooltip");
    private static final Component TITLE = new TranslatableComponent("mco.configure.world.backup");
    private static final Component NO_BACKUPS_LABEL = new TranslatableComponent("mco.backup.nobackups");
    static int lastScrollPosition = -1;
    private final RealmsConfigureWorldScreen lastScreen;
    List<Backup> backups = Collections.emptyList();
    @Nullable
    Component toolTip;
    RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
    int selectedBackup = -1;
    private final int slotId;
    private Button downloadButton;
    private Button restoreButton;
    private Button changesButton;
    Boolean noBackups = false;
    final RealmsServer serverData;
    private static final String UPLOADED_KEY = "Uploaded";

    public RealmsBackupScreen(RealmsConfigureWorldScreen param0, RealmsServer param1, int param2) {
        super(new TranslatableComponent("mco.configure.world.backup"));
        this.lastScreen = param0;
        this.serverData = param1;
        this.slotId = param2;
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.backupObjectSelectionList = new RealmsBackupScreen.BackupObjectSelectionList();
        if (lastScrollPosition != -1) {
            this.backupObjectSelectionList.setScrollAmount((double)lastScrollPosition);
        }

        (new Thread("Realms-fetch-backups") {
            @Override
            public void run() {
                RealmsClient var0 = RealmsClient.create();

                try {
                    List<Backup> var1 = var0.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
                    RealmsBackupScreen.this.minecraft.execute(() -> {
                        RealmsBackupScreen.this.backups = var1;
                        RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                        RealmsBackupScreen.this.backupObjectSelectionList.clear();

                        for(Backup var0x : RealmsBackupScreen.this.backups) {
                            RealmsBackupScreen.this.backupObjectSelectionList.addEntry(var0x);
                        }

                        RealmsBackupScreen.this.generateChangeList();
                    });
                } catch (RealmsServiceException var3) {
                    RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)var3);
                }

            }
        }).start();
        this.downloadButton = this.addRenderableWidget(
            new Button(this.width - 135, row(1), 120, 20, new TranslatableComponent("mco.backup.button.download"), param0 -> this.downloadClicked())
        );
        this.restoreButton = this.addRenderableWidget(
            new Button(
                this.width - 135, row(3), 120, 20, new TranslatableComponent("mco.backup.button.restore"), param0 -> this.restoreClicked(this.selectedBackup)
            )
        );
        this.changesButton = this.addRenderableWidget(
            new Button(this.width - 135, row(5), 120, 20, new TranslatableComponent("mco.backup.changes.tooltip"), param0 -> {
                this.minecraft.setScreen(new RealmsBackupInfoScreen(this, this.backups.get(this.selectedBackup)));
                this.selectedBackup = -1;
            })
        );
        this.addRenderableWidget(
            new Button(this.width - 100, this.height - 35, 85, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.addWidget(this.backupObjectSelectionList);
        this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
        this.updateButtonStates();
    }

    void generateChangeList() {
        if (this.backups.size() > 1) {
            for(int var0 = 0; var0 < this.backups.size() - 1; ++var0) {
                Backup var1 = this.backups.get(var0);
                Backup var2 = this.backups.get(var0 + 1);
                if (!var1.metadata.isEmpty() && !var2.metadata.isEmpty()) {
                    for(String var3 : var1.metadata.keySet()) {
                        if (var3.contains("Uploaded") || !var2.metadata.containsKey(var3)) {
                            this.addToChangeList(var1, var3);
                        } else if (!var1.metadata.get(var3).equals(var2.metadata.get(var3))) {
                            this.addToChangeList(var1, var3);
                        }
                    }
                }
            }

        }
    }

    private void addToChangeList(Backup param0, String param1) {
        if (param1.contains("Uploaded")) {
            String var0 = DateFormat.getDateTimeInstance(3, 3).format(param0.lastModifiedDate);
            param0.changeList.put(param1, var0);
            param0.setUploadedVersion(true);
        } else {
            param0.changeList.put(param1, param0.metadata.get(param1));
        }

    }

    void updateButtonStates() {
        this.restoreButton.visible = this.shouldRestoreButtonBeVisible();
        this.changesButton.visible = this.shouldChangesButtonBeVisible();
    }

    private boolean shouldChangesButtonBeVisible() {
        if (this.selectedBackup == -1) {
            return false;
        } else {
            return !this.backups.get(this.selectedBackup).changeList.isEmpty();
        }
    }

    private boolean shouldRestoreButtonBeVisible() {
        if (this.selectedBackup == -1) {
            return false;
        } else {
            return !this.serverData.expired;
        }
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    void restoreClicked(int param0) {
        if (param0 >= 0 && param0 < this.backups.size() && !this.serverData.expired) {
            this.selectedBackup = param0;
            Date var0 = this.backups.get(param0).lastModifiedDate;
            String var1 = DateFormat.getDateTimeInstance(3, 3).format(var0);
            String var2 = RealmsUtil.convertToAgePresentationFromInstant(var0);
            Component var3 = new TranslatableComponent("mco.configure.world.restore.question.line1", var1, var2);
            Component var4 = new TranslatableComponent("mco.configure.world.restore.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0x -> {
                if (param0x) {
                    this.restore();
                } else {
                    this.selectedBackup = -1;
                    this.minecraft.setScreen(this);
                }

            }, RealmsLongConfirmationScreen.Type.Warning, var3, var4, true));
        }

    }

    private void downloadClicked() {
        Component var0 = new TranslatableComponent("mco.configure.world.restore.download.question.line1");
        Component var1 = new TranslatableComponent("mco.configure.world.restore.download.question.line2");
        this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0 -> {
            if (param0) {
                this.downloadWorldData();
            } else {
                this.minecraft.setScreen(this);
            }

        }, RealmsLongConfirmationScreen.Type.Info, var0, var1, true));
    }

    private void downloadWorldData() {
        this.minecraft
            .setScreen(
                new RealmsLongRunningMcoTaskScreen(
                    this.lastScreen.getNewScreen(),
                    new DownloadTask(
                        this.serverData.id,
                        this.slotId,
                        this.serverData.name + " (" + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot) + ")",
                        this
                    )
                )
            );
    }

    private void restore() {
        Backup var0 = this.backups.get(this.selectedBackup);
        this.selectedBackup = -1;
        this.minecraft
            .setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new RestoreTask(var0, this.serverData.id, this.lastScreen)));
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.toolTip = null;
        this.renderBackground(param0);
        this.backupObjectSelectionList.render(param0, param1, param2, param3);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 12, 16777215);
        this.font.draw(param0, TITLE, (float)((this.width - 150) / 2 - 90), 20.0F, 10526880);
        if (this.noBackups) {
            this.font.draw(param0, NO_BACKUPS_LABEL, 20.0F, (float)(this.height / 2 - 10), 16777215);
        }

        this.downloadButton.active = !this.noBackups;
        super.render(param0, param1, param2, param3);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(param0, this.toolTip, param1, param2);
        }

    }

    protected void renderMousehoverTooltip(PoseStack param0, @Nullable Component param1, int param2, int param3) {
        if (param1 != null) {
            int var0 = param2 + 12;
            int var1 = param3 - 12;
            int var2 = this.font.width(param1);
            this.fillGradient(param0, var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, param1, (float)var0, (float)var1, 16777215);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupObjectSelectionList extends RealmsObjectSelectionList<RealmsBackupScreen.Entry> {
        public BackupObjectSelectionList() {
            super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height, 32, RealmsBackupScreen.this.height - 15, 36);
        }

        public void addEntry(Backup param0) {
            this.addEntry(RealmsBackupScreen.this.new Entry(param0));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 0.93);
        }

        @Override
        public boolean isFocused() {
            return RealmsBackupScreen.this.getFocused() == this;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public void renderBackground(PoseStack param0) {
            RealmsBackupScreen.this.renderBackground(param0);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (param2 != 0) {
                return false;
            } else if (param0 < (double)this.getScrollbarPosition() && param1 >= (double)this.y0 && param1 <= (double)this.y1) {
                int var0 = this.width / 2 - 92;
                int var1 = this.width;
                int var2 = (int)Math.floor(param1 - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount();
                int var3 = var2 / this.itemHeight;
                if (param0 >= (double)var0 && param0 <= (double)var1 && var3 >= 0 && var2 >= 0 && var3 < this.getItemCount()) {
                    this.selectItem(var3);
                    this.itemClicked(var2, var3, param0, param1, this.width);
                }

                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getScrollbarPosition() {
            return this.width - 5;
        }

        @Override
        public void itemClicked(int param0, int param1, double param2, double param3, int param4) {
            int var0 = this.width - 35;
            int var1 = param1 * this.itemHeight + 36 - (int)this.getScrollAmount();
            int var2 = var0 + 10;
            int var3 = var1 - 3;
            if (param2 >= (double)var0 && param2 <= (double)(var0 + 9) && param3 >= (double)var1 && param3 <= (double)(var1 + 9)) {
                if (!RealmsBackupScreen.this.backups.get(param1).changeList.isEmpty()) {
                    RealmsBackupScreen.this.selectedBackup = -1;
                    RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
                    this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, RealmsBackupScreen.this.backups.get(param1)));
                }
            } else if (param2 >= (double)var2 && param2 < (double)(var2 + 13) && param3 >= (double)var3 && param3 < (double)(var3 + 15)) {
                RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
                RealmsBackupScreen.this.restoreClicked(param1);
            }

        }

        @Override
        public void selectItem(int param0) {
            super.selectItem(param0);
            this.selectInviteListItem(param0);
        }

        public void selectInviteListItem(int param0) {
            RealmsBackupScreen.this.selectedBackup = param0;
            RealmsBackupScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsBackupScreen.Entry param0) {
            super.setSelected(param0);
            RealmsBackupScreen.this.selectedBackup = this.children().indexOf(param0);
            RealmsBackupScreen.this.updateButtonStates();
        }
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsBackupScreen.Entry> {
        private final Backup backup;

        public Entry(Backup param0) {
            this.backup = param0;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            this.renderBackupItem(param0, this.backup, param3 - 40, param2, param6, param7);
        }

        private void renderBackupItem(PoseStack param0, Backup param1, int param2, int param3, int param4, int param5) {
            int var0 = param1.isUploadedVersion() ? -8388737 : 16777215;
            RealmsBackupScreen.this.font
                .draw(
                    param0,
                    "Backup (" + RealmsUtil.convertToAgePresentationFromInstant(param1.lastModifiedDate) + ")",
                    (float)(param2 + 40),
                    (float)(param3 + 1),
                    var0
                );
            RealmsBackupScreen.this.font
                .draw(param0, this.getMediumDatePresentation(param1.lastModifiedDate), (float)(param2 + 40), (float)(param3 + 12), 5000268);
            int var1 = RealmsBackupScreen.this.width - 175;
            int var2 = -3;
            int var3 = var1 - 10;
            int var4 = 0;
            if (!RealmsBackupScreen.this.serverData.expired) {
                this.drawRestore(param0, var1, param3 + -3, param4, param5);
            }

            if (!param1.changeList.isEmpty()) {
                this.drawInfo(param0, var3, param3 + 0, param4, param5);
            }

        }

        private String getMediumDatePresentation(Date param0) {
            return DateFormat.getDateTimeInstance(3, 3).format(param0);
        }

        private void drawRestore(PoseStack param0, int param1, int param2, int param3, int param4) {
            boolean var0 = param3 >= param1
                && param3 <= param1 + 12
                && param4 >= param2
                && param4 <= param2 + 14
                && param4 < RealmsBackupScreen.this.height - 15
                && param4 > 32;
            RenderSystem.setShaderTexture(0, RealmsBackupScreen.RESTORE_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            param0.pushPose();
            param0.scale(0.5F, 0.5F, 0.5F);
            float var1 = var0 ? 28.0F : 0.0F;
            GuiComponent.blit(param0, param1 * 2, param2 * 2, 0.0F, var1, 23, 28, 23, 56);
            param0.popPose();
            if (var0) {
                RealmsBackupScreen.this.toolTip = RealmsBackupScreen.RESTORE_TOOLTIP;
            }

        }

        private void drawInfo(PoseStack param0, int param1, int param2, int param3, int param4) {
            boolean var0 = param3 >= param1
                && param3 <= param1 + 8
                && param4 >= param2
                && param4 <= param2 + 8
                && param4 < RealmsBackupScreen.this.height - 15
                && param4 > 32;
            RenderSystem.setShaderTexture(0, RealmsBackupScreen.PLUS_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            param0.pushPose();
            param0.scale(0.5F, 0.5F, 0.5F);
            float var1 = var0 ? 15.0F : 0.0F;
            GuiComponent.blit(param0, param1 * 2, param2 * 2, 0.0F, var1, 15, 15, 15, 30);
            param0.popPose();
            if (var0) {
                RealmsBackupScreen.this.toolTip = RealmsBackupScreen.HAS_CHANGES_TOOLTIP;
            }

        }

        @Override
        public Component getNarration() {
            return new TranslatableComponent("narrator.select", this.backup.lastModifiedDate.toString());
        }
    }
}
