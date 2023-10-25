package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
    private static final Component TITLE = Component.translatable("mco.backup.info.title");
    private static final Component UNKNOWN = Component.translatable("mco.backup.unknown");
    private final Screen lastScreen;
    final Backup backup;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen param0, Backup param1) {
        super(TITLE);
        this.lastScreen = param0;
        this.backup = param1;
    }

    @Override
    public void init() {
        this.layout.addToHeader(new StringWidget(TITLE, this.font));
        this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
        this.addRenderableWidget(this.backupInfoList);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, param0 -> this.onClose()).build());
        this.layout.arrangeElements();
        this.layout.visitWidgets(param1 -> {
        });
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.backupInfoList.updateSize(this.width, this.height, this.layout.getHeaderHeight(), this.height - this.layout.getFooterHeight());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    Component checkForSpecificMetadata(String param0, String param1) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        if (var0.contains("game") && var0.contains("mode")) {
            return this.gameModeMetadata(param1);
        } else {
            return (Component)(var0.contains("game") && var0.contains("difficulty") ? this.gameDifficultyMetadata(param1) : Component.literal(param1));
        }
    }

    private Component gameDifficultyMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(param0)).getDisplayName();
        } catch (Exception var3) {
            return UNKNOWN;
        }
    }

    private Component gameModeMetadata(String param0) {
        try {
            return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(param0)).getShortDisplayName();
        } catch (Exception var3) {
            return UNKNOWN;
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry> {
        public BackupInfoList(Minecraft param0) {
            super(
                param0,
                RealmsBackupInfoScreen.this.width,
                RealmsBackupInfoScreen.this.height,
                RealmsBackupInfoScreen.this.layout.getHeaderHeight(),
                RealmsBackupInfoScreen.this.height - RealmsBackupInfoScreen.this.layout.getFooterHeight(),
                36
            );
            if (RealmsBackupInfoScreen.this.backup.changeList != null) {
                RealmsBackupInfoScreen.this.backup
                    .changeList
                    .forEach((param0x, param1) -> this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(param0x, param1)));
            }

        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry> {
        private static final Component TEMPLATE_NAME = Component.translatable("mco.backup.entry.templateName");
        private static final Component GAME_DIFFICULTY = Component.translatable("mco.backup.entry.gameDifficulty");
        private static final Component NAME = Component.translatable("mco.backup.entry.name");
        private static final Component GAME_SERVER_VERSION = Component.translatable("mco.backup.entry.gameServerVersion");
        private static final Component UPLOADED = Component.translatable("mco.backup.entry.uploaded");
        private static final Component ENABLED_PACK = Component.translatable("mco.backup.entry.enabledPack");
        private static final Component DESCRIPTION = Component.translatable("mco.backup.entry.description");
        private static final Component GAME_MODE = Component.translatable("mco.backup.entry.gameMode");
        private static final Component SEED = Component.translatable("mco.backup.entry.seed");
        private static final Component WORLD_TYPE = Component.translatable("mco.backup.entry.worldType");
        private static final Component UNDEFINED = Component.translatable("mco.backup.entry.undefined");
        private final String key;
        private final String value;

        public BackupInfoListEntry(String param0, String param1) {
            this.key = param0;
            this.value = param1;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            param0.drawString(RealmsBackupInfoScreen.this.font, this.translateKey(this.key), param3, param2, -6250336);
            param0.drawString(
                RealmsBackupInfoScreen.this.font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), param3, param2 + 12, -1
            );
        }

        private Component translateKey(String param0) {
            return switch(param0) {
                case "template_name" -> TEMPLATE_NAME;
                case "game_difficulty" -> GAME_DIFFICULTY;
                case "name" -> NAME;
                case "game_server_version" -> GAME_SERVER_VERSION;
                case "uploaded" -> UPLOADED;
                case "enabled_packs" -> ENABLED_PACK;
                case "description" -> DESCRIPTION;
                case "game_mode" -> GAME_MODE;
                case "seed" -> SEED;
                case "world_type" -> WORLD_TYPE;
                default -> UNDEFINED;
            };
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.key + " " + this.value);
        }
    }
}
