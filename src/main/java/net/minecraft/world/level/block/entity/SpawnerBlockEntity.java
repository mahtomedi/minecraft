package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity implements TickableBlockEntity {
    private final BaseSpawner spawner = new BaseSpawner() {
        @Override
        public void broadcastEvent(int param0) {
            SpawnerBlockEntity.this.level.blockEvent(SpawnerBlockEntity.this.worldPosition, Blocks.SPAWNER, param0, 0);
        }

        @Override
        public Level getLevel() {
            return SpawnerBlockEntity.this.level;
        }

        @Override
        public BlockPos getPos() {
            return SpawnerBlockEntity.this.worldPosition;
        }

        @Override
        public void setNextSpawnData(SpawnData param0) {
            super.setNextSpawnData(param0);
            if (this.getLevel() != null) {
                BlockState var0 = this.getLevel().getBlockState(this.getPos());
                this.getLevel().sendBlockUpdated(SpawnerBlockEntity.this.worldPosition, var0, var0, 4);
            }

        }
    };

    public SpawnerBlockEntity() {
        super(BlockEntityType.MOB_SPAWNER);
    }

    @Override
    public void load(BlockState param0, CompoundTag param1) {
        super.load(param0, param1);
        this.spawner.load(param1);
    }

    @Override
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        this.spawner.save(param0);
        return param0;
    }

    @Override
    public void tick() {
        this.spawner.tick();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return new ClientboundBlockEntityDataPacket(this.worldPosition, 1, this.getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = this.save(new CompoundTag());
        var0.remove("SpawnPotentials");
        return var0;
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        return this.spawner.onEventTriggered(param0) ? true : super.triggerEvent(param0, param1);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}
