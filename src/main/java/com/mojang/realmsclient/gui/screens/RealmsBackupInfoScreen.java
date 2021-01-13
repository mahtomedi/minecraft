package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
    private final Screen lastScreen;
    private final Backup backup;
    private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen param0, Backup param1) {
        this.lastScreen = param0;
        this.backup = param1;
    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addButton(
            new Button(
                this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, param0 -> this.minecraft.setScreen(this.lastScreen)
            )
        );
        this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
        this.addWidget(this.backupInfoList);
        this.magicalSpecialHackyFocus(this.backupInfoList);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, "Changes from last backup", this.width / 2, 10, 16777215);
        this.backupInfoList.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }

    private Component checkForSpecificMetadata(String param0, String param1) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        if (var0.contains("game") && var0.contains("mode")) {
            return this.gameModeMetadata(param1);
        } else {
            return (Component)(var0.contains("game") && var0.contains("difficulty") ? this.gameDifficultyMetadata(param1) : new TextComponent(param1));
        }
    }

    private Component gameDifficultyMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return new TextComponent("UNKNOWN");
        }
    }

    private Component gameModeMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return new TextComponent("UNKNOWN");
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry> {
        public BackupInfoList(Minecraft param0) {
            super(param0, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
            this.setRenderSelection(false);
            if (RealmsBackupInfoScreen.this.backup.changeList != null) {
                RealmsBackupInfoScreen.this.backup
                    .changeList
                    .forEach((param0x, param1) -> this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(param0x, param1)));
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
        private final String key;
        private final String value;

        public BackupInfoListEntry(String param0, String param1) {
            this.key = param0;
            this.value = param1;
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            Font var0 = RealmsBackupInfoScreen.this.minecraft.font;
            GuiComponent.drawString(param0, var0, this.key, param3, param2, 10526880);
            GuiComponent.drawString(param0, var0, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), param3, param2 + 12, 16777215);
        }
    }
}
