package net.minecraft.client.multiplayer;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LevelLoadStatusManager {
    private final LocalPlayer player;
    private final ClientLevel level;
    private final LevelRenderer levelRenderer;
    private LevelLoadStatusManager.Status status = LevelLoadStatusManager.Status.WAITING_FOR_SERVER;

    public LevelLoadStatusManager(LocalPlayer param0, ClientLevel param1, LevelRenderer param2) {
        this.player = param0;
        this.level = param1;
        this.levelRenderer = param2;
    }

    public void tick() {
        switch(this.status) {
            case WAITING_FOR_PLAYER_CHUNK:
                BlockPos var0 = this.player.blockPosition();
                boolean var1 = this.level.isOutsideBuildHeight(var0.getY());
                if (var1 || this.levelRenderer.isSectionCompiled(var0) || this.player.isSpectator() || !this.player.isAlive()) {
                    this.status = LevelLoadStatusManager.Status.LEVEL_READY;
                }
            case WAITING_FOR_SERVER:
            case LEVEL_READY:
        }
    }

    public boolean levelReady() {
        return this.status == LevelLoadStatusManager.Status.LEVEL_READY;
    }

    public void loadingPacketsReceived() {
        if (this.status == LevelLoadStatusManager.Status.WAITING_FOR_SERVER) {
            this.status = LevelLoadStatusManager.Status.WAITING_FOR_PLAYER_CHUNK;
        }

    }

    @OnlyIn(Dist.CLIENT)
    static enum Status {
        WAITING_FOR_SERVER,
        WAITING_FOR_PLAYER_CHUNK,
        LEVEL_READY;
    }
}
