package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.state.BlockState;

public class NeitherPortalEntity extends BlockEntity {
    private int dimension;

    public NeitherPortalEntity() {
        super(BlockEntityType.NEITHER);
    }

    public NeitherPortalEntity(int param0) {
        this();
        this.dimension = param0;
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        param0.putInt("Dimension", this.dimension);
        return param0;
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.dimension = param1.getInt("Dimension");
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 15, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public int getDimension() {
        return this.dimension;
    }

    public void setDimension(int param0) {
        this.dimension = param0;
    }
}
