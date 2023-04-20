package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
    @Nullable
    private Component header;
    @Nullable
    private Component stage;
    private int progress;
    private boolean stop;
    private final boolean clearScreenAfterStop;

    public ProgressScreen(boolean param0) {
        super(GameNarrator.NO_TITLE);
        this.clearScreenAfterStop = param0;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void progressStartNoAbort(Component param0) {
        this.progressStart(param0);
    }

    @Override
    public void progressStart(Component param0) {
        this.header = param0;
        this.progressStage(Component.translatable("progress.working"));
    }

    @Override
    public void progressStage(Component param0) {
        this.stage = param0;
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStagePercentage(int param0) {
        this.progress = param0;
    }

    @Override
    public void stop() {
        this.stop = true;
    }

    @Override
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        if (this.stop) {
            if (this.clearScreenAfterStop) {
                this.minecraft.setScreen(null);
            }

        } else {
            this.renderBackground(param0);
            if (this.header != null) {
                param0.drawCenteredString(this.font, this.header, this.width / 2, 70, 16777215);
            }

            if (this.stage != null && this.progress != 0) {
                param0.drawCenteredString(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215);
            }

            super.render(param0, param1, param2, param3);
        }
    }
}
