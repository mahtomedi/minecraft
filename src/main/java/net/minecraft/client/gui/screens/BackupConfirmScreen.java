package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackupConfirmScreen extends Screen {
    private final Screen lastScreen;
    protected final BackupConfirmScreen.Listener listener;
    private final Component description;
    private final boolean promptForCacheErase;
    private final List<Component> lines = Lists.newArrayList();
    private Checkbox eraseCache;

    public BackupConfirmScreen(Screen param0, BackupConfirmScreen.Listener param1, Component param2, Component param3, boolean param4) {
        super(param2);
        this.lastScreen = param0;
        this.listener = param1;
        this.description = param3;
        this.promptForCacheErase = param4;
    }

    @Override
    protected void init() {
        super.init();
        this.lines.clear();
        this.lines.addAll(this.font.split(this.description, this.width - 50));
        int var0 = (this.lines.size() + 1) * 9;
        this.addButton(
            new Button(
                this.width / 2 - 155,
                100 + var0,
                150,
                20,
                new TranslatableComponent("selectWorld.backupJoinConfirmButton"),
                param0 -> this.listener.proceed(true, this.eraseCache.selected())
            )
        );
        this.addButton(
            new Button(
                this.width / 2 - 155 + 160,
                100 + var0,
                150,
                20,
                new TranslatableComponent("selectWorld.backupJoinSkipButton"),
                param0 -> this.listener.proceed(false, this.eraseCache.selected())
            )
        );
        this.addButton(
            new Button(this.width / 2 - 155 + 80, 124 + var0, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + var0, 150, 20, new TranslatableComponent("selectWorld.backupEraseCache"), false);
        if (this.promptForCacheErase) {
            this.addButton(this.eraseCache);
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        this.drawCenteredString(param0, this.font, this.title, this.width / 2, 50, 16777215);
        int var0 = 70;

        for(Component var1 : this.lines) {
            this.drawCenteredString(param0, this.font, var1, this.width / 2, var0, 16777215);
            var0 += 9;
        }

        super.render(param0, param1, param2, param3);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
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

    @OnlyIn(Dist.CLIENT)
    public interface Listener {
        void proceed(boolean var1, boolean var2);
    }
}
