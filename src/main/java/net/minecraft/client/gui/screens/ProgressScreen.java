package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import javax.annotation.Nullable;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
        this.header = param0;
        this.progressStage(new TranslatableComponent("progress.working"));
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
    public void render(PoseStack param0, int param1, int param2, float param3) {
        if (this.stop) {
            if (!this.minecraft.isConnectedToRealms()) {
                this.minecraft.setScreen(null);
            }

        } else {
            this.renderBackground(param0);
            if (this.header != null) {
                this.drawCenteredString(param0, this.font, this.header, this.width / 2, 70, 16777215);
            }

            if (this.stage != null && this.progress != 0) {
                this.drawCenteredString(
                    param0, this.font, new TextComponent("").append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, 16777215
                );
            }

            super.render(param0, param1, param2, param3);
        }
    }
}
