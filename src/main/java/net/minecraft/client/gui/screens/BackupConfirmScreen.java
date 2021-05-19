package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackupConfirmScreen extends Screen {
    @Nullable
    private final Screen lastScreen;
    protected final BackupConfirmScreen.Listener listener;
    private final Component description;
    private final boolean promptForCacheErase;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected int id;
    private Checkbox eraseCache;

    public BackupConfirmScreen(@Nullable Screen param0, BackupConfirmScreen.Listener param1, Component param2, Component param3, boolean param4) {
        super(param2);
        this.lastScreen = param0;
        this.listener = param1;
        this.description = param3;
        this.promptForCacheErase = param4;
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
        int var0 = (this.message.getLineCount() + 1) * 9;
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 155,
                100 + var0,
                150,
                20,
                new TranslatableComponent("selectWorld.backupJoinConfirmButton"),
                param0 -> this.listener.proceed(true, this.eraseCache.selected())
            )
        );
        this.addRenderableWidget(
            new Button(
                this.width / 2 - 155 + 160,
                100 + var0,
                150,
                20,
                new TranslatableComponent("selectWorld.backupJoinSkipButton"),
                param0 -> this.listener.proceed(false, this.eraseCache.selected())
            )
        );
        this.addRenderableWidget(
            new Button(this.width / 2 - 155 + 80, 124 + var0, 150, 20, CommonComponents.GUI_CANCEL, param0 -> this.minecraft.setScreen(this.lastScreen))
        );
        this.eraseCache = new Checkbox(this.width / 2 - 155 + 80, 76 + var0, 150, 20, new TranslatableComponent("selectWorld.backupEraseCache"), false);
        if (this.promptForCacheErase) {
            this.addRenderableWidget(this.eraseCache);
        }

    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderBackground(param0);
        drawCenteredString(param0, this.font, this.title, this.width / 2, 50, 16777215);
        this.message.renderCentered(param0, this.width / 2, 70);
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
