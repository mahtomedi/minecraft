package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ResourceLocation PLUS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/plus_icon.png");
    private static final ResourceLocation RESTORE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/restore_icon.png");
    private static int lastScrollPosition = -1;
    private final RealmsConfigureWorldScreen lastScreen;
    private List<Backup> backups = Collections.emptyList();
    private String toolTip;
    private RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
    private int selectedBackup = -1;
    private final int slotId;
    private Button downloadButton;
    private Button restoreButton;
    private Button changesButton;
    private Boolean noBackups = false;
    private final RealmsServer serverData;
    private RealmsLabel titleLabel;

    public RealmsBackupScreen(RealmsConfigureWorldScreen param0, RealmsServer param1, int param2) {
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
        this.downloadButton = this.addButton(
            new Button(this.width - 135, row(1), 120, 20, I18n.get("mco.backup.button.download"), param0 -> this.downloadClicked())
        );
        this.restoreButton = this.addButton(
            new Button(this.width - 135, row(3), 120, 20, I18n.get("mco.backup.button.restore"), param0 -> this.restoreClicked(this.selectedBackup))
        );
        this.changesButton = this.addButton(new Button(this.width - 135, row(5), 120, 20, I18n.get("mco.backup.changes.tooltip"), param0 -> {
            this.minecraft.setScreen(new RealmsBackupInfoScreen(this, this.backups.get(this.selectedBackup)));
            this.selectedBackup = -1;
        }));
        this.addButton(new Button(this.width - 100, this.height - 35, 85, 20, I18n.get("gui.back"), param0 -> this.minecraft.setScreen(this.lastScreen)));
        this.addWidget(this.backupObjectSelectionList);
        this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.configure.world.backup"), this.width / 2, 12, 16777215));
        this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
        this.updateButtonStates();
        this.narrateLabels();
    }

    private void generateChangeList() {
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

    private void updateButtonStates() {
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

    private void restoreClicked(int param0) {
        if (param0 >= 0 && param0 < this.backups.size() && !this.serverData.expired) {
            this.selectedBackup = param0;
            Date var0 = this.backups.get(param0).lastModifiedDate;
            String var1 = DateFormat.getDateTimeInstance(3, 3).format(var0);
            String var2 = RealmsUtil.convertToAgePresentationFromInstant(var0);
            String var3 = I18n.get("mco.configure.world.restore.question.line1", var1, var2);
            String var4 = I18n.get("mco.configure.world.restore.question.line2");
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
        String var0 = I18n.get("mco.configure.world.restore.download.question.line1");
        String var1 = I18n.get("mco.configure.world.restore.download.question.line2");
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
    public void render(int param0, int param1, float param2) {
        this.toolTip = null;
        this.renderBackground();
        this.backupObjectSelectionList.render(param0, param1, param2);
        this.titleLabel.render(this);
        this.font.draw(I18n.get("mco.configure.world.backup"), (float)((this.width - 150) / 2 - 90), 20.0F, 10526880);
        if (this.noBackups) {
            this.font.draw(I18n.get("mco.backup.nobackups"), 20.0F, (float)(this.height / 2 - 10), 16777215);
        }

        this.downloadButton.active = !this.noBackups;
        super.render(param0, param1, param2);
        if (this.toolTip != null) {
            this.renderMousehoverTooltip(this.toolTip, param0, param1);
        }

    }

    protected void renderMousehoverTooltip(String param0, int param1, int param2) {
        if (param0 != null) {
            int var0 = param1 + 12;
            int var1 = param2 - 12;
            int var2 = this.font.width(param0);
            this.fillGradient(var0 - 3, var1 - 3, var0 + var2 + 3, var1 + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(param0, (float)var0, (float)var1, 16777215);
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
        public void renderBackground() {
            RealmsBackupScreen.this.renderBackground();
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
            this.setSelectedItem(param0);
            if (param0 != -1) {
                NarrationHelper.now(I18n.get("narrator.select", RealmsBackupScreen.this.backups.get(param0).lastModifiedDate.toString()));
            }

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
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderBackupItem(this.backup, param2 - 40, param1, param5, param6);
        }

        private void renderBackupItem(Backup param0, int param1, int param2, int param3, int param4) {
            int var0 = param0.isUploadedVersion() ? -8388737 : 16777215;
            RealmsBackupScreen.this.font
                .draw(
                    "Backup (" + RealmsUtil.convertToAgePresentationFromInstant(param0.lastModifiedDate) + ")", (float)(param1 + 40), (float)(param2 + 1), var0
                );
            RealmsBackupScreen.this.font.draw(this.getMediumDatePresentation(param0.lastModifiedDate), (float)(param1 + 40), (float)(param2 + 12), 5000268);
            int var1 = RealmsBackupScreen.this.width - 175;
            int var2 = -3;
            int var3 = var1 - 10;
            int var4 = 0;
            if (!RealmsBackupScreen.this.serverData.expired) {
                this.drawRestore(var1, param2 + -3, param3, param4);
            }

            if (!param0.changeList.isEmpty()) {
                this.drawInfo(var3, param2 + 0, param3, param4);
            }

        }

        private String getMediumDatePresentation(Date param0) {
            return DateFormat.getDateTimeInstance(3, 3).format(param0);
        }

        private void drawRestore(int param0, int param1, int param2, int param3) {
            boolean var0 = param2 >= param0
                && param2 <= param0 + 12
                && param3 >= param1
                && param3 <= param1 + 14
                && param3 < RealmsBackupScreen.this.height - 15
                && param3 > 32;
            RealmsBackupScreen.this.minecraft.getTextureManager().bind(RealmsBackupScreen.RESTORE_ICON_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            float var1 = var0 ? 28.0F : 0.0F;
            GuiComponent.blit(param0 * 2, param1 * 2, 0.0F, var1, 23, 28, 23, 56);
            RenderSystem.popMatrix();
            if (var0) {
                RealmsBackupScreen.this.toolTip = I18n.get("mco.backup.button.restore");
            }

        }

        private void drawInfo(int param0, int param1, int param2, int param3) {
            boolean var0 = param2 >= param0
                && param2 <= param0 + 8
                && param3 >= param1
                && param3 <= param1 + 8
                && param3 < RealmsBackupScreen.this.height - 15
                && param3 > 32;
            RealmsBackupScreen.this.minecraft.getTextureManager().bind(RealmsBackupScreen.PLUS_ICON_LOCATION);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(0.5F, 0.5F, 0.5F);
            float var1 = var0 ? 15.0F : 0.0F;
            GuiComponent.blit(param0 * 2, param1 * 2, 0.0F, var1, 15, 15, 15, 30);
            RenderSystem.popMatrix();
            if (var0) {
                RealmsBackupScreen.this.toolTip = I18n.get("mco.backup.changes.tooltip");
            }

        }
    }
}
