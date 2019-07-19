package net.minecraft.client.gui.screens;

import java.util.Objects;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ProgressListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ProgressScreen extends Screen implements ProgressListener {
    private String title = "";
    private String stage = "";
    private int progress;
    private boolean stop;

    public ProgressScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void progressStartNoAbort(Component param0) {
        this.progressStart(param0);
    }

    @Override
    public void progressStart(Component param0) {
        this.title = param0.getColoredString();
        this.progressStage(new TranslatableComponent("progress.working"));
    }

    @Override
    public void progressStage(Component param0) {
        this.stage = param0.getColoredString();
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
    public void render(int param0, int param1, float param2) {
        if (this.stop) {
            if (!this.minecraft.isConnectedToRealms()) {
                this.minecraft.setScreen(null);
            }

        } else {
            this.renderBackground();
            this.drawCenteredString(this.font, this.title, this.width / 2, 70, 16777215);
            if (!Objects.equals(this.stage, "") && this.progress != 0) {
                this.drawCenteredString(this.font, this.stage + " " + this.progress + "%", this.width / 2, 90, 16777215);
            }

            super.render(param0, param1, param2);
        }
    }
}
