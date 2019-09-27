package net.minecraft.server.level;

import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BlockDestructionProgress implements Comparable<BlockDestructionProgress> {
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

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            BlockDestructionProgress var0 = (BlockDestructionProgress)param0;
            return this.id == var0.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    public int compareTo(BlockDestructionProgress param0) {
        return this.progress != param0.progress ? Integer.compare(this.progress, param0.progress) : Integer.compare(this.id, param0.id);
    }
}
