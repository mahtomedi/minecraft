package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class ComparatorBlockEntity extends BlockEntity {
    private int output;

    public ComparatorBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.COMPARATOR, param0, param1);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putInt("OutputSignal", this.output);
        return param0;
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.output = param0.getInt("OutputSignal");
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int param0) {
        this.output = param0;
    }
}
