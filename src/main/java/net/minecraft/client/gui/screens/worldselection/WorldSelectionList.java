package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
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
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelResource;
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
    private static final Component FROM_NEWER_TOOLTIP_1 = new TranslatableComponent("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    private static final Component FROM_NEWER_TOOLTIP_2 = new TranslatableComponent("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    private static final Component SNAPSHOT_TOOLTIP_1 = new TranslatableComponent("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    private static final Component SNAPSHOT_TOOLTIP_2 = new TranslatableComponent("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    private static final Component WORLD_LOCKED_TOOLTIP = new TranslatableComponent("selectWorld.locked").withStyle(ChatFormatting.RED);
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
                this.minecraft.setScreen(new ErrorScreen(new TranslatableComponent("selectWorld.unable_to_load"), new TextComponent(var7.getMessage())));
                return;
            }

            Collections.sort(this.cachedList);
        }

        if (this.cachedList.isEmpty()) {
            this.minecraft.setScreen(CreateWorldScreen.create(null));
        } else {
            String var2 = param0.get().toLowerCase(Locale.ROOT);

            for(LevelSummary var3 : this.cachedList) {
                if (var3.getLevelName().toLowerCase(Locale.ROOT).contains(var2) || var3.getLevelId().toLowerCase(Locale.ROOT).contains(var2)) {
                    this.addEntry(new WorldSelectionList.WorldListEntry(this, var3));
                }
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
                                var0.isHardcore()
                                    ? new TranslatableComponent("gameMode.hardcore")
                                    : new TranslatableComponent("gameMode." + var0.getGameMode().getName()),
                                var0.hasCheats() ? new TranslatableComponent("selectWorld.cheats") : TextComponent.EMPTY,
                                var0.getWorldVersionName()
                            )
                        )
                        .getString()
                );
        }

        this.screen.updateButtonStatus(param0 != null && !param0.summary.isLocked());
    }

    @Override
    protected void moveSelection(AbstractSelectionList.SelectionDirection param0) {
        this.moveSelection(param0, param0x -> !param0x.summary.isLocked());
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

        public WorldListEntry(WorldSelectionList param1, LevelSummary param2) {
            this.screen = param1.getScreen();
            this.summary = param2;
            this.minecraft = Minecraft.getInstance();
            String var0 = param2.getLevelId();
            this.iconLocation = new ResourceLocation(
                "minecraft", "worlds/" + Util.sanitizeName(var0, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(var0) + "/icon"
            );
            this.iconFile = param2.getIcon();
            if (!this.iconFile.isFile()) {
                this.iconFile = null;
            }

            this.icon = this.loadServerIcon();
        }

        @Override
        public void render(PoseStack param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            String var0 = this.summary.getLevelName();
            String var1 = this.summary.getLevelId() + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(var0)) {
                var0 = I18n.get("selectWorld.world") + " " + (param1 + 1);
            }

            Component var2 = this.summary.getInfo();
            this.minecraft.font.draw(param0, var0, (float)(param3 + 32 + 3), (float)(param2 + 1), 16777215);
            this.minecraft.font.draw(param0, var1, (float)(param3 + 32 + 3), (float)(param2 + 9 + 3), 8421504);
            this.minecraft.font.draw(param0, var2, (float)(param3 + 32 + 3), (float)(param2 + 9 + 9 + 3), 8421504);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            this.minecraft.getTextureManager().bind(this.icon != null ? this.iconLocation : WorldSelectionList.ICON_MISSING);
            RenderSystem.enableBlend();
            GuiComponent.blit(param0, param3, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen || param8) {
                this.minecraft.getTextureManager().bind(WorldSelectionList.ICON_OVERLAY_LOCATION);
                GuiComponent.fill(param0, param3, param2, param3 + 32, param2 + 32, -1601138544);
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                int var3 = param6 - param3;
                boolean var4 = var3 < 32;
                int var5 = var4 ? 32 : 0;
                if (this.summary.isLocked()) {
                    GuiComponent.blit(param0, param3, param2, 96.0F, (float)var5, 32, 32, 256, 256);
                    if (var4) {
                        this.screen.setToolTip(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
                    }
                } else if (this.summary.markVersionInList()) {
                    GuiComponent.blit(param0, param3, param2, 32.0F, (float)var5, 32, 32, 256, 256);
                    if (this.summary.askToOpenWorld()) {
                        GuiComponent.blit(param0, param3, param2, 96.0F, (float)var5, 32, 32, 256, 256);
                        if (var4) {
                            this.screen
                                .setToolTip(
                                    ImmutableList.of(
                                        WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(),
                                        WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText()
                                    )
                                );
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        GuiComponent.blit(param0, param3, param2, 64.0F, (float)var5, 32, 32, 256, 256);
                        if (var4) {
                            this.screen
                                .setToolTip(
                                    ImmutableList.of(
                                        WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText()
                                    )
                                );
                        }
                    }
                } else {
                    GuiComponent.blit(param0, param3, param2, 0.0F, (float)var5, 32, 32, 256, 256);
                }
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.summary.isLocked()) {
                return true;
            } else {
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
        }

        public void joinWorld() {
            if (!this.summary.isLocked()) {
                LevelSummary.BackupStatus var0 = this.summary.backupStatus();
                if (var0.shouldBackup()) {
                    String var1 = "selectWorld.backupQuestion." + var0.getTranslationKey();
                    String var2 = "selectWorld.backupWarning." + var0.getTranslationKey();
                    MutableComponent var3 = new TranslatableComponent(var1);
                    if (var0.isSevere()) {
                        var3.withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
                    }

                    Component var4 = new TranslatableComponent(var2, this.summary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
                    this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (param0, param1) -> {
                        if (param0) {
                            String var0x = this.summary.getLevelId();

                            try (LevelStorageSource.LevelStorageAccess var1x = this.minecraft.getLevelSource().createAccess(var0x)) {
                                EditWorldScreen.makeBackupAndShowToast(var1x);
                            } catch (IOException var17) {
                                SystemToast.onWorldAccessFailure(this.minecraft, var0x);
                                WorldSelectionList.LOGGER.error("Failed to backup level {}", var0x, var17);
                            }
                        }

                        this.loadWorld();
                    }, var3, var4, false));
                } else if (this.summary.askToOpenWorld()) {
                    this.minecraft
                        .setScreen(
                            new ConfirmScreen(
                                param0 -> {
                                    if (param0) {
                                        try {
                                            this.loadWorld();
                                        } catch (Exception var3x) {
                                            WorldSelectionList.LOGGER.error("Failure to open 'future world'", (Throwable)var3x);
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
                                new TranslatableComponent("selectWorld.versionWarning", this.summary.getWorldVersionName()),
                                new TranslatableComponent("selectWorld.versionJoinButton"),
                                CommonComponents.GUI_CANCEL
                            )
                        );
                } else {
                    this.loadWorld();
                }

            }
        }

        public void deleteWorld() {
            this.minecraft
                .setScreen(
                    new ConfirmScreen(
                        param0 -> {
                            if (param0) {
                                this.minecraft.setScreen(new ProgressScreen());
                                this.doDeleteWorld();
                            }
            
                            this.minecraft.setScreen(this.screen);
                        },
                        new TranslatableComponent("selectWorld.deleteQuestion"),
                        new TranslatableComponent("selectWorld.deleteWarning", this.summary.getLevelName()),
                        new TranslatableComponent("selectWorld.deleteButton"),
                        CommonComponents.GUI_CANCEL
                    )
                );
        }

        public void doDeleteWorld() {
            LevelStorageSource var0 = this.minecraft.getLevelSource();
            String var1 = this.summary.getLevelId();

            try (LevelStorageSource.LevelStorageAccess var2 = var0.createAccess(var1)) {
                var2.deleteLevel();
            } catch (IOException var16) {
                SystemToast.onWorldDeleteFailure(this.minecraft, var1);
                WorldSelectionList.LOGGER.error("Failed to delete world {}", var1, var16);
            }

            WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
        }

        public void editWorld() {
            String var0 = this.summary.getLevelId();

            try {
                LevelStorageSource.LevelStorageAccess var1 = this.minecraft.getLevelSource().createAccess(var0);
                this.minecraft.setScreen(new EditWorldScreen(param2 -> {
                    try {
                        var1.close();
                    } catch (IOException var5) {
                        WorldSelectionList.LOGGER.error("Failed to unlock level {}", var0, var5);
                    }

                    if (param2) {
                        WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
                    }

                    this.minecraft.setScreen(this.screen);
                }, var1));
            } catch (IOException var3) {
                SystemToast.onWorldAccessFailure(this.minecraft, var0);
                WorldSelectionList.LOGGER.error("Failed to access level {}", var0, var3);
                WorldSelectionList.this.refreshList(() -> this.screen.searchBox.getValue(), true);
            }

        }

        public void recreateWorld() {
            this.queueLoadScreen();
            RegistryAccess.RegistryHolder var0 = RegistryAccess.builtin();

            try (
                LevelStorageSource.LevelStorageAccess var1 = this.minecraft.getLevelSource().createAccess(this.summary.getLevelId());
                Minecraft.ServerStem var2 = this.minecraft.makeServerStem(var0, Minecraft::loadDataPacks, Minecraft::loadWorldData, false, var1);
            ) {
                LevelSettings var3 = var2.worldData().getLevelSettings();
                DataPackConfig var4 = var3.getDataPackConfig();
                WorldGenSettings var5 = var2.worldData().worldGenSettings();
                Path var6 = CreateWorldScreen.createTempDataPackDirFromExistingWorld(var1.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                if (var5.isOldCustomizedWorld()) {
                    this.minecraft
                        .setScreen(
                            new ConfirmScreen(
                                param5 -> this.minecraft
                                        .setScreen((Screen)(param5 ? new CreateWorldScreen(this.screen, var3, var5, var6, var4, var0) : this.screen)),
                                new TranslatableComponent("selectWorld.recreate.customized.title"),
                                new TranslatableComponent("selectWorld.recreate.customized.text"),
                                CommonComponents.GUI_PROCEED,
                                CommonComponents.GUI_CANCEL
                            )
                        );
                } else {
                    this.minecraft.setScreen(new CreateWorldScreen(this.screen, var3, var5, var6, var4, var0));
                }
            } catch (Exception var37) {
                WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var37);
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
                this.queueLoadScreen();
                this.minecraft.loadLevel(this.summary.getLevelId());
            }

        }

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericDirtMessageScreen(new TranslatableComponent("selectWorld.data_read")));
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
