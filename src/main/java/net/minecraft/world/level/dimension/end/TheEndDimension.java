package net.minecraft.world.level.dimension.end;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;

public class TheEndDimension extends Dimension {
    public static final BlockPos END_SPAWN_POINT = new BlockPos(100, 50, 0);
    private final EndDragonFight dragonFight;

    public TheEndDimension(Level param0, DimensionType param1) {
        super(param0, param1, 0.0F);
        if (param0 instanceof ServerLevel) {
            ServerLevel var0 = (ServerLevel)param0;
            LevelData var1 = var0.getLevelData();
            if (var1 instanceof ServerLevelData) {
                CompoundTag var2 = ((ServerLevelData)var1).getDimensionData();
                this.dragonFight = new EndDragonFight(var0, var2.getCompound("DragonFight"));
            } else {
                this.dragonFight = null;
            }
        } else {
            this.dragonFight = null;
        }

    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.0F;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(long param0, ChunkPos param1, boolean param2) {
        Random var0 = new Random(param0);
        BlockPos var1 = new BlockPos(param1.getMinBlockX() + var0.nextInt(15), 0, param1.getMaxBlockZ() + var0.nextInt(15));
        return this.level.getTopBlockState(var1).getMaterial().blocksMotion() ? var1 : null;
    }

    @Override
    public BlockPos getDimensionSpecificSpawn() {
        return END_SPAWN_POINT;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(long param0, int param1, int param2, boolean param3) {
        return this.getSpawnPosInChunk(param0, new ChunkPos(param1 >> 4, param2 >> 4), param3);
    }

    @Override
    public DimensionType getType() {
        return DimensionType.THE_END;
    }

    @Override
    public void saveData(ServerLevelData param0) {
        CompoundTag var0 = new CompoundTag();
        if (this.dragonFight != null) {
            var0.put("DragonFight", this.dragonFight.saveData());
        }

        param0.setDimensionData(var0);
    }

    @Override
    public void tick() {
        if (this.dragonFight != null) {
            this.dragonFight.tick();
        }

    }

    @Nullable
    public EndDragonFight getDragonFight() {
        return this.dragonFight;
    }
}
