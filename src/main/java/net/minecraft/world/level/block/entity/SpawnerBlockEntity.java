package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity implements Spawner {
    private final BaseSpawner spawner = new BaseSpawner() {
        @Override
        public void broadcastEvent(Level param0, BlockPos param1, int param2) {
            param0.blockEvent(param1, Blocks.SPAWNER, param2, 0);
        }

        @Override
        public void setNextSpawnData(@Nullable Level param0, BlockPos param1, SpawnData param2) {
            super.setNextSpawnData(param0, param1, param2);
            if (param0 != null) {
                BlockState var0 = param0.getBlockState(param1);
                param0.sendBlockUpdated(param1, var0, var0, 4);
            }

        }
    };

    public SpawnerBlockEntity(BlockPos param0, BlockState param1) {
        super(BlockEntityType.MOB_SPAWNER, param0, param1);
    }

    @Override
    public void load(CompoundTag param0) {
        super.load(param0);
        this.spawner.load(this.level, this.worldPosition, param0);
    }

    @Override
    protected void saveAdditional(CompoundTag param0) {
        super.saveAdditional(param0);
        this.spawner.save(param0);
    }

    public static void clientTick(Level param0, BlockPos param1, BlockState param2, SpawnerBlockEntity param3) {
        param3.spawner.clientTick(param0, param1);
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, SpawnerBlockEntity param3) {
        param3.spawner.serverTick((ServerLevel)param0, param1);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag var0 = this.saveWithoutMetadata();
        var0.remove("SpawnPotentials");
        return var0;
    }

    @Override
    public boolean triggerEvent(int param0, int param1) {
        return this.spawner.onEventTriggered(this.level, param0) ? true : super.triggerEvent(param0, param1);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void setEntityId(EntityType<?> param0, RandomSource param1) {
        this.spawner.setEntityId(param0, this.level, param1, this.worldPosition);
        this.setChanged();
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}
