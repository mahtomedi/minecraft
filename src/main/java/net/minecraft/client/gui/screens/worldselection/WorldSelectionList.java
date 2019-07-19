package net.minecraft.client.gui.screens.worldselection;

import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.WorldListEntry> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    private final SelectWorldScreen screen;
    @Nullable
    private List<LevelSummary> cachedList;

    public WorldSelectionList(
        SelectWorldScreen param0,
        Minecraft param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        Supplier<String> param7,
        @Nullable WorldSelectionList param8
    ) {
        super(param1, param2, param3, param4, param5, param6);
        this.screen = param0;
        if (param8 != null) {
            this.cachedList = param8.cachedList;
        }

        this.refreshList(param7, false);
    }

    public void refreshList(Supplier<String> param0, boolean param1) {
        this.clearEntries();
        LevelStorageSource var0 = this.minecraft.getLevelSource();
        if (this.cachedList == null || param1) {
            try {
                this.cachedList = var0.getLevelList();
            } catch (LevelStorageException var7) {
                LOGGER.error("Couldn't load level list", (Throwable)var7);
                this.minecraft.setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load"), var7.getMessage()));
                return;
            }

            Collections.sort(this.cachedList);
        }

        String var2 = param0.get().toLowerCase(Locale.ROOT);

        for(LevelSummary var3 : this.cachedList) {
            if (var3.getLevelName().toLowerCase(Locale.ROOT).contains(var2) || var3.getLevelId().toLowerCase(Locale.ROOT).contains(var2)) {
                this.addEntry(new WorldSelectionList.WorldListEntry(this, var3, this.minecraft.getLevelSource()));
            }
        }

    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    @Override
    protected boolean isFocused() {
        return this.screen.getFocused() == this;
    }

    public void setSelected(@Nullable WorldSelectionList.WorldListEntry param0) {
        super.setSelected(param0);
        if (param0 != null) {
            LevelSummary var0 = param0.summary;
            NarratorChatListener.INSTANCE
                .sayNow(
                    new TranslatableComponent(
                            "narrator.select",
                            new TranslatableComponent(
                                "narrator.select.world",
                                var0.getLevelName(),
                                new Date(var0.getLastPlayed()),
                                var0.isHardcore() ? I18n.get("gameMode.hardcore") : I18n.get("gameMode." + var0.getGameMode().getName()),
                                var0.hasCheats() ? I18n.get("selectWorld.cheats") : "",
                                var0.getWorldVersionName()
                            )
                        )
                        .getString()
                );
        }

    }

    @Override
    protected void moveSelection(int param0) {
        super.moveSelection(param0);
        this.screen.updateButtonStatus(true);
    }

    public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
        return Optional.ofNullable(this.getSelected());
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @OnlyIn(Dist.CLIENT)
    public final class WorldListEntry extends ObjectSelectionList.Entry<WorldSelectionList.WorldListEntry> implements AutoCloseable {
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        private final LevelSummary summary;
        private final ResourceLocation iconLocation;
        private File iconFile;
        @Nullable
        private final DynamicTexture icon;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList param1, LevelSummary param2, LevelStorageSource param3) {
            this.screen = param1.getScreen();
            this.summary = param2;
            this.minecraft = Minecraft.getInstance();
            this.iconLocation = new ResourceLocation("worlds/" + Hashing.sha1().hashUnencodedChars(param2.getLevelId()) + "/icon");
            this.iconFile = param3.getFile(param2.getLevelId(), "icon.png");
            if (!this.iconFile.isFile()) {
                this.iconFile = null;
            }

            this.icon = this.loadServerIcon();
        }

        @Override
        public void render(int param0, int param1, int param2, int param3, int param4, int param5, int param6, boolean param7, float param8) {
            String var0 = this.summary.getLevelName();
            String var1 = this.summary.getLevelId() + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(var0)) {
                var0 = I18n.get("selectWorld.world") + " " + (param0 + 1);
            }

            String var2 = "";
            if (this.summary.isRequiresConversion()) {
                var2 = I18n.get("selectWorld.conversion") + " " + var2;
            } else {
                var2 = I18n.get("gameMode." + this.summary.getGameMode().getName());
                if (this.summary.isHardcore()) {
                    var2 = ChatFormatting.DARK_RED + I18n.get("gameMode.hardcore") + ChatFormatting.RESET;
                }

                if (this.summary.hasCheats()) {
                    var2 = var2 + ", " + I18n.get("selectWorld.cheats");
                }

                String var3 = this.summary.getWorldVersionName().getColoredString();
                if (this.summary.markVersionInList()) {
                    if (this.summary.askToOpenWorld()) {
                        var2 = var2 + ", " + I18n.get("selectWorld.version") + " " + ChatFormatting.RED + var3 + ChatFormatting.RESET;
                    } else {
                        var2 = var2 + ", " + I18n.get("selectWorld.version") + " " + ChatFormatting.ITALIC + var3 + ChatFormatting.RESET;
                    }
                } else {
                    var2 = var2 + ", " + I18n.get("selectWorld.version") + " " + var3;
                }
            }

            this.minecraft.font.draw(var0, (float)(param2 + 32 + 3), (float)(param1 + 1), 16777215);
            this.minecraft.font.draw(var1, (float)(param2 + 32 + 3), (float)(param1 + 9 + 3), 8421504);
            this.minecraft.font.draw(var2, (float)(param2 + 32 + 3), (float)(param1 + 9 + 9 + 3), 8421504);
            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(this.icon != null ? this.iconLocation : WorldSelectionList.ICON_MISSING);
            GlStateManager.enableBlend();
            GuiComponent.blit(param2, param1, 0.0F, 0.0F, 32, 32, 32, 32);
            GlStateManager.disableBlend();
            if (this.minecraft.options.touchscreen || param7) {
                this.minecraft.getTextureManager().bind(WorldSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param2, param1, param2 + 32, param1 + 32, -1601138544);
                GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var4 = param5 - param2;
                int var5 = var4 < 32 ? 32 : 0;
                if (this.summary.markVersionInList()) {
                    GuiComponent.blit(param2, param1, 32.0F, (float)var5, 32, 32, 256, 256);
                    if (this.summary.isOldCustomizedWorld()) {
                        GuiComponent.blit(param2, param1, 96.0F, (float)var5, 32, 32, 256, 256);
                        if (var4 < 32) {
                            Component var6 = new TranslatableComponent("selectWorld.tooltip.unsupported", this.summary.getWorldVersionName())
                                .withStyle(ChatFormatting.RED);
                            this.screen.setToolTip(this.minecraft.font.insertLineBreaks(var6.getColoredString(), 175));
                        }
                    } else if (this.summary.askToOpenWorld()) {
                        GuiComponent.blit(param2, param1, 96.0F, (float)var5, 32, 32, 256, 256);
                        if (var4 < 32) {
                            this.screen
                                .setToolTip(
                                    ChatFormatting.RED
                                        + I18n.get("selectWorld.tooltip.fromNewerVersion1")
                                        + "\n"
                                        + ChatFormatting.RED
                                        + I18n.get("selectWorld.tooltip.fromNewerVersion2")
                                );
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        GuiComponent.blit(param2, param1, 64.0F, (float)var5, 32, 32, 256, 256);
                        if (var4 < 32) {
                            this.screen
                                .setToolTip(
                                    ChatFormatting.GOLD
                                        + I18n.get("selectWorld.tooltip.snapshot1")
                                        + "\n"
                                        + ChatFormatting.GOLD
                                        + I18n.get("selectWorld.tooltip.snapshot2")
                                );
                        }
                    }
                } else {
                    GuiComponent.blit(param2, param1, 0.0F, (float)var5, 32, 32, 256, 256);
                }
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            WorldSelectionList.this.setSelected(this);
            this.screen.updateButtonStatus(WorldSelectionList.this.getSelectedOpt().isPresent());
            if (param0 - (double)WorldSelectionList.this.getRowLeft() <= 32.0) {
                this.joinWorld();
                return true;
            } else if (Util.getMillis() - this.lastClickTime < 250L) {
                this.joinWorld();
                return true;
            } else {
                this.lastClickTime = Util.getMillis();
                return false;
            }
        }

        public void joinWorld() {
            if (this.summary.shouldBackup() || this.summary.isOldCustomizedWorld()) {
                Component var0 = new TranslatableComponent("selectWorld.backupQuestion");
                Component var1 = new TranslatableComponent(
                    "selectWorld.backupWarning", this.summary.getWorldVersionName().getColoredString(), SharedConstants.getCurrentVersion().getName()
                );
                if (this.summary.isOldCustomizedWorld()) {
                    var0 = new TranslatableComponent("selectWorld.backupQuestion.customized");
                    var1 = new TranslatableComponent("selectWorld.backupWarning.customized");
                }

                this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (param0, param1) -> {
                    if (param0) {
                        String var0x = this.summary.getLevelId();
                        EditWorldScreen.makeBackupAndShowToast(this.minecraft.getLevelSource(), var0x);
                    }

                    this.loadWorld();
                }, var0, var1, false));
            } else if (this.summary.askToOpenWorld()) {
                this.minecraft
                    .setScreen(
                        new ConfirmScreen(
                            param0 -> {
                                if (param0) {
                                    try {
                                        this.loadWorld();
                                    } catch (Exception var3) {
                                        WorldSelectionList.LOGGER.error("Failure to open 'future world'", (Throwable)var3);
                                        this.minecraft
                                            .setScreen(
                                                new AlertScreen(
                                                    () -> this.minecraft.setScreen(this.screen),
                                                    new TranslatableComponent("selectWorld.futureworld.error.title"),
                                                    new TranslatableComponent("selectWorld.futureworld.error.text")
                                                )
                                            );
                                    }
                                } else {
                                    this.minecraft.setScreen(this.screen);
                                }
            
                            },
                            new TranslatableComponent("selectWorld.versionQuestion"),
                            new TranslatableComponent("selectWorld.versionWarning", this.summary.getWorldVersionName().getColoredString()),
                            I18n.get("selectWorld.versionJoinButton"),
                            I18n.get("gui.cancel")
                        )
                    );
            } else {
                this.loadWorld();
            }

        }

        public void deleteWorld() {
            this.minecraft
                .setScreen(
                    new ConfirmScreen(
                        param0 -> {
                            if (param0) {
                                this.minecraft.setScreen(new ProgressScreen());
                                LevelStorageSource var0 = this.minecraft.getLevelSource();
                                var0.deleteLevel(this.summary.getLevelId());
                                WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
                            }
            
                            this.minecraft.setScreen(this.screen);
                        },
                        new TranslatableComponent("selectWorld.deleteQuestion"),
                        new TranslatableComponent("selectWorld.deleteWarning", this.summary.getLevelName()),
                        I18n.get("selectWorld.deleteButton"),
                        I18n.get("gui.cancel")
                    )
                );
        }

        public void editWorld() {
            this.minecraft.setScreen(new EditWorldScreen(param0 -> {
                if (param0) {
                    WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
                }

                this.minecraft.setScreen(this.screen);
            }, this.summary.getLevelId()));
        }

        public void recreateWorld() {
            try {
                this.minecraft.setScreen(new ProgressScreen());
                CreateWorldScreen var0 = new CreateWorldScreen(this.screen);
                LevelStorage var1 = this.minecraft.getLevelSource().selectLevel(this.summary.getLevelId(), null);
                LevelData var2 = var1.prepareLevel();
                if (var2 != null) {
                    var0.copyFromWorld(var2);
                    if (this.summary.isOldCustomizedWorld()) {
                        this.minecraft
                            .setScreen(
                                new ConfirmScreen(
                                    param1 -> this.minecraft.setScreen((Screen)(param1 ? var0 : this.screen)),
                                    new TranslatableComponent("selectWorld.recreate.customized.title"),
                                    new TranslatableComponent("selectWorld.recreate.customized.text"),
                                    I18n.get("gui.proceed"),
                                    I18n.get("gui.cancel")
                                )
                            );
                    } else {
                        this.minecraft.setScreen(var0);
                    }
                }
            } catch (Exception var4) {
                WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var4);
                this.minecraft
                    .setScreen(
                        new AlertScreen(
                            () -> this.minecraft.setScreen(this.screen),
                            new TranslatableComponent("selectWorld.recreate.error.title"),
                            new TranslatableComponent("selectWorld.recreate.error.text")
                        )
                    );
            }

        }

        private void loadWorld() {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
                this.minecraft.selectLevel(this.summary.getLevelId(), this.summary.getLevelName(), null);
            }

        }

        @Nullable
        private DynamicTexture loadServerIcon() {
            boolean var0 = this.iconFile != null && this.iconFile.isFile();
            if (var0) {
                try (InputStream var1 = new FileInputStream(this.iconFile)) {
                    NativeImage var2 = NativeImage.read(var1);
                    Validate.validState(var2.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(var2.getHeight() == 64, "Must be 64 pixels high");
                    DynamicTexture var3 = new DynamicTexture(var2);
                    this.minecraft.getTextureManager().register(this.iconLocation, var3);
                    return var3;
                } catch (Throwable var18) {
                    WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var18);
                    this.iconFile = null;
                    return null;
                }
            } else {
                this.minecraft.getTextureManager().release(this.iconLocation);
                return null;
            }
        }

        @Override
        public void close() {
            if (this.icon != null) {
                this.icon.close();
            }

        }
    }
}
