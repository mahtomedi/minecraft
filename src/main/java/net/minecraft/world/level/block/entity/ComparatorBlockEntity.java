package net.minecraft.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;

public class ComparatorBlockEntity extends BlockEntity {
    private int output;

    public ComparatorBlockEntity() {
        super(BlockEntityType.COMPARATOR);
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
