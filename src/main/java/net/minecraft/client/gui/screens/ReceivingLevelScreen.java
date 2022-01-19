package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReceivingLevelScreen extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = new TranslatableComponent("multiplayer.downloadingTerrain");
    private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 2000L;
    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped = false;
    private final long createdAt = System.currentTimeMillis();

    public ReceivingLevelScreen() {
        super(NarratorChatListener.NO_TITLE);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void render(PoseStack param0, int param1, int param2, float param3) {
        this.renderDirtBackground(0);
        drawCenteredString(param0, this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(param0, param1, param2, param3);
    }

    @Override
    public void tick() {
        if ((this.oneTickSkipped || System.currentTimeMillis() > this.createdAt + 2000L)
            && this.minecraft.levelRenderer.isChunkCompiled(this.minecraft.player.blockPosition())) {
            this.onClose();
        }

        if (this.loadingPacketsReceived) {
            this.oneTickSkipped = true;
        }

    }

    public void loadingPacketsReceived() {
        this.loadingPacketsReceived = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
