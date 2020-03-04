package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.realms.NarrationHelper;
import net.minecraft.realms.RealmsLabel;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RealmsSelectFileToUploadScreen extends RealmsScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private final RealmsResetWorldScreen lastScreen;
    private final long worldId;
    private final int slotId;
    private Button uploadButton;
    private List<LevelSummary> levelList = Lists.newArrayList();
    private int selectedWorld = -1;
    private RealmsSelectFileToUploadScreen.WorldSelectionList worldSelectionList;
    private String worldLang;
    private String conversionLang;
    private RealmsLabel titleLabel;
    private RealmsLabel subtitleLabel;
    private RealmsLabel noWorldsLabel;
    private final Runnable callback;

    public RealmsSelectFileToUploadScreen(long param0, int param1, RealmsResetWorldScreen param2, Runnable param3) {
        this.lastScreen = param2;
        this.worldId = param0;
        this.slotId = param1;
        this.callback = param3;
    }

    private void loadLevelList() throws Exception {
        this.levelList = this.minecraft.getLevelSource().getLevelList().stream().sorted((param0, param1) -> {
            if (param0.getLastPlayed() < param1.getLastPlayed()) {
                return 1;
            } else {
                return param0.getLastPlayed() > param1.getLastPlayed() ? -1 : param0.getLevelId().compareTo(param1.getLevelId());
            }
        }).collect(Collectors.toList());

        for(LevelSummary var0 : this.levelList) {
            this.worldSelectionList.addEntry(var0);
        }

    }

    @Override
    public void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldSelectionList = new RealmsSelectFileToUploadScreen.WorldSelectionList();

        try {
            this.loadLevelList();
        } catch (Exception var2) {
            LOGGER.error("Couldn't load level list", (Throwable)var2);
            this.minecraft.setScreen(new RealmsGenericErrorScreen("Unable to load worlds", var2.getMessage(), this.lastScreen));
            return;
        }

        this.worldLang = I18n.get("selectWorld.world");
        this.conversionLang = I18n.get("selectWorld.conversion");
        this.addWidget(this.worldSelectionList);
        this.uploadButton = this.addButton(
            new Button(this.width / 2 - 154, this.height - 32, 153, 20, I18n.get("mco.upload.button.name"), param0 -> this.upload())
        );
        this.uploadButton.active = this.selectedWorld >= 0 && this.selectedWorld < this.levelList.size();
        this.addButton(new Button(this.width / 2 + 6, this.height - 32, 153, 20, I18n.get("gui.back"), param0 -> this.minecraft.setScreen(this.lastScreen)));
        this.titleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.upload.select.world.title"), this.width / 2, 13, 16777215));
        this.subtitleLabel = this.addWidget(new RealmsLabel(I18n.get("mco.upload.select.world.subtitle"), this.width / 2, row(-1), 10526880));
        if (this.levelList.isEmpty()) {
            this.noWorldsLabel = this.addWidget(new RealmsLabel(I18n.get("mco.upload.select.world.none"), this.width / 2, this.height / 2 - 20, 16777215));
        } else {
            this.noWorldsLabel = null;
        }

        this.narrateLabels();
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void upload() {
        if (this.selectedWorld != -1 && !this.levelList.get(this.selectedWorld).isHardcore()) {
            LevelSummary var0 = this.levelList.get(this.selectedWorld);
            this.minecraft.setScreen(new RealmsUploadScreen(this.worldId, this.slotId, this.lastScreen, var0, this.callback));
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
            this.minecraft.setScreen(this.lastScreen);
            return true;
        } else {
            return super.keyPressed(param0, param1, param2);
        }
    }

    private static String gameModeName(LevelSummary param0) {
        return param0.getGameMode().getDisplayName().getString();
    }

    private static String formatLastPlayed(LevelSummary param0) {
        return DATE_FORMAT.format(new Date(param0.getLastPlayed()));
    }

    @OnlyIn(Dist.CLIENT)
    class Entry extends ObjectSelectionList.Entry<RealmsSelectFileToUploadScreen.Entry> {
        private final LevelSummary levelSummary;

        public Entry(LevelSummary param0) {
            this.levelSummary = param0;
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            this.renderItem(this.levelSummary, param0, param2, param1);
        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            RealmsSelectFileToUploadScreen.this.worldSelectionList.selectItem(RealmsSelectFileToUploadScreen.this.levelList.indexOf(this.levelSummary));
            return true;
        }

        protected void renderItem(LevelSummary param0, int param1, int param2, int param3) {
            String var0 = param0.getLevelName();
            if (var0 == null || var0.isEmpty()) {
                var0 = RealmsSelectFileToUploadScreen.this.worldLang + " " + (param1 + 1);
            }

            String var1 = param0.getLevelId();
            var1 = var1 + " (" + RealmsSelectFileToUploadScreen.formatLastPlayed(param0);
            var1 = var1 + ")";
            String var2 = "";
            if (param0.isRequiresConversion()) {
                var2 = RealmsSelectFileToUploadScreen.this.conversionLang + " " + var2;
            } else {
                var2 = RealmsSelectFileToUploadScreen.gameModeName(param0);
                if (param0.isHardcore()) {
                    var2 = ChatFormatting.DARK_RED + I18n.get("mco.upload.hardcore") + ChatFormatting.RESET;
                }

                if (param0.hasCheats()) {
                    var2 = var2 + ", " + I18n.get("selectWorld.cheats");
                }
            }

            RealmsSelectFileToUploadScreen.this.font.draw(var0, (float)(param2 + 2), (float)(param3 + 1), 16777215);
            RealmsSelectFileToUploadScreen.this.font.draw(var1, (float)(param2 + 2), (float)(param3 + 12), 8421504);
            RealmsSelectFileToUploadScreen.this.font.draw(var2, (float)(param2 + 2), (float)(param3 + 12 + 10), 8421504);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class WorldSelectionList extends RealmsObjectSelectionList<RealmsSelectFileToUploadScreen.Entry> {
        public WorldSelectionList() {
            super(
                RealmsSelectFileToUploadScreen.this.width,
                RealmsSelectFileToUploadScreen.this.height,
                RealmsSelectFileToUploadScreen.row(0),
                RealmsSelectFileToUploadScreen.this.height - 40,
                36
            );
        }

        public void addEntry(LevelSummary param0) {
            this.addEntry(RealmsSelectFileToUploadScreen.this.new Entry(param0));
        }

        @Override
        public int getMaxPosition() {
            return RealmsSelectFileToUploadScreen.this.levelList.size() * 36;
        }

        @Override
        public boolean isFocused() {
            return RealmsSelectFileToUploadScreen.this.getFocused() == this;
        }

        @Override
        public void renderBackground() {
            RealmsSelectFileToUploadScreen.this.renderBackground();
        }

        @Override
        public void selectItem(int param0) {
            this.setSelectedItem(param0);
            if (param0 != -1) {
                LevelSummary var0 = RealmsSelectFileToUploadScreen.this.levelList.get(param0);
                String var1 = I18n.get("narrator.select.list.position", param0 + 1, RealmsSelectFileToUploadScreen.this.levelList.size());
                String var2 = NarrationHelper.join(
                    Arrays.asList(
                        var0.getLevelName(), RealmsSelectFileToUploadScreen.formatLastPlayed(var0), RealmsSelectFileToUploadScreen.gameModeName(var0), var1
                    )
                );
                NarrationHelper.now(I18n.get("narrator.select", var2));
            }

        }

        public void setSelected(@Nullable RealmsSelectFileToUploadScreen.Entry param0) {
            super.setSelected(param0);
            RealmsSelectFileToUploadScreen.this.selectedWorld = this.children().indexOf(param0);
            RealmsSelectFileToUploadScreen.this.uploadButton.active = RealmsSelectFileToUploadScreen.this.selectedWorld >= 0
                && RealmsSelectFileToUploadScreen.this.selectedWorld < this.getItemCount()
                && !RealmsSelectFileToUploadScreen.this.levelList.get(RealmsSelectFileToUploadScreen.this.selectedWorld).isHardcore();
        }
    }
}
