package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockDestructionProgress {
    private final int id;
    private final BlockPos pos;
    private int progress;
    private int updatedRenderTick;

    public BlockDestructionProgress(int param0, BlockPos param1) {
        this.id = param0;
        this.pos = param1;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public void setProgress(int param0) {
        if (param0 > 10) {
            param0 = 10;
        }

        this.progress = param0;
    }

    public int getProgress() {
        return this.progress;
    }

    public void updateTick(int param0) {
        this.updatedRenderTick = param0;
    }

    public int getUpdatedRenderTick() {
        return this.updatedRenderTick;
    }
}
