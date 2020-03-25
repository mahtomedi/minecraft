package net.minecraft.world.level.block.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class JukeboxBlockEntity extends BlockEntity implements Clearable {
    private ItemStack record = ItemStack.EMPTY;

    public JukeboxBlockEntity() {
        super(BlockEntityType.JUKEBOX);
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        if (param1.contains("RecordItem", 10)) {
            this.setRecord(ItemStack.of(param1.getCompound("RecordItem")));
        }

    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        if (!this.getRecord().isEmpty()) {
            param0.put("RecordItem", this.getRecord().save(new CompoundTag()));
        }

        return param0;
    }

    public ItemStack getRecord() {
        return this.record;
    }

    public void setRecord(ItemStack param0) {
        this.record = param0;
        this.setChanged();
    }

    @Override
    public void clearContent() {
        this.setRecord(ItemStack.EMPTY);
    }
}
