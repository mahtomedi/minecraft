package net.minecraft.world.level.dimension.end;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSourceType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorType;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.TheEndGeneratorSettings;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
    public ChunkGenerator<?> createRandomLevelGenerator() {
        TheEndGeneratorSettings var0 = ChunkGeneratorType.FLOATING_ISLANDS.createSettings();
        var0.setDefaultBlock(Blocks.END_STONE.defaultBlockState());
        var0.setDefaultFluid(Blocks.AIR.defaultBlockState());
        var0.setSpawnPosition(this.getDimensionSpecificSpawn());
        return ChunkGeneratorType.FLOATING_ISLANDS
            .create(this.level, BiomeSourceType.THE_END.create(BiomeSourceType.THE_END.createSettings(this.level.getSeed())), var0);
    }

    @Override
    public float getTimeOfDay(long param0, float param1) {
        return 0.0F;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    @Override
    public float[] getSunriseColor(float param0, float param1) {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 param0, float param1) {
        return param0.scale(0.15F);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean hasGround() {
        return false;
    }

    @Override
    public boolean mayRespawn() {
        return false;
    }

    @Override
    public boolean isNaturalDimension() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getCloudHeight() {
        return 8.0F;
    }

    @Nullable
    @Override
    public BlockPos getSpawnPosInChunk(ChunkPos param0, boolean param1) {
        Random var0 = new Random(this.level.getSeed());
        BlockPos var1 = new BlockPos(param0.getMinBlockX() + var0.nextInt(15), 0, param0.getMaxBlockZ() + var0.nextInt(15));
        return this.level.getTopBlockState(var1).getMaterial().blocksMotion() ? var1 : null;
    }

    @Override
    public BlockPos getDimensionSpecificSpawn() {
        return END_SPAWN_POINT;
    }

    @Nullable
    @Override
    public BlockPos getValidSpawnPosition(int param0, int param1, boolean param2) {
        return this.getSpawnPosInChunk(new ChunkPos(param0 >> 4, param1 >> 4), param2);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean isFoggyAt(int param0, int param1) {
        return false;
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
