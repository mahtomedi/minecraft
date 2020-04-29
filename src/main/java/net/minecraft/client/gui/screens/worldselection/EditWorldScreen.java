package net.minecraft.client.gui.screens.worldselection;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@OnlyIn(Dist.CLIENT)
public class EditWorldScreen extends Screen {
    private static final Logger LOGGER = LogManager.getLogger();
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final LevelStorageSource.LevelStorageAccess levelAccess;

    public EditWorldScreen(BooleanConsumer param0, LevelStorageSource.LevelStorageAccess param1) {
        super(new TranslatableComponent("selectWorld.edit.title"));
        this.callback = param0;
        this.levelAccess = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button var0 = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.resetIcon"), param0 -> {
                FileUtils.deleteQuietly(this.levelAccess.getIconFile());
                param0.active = false;
            })
        );
        this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 48 + 5,
                200,
                20,
                new TranslatableComponent("selectWorld.edit.openFolder"),
                param0 -> Util.getPlatform().openFile(this.levelAccess.getLevelPath(LevelResource.ROOT).toFile())
            )
        );
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backup"), param0 -> {
            boolean var0x = makeBackupAndShowToast(this.levelAccess);
            this.callback.accept(!var0x);
        }));
        this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, new TranslatableComponent("selectWorld.edit.backupFolder"), param0 -> {
                LevelStorageSource var0x = this.minecraft.getLevelSource();
                Path var1x = var0x.getBackupPath();
    
                try {
                    Files.createDirectories(Files.exists(var1x) ? var1x.toRealPath() : var1x);
                } catch (IOException var5) {
                    throw new RuntimeException(var5);
                }
    
                Util.getPlatform().openFile(var1x.toFile());
            })
        );
        this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 120 + 5,
                200,
                20,
                new TranslatableComponent("selectWorld.edit.optimize"),
                param0 -> this.minecraft.setScreen(new BackupConfirmScreen(this, (param0x, param1) -> {
                        if (param0x) {
                            makeBackupAndShowToast(this.levelAccess);
                        }
        
                        this.minecraft.setScreen(OptimizeWorldScreen.create(this.callback, this.minecraft.getFixerUpper(), this.levelAccess, param1));
                    }, new TranslatableComponent("optimizeWorld.confirm.title"), new TranslatableComponent("optimizeWorld.confirm.description"), true))
            )
        );
        this.renameButton = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, new TranslatableComponent("selectWorld.edit.save"), param0 -> this.onRename())
        );
        this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, CommonComponents.GUI_CANCEL, param0 -> this.callback.accept(false)));
        var0.active = this.levelAccess.getIconFile().isFile();
        WorldData var1 = this.levelAccess.getDataTag();
        String var2 = var1 == null ? "" : var1.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 53, 200, 20, new TranslatableComponent("selectWorld.enterName"));
        this.nameEdit.setValue(var2);
        this.nameEdit.setResponder(param0 -> this.renameButton.active = !param0.trim().isEmpty());
        this.children.add(this.nameEdit);
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void resize(Minecraft param0, int param1, int param2) {
        String var0 = this.nameEdit.getValue();
        this.init(param0, param1, param2);
        this.nameEdit.setValue(var0);
    }

    @Override
    public void onClose() {
        this.callback.accept(false);
    }

    @Override
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onRename() {
        try {
            this.levelAccess.renameLevel(this.nameEdit.getValue().trim());
            this.callback.accept(true);
        } catch (IOException var2) {
            LOGGER.error("Failed to access world '{}'", this.levelAccess.getLevelId(), var2);
            SystemToast.onWorldAccessFailure(this.minecraft, this.levelAccess.getLevelId());
            this.callback.accept(true);
        }

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
            Component var3 = new TranslatableComponent("selectWorld.edit.backupFailed");
            Component var4 = new TextComponent(var1.getMessage());
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, var3, var4));
            return false;
        } else {
            Component var5 = new TranslatableComponent("selectWorld.edit.backupCreated", param0.getLevelId());
            Component var6 = new TranslatableComponent("selectWorld.edit.backupSize", Mth.ceil((double)var0 / 1048576.0));
            Minecraft.getInstance().getToasts().addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, var5, var6));
            return true;
        }
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 20, 16777215);
        this.drawString(param0, this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 40, 10526880);
        this.nameEdit.render(param0, param1, param2, param3);
        super.render(param0, param1, param2, param3);
    }
}
