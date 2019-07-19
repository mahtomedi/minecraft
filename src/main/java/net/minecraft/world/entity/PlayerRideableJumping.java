package net.minecraft.world.entity;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface PlayerRideableJumping {
    @OnlyIn(Dist.CLIENT)
    void onPlayerJump(int var1);

    boolean canJump();

    void handleStartJump(int var1);

    void handleStopJump();
}
