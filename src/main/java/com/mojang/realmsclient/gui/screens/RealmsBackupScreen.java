package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupScreen extends RealmsScreen {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
    static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
    private static final Component TITLE = Component.translatable("mco.configure.world.backup");
    private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
    private final RealmsConfigureWorldScreen lastScreen;
    List<Backup> backups = Collections.emptyList();
    RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
    int selectedBackup = -1;
    private final int slotId;
    private Button downloadButton;
    private Button restoreButton;
    private Button changesButton;
    Boolean noBackups = false;
    final RealmsServer serverData;
    private static final String UPLOADED_KEY = "uploaded";

    public RealmsBackupScreen(RealmsConfigureWorldScreen param0, RealmsServer param1, int param2) {
        super(TITLE);
        this.lastScreen = param0;
        this.serverData = param1;
        this.slotId = param2;
    }

    @Override
    public void init() {
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

                    });
                } catch (RealmsServiceException var3) {
                    RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)var3);
                }

            }
        }).start();
        this.downloadButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.backup.button.download"), param0 -> this.downloadClicked())
                .bounds(this.width - 135, row(1), 120, 20)
                .build()
        );
        this.restoreButton = this.addRenderableWidget(
            Button.builder(Component.translatable("mco.backup.button.restore"), param0 -> this.restoreClicked(this.selectedBackup))
                .bounds(this.width - 135, row(3), 120, 20)
                .build()
        );
        this.changesButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.backup.changes.tooltip"), param0 -> {
            this.minecraft.setScreen(new RealmsBackupInfoScreen(this, this.backups.get(this.selectedBackup)));
            this.selectedBackup = -1;
        }).bounds(this.width - 135, row(5), 120, 20).build());
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen))
                .bounds(this.width - 100, this.height - 35, 85, 20)
                .build()
        );
        this.backupObjectSelectionList = this.addRenderableWidget(new RealmsBackupScreen.BackupObjectSelectionList());
        this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
        this.updateButtonStates();
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
            Component var2 = RealmsUtil.convertToAgePresentationFromInstant(var0);
            Component var3 = Component.translatable("mco.configure.world.restore.question.line1", var1, var2);
            Component var4 = Component.translatable("mco.configure.world.restore.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0x -> {
                if (param0x) {
                    this.restore();
                } else {
                    this.selectedBackup = -1;
                    this.minecraft.setScreen(this);
                }

            }, RealmsLongConfirmationScreen.Type.WARNING, var3, var4, true));
        }

    }

    private void downloadClicked() {
        Component var0 = Component.translatable("mco.configure.world.restore.download.question.line1");
        Component var1 = Component.translatable("mco.configure.world.restore.download.question.line2");
        this.minecraft.setScreen(new RealmsLongConfirmationScreen(param0 -> {
            if (param0) {
                this.downloadWorldData();
            } else {
                this.minecraft.setScreen(this);
            }

        }, RealmsLongConfirmationScreen.Type.INFO, var0, var1, true));
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
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 12, -1);
        if (this.noBackups) {
            param0.drawString(this.font, NO_BACKUPS_LABEL, 20, this.height / 2 - 10, -1, false);
        }

        this.downloadButton.active = !this.noBackups;
    }

    @OnlyIn(Dist.CLIENT)
    class BackupObjectSelectionList extends RealmsObjectSelectionList<RealmsBackupScreen.Entry> {
        public BackupObjectSelectionList() {
            super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height - 47, 32, 36);
        }

        public void addEntry(Backup param0) {
            this.addEntry(RealmsBackupScreen.this.new Entry(param0));
        }

        @Override
        public int getRowWidth() {
            return (int)((double)this.width * 0.93);
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public int getScrollbarPosition() {
            return this.width - 5;
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
        private static final int Y_PADDING = 2;
        private static final int X_PADDING = 7;
        private static final WidgetSprites CHANGES_BUTTON_SPRITES = new WidgetSprites(
            new ResourceLocation("backup/changes"), new ResourceLocation("backup/changes_highlighted")
        );
        private static final WidgetSprites RESTORE_BUTTON_SPRITES = new WidgetSprites(
            new ResourceLocation("backup/restore"), new ResourceLocation("backup/restore_highlighted")
        );
        private final Backup backup;
        private final List<AbstractWidget> children = new ArrayList<>();
        @Nullable
        private ImageButton restoreButton;
        @Nullable
        private ImageButton changesButton;

        public Entry(Backup param0) {
            this.backup = param0;
            this.populateChangeList(param0);
            if (!param0.changeList.isEmpty()) {
                this.addChangesButton();
            }

            if (!RealmsBackupScreen.this.serverData.expired) {
                this.addRestoreButton();
            }

        }

        private void populateChangeList(Backup param0) {
            int var0 = RealmsBackupScreen.this.backups.indexOf(param0);
            if (var0 != RealmsBackupScreen.this.backups.size() - 1) {
                Backup var1 = RealmsBackupScreen.this.backups.get(var0 + 1);

                for(String var2 : param0.metadata.keySet()) {
                    if (var2.contains("uploaded") || !var1.metadata.containsKey(var2)) {
                        this.addToChangeList(var2);
                    } else if (!param0.metadata.get(var2).equals(var1.metadata.get(var2))) {
                        this.addToChangeList(var2);
                    }
                }

            }
        }

        private void addToChangeList(String param0) {
            if (param0.contains("uploaded")) {
                String var0 = DateFormat.getDateTimeInstance(3, 3).format(this.backup.lastModifiedDate);
                this.backup.changeList.put(param0, var0);
                this.backup.setUploadedVersion(true);
            } else {
                this.backup.changeList.put(param0, this.backup.metadata.get(param0));
            }

        }

        private void addChangesButton() {
            int var0 = 9;
            int var1 = 9;
            int var2 = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 9 - 28;
            int var3 = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
            this.changesButton = new ImageButton(
                var2,
                var3,
                9,
                9,
                CHANGES_BUTTON_SPRITES,
                param0 -> RealmsBackupScreen.this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, this.backup)),
                CommonComponents.EMPTY
            );
            this.changesButton.setTooltip(Tooltip.create(RealmsBackupScreen.HAS_CHANGES_TOOLTIP));
            this.children.add(this.changesButton);
        }

        private void addRestoreButton() {
            int var0 = 17;
            int var1 = 10;
            int var2 = RealmsBackupScreen.this.backupObjectSelectionList.getRowRight() - 17 - 7;
            int var3 = RealmsBackupScreen.this.backupObjectSelectionList.getRowTop(RealmsBackupScreen.this.backups.indexOf(this.backup)) + 2;
            this.restoreButton = new ImageButton(
                var2,
                var3,
                17,
                10,
                RESTORE_BUTTON_SPRITES,
                param0 -> RealmsBackupScreen.this.restoreClicked(RealmsBackupScreen.this.backups.indexOf(this.backup)),
                CommonComponents.EMPTY
            );
            this.restoreButton.setTooltip(Tooltip.create(RealmsBackupScreen.RESTORE_TOOLTIP));
            this.children.add(this.restoreButton);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.restoreButton != null) {
                this.restoreButton.mouseClicked(param0, param1, param2);
            }

            if (this.changesButton != null) {
                this.changesButton.mouseClicked(param0, param1, param2);
            }

            return true;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0 = this.backup.isUploadedVersion() ? -8388737 : 16777215;
            param0.drawString(
                RealmsBackupScreen.this.font,
                Component.translatable("mco.backup.entry", RealmsUtil.convertToAgePresentationFromInstant(this.backup.lastModifiedDate)),
                param3,
                param2 + 1,
                var0,
                false
            );
            param0.drawString(RealmsBackupScreen.this.font, this.getMediumDatePresentation(this.backup.lastModifiedDate), param3, param2 + 12, 5000268, false);
            this.children.forEach(param5x -> {
                param5x.setY(param2 + 2);
                param5x.render(param0, param6, param7, param9);
            });
        }

        private String getMediumDatePresentation(Date param0) {
            return DateFormat.getDateTimeInstance(3, 3).format(param0);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.backup.lastModifiedDate.toString());
        }
    }
}
