package net.minecraft.client.gui.screens.worldselection;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
    static final Logger LOGGER = LogUtils.getLogger();
    static final DateFormat DATE_FORMAT = new SimpleDateFormat();
    static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final ResourceLocation ICON_OVERLAY_LOCATION = new ResourceLocation("textures/gui/world_selection.png");
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    private final SelectWorldScreen screen;
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    @Nullable
    private List<LevelSummary> currentlyDisplayedLevels;
    private String filter;
    private final WorldSelectionList.LoadingHeader loadingHeader;

    public WorldSelectionList(
        SelectWorldScreen param0,
        Minecraft param1,
        int param2,
        int param3,
        int param4,
        int param5,
        int param6,
        String param7,
        @Nullable WorldSelectionList param8
    ) {
        super(param1, param2, param3, param4, param5, param6);
        this.screen = param0;
        this.loadingHeader = new WorldSelectionList.LoadingHeader(param1);
        this.filter = param7;
        if (param8 != null) {
            this.pendingLevels = param8.pendingLevels;
        } else {
            this.pendingLevels = this.loadLevels();
        }

        this.handleNewLevels(this.pollLevelsIgnoreErrors());
    }

    @Nullable
    private List<LevelSummary> pollLevelsIgnoreErrors() {
        try {
            return this.pendingLevels.getNow(null);
        } catch (CancellationException | CompletionException var2) {
            return null;
        }
    }

    void reloadWorldList() {
        this.pendingLevels = this.loadLevels();
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        List<LevelSummary> var0 = this.pollLevelsIgnoreErrors();
        if (var0 != this.currentlyDisplayedLevels) {
            this.handleNewLevels(var0);
        }

        super.render(param0, param1, param2, param3);
    }

    private void handleNewLevels(@Nullable List<LevelSummary> param0) {
        if (param0 == null) {
            this.fillLoadingLevels();
        } else {
            this.fillLevels(this.filter, param0);
        }

        this.currentlyDisplayedLevels = param0;
    }

    public void updateFilter(String param0) {
        if (this.currentlyDisplayedLevels != null && !param0.equals(this.filter)) {
            this.fillLevels(param0, this.currentlyDisplayedLevels);
        }

        this.filter = param0;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates var0;
        try {
            var0 = this.minecraft.getLevelSource().findLevelCandidates();
        } catch (LevelStorageException var3) {
            LOGGER.error("Couldn't load level list", (Throwable)var3);
            this.handleLevelLoadFailure(var3.getMessageComponent());
            return CompletableFuture.completedFuture(List.of());
        }

        if (var0.isEmpty()) {
            CreateWorldScreen.openFresh(this.minecraft, null);
            return CompletableFuture.completedFuture(List.of());
        } else {
            return this.minecraft.getLevelSource().loadLevelSummaries(var0).exceptionally(param0 -> {
                this.minecraft.delayCrash(CrashReport.forThrowable(param0, "Couldn't load level list"));
                return List.of();
            });
        }
    }

    private void fillLevels(String param0, List<LevelSummary> param1) {
        this.clearEntries();
        param0 = param0.toLowerCase(Locale.ROOT);

        for(LevelSummary var0 : param1) {
            if (this.filterAccepts(param0, var0)) {
                this.addEntry(new WorldSelectionList.WorldListEntry(this, var0));
            }
        }

        this.notifyListUpdated();
    }

    private boolean filterAccepts(String param0, LevelSummary param1) {
        return param1.getLevelName().toLowerCase(Locale.ROOT).contains(param0) || param1.getLevelId().toLowerCase(Locale.ROOT).contains(param0);
    }

    private void fillLoadingLevels() {
        this.clearEntries();
        this.addEntry(this.loadingHeader);
        this.notifyListUpdated();
    }

    private void notifyListUpdated() {
        this.screen.triggerImmediateNarration(true);
    }

    private void handleLevelLoadFailure(Component param0) {
        this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), param0));
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 20;
    }

    @Override
    public int getRowWidth() {
        return super.getRowWidth() + 50;
    }

    public void setSelected(@Nullable WorldSelectionList.Entry param0) {
        super.setSelected(param0);
        this.screen.updateButtonStatus(param0 != null && param0.isSelectable(), param0 != null);
    }

    public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
        WorldSelectionList.Entry var0 = this.getSelected();
        return var0 instanceof WorldSelectionList.WorldListEntry var1 ? Optional.of(var1) : Optional.empty();
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateNarration(NarrationElementOutput param0) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(param0);
        } else {
            super.updateNarration(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ObjectSelectionList.Entry<WorldSelectionList.Entry> implements AutoCloseable {
        public abstract boolean isSelectable();

        @Override
        public void close() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class LoadingHeader extends WorldSelectionList.Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft param0) {
            this.minecraft = param0;
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            int var0 = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int var1 = param2 + (param5 - 9) / 2;
            param0.drawString(this.minecraft.font, LOADING_LABEL, var0, var1, 16777215, false);
            String var2 = LoadingDotsText.get(Util.getMillis());
            int var3 = (this.minecraft.screen.width - this.minecraft.font.width(var2)) / 2;
            int var4 = var1 + 9;
            param0.drawString(this.minecraft.font, var2, var3, var4, 8421504, false);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }

        @Override
        public boolean isSelectable() {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class WorldListEntry extends WorldSelectionList.Entry implements AutoCloseable {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int ICON_OVERLAY_X_JOIN = 0;
        private static final int ICON_OVERLAY_X_JOIN_WITH_NOTIFY = 32;
        private static final int ICON_OVERLAY_X_WARNING = 64;
        private static final int ICON_OVERLAY_X_ERROR = 96;
        private static final int ICON_OVERLAY_Y_UNSELECTED = 0;
        private static final int ICON_OVERLAY_Y_SELECTED = 32;
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        private final LevelSummary summary;
        private final ResourceLocation iconLocation;
        @Nullable
        private Path iconFile;
        @Nullable
        private final DynamicTexture icon;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList param1, LevelSummary param2) {
            this.minecraft = param1.minecraft;
            this.screen = param1.getScreen();
            this.summary = param2;
            String var0 = param2.getLevelId();
            this.iconLocation = new ResourceLocation(
                "minecraft", "worlds/" + Util.sanitizeName(var0, ResourceLocation::validPathChar) + "/" + Hashing.sha1().hashUnencodedChars(var0) + "/icon"
            );
            this.iconFile = param2.getIcon();
            if (!Files.isRegularFile(this.iconFile)) {
                this.iconFile = null;
            }

            this.icon = this.loadServerIcon();
        }

        @Override
        public Component getNarration() {
            Component var0 = Component.translatable(
                "narrator.select.world",
                this.summary.getLevelName(),
                new Date(this.summary.getLastPlayed()),
                this.summary.isHardcore()
                    ? Component.translatable("gameMode.hardcore")
                    : Component.translatable("gameMode." + this.summary.getGameMode().getName()),
                this.summary.hasCheats() ? Component.translatable("selectWorld.cheats") : CommonComponents.EMPTY,
                this.summary.getWorldVersionName()
            );
            Component var1;
            if (this.summary.isLocked()) {
                var1 = CommonComponents.joinForNarration(var0, WorldSelectionList.WORLD_LOCKED_TOOLTIP);
            } else {
                var1 = var0;
            }

            return Component.translatable("narrator.select", var1);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            String var0 = this.summary.getLevelName();
            String var1 = this.summary.getLevelId() + " (" + WorldSelectionList.DATE_FORMAT.format(new Date(this.summary.getLastPlayed())) + ")";
            if (StringUtils.isEmpty(var0)) {
                var0 = I18n.get("selectWorld.world") + " " + (param1 + 1);
            }

            Component var2 = this.summary.getInfo();
            param0.drawString(this.minecraft.font, var0, param3 + 32 + 3, param2 + 1, 16777215, false);
            param0.drawString(this.minecraft.font, var1, param3 + 32 + 3, param2 + 9 + 3, 8421504, false);
            param0.drawString(this.minecraft.font, var2, param3 + 32 + 3, param2 + 9 + 9 + 3, 8421504, false);
            ResourceLocation var3 = this.icon != null ? this.iconLocation : WorldSelectionList.ICON_MISSING;
            RenderSystem.enableBlend();
            param0.blit(var3, param3, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen().get() || param8) {
                param0.fill(param3, param2, param3 + 32, param2 + 32, -1601138544);
                int var4 = param6 - param3;
                boolean var5 = var4 < 32;
                int var6 = var5 ? 32 : 0;
                if (this.summary.isLocked()) {
                    param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, (float)var6, 32, 32, 256, 256);
                    if (var5) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
                    }
                } else if (this.summary.requiresManualConversion()) {
                    param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, (float)var6, 32, 32, 256, 256);
                    if (var5) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
                    }
                } else if (this.summary.markVersionInList()) {
                    param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 32.0F, (float)var6, 32, 32, 256, 256);
                    if (this.summary.askToOpenWorld()) {
                        param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 96.0F, (float)var6, 32, 32, 256, 256);
                        if (var5) {
                            this.screen
                                .setTooltipForNextRenderPass(
                                    ImmutableList.of(
                                        WorldSelectionList.FROM_NEWER_TOOLTIP_1.getVisualOrderText(),
                                        WorldSelectionList.FROM_NEWER_TOOLTIP_2.getVisualOrderText()
                                    )
                                );
                        }
                    } else if (!SharedConstants.getCurrentVersion().isStable()) {
                        param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 64.0F, (float)var6, 32, 32, 256, 256);
                        if (var5) {
                            this.screen
                                .setTooltipForNextRenderPass(
                                    ImmutableList.of(
                                        WorldSelectionList.SNAPSHOT_TOOLTIP_1.getVisualOrderText(), WorldSelectionList.SNAPSHOT_TOOLTIP_2.getVisualOrderText()
                                    )
                                );
                        }
                    }
                } else {
                    param0.blit(WorldSelectionList.ICON_OVERLAY_LOCATION, param3, param2, 0.0F, (float)var6, 32, 32, 256, 256);
                }
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (this.summary.isDisabled()) {
                return true;
            } else {
                WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
                if (param0 - (double)WorldSelectionList.this.getRowLeft() <= 32.0) {
                    this.joinWorld();
                    return true;
                } else if (Util.getMillis() - this.lastClickTime < 250L) {
                    this.joinWorld();
                    return true;
                } else {
                    this.lastClickTime = Util.getMillis();
                    return true;
                }
            }
        }

        public void joinWorld() {
            if (!this.summary.isDisabled()) {
                LevelSummary.BackupStatus var0 = this.summary.backupStatus();
                if (var0.shouldBackup()) {
                    String var1 = "selectWorld.backupQuestion." + var0.getTranslationKey();
                    String var2 = "selectWorld.backupWarning." + var0.getTranslationKey();
                    MutableComponent var3 = Component.translatable(var1);
                    if (var0.isSevere()) {
                        var3.withStyle(ChatFormatting.BOLD, ChatFormatting.RED);
                    }

                    Component var4 = Component.translatable(var2, this.summary.getWorldVersionName(), SharedConstants.getCurrentVersion().getName());
                    this.minecraft.setScreen(new BackupConfirmScreen(this.screen, (param0, param1) -> {
                        if (param0) {
                            String var0x = this.summary.getLevelId();

                            try (LevelStorageSource.LevelStorageAccess var1x = this.minecraft.getLevelSource().createAccess(var0x)) {
                                EditWorldScreen.makeBackupAndShowToast(var1x);
                            } catch (IOException var9) {
                                SystemToast.onWorldAccessFailure(this.minecraft, var0x);
                                WorldSelectionList.LOGGER.error("Failed to backup level {}", var0x, var9);
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
                                                        Component.translatable("selectWorld.futureworld.error.title"),
                                                        Component.translatable("selectWorld.futureworld.error.text")
                                                    )
                                                );
                                        }
                                    } else {
                                        this.minecraft.setScreen(this.screen);
                                    }
            
                                },
                                Component.translatable("selectWorld.versionQuestion"),
                                Component.translatable("selectWorld.versionWarning", this.summary.getWorldVersionName()),
                                Component.translatable("selectWorld.versionJoinButton"),
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
                                this.minecraft.setScreen(new ProgressScreen(true));
                                this.doDeleteWorld();
                            }
            
                            this.minecraft.setScreen(this.screen);
                        },
                        Component.translatable("selectWorld.deleteQuestion"),
                        Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()),
                        Component.translatable("selectWorld.deleteButton"),
                        CommonComponents.GUI_CANCEL
                    )
                );
        }

        public void doDeleteWorld() {
            LevelStorageSource var0 = this.minecraft.getLevelSource();
            String var1 = this.summary.getLevelId();

            try (LevelStorageSource.LevelStorageAccess var2 = var0.createAccess(var1)) {
                var2.deleteLevel();
            } catch (IOException var8) {
                SystemToast.onWorldDeleteFailure(this.minecraft, var1);
                WorldSelectionList.LOGGER.error("Failed to delete world {}", var1, var8);
            }

            WorldSelectionList.this.reloadWorldList();
        }

        public void editWorld() {
            this.queueLoadScreen();
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
                        WorldSelectionList.this.reloadWorldList();
                    }

                    this.minecraft.setScreen(this.screen);
                }, var1));
            } catch (IOException var3) {
                SystemToast.onWorldAccessFailure(this.minecraft, var0);
                WorldSelectionList.LOGGER.error("Failed to access level {}", var0, var3);
                WorldSelectionList.this.reloadWorldList();
            }

        }

        public void recreateWorld() {
            this.queueLoadScreen();

            try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().createAccess(this.summary.getLevelId())) {
                Pair<LevelSettings, WorldCreationContext> var1 = this.minecraft.createWorldOpenFlows().recreateWorldData(var0);
                LevelSettings var2 = var1.getFirst();
                WorldCreationContext var3 = var1.getSecond();
                Path var4 = CreateWorldScreen.createTempDataPackDirFromExistingWorld(var0.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                if (var3.options().isOldCustomizedWorld()) {
                    this.minecraft
                        .setScreen(
                            new ConfirmScreen(
                                param3 -> this.minecraft
                                        .setScreen(
                                            (Screen)(param3 ? CreateWorldScreen.createFromExisting(this.minecraft, this.screen, var2, var3, var4) : this.screen)
                                        ),
                                Component.translatable("selectWorld.recreate.customized.title"),
                                Component.translatable("selectWorld.recreate.customized.text"),
                                CommonComponents.GUI_PROCEED,
                                CommonComponents.GUI_CANCEL
                            )
                        );
                } else {
                    this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.screen, var2, var3, var4));
                }
            } catch (Exception var8) {
                WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var8);
                this.minecraft
                    .setScreen(
                        new AlertScreen(
                            () -> this.minecraft.setScreen(this.screen),
                            Component.translatable("selectWorld.recreate.error.title"),
                            Component.translatable("selectWorld.recreate.error.text")
                        )
                    );
            }

        }

        private void loadWorld() {
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            if (this.minecraft.getLevelSource().levelExists(this.summary.getLevelId())) {
                this.queueLoadScreen();
                this.minecraft.createWorldOpenFlows().loadLevel(this.screen, this.summary.getLevelId());
            }

        }

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        @Nullable
        private DynamicTexture loadServerIcon() {
            boolean var0 = this.iconFile != null && Files.isRegularFile(this.iconFile);
            if (var0) {
                try {
                    DynamicTexture var5;
                    try (InputStream var1 = Files.newInputStream(this.iconFile)) {
                        NativeImage var2 = NativeImage.read(var1);
                        Preconditions.checkState(var2.getWidth() == 64, "Must be 64 pixels wide");
                        Preconditions.checkState(var2.getHeight() == 64, "Must be 64 pixels high");
                        DynamicTexture var3 = new DynamicTexture(var2);
                        this.minecraft.getTextureManager().register(this.iconLocation, var3);
                        var5 = var3;
                    }

                    return var5;
                } catch (Throwable var8) {
                    WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var8);
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

        public String getLevelName() {
            return this.summary.getLevelName();
        }

        @Override
        public boolean isSelectable() {
            return !this.summary.isDisabled();
        }
    }
}
