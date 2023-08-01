package net.minecraft.client.gui.screens;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ReceivingLevelScreen extends Screen {
    private static final Component DOWNLOADING_TERRAIN_TEXT = Component.translatable("multiplayer.downloadingTerrain");
    private static final long CHUNK_LOADING_START_WAIT_LIMIT_MS = 30000L;
    private boolean loadingPacketsReceived = false;
    private boolean oneTickSkipped = false;
    private final long createdAt = System.currentTimeMillis();

    public ReceivingLevelScreen() {
        super(GameNarrator.NO_TITLE);
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
    public void render(GuiGraphics param0, int param1, int param2, float param3) {
        super.render(param0, param1, param2, param3);
        param0.drawCenteredString(this.font, DOWNLOADING_TERRAIN_TEXT, this.width / 2, this.height / 2 - 50, 16777215);
    }

    @Override
    public void renderBackground(GuiGraphics param0, int param1, int param2, float param3) {
        this.renderDirtBackground(param0);
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() > this.createdAt + 30000L) {
            this.onClose();
        } else {
            if (this.oneTickSkipped) {
                if (this.minecraft.player == null) {
                    return;
                }

                BlockPos var0 = this.minecraft.player.blockPosition();
                boolean var1 = this.minecraft.level != null && this.minecraft.level.isOutsideBuildHeight(var0.getY());
                if (var1 || this.minecraft.levelRenderer.isSectionCompiled(var0) || this.minecraft.player.isSpectator() || !this.minecraft.player.isAlive()) {
                    this.onClose();
                }
            } else {
                this.oneTickSkipped = this.loadingPacketsReceived;
            }

        }
    }

    @Override
    public void onClose() {
        this.minecraft.getNarrator().sayNow(Component.translatable("narrator.ready_to_play"));
        super.onClose();
    }

    public void loadingPacketsReceived() {
        this.loadingPacketsReceived = true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
