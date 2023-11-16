package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericDirtMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class WorldSelectionList extends ObjectSelectionList<WorldSelectionList.Entry> {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    static final ResourceLocation ERROR_HIGHLIGHTED_SPRITE = new ResourceLocation("world_list/error_highlighted");
    static final ResourceLocation ERROR_SPRITE = new ResourceLocation("world_list/error");
    static final ResourceLocation MARKED_JOIN_HIGHLIGHTED_SPRITE = new ResourceLocation("world_list/marked_join_highlighted");
    static final ResourceLocation MARKED_JOIN_SPRITE = new ResourceLocation("world_list/marked_join");
    static final ResourceLocation WARNING_HIGHLIGHTED_SPRITE = new ResourceLocation("world_list/warning_highlighted");
    static final ResourceLocation WARNING_SPRITE = new ResourceLocation("world_list/warning");
    static final ResourceLocation JOIN_HIGHLIGHTED_SPRITE = new ResourceLocation("world_list/join_highlighted");
    static final ResourceLocation JOIN_SPRITE = new ResourceLocation("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    static final Component INCOMPATIBLE_VERSION_TOOLTIP = Component.translatable("selectWorld.incompatible.tooltip").withStyle(ChatFormatting.RED);
    static final Component WORLD_EXPERIMENTAL = Component.translatable("selectWorld.experimental");
    private final SelectWorldScreen screen;
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    @Nullable
    private List<LevelSummary> currentlyDisplayedLevels;
    private String filter;
    private final WorldSelectionList.LoadingHeader loadingHeader;

    public WorldSelectionList(
        SelectWorldScreen param0, Minecraft param1, int param2, int param3, int param4, int param5, String param6, @Nullable WorldSelectionList param7
    ) {
        super(param1, param2, param3, param4, param5);
        this.screen = param0;
        this.loadingHeader = new WorldSelectionList.LoadingHeader(param1);
        this.filter = param6;
        if (param7 != null) {
            this.pendingLevels = param7.pendingLevels;
        } else {
            this.pendingLevels = this.loadLevels();
        }

        this.handleNewLevels(this.pollLevelsIgnoreErrors());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(WorldSelectionList.Entry::close);
        super.clearEntries();
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
    public boolean keyPressed(int param0, int param1, int param2) {
        if (CommonInputs.selected(param0)) {
            Optional<WorldSelectionList.WorldListEntry> var0 = this.getSelectedOpt();
            if (var0.isPresent()) {
                if (var0.get().canJoin()) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    var0.get().joinWorld();
                }

                return true;
            }
        }

        return super.keyPressed(param0, param1, param2);
    }

    @Override
    public void renderWidget(GuiGraphics param0, int param1, int param2, float param3) {
        List<LevelSummary> var0 = this.pollLevelsIgnoreErrors();
        if (var0 != this.currentlyDisplayedLevels) {
            this.handleNewLevels(var0);
        }

        super.renderWidget(param0, param1, param2, param3);
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
        this.setScrollAmount(this.getScrollAmount());
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
        this.screen.updateButtonStatus(param0 instanceof WorldSelectionList.WorldListEntry var0 ? var0.summary : null);
    }

    public Optional<WorldSelectionList.WorldListEntry> getSelectedOpt() {
        WorldSelectionList.Entry var0 = this.getSelected();
        return var0 instanceof WorldSelectionList.WorldListEntry var1 ? Optional.of(var1) : Optional.empty();
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput param0) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(param0);
        } else {
            super.updateWidgetNarration(param0);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public abstract static class Entry extends ObjectSelectionList.Entry<WorldSelectionList.Entry> implements AutoCloseable {
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
            param0.drawString(this.minecraft.font, var2, var3, var4, -8355712, false);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public final class WorldListEntry extends WorldSelectionList.Entry implements AutoCloseable {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        final LevelSummary summary;
        private final FaviconTexture icon;
        @Nullable
        private Path iconFile;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList param1, LevelSummary param2) {
            this.minecraft = param1.minecraft;
            this.screen = param1.getScreen();
            this.summary = param2;
            this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), param2.getLevelId());
            this.iconFile = param2.getIcon();
            this.validateIconFile();
            this.loadIcon();
        }

        private void validateIconFile() {
            if (this.iconFile != null) {
                try {
                    BasicFileAttributes var0 = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                    if (var0.isSymbolicLink()) {
                        List<ForbiddenSymlinkInfo> var1 = this.minecraft.directoryValidator().validateSymlink(this.iconFile);
                        if (!var1.isEmpty()) {
                            WorldSelectionList.LOGGER.warn("{}", ContentValidationException.getMessage(this.iconFile, var1));
                            this.iconFile = null;
                        } else {
                            var0 = Files.readAttributes(this.iconFile, BasicFileAttributes.class);
                        }
                    }

                    if (!var0.isRegularFile()) {
                        this.iconFile = null;
                    }
                } catch (NoSuchFileException var31) {
                    this.iconFile = null;
                } catch (IOException var4) {
                    WorldSelectionList.LOGGER.error("could not validate symlink", (Throwable)var4);
                    this.iconFile = null;
                }

            }
        }

        @Override
        public Component getNarration() {
            Component var0 = Component.translatable(
                "narrator.select.world_info",
                this.summary.getLevelName(),
                Component.translationArg(new Date(this.summary.getLastPlayed())),
                this.summary.getInfo()
            );
            if (this.summary.isLocked()) {
                var0 = CommonComponents.joinForNarration(var0, WorldSelectionList.WORLD_LOCKED_TOOLTIP);
            }

            if (this.summary.isExperimental()) {
                var0 = CommonComponents.joinForNarration(var0, WorldSelectionList.WORLD_EXPERIMENTAL);
            }

            return Component.translatable("narrator.select", var0);
        }

        @Override
        public void render(GuiGraphics param0, int param1, int param2, int param3, int param4, int param5, int param6, int param7, boolean param8, float param9) {
            String var0 = this.summary.getLevelName();
            String var1 = this.summary.getLevelId();
            long var2 = this.summary.getLastPlayed();
            if (var2 != -1L) {
                var1 = var1 + " (" + WorldSelectionList.DATE_FORMAT.format(Instant.ofEpochMilli(var2)) + ")";
            }

            if (StringUtils.isEmpty(var0)) {
                var0 = I18n.get("selectWorld.world") + " " + (param1 + 1);
            }

            Component var3 = this.summary.getInfo();
            param0.drawString(this.minecraft.font, var0, param3 + 32 + 3, param2 + 1, 16777215, false);
            param0.drawString(this.minecraft.font, var1, param3 + 32 + 3, param2 + 9 + 3, -8355712, false);
            param0.drawString(this.minecraft.font, var3, param3 + 32 + 3, param2 + 9 + 9 + 3, -8355712, false);
            RenderSystem.enableBlend();
            param0.blit(this.icon.textureLocation(), param3, param2, 0.0F, 0.0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            if (this.minecraft.options.touchscreen().get() || param8) {
                param0.fill(param3, param2, param3 + 32, param2 + 32, -1601138544);
                int var4 = param6 - param3;
                boolean var5 = var4 < 32;
                ResourceLocation var6 = var5 ? WorldSelectionList.JOIN_HIGHLIGHTED_SPRITE : WorldSelectionList.JOIN_SPRITE;
                ResourceLocation var7 = var5 ? WorldSelectionList.WARNING_HIGHLIGHTED_SPRITE : WorldSelectionList.WARNING_SPRITE;
                ResourceLocation var8 = var5 ? WorldSelectionList.ERROR_HIGHLIGHTED_SPRITE : WorldSelectionList.ERROR_SPRITE;
                ResourceLocation var9 = var5 ? WorldSelectionList.MARKED_JOIN_HIGHLIGHTED_SPRITE : WorldSelectionList.MARKED_JOIN_SPRITE;
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary || this.summary instanceof LevelSummary.CorruptedLevelSummary) {
                    param0.blitSprite(var8, param3, param2, 32, 32);
                    param0.blitSprite(var9, param3, param2, 32, 32);
                    return;
                }

                if (this.summary.isLocked()) {
                    param0.blitSprite(var8, param3, param2, 32, 32);
                    if (var5) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_LOCKED_TOOLTIP, 175));
                    }
                } else if (this.summary.requiresManualConversion()) {
                    param0.blitSprite(var8, param3, param2, 32, 32);
                    if (var5) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.WORLD_REQUIRES_CONVERSION, 175));
                    }
                } else if (!this.summary.isCompatible()) {
                    param0.blitSprite(var8, param3, param2, 32, 32);
                    if (var5) {
                        this.screen.setTooltipForNextRenderPass(this.minecraft.font.split(WorldSelectionList.INCOMPATIBLE_VERSION_TOOLTIP, 175));
                    }
                } else if (this.summary.shouldBackup()) {
                    param0.blitSprite(var9, param3, param2, 32, 32);
                    if (this.summary.isDowngrade()) {
                        param0.blitSprite(var8, param3, param2, 32, 32);
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
                        param0.blitSprite(var7, param3, param2, 32, 32);
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
                    param0.blitSprite(var6, param3, param2, 32, 32);
                }
            }

        }

        @Override
        public boolean mouseClicked(double param0, double param1, int param2) {
            if (!this.summary.primaryActionActive()) {
                return true;
            } else {
                WorldSelectionList.this.setSelected((WorldSelectionList.Entry)this);
                if (!(param0 - (double)WorldSelectionList.this.getRowLeft() <= 32.0) && Util.getMillis() - this.lastClickTime >= 250L) {
                    this.lastClickTime = Util.getMillis();
                    return true;
                } else {
                    if (this.canJoin()) {
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        this.joinWorld();
                    }

                    return true;
                }
            }
        }

        public boolean canJoin() {
            return this.summary.primaryActionActive();
        }

        public void joinWorld() {
            if (this.summary.primaryActionActive()) {
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
                    this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                } else {
                    this.minecraft.createWorldOpenFlows().checkForBackupAndLoad(this.summary.getLevelId(), () -> {
                        WorldSelectionList.this.reloadWorldList();
                        this.minecraft.setScreen(this.screen);
                    });
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

            LevelStorageSource.LevelStorageAccess var1;
            try {
                var1 = this.minecraft.getLevelSource().validateAndCreateAccess(var0);
            } catch (IOException var6) {
                SystemToast.onWorldAccessFailure(this.minecraft, var0);
                WorldSelectionList.LOGGER.error("Failed to access level {}", var0, var6);
                WorldSelectionList.this.reloadWorldList();
                return;
            } catch (ContentValidationException var71) {
                WorldSelectionList.LOGGER.warn("{}", var71.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }

            EditWorldScreen var5;
            try {
                var5 = EditWorldScreen.create(this.minecraft, var1, param1 -> {
                    var1.safeClose();
                    if (param1) {
                        WorldSelectionList.this.reloadWorldList();
                    }

                    this.minecraft.setScreen(this.screen);
                });
            } catch (NbtException | ReportedNbtException | IOException var5) {
                var1.safeClose();
                SystemToast.onWorldAccessFailure(this.minecraft, var0);
                WorldSelectionList.LOGGER.error("Failed to load world data {}", var0, var5);
                WorldSelectionList.this.reloadWorldList();
                return;
            }

            this.minecraft.setScreen(var5);
        }

        public void recreateWorld() {
            this.queueLoadScreen();

            try (LevelStorageSource.LevelStorageAccess var0 = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId())) {
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
            } catch (ContentValidationException var8) {
                WorldSelectionList.LOGGER.warn("{}", var8.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
            } catch (Exception var9) {
                WorldSelectionList.LOGGER.error("Unable to recreate world", (Throwable)var9);
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

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean var0 = this.iconFile != null && Files.isRegularFile(this.iconFile);
            if (var0) {
                try (InputStream var1 = Files.newInputStream(this.iconFile)) {
                    this.icon.upload(NativeImage.read(var1));
                } catch (Throwable var7) {
                    WorldSelectionList.LOGGER.error("Invalid icon for world {}", this.summary.getLevelId(), var7);
                    this.iconFile = null;
                }
            } else {
                this.icon.clear();
            }

        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelName() {
            return this.summary.getLevelName();
        }
    }
}
