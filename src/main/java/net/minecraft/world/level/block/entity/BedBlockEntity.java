package net.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BedBlockEntity extends BlockEntity {
    private DyeColor color;

    public BedBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.BED, param0, param1);
        this.color = ((BedBlock)param1.getBlock()).getColor();
    }

    public BedBlockEntity(BlockPos param0, BlockState param1, DyeColor param2) {
        super(BlockEntityType.BED, param0, param1);
        this.color = param2;
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public DyeColor getColor() {
        return this.color;
    }

    public void setColor(DyeColor param0) {
        this.color = param0;
    }
}
