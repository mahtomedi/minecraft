package net.minecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BackupConfirmScreen extends Screen {
    private final Runnable onCancel;
    protected final BackupConfirmScreen.Listener onProceed;
    private final Component description;
    private final boolean promptForCacheErase;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    protected int id;
    private Checkbox eraseCache;

    public BackupConfirmScreen(Runnable param0, BackupConfirmScreen.Listener param1, Component param2, Component param3, boolean param4) {
        super(param2);
        this.onCancel = param0;
        this.onProceed = param1;
        this.description = param3;
        this.promptForCacheErase = param4;
    }

    @Override
    protected void init() {
        super.init();
        this.message = MultiLineLabel.create(this.font, this.description, this.width - 50);
        int var0 = (this.message.getLineCount() + 1) * 9;
        this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.backupJoinConfirmButton"), param0 -> this.onProceed.proceed(true, this.eraseCache.selected()))
                .bounds(this.width / 2 - 155, 100 + var0, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(Component.translatable("selectWorld.backupJoinSkipButton"), param0 -> this.onProceed.proceed(false, this.eraseCache.selected()))
                .bounds(this.width / 2 - 155 + 160, 100 + var0, 150, 20)
                .build()
        );
        this.addRenderableWidget(
            Button.builder(CommonComponents.GUI_CANCEL, param0 -> this.onCancel.run()).bounds(this.width / 2 - 155 + 80, 124 + var0, 150, 20).build()
        );
        this.eraseCache = Checkbox.builder(Component.translatable("selectWorld.backupEraseCache"), this.font).pos(this.width / 2 - 155 + 80, 76 + var0).build();
        if (this.promptForCacheErase) {
            this.addRenderableWidget(this.eraseCache);
        }

    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, this.title, this.width / 2, 50, 16777215);
        this.message.renderCentered(param0, this.width / 2, 70);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public boolean keyPressed(int param0, int param1, int param2) {
        if (param0 == 256) {
            this.onCancel.run();
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
