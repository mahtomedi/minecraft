package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SpawnerBlockEntity extends BlockEntity {
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
    public CompoundTag save(CompoundTag param0) {
        super.save(param0);
        this.spawner.save(this.level, this.worldPosition, param0);
        return param0;
    }

    public static void clientTick(Level param0, BlockPos param1, BlockState param2, SpawnerBlockEntity param3) {
        param3.spawner.clientTick(param0, param1);
    }

    public static void serverTick(Level param0, BlockPos param1, BlockState param2, SpawnerBlockEntity param3) {
        param3.spawner.serverTick((ServerLevel)param0, param1);
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
        return this.spawner.onEventTriggered(this.level, param0) ? true : super.triggerEvent(param0, param1);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}
