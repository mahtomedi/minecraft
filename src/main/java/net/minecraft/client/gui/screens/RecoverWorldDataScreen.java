package net.minecraft.client.gui.screens;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class RecoverWorldDataScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int SCREEN_SIDE_MARGIN = 25;
    private static final Component TITLE = Component.translatable("recover_world.title").withStyle(ChatFormatting.BOLD);
    private static final Component BUGTRACKER_BUTTON = Component.translatable("recover_world.bug_tracker");
    private static final Component RESTORE_BUTTON = Component.translatable("recover_world.restore");
    private static final Component NO_FALLBACK_TOOLTIP = Component.translatable("recover_world.no_fallback");
    private static final Component DONE_TITLE = Component.translatable("recover_world.done.title");
    private static final Component DONE_SUCCESS = Component.translatable("recover_world.done.success");
    private static final Component DONE_FAILED = Component.translatable("recover_world.done.failed");
    private static final Component NO_ISSUES = Component.translatable("recover_world.issue.none").withStyle(ChatFormatting.GREEN);
    private static final Component MISSING_FILE = Component.translatable("recover_world.issue.missing_file").withStyle(ChatFormatting.RED);
    private final BooleanConsumer callback;
    private final LinearLayout layout = LinearLayout.vertical().spacing(10);
    private final Component message;
    private final MultiLineTextWidget messageWidget;
    private final MultiLineTextWidget issuesWidget;
    private final LevelStorageSource.LevelStorageAccess storageAccess;

    public RecoverWorldDataScreen(Minecraft param0, BooleanConsumer param1, LevelStorageSource.LevelStorageAccess param2) {
        super(TITLE);
        this.callback = param1;
        this.message = Component.translatable("recover_world.message", Component.literal(param2.getLevelId()).withStyle(ChatFormatting.GRAY));
        this.messageWidget = new MultiLineTextWidget(this.message, param0.font);
        this.storageAccess = param2;
        Exception var0 = this.collectIssue(param2, false);
        Exception var1 = this.collectIssue(param2, true);
        Component var2 = Component.empty().append(this.buildInfo(param2, false, var0)).append("\n").append(this.buildInfo(param2, true, var1));
        this.issuesWidget = new MultiLineTextWidget(var2, param0.font);
        boolean var3 = var0 != null && var1 == null;
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(new StringWidget(this.title, param0.font));
        this.layout.addChild(this.messageWidget.setCentered(true));
        this.layout.addChild(this.issuesWidget);
        LinearLayout var4 = LinearLayout.horizontal().spacing(5);
        var4.addChild(Button.builder(BUGTRACKER_BUTTON, ConfirmLinkScreen.confirmLink(this, "https://aka.ms/snapshotbugs?ref=game")).size(120, 20).build());
        var4.addChild(
                Button.builder(RESTORE_BUTTON, param1x -> this.attemptRestore(param0))
                    .size(120, 20)
                    .tooltip(var3 ? null : Tooltip.create(NO_FALLBACK_TOOLTIP))
                    .build()
            )
            .active = var3;
        this.layout.addChild(var4);
        this.layout.addChild(Button.builder(CommonComponents.GUI_BACK, param0x -> this.onClose()).size(120, 20).build());
        this.layout.visitWidgets(this::addRenderableWidget);
    }

    private void attemptRestore(Minecraft param0) {
        Exception var0 = this.collectIssue(this.storageAccess, false);
        Exception var1 = this.collectIssue(this.storageAccess, true);
        if (var0 != null && var1 == null) {
            param0.forceSetScreen(new GenericDirtMessageScreen(Component.translatable("recover_world.restoring")));
            EditWorldScreen.makeBackupAndShowToast(this.storageAccess);
            if (this.storageAccess.restoreLevelDataFromOld()) {
                param0.setScreen(new ConfirmScreen(this.callback, DONE_TITLE, DONE_SUCCESS, CommonComponents.GUI_CONTINUE, CommonComponents.GUI_BACK));
            } else {
                param0.setScreen(new AlertScreen(() -> this.callback.accept(false), DONE_TITLE, DONE_FAILED));
            }

        } else {
            LOGGER.error(
                "Failed to recover world, files not as expected. level.dat: {}, level.dat_old: {}",
                var0 != null ? var0.getMessage() : "no issues",
                var1 != null ? var1.getMessage() : "no issues"
            );
            param0.setScreen(new AlertScreen(() -> this.callback.accept(false), DONE_TITLE, DONE_FAILED));
        }
    }

    private Component buildInfo(LevelStorageSource.LevelStorageAccess param0, boolean param1, @Nullable Exception param2) {
        if (param1 && param2 instanceof FileNotFoundException) {
            return Component.empty();
        } else {
            MutableComponent var0 = Component.empty();
            Instant var1 = param0.getFileModificationTime(param1);
            MutableComponent var2 = var1 != null
                ? Component.literal(WorldSelectionList.DATE_FORMAT.format(var1))
                : Component.translatable("recover_world.state_entry.unknown");
            var0.append(Component.translatable("recover_world.state_entry", var2.withStyle(ChatFormatting.GRAY)));
            if (param2 == null) {
                var0.append(NO_ISSUES);
            } else if (param2 instanceof FileNotFoundException) {
                var0.append(MISSING_FILE);
            } else if (param2 instanceof ReportedNbtException) {
                var0.append(Component.literal(param2.getCause().toString()).withStyle(ChatFormatting.RED));
            } else {
                var0.append(Component.literal(param2.toString()).withStyle(ChatFormatting.RED));
            }

            return var0;
        }
    }

    @Nullable
    private Exception collectIssue(LevelStorageSource.LevelStorageAccess param0, boolean param1) {
        try {
            if (!param1) {
                param0.getSummary(param0.getDataTag());
            } else {
                param0.getSummary(param0.getDataTagFallback());
            }

            return null;
        } catch (NbtException | ReportedNbtException | IOException var4) {
            return var4;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.issuesWidget.setMaxWidth(this.width - 50);
        this.messageWidget.setMaxWidth(this.width - 50);
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), this.message);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }
}
