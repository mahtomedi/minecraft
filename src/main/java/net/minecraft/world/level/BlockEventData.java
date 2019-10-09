package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class BlockEventData {
    private final BlockPos pos;
    private final Block block;
    private final int paramA;
    private final int paramB;

    public BlockEventData(BlockPos param0, Block param1, int param2, int param3) {
        this.pos = param0;
        this.block = param1;
        this.paramA = param2;
        this.paramB = param3;
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Block getBlock() {
        return this.block;
    }

    public int getParamA() {
        return this.paramA;
    }

    public int getParamB() {
        return this.paramB;
    }

    @Override
    public boolean equals(Object param0) {
        if (!(param0 instanceof BlockEventData)) {
            return false;
        } else {
            BlockEventData var0 = (BlockEventData)param0;
            return this.pos.equals(var0.pos) && this.paramA == var0.paramA && this.paramB == var0.paramB && this.block == var0.block;
        }
    }

    @Override
    public int hashCode() {
        int var0 = this.pos.hashCode();
        var0 = 31 * var0 + this.block.hashCode();
        var0 = 31 * var0 + this.paramA;
        return 31 * var0 + this.paramB;
    }

    @Override
    public String toString() {
        return "TE(" + this.pos + ")," + this.paramA + "," + this.paramB + "," + this.block;
    }
}
