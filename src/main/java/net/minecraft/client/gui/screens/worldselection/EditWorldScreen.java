package net.minecraft.client.gui.screens.worldselection;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.gui.screens.BackupConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.FileUtils;

@OnlyIn(Dist.CLIENT)
public class EditWorldScreen extends Screen {
    private Button renameButton;
    private final BooleanConsumer callback;
    private EditBox nameEdit;
    private final String levelId;

    public EditWorldScreen(BooleanConsumer param0, String param1) {
        super(new TranslatableComponent("selectWorld.edit.title"));
        this.callback = param0;
        this.levelId = param1;
    }

    @Override
    public void tick() {
        this.nameEdit.tick();
    }

    @Override
    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        Button var0 = this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 24 + 5, 200, 20, I18n.get("selectWorld.edit.resetIcon"), param0 -> {
            LevelStorageSource var0x = this.minecraft.getLevelSource();
            FileUtils.deleteQuietly(var0x.getFile(this.levelId, "icon.png"));
            param0.active = false;
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 48 + 5, 200, 20, I18n.get("selectWorld.edit.openFolder"), param0 -> {
            LevelStorageSource var0x = this.minecraft.getLevelSource();
            Util.getPlatform().openFile(var0x.getFile(this.levelId, "icon.png").getParentFile());
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 72 + 5, 200, 20, I18n.get("selectWorld.edit.backup"), param0 -> {
            LevelStorageSource var0x = this.minecraft.getLevelSource();
            makeBackupAndShowToast(var0x, this.levelId);
            this.callback.accept(false);
        }));
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 96 + 5, 200, 20, I18n.get("selectWorld.edit.backupFolder"), param0 -> {
            LevelStorageSource var0x = this.minecraft.getLevelSource();
            Path var1x = var0x.getBackupPath();

            try {
                Files.createDirectories(Files.exists(var1x) ? var1x.toRealPath() : var1x);
            } catch (IOException var5) {
                throw new RuntimeException(var5);
            }

            Util.getPlatform().openFile(var1x.toFile());
        }));
        this.addButton(
            new Button(
                this.width / 2 - 100,
                this.height / 4 + 120 + 5,
                200,
                20,
                I18n.get("selectWorld.edit.optimize"),
                param0 -> this.minecraft.setScreen(new BackupConfirmScreen(this, (param0x, param1) -> {
                        if (param0x) {
                            makeBackupAndShowToast(this.minecraft.getLevelSource(), this.levelId);
                        }
        
                        this.minecraft.setScreen(new OptimizeWorldScreen(this.callback, this.levelId, this.minecraft.getLevelSource(), param1));
                    }, new TranslatableComponent("optimizeWorld.confirm.title"), new TranslatableComponent("optimizeWorld.confirm.description"), true))
            )
        );
        this.renameButton = this.addButton(
            new Button(this.width / 2 - 100, this.height / 4 + 144 + 5, 98, 20, I18n.get("selectWorld.edit.save"), param0 -> this.onRename())
        );
        this.addButton(new Button(this.width / 2 + 2, this.height / 4 + 144 + 5, 98, 20, I18n.get("gui.cancel"), param0 -> this.callback.accept(false)));
        var0.active = this.minecraft.getLevelSource().getFile(this.levelId, "icon.png").isFile();
        LevelStorageSource var1 = this.minecraft.getLevelSource();
        LevelData var2 = var1.getDataTagFor(this.levelId);
        String var3 = var2 == null ? "" : var2.getLevelName();
        this.nameEdit = new EditBox(this.font, this.width / 2 - 100, 53, 200, 20, I18n.get("selectWorld.enterName"));
        this.nameEdit.setValue(var3);
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
    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
    }

    private void onRename() {
        LevelStorageSource var0 = this.minecraft.getLevelSource();
        var0.renameLevel(this.levelId, this.nameEdit.getValue().trim());
        this.callback.accept(true);
    }

    public static void makeBackupAndShowToast(LevelStorageSource param0, String param1) {
        ToastComponent var0 = Minecraft.getInstance().getToasts();
        long var1 = 0L;
        IOException var2 = null;

        try {
            var1 = param0.makeWorldBackup(param1);
        } catch (IOException var8) {
            var2 = var8;
        }

        Component var4;
        Component var5;
        if (var2 != null) {
            var4 = new TranslatableComponent("selectWorld.edit.backupFailed");
            var5 = new TextComponent(var2.getMessage());
        } else {
            var4 = new TranslatableComponent("selectWorld.edit.backupCreated", param1);
            var5 = new TranslatableComponent("selectWorld.edit.backupSize", Mth.ceil((double)var1 / 1048576.0));
        }

        var0.addToast(new SystemToast(SystemToast.SystemToastIds.WORLD_BACKUP, var4, var5));
    }

    @Override
    public void render(int param0, int param1, float param2) {
        this.renderBackground();
        this.drawCenteredString(this.font, this.title.getColoredString(), this.width / 2, 20, 16777215);
        this.drawString(this.font, I18n.get("selectWorld.enterName"), this.width / 2 - 100, 40, 10526880);
        this.nameEdit.render(param0, param1, param2);
        super.render(param0, param1, param2);
    }
}
