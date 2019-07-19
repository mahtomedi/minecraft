package com.mojang.realmsclient.gui.screens;

import com.mojang.realmsclient.dto.Backup;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RealmsSimpleScrolledSelectionList;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsBackupInfoScreen extends RealmsScreen {
    private final RealmsScreen lastScreen;
    private final int BUTTON_BACK_ID = 0;
    private final Backup backup;
    private final List<String> keys = new ArrayList<>();
    private RealmsBackupInfoScreen.BackupInfoList backupInfoList;
    String[] difficulties = new String[]{
        getLocalizedString("options.difficulty.peaceful"),
        getLocalizedString("options.difficulty.easy"),
        getLocalizedString("options.difficulty.normal"),
        getLocalizedString("options.difficulty.hard")
    };
    String[] gameModes = new String[]{
        getLocalizedString("selectWorld.gameMode.survival"),
        getLocalizedString("selectWorld.gameMode.creative"),
        getLocalizedString("selectWorld.gameMode.adventure")
    };

    public RealmsBackupInfoScreen(RealmsScreen param0, Backup param1) {
        this.lastScreen = param0;
        this.backup = param1;
        if (param1.changeList != null) {
            for(Entry<String, String> var0 : param1.changeList.entrySet()) {
                this.keys.add(var0.getKey());
            }
        }

    }

    @Override
    public void tick() {
    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.buttonsAdd(new RealmsButton(0, this.width() / 2 - 100, this.height() / 4 + 120 + 24, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsBackupInfoScreen.this.lastScreen);
            }
        });
        this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList();
        this.addWidget(this.backupInfoList);
        this.focusOn(this.backupInfoList);
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            Realms.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString("Changes from last backup", this.width() / 2, 10, 16777215);
        this.backupInfoList.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }

    private String checkForSpecificMetadata(String param0, String param1) {
        String var0 = param0.toLowerCase(Locale.ROOT);
        if (var0.contains("game") && var0.contains("mode")) {
            return this.gameModeMetadata(param1);
        } else {
            return var0.contains("game") && var0.contains("difficulty") ? this.gameDifficultyMetadata(param1) : param1;
        }
    }

    private String gameDifficultyMetadata(String param0) {
        try {
            return this.difficulties[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return "UNKNOWN";
        }
    }

    private String gameModeMetadata(String param0) {
        try {
            return this.gameModes[Integer.parseInt(param0)];
        } catch (Exception var3) {
            return "UNKNOWN";
        }
    }

    @OnlyIn(Dist.CLIENT)
    class BackupInfoList extends RealmsSimpleScrolledSelectionList {
        public BackupInfoList() {
            super(RealmsBackupInfoScreen.this.width(), RealmsBackupInfoScreen.this.height(), 32, RealmsBackupInfoScreen.this.height() - 64, 36);
        }

        @Override
        public int getItemCount() {
            return RealmsBackupInfoScreen.this.backup.changeList.size();
        }

        @Override
        public boolean isSelectedItem(int param0) {
            return false;
        }

        @Override
        public int getMaxPosition() {
            return this.getItemCount() * 36;
        }

        @Override
        public void renderBackground() {
        }

        @Override
        public void renderItem(int param0, int param1, int param2, int param3, Tezzelator param4, int param5, int param6) {
            String var0 = RealmsBackupInfoScreen.this.keys.get(param0);
            RealmsBackupInfoScreen.this.drawString(var0, this.width() / 2 - 40, param2, 10526880);
            String var1 = RealmsBackupInfoScreen.this.backup.changeList.get(var0);
            RealmsBackupInfoScreen.this.drawString(
                RealmsBackupInfoScreen.this.checkForSpecificMetadata(var0, var1), this.width() / 2 - 40, param2 + 12, 16777215
            );
        }
    }
}
