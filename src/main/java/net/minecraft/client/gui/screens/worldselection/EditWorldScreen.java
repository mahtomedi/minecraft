package net.minecraft.client.gui.screens.worldselection;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.ChatFormatting;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EditWorldScreen extends Screen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component NAME_LABEL = Component.translatable("selectWorld.enterName").withStyle(ChatFormatting.GRAY);
    private static final Component RESET_ICON_BUTTON = Component.translatable("selectWorld.edit.resetIcon");
    private static final Component FOLDER_BUTTON = Component.translatable("selectWorld.edit.openFolder");
    private static final Component BACKUP_BUTTON = Component.translatable("selectWorld.edit.backup");
    private static final Component BACKUP_FOLDER_BUTTON = Component.translatable("selectWorld.edit.backupFolder");
    private static final Component OPTIMIZE_BUTTON = Component.translatable("selectWorld.edit.optimize");
    private static final Component OPTIMIZE_TITLE = Component.translatable("optimizeWorld.confirm.title");
    private static final Component OPTIMIIZE_DESCRIPTION = Component.translatable("optimizeWorld.confirm.description");
    private static final Component SAVE_BUTTON = Component.translatable("selectWorld.edit.save");
    private static final int DEFAULT_WIDTH = 200;
    private static final int VERTICAL_SPACING = 4;
    private static final int HALF_WIDTH = 98;
    private final LinearLayout layout = LinearLayout.vertical().spacing(5);
    private final BooleanConsumer callback;
    private final LevelStorageSource.LevelStorageAccess levelAccess;

    public static EditWorldScreen create(Minecraft param0, LevelStorageSource.LevelStorageAccess param1, BooleanConsumer param2) throws IOException {
        LevelSummary var0 = param1.getSummary(param1.getDataTag());
        return new EditWorldScreen(param0, param1, var0.getLevelName(), param2);
    }

    private EditWorldScreen(Minecraft param0, LevelStorageSource.LevelStorageAccess param1, String param2, BooleanConsumer param3) {
        super(Component.translatable("selectWorld.edit.title"));
        this.callback = param3;
        this.levelAccess = param1;
        Font var0 = param0.font;
        this.layout.addChild(new SpacerElement(200, 20));
        this.layout.addChild(new StringWidget(NAME_LABEL, var0));
        EditBox var1 = this.layout.addChild(new EditBox(var0, 200, 20, NAME_LABEL));
        var1.setValue(param2);
        LinearLayout var2 = LinearLayout.horizontal().spacing(4);
        Button var3 = var2.addChild(Button.builder(SAVE_BUTTON, param1x -> this.onRename(var1.getValue())).width(98).build());
        var2.addChild(Button.builder(CommonComponents.GUI_CANCEL, param0x -> this.onClose()).width(98).build());
        var1.setResponder(param1x -> var3.active = !Util.isBlank(param1x));
        this.layout.addChild(Button.builder(RESET_ICON_BUTTON, param1x -> {
            param1.getIconFile().ifPresent(param0x -> FileUtils.deleteQuietly(param0x.toFile()));
            param1x.active = false;
        }).width(200).build()).active = param1.getIconFile().filter(param0x -> Files.isRegularFile(param0x)).isPresent();
        this.layout
            .addChild(
                Button.builder(FOLDER_BUTTON, param1x -> Util.getPlatform().openFile(param1.getLevelPath(LevelResource.ROOT).toFile())).width(200).build()
            );
        this.layout.addChild(Button.builder(BACKUP_BUTTON, param1x -> {
            boolean var0x = makeBackupAndShowToast(param1);
            this.callback.accept(!var0x);
        }).width(200).build());
        this.layout.addChild(Button.builder(BACKUP_FOLDER_BUTTON, param1x -> {
            LevelStorageSource var0x = param0.getLevelSource();
            Path var1x = var0x.getBackupPath();

            try {
                FileUtil.createDirectoriesSafe(var1x);
            } catch (IOException var5x) {
                throw new RuntimeException(var5x);
            }

            Util.getPlatform().openFile(var1x.toFile());
        }).width(200).build());
        this.layout
            .addChild(
                Button.builder(OPTIMIZE_BUTTON, param2x -> param0.setScreen(new BackupConfirmScreen(() -> param0.setScreen(this), (param2xx, param3x) -> {
                        if (param2xx) {
                            makeBackupAndShowToast(param1);
                        }
        
                        param0.setScreen(OptimizeWorldScreen.create(param0, this.callback, param0.getFixerUpper(), param1, param3x));
                    }, OPTIMIZE_TITLE, OPTIMIIZE_DESCRIPTION, true))).width(200).build()
            );
        this.layout.addChild(new SpacerElement(200, 20));
        this.layout.addChild(var2);
        this.setInitialFocus(var1);
        this.layout.visitWidgets(param1x -> {
        });
    }

    @Override
    protected void init() {
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    private void onRename(String param0) {
        try {
            this.levelAccess.renameLevel(param0);
        } catch (NbtException | ReportedNbtException | IOException var3) {
            LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), var3);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
        }

        this.callback.accept(true);
    }

    public static boolean makeBackupAndShowToast(LevelStorageSource.LevelStorageAccess param0) {
        long var0 = 0L;
        IOException var1 = null;

        try {
            var0 = param0.makeWorldBackup();
        } catch (IOException var61) {
            var1 = var61;
        }

        if (var1 != null) {
            Component var3 = Component.translatable("selectWorld.edit.backupFailed");
            Component var4 = Component.literal(var1.getMessage());
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP, var3, var4));
            return false;
        } else {
            Component var5 = Component.translatable("selectWorld.edit.backupCreated", param0.getLevelId());
            Component var6 = Component.translatable("selectWorld.edit.backupSize", Mth.ceil((double)var0 / 1048576.0));
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastId.WORLD_BACKUP, var5, var6));
            return true;
        }
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);
    }
}
