package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import com.mojang.realmsclient.gui.RealmsConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import net.minecraft.realms.RealmListEntry;
import net.minecraft.realms.Realms;
import net.minecraft.realms.RealmsAnvilLevelStorageSource;
import net.minecraft.realms.RealmsButton;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsLevelSummary;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.Tezzelator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final RealmsResetWorldScreen lastScreen;
    private final long worldId;
    private final int slotId;
    private RealmsButton uploadButton;
    private final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private List<RealmsLevelSummary> levelList = Lists.newArrayList();
    private int selectedWorld = -1;
    private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
    private String worldLang;
    private String conversionLang;
    private final String[] gameModesLang = new String[4];
    private RealmsLabel titleLabel;
    private RealmsLabel subtitleLabel;
    private RealmsLabel noWorldsLabel;

    public RealmsSelectFileToUploadScreen(long param0, int param1, RealmsResetWorldScreen param2) {
        this.lastScreen = param2;
        this.worldId = param0;
        this.slotId = param1;
    }

    private void loadLevelList() throws Exception {
        RealmsAnvilLevelStorageSource var0 = this.getLevelStorageSource();
        this.levelList = var0.getLevelList();
        Collections.sort(this.levelList);

        for(RealmsLevelSummary var1 : this.levelList) {
            this.worldSelectionList.addEntry(var1);
        }

    }

    @Override
    public void init() {
        this.setKeyboardHandlerSendRepeatsToGui(true);
        this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

        try {
            this.loadLevelList();
        } catch (Exception var2) {
            LOGGER.error("Couldn't load level list", (Throwable)var2);
            Realms.setScreen(new RealmsGenericErrorScreen("Unable to load worlds", var2.getMessage(), this.lastScreen));
            return;
        }

        this.worldLang = getLocalizedString("selectWorld.world");
        this.conversionLang = getLocalizedString("selectWorld.conversion");
        this.gameModesLang[Realms.survivalId()] = getLocalizedString("gameMode.survival");
        this.gameModesLang[Realms.creativeId()] = getLocalizedString("gameMode.creative");
        this.gameModesLang[Realms.adventureId()] = getLocalizedString("gameMode.adventure");
        this.gameModesLang[Realms.spectatorId()] = getLocalizedString("gameMode.spectator");
        this.addWidget(this.worldSelectionList);
        this.buttonsAdd(new RealmsButton(1, this.width() / 2 + 6, this.height() - 32, 153, 20, getLocalizedString("gui.back")) {
            @Override
            public void onPress() {
                Realms.setScreen(RealmsSelectFileToUploadScreen.this.lastScreen);
            }
        });
        this.buttonsAdd(
            this.uploadButton = new RealmsButton(2, this.width() / 2 - 154, this.height() - 32, 153, 20, getLocalizedString("mco.upload.button.name")) {
                @Override
                public void onPress() {
                    RealmsSelectFileToUploadScreen.this.upload();
                }
            }
        );
        this.uploadButton.active(this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size());
        this.addWidget(this.titleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.title"), this.width() / 2, 13, 16777215));
        this.addWidget(
            this.subtitleLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.subtitle"), this.width() / 2, RealmsConstants.row(-1), 10526880)
        );
        if (this.levelList.isEmpty()) {
            this.addWidget(
                this.noWorldsLabel = new RealmsLabel(getLocalizedString("mco.upload.select.world.none"), this.width() / 2, this.height() / 2 - 20, 16777215)
            );
        } else {
            this.noWorldsLabel = null;
        }

        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.setKeyboardHandlerSendRepeatsToGui(false);
    }

    private void upload() {
        if (this.selectedWorld != -1 && !this.levelList.get(this.selectedWorld).isHardcore()) {
            RealmsLevelSummary var0 = this.levelList.get(this.selectedWorld);
            Realms.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, var0));
        }

    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.worldSelectionList.render(param0, param1, param2);
        this.titleLabel.render(this);
        this.subtitleLabel.render(this);
        if (this.noWorldsLabel != null) {
            this.noWorldsLabel.render(this);
        }

        super.render(param0, param1, param2);
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
    public void tick() {
        super.tick();
    }

    private String gameModeName(RealmsLevelSummary param0) {
        return this.gameModesLang[param0.getGameMode()];
    }

    private String formatLastPlayed(RealmsLevelSummary param0) {
        return this.DATE_FORMAT.format(new Date(param0.getLastPlayed()));
    }

    @OnlyIn(Dist.CLIENT)
    class WorldListEntry extends RealmListEntry {
        final RealmsLevelSummary levelSummary;

        public WorldListEntry(RealmsLevelSummary param0) {
            this.levelSummary = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderItem(this.levelSummary, param0, param2, param1, param4, Tezzelator.instance, param5, param6);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
            return true;
        }

        protected void renderItem(RealmsLevelSummary param0, int param1, int param2, int param3, int param4, Tezzelator param5, int param6, int param7) {
            String var0 = param0.getLevelName();
            if (var0 == null || var0.isEmpty()) {
                var0 = RealmsSelectFileToUploadScreen.this.worldLang + " " + (param1 + 1);
            }

            String var1 = param0.getLevelId();
            var1 = var1 + " (" + RealmsSelectFileToUploadScreen.this.formatLastPlayed(param0);
            var1 = var1 + ")";
            String var2 = "";
            if (param0.isRequiresConversion()) {
                var2 = RealmsSelectFileToUploadScreen.this.conversionLang + " " + var2;
            } else {
                var2 = RealmsSelectFileToUploadScreen.this.gameModeName(param0);
                if (param0.isHardcore()) {
                    var2 = ChatFormatting.DARK_RED + RealmsScreen.getLocalizedString("mco.upload.hardcore") + ChatFormatting.RESET;
                }

                if (param0.hasCheats()) {
                    var2 = var2 + ", " + RealmsScreen.getLocalizedString("selectWorld.cheats");
                }
            }

            RealmsSelectFileToUploadScreen.this.drawString(var0, param2 + 2, param3 + 1, 16777215);
            RealmsSelectFileToUploadScreen.this.drawString(var1, param2 + 2, param3 + 12, 8421504);
            RealmsSelectFileToUploadScreen.this.drawString(var2, param2 + 2, param3 + 12 + 10, 8421504);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldSelectionList extends RealmsObjectSelectionList {
        public WorldSelectionList() {
            super(
                RealmsSelectFileToUploadScreen.this.width(),
                RealmsSelectFileToUploadScreen.this.height(),
                RealmsConstants.row(0),
                RealmsSelectFileToUploadScreen.this.height() - 40,
                36
            );
        }

        public void addEntry(RealmsLevelSummary param0) {
            this.addEntry(RealmsSelectFileToUploadScreen.this.new WorldListEntry(param0));
        }

        @Override
        public int getItemCount() {
            return RealmsSelectFileToUploadScreen.this.levelList.size();
        }

        @Override
        public int getMaxPosition() {
            return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectFileToUploadScreen.this.isFocused(this);
        }

        @Override
        public void renderBackground() {
            RealmsSelectFileToUploadScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int param0) {
            this.setSelected(param0);
            if (param0 != -1) {
                RealmsLevelSummary var0 = RealmsSelectFileToUploadScreen.this.levelList.get(param0);
                String var1 = RealmsScreen.getLocalizedString("narrator.select.list.position", param0 + 1, RealmsSelectFileToUploadScreen.this.levelList.size());
                String var2 = Realms.joinNarrations(
                    Arrays.asList(
                        var0.getLevelName(),
                        RealmsSelectFileToUploadScreen.this.formatLastPlayed(var0),
                        RealmsSelectFileToUploadScreen.this.gameModeName(var0),
                        var1
                    )
                );
                Realms.narrateNow(RealmsScreen.getLocalizedString("narrator.select", var2));
            }

            RealmsSelectFileToUploadScreen.this.selectedWorld = param0;
            RealmsSelectFileToUploadScreen.this.uploadButton
                .active(
                    RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
                        && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
                        && !RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld).isHardcore()
                );
        }
    }
}
