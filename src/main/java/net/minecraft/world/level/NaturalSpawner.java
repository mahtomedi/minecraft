package net.minecraft.world.level;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.WeighedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void spawnCategoryForChunk(MobCategory param0, ServerLevel param1, LevelChunk param2) {
        BlockPos var0 = getRandomPosWithin(param1, param2);
        if (var0.getY() >= 1) {
            spawnCategoryForPosition(param0, param1, param2, var0);
        }
    }

    public static void spawnCategoryForPosition(MobCategory param0, ServerLevel param1, ChunkAccess param2, BlockPos param3) {
        StructureFeatureManager var0 = param1.structureFeatureManager();
        ChunkGenerator<?> var1 = param1.getChunkSource().getGenerator();
        int var2 = param3.getY();
        BlockState var3 = param2.getBlockState(param3);
        if (!var3.isRedstoneConductor(param2, param3)) {
            BlockPos.MutableBlockPos var4 = new BlockPos.MutableBlockPos();
            int var5 = 0;

            for(int var6 = 0; var6 < 3; ++var6) {
                int var7 = param3.getX();
                int var8 = param3.getZ();
                int var9 = 6;
                Biome.SpawnerData var10 = null;
                SpawnGroupData var11 = null;
                int var12 = Mth.ceil(Math.random() * 4.0);
                int var13 = 0;

                for(int var14 = 0; var14 < var12; ++var14) {
                    var7 += param1.random.nextInt(6) - param1.random.nextInt(6);
                    var8 += param1.random.nextInt(6) - param1.random.nextInt(6);
                    var4.set(var7, var2, var8);
                    float var15 = (float)var7 + 0.5F;
                    float var16 = (float)var8 + 0.5F;
                    Player var17 = param1.getNearestPlayer((double)var15, (double)var2, (double)var16, -1.0, false);
                    if (var17 != null) {
                        double var18 = var17.distanceToSqr((double)var15, (double)var2, (double)var16);
                        if (isRightDistanceToPlayerAndSpawnPoint(param1, param2, var4, var18)) {
                            if (var10 == null) {
                                var10 = getRandomSpawnMobAt(var0, var1, param0, param1.random, var4);
                                if (var10 == null) {
                                    break;
                                }

                                var12 = var10.minCount + param1.random.nextInt(1 + var10.maxCount - var10.minCount);
                            }

                            if (isValidSpawnPostitionForType(param1, param0, var0, var1, var10, var4, var18)) {
                                Mob var19 = getMobForSpawn(param1, var10.type);
                                if (var19 == null) {
                                    return;
                                }

                                var19.moveTo((double)var15, (double)var2, (double)var16, param1.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidPositionForMob(param1, var19, var18)) {
                                    var11 = var19.finalizeSpawn(param1, param1.getCurrentDifficultyAt(var19.blockPosition()), MobSpawnType.NATURAL, var11, null);
                                    ++var5;
                                    ++var13;
                                    param1.addFreshEntity(var19);
                                    if (var5 >= var19.getMaxSpawnClusterSize()) {
                                        return;
                                    }

                                    if (var19.isMaxGroupSizeReached(var13)) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private static boolean isRightDistanceToPlayerAndSpawnPoint(ServerLevel param0, ChunkAccess param1, BlockPos.MutableBlockPos param2, double param3) {
        if (param3 <= 576.0) {
            return false;
        } else if (param0.getSharedSpawnPos()
            .closerThan(new Vec3((double)((float)param2.getX() + 0.5F), (double)param2.getY(), (double)((float)param2.getZ() + 0.5F)), 24.0)) {
            return false;
        } else {
            ChunkPos var0 = new ChunkPos(param2);
            return Objects.equals(var0, param1.getPos()) || param0.getChunkSource().isEntityTickingChunk(var0);
        }
    }

    private static boolean isValidSpawnPostitionForType(
        ServerLevel param0,
        MobCategory param1,
        StructureFeatureManager param2,
        ChunkGenerator<?> param3,
        Biome.SpawnerData param4,
        BlockPos.MutableBlockPos param5,
        double param6
    ) {
        EntityType<?> var0 = param4.type;
        if (var0.getCategory() == MobCategory.MISC) {
            return false;
        } else if (!var0.canSpawnFarFromPlayer() && param6 > (double)(var0.getInstantDespawnDistance() * var0.getInstantDespawnDistance())) {
            return false;
        } else if (var0.canSummon() && canSpawnMobAt(param2, param3, param1, param4, param5)) {
            SpawnPlacements.Type var1 = SpawnPlacements.getPlacementType(var0);
            if (!isSpawnPositionOk(var1, param0, param5, var0)) {
                return false;
            } else if (!SpawnPlacements.checkSpawnRules(var0, param0, MobSpawnType.NATURAL, param5, param0.random)) {
                return false;
            } else {
                return param0.noCollision(var0.getAABB((double)((float)param5.getX() + 0.5F), (double)param5.getY(), (double)((float)param5.getZ() + 0.5F)));
            }
        } else {
            return false;
        }
    }

    @Nullable
    private static Mob getMobForSpawn(ServerLevel param0, EntityType<?> param1) {
        try {
            Entity var0 = param1.create(param0);
            if (!(var0 instanceof Mob)) {
                throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(param1));
            } else {
                return (Mob)var0;
            }
        } catch (Exception var4) {
            LOGGER.warn("Failed to create mob", (Throwable)var4);
            return null;
        }
    }

    private static boolean isValidPositionForMob(ServerLevel param0, Mob param1, double param2) {
        if (param2 > (double)(param1.getType().getInstantDespawnDistance() * param1.getType().getInstantDespawnDistance()) && param1.removeWhenFarAway(param2)) {
            return false;
        } else {
            return param1.checkSpawnRules(param0, MobSpawnType.NATURAL) && param1.checkSpawnObstruction(param0);
        }
    }

    @Nullable
    private static Biome.SpawnerData getRandomSpawnMobAt(
        StructureFeatureManager param0, ChunkGenerator<?> param1, MobCategory param2, Random param3, BlockPos param4
    ) {
        List<Biome.SpawnerData> var0 = param1.getMobsAt(param0, param2, param4);
        return var0.isEmpty() ? null : WeighedRandom.getRandomItem(param3, var0);
    }

    private static boolean canSpawnMobAt(
        StructureFeatureManager param0, ChunkGenerator<?> param1, MobCategory param2, Biome.SpawnerData param3, BlockPos param4
    ) {
        return param1.getMobsAt(param0, param2, param4).contains(param3);
    }

    private static BlockPos getRandomPosWithin(Level param0, LevelChunk param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.getMinBlockX() + param0.random.nextInt(16);
        int var2 = var0.getMinBlockZ() + param0.random.nextInt(16);
        int var3 = param1.getHeight(Heightmap.Types.WORLD_SURFACE, var1, var2) + 1;
        int var4 = param0.random.nextInt(var3 + 1);
        return new BlockPos(var1, var4, var2);
    }

    public static boolean isValidEmptySpawnBlock(BlockGetter param0, BlockPos param1, BlockState param2, FluidState param3) {
        if (param2.isCollisionShapeFullBlock(param0, param1)) {
            return false;
        } else if (param2.isSignalSource()) {
            return false;
        } else if (!param3.isEmpty()) {
            return false;
        } else {
            return !param2.is(BlockTags.RAILS);
        }
    }

    public static boolean isSpawnPositionOk(SpawnPlacements.Type param0, LevelReader param1, BlockPos param2, @Nullable EntityType<?> param3) {
        if (param0 == SpawnPlacements.Type.NO_RESTRICTIONS) {
            return true;
        } else if (param3 != null && param1.getWorldBorder().isWithinBounds(param2)) {
            BlockState var0 = param1.getBlockState(param2);
            FluidState var1 = param1.getFluidState(param2);
            BlockPos var2 = param2.above();
            BlockPos var3 = param2.below();
            switch(param0) {
                case IN_WATER:
                    return var1.is(FluidTags.WATER)
                        && param1.getFluidState(var3).is(FluidTags.WATER)
                        && !param1.getBlockState(var2).isRedstoneConductor(param1, var2);
                case IN_LAVA:
                    return var1.is(FluidTags.LAVA)
                        && param1.getFluidState(var3).is(FluidTags.LAVA)
                        && !param1.getBlockState(var2).isRedstoneConductor(param1, var2);
                case ON_GROUND:
                default:
                    BlockState var4 = param1.getBlockState(var3);
                    if (!var4.isValidSpawn(param1, var3, param3)) {
                        return false;
                    } else {
                        return isValidEmptySpawnBlock(param1, param2, var0, var1)
                            && isValidEmptySpawnBlock(param1, var2, param1.getBlockState(var2), param1.getFluidState(var2));
                    }
            }
        } else {
            return false;
        }
    }

    public static void spawnMobsForChunkGeneration(LevelAccessor param0, Biome param1, int param2, int param3, Random param4) {
        List<Biome.SpawnerData> var0 = param1.getMobs(MobCategory.CREATURE);
        if (!var0.isEmpty()) {
            int var1 = param2 << 4;
            int var2 = param3 << 4;

            while(param4.nextFloat() < param1.getCreatureProbability()) {
                Biome.SpawnerData var3 = WeighedRandom.getRandomItem(param4, var0);
                int var4 = var3.minCount + param4.nextInt(1 + var3.maxCount - var3.minCount);
                SpawnGroupData var5 = null;
                int var6 = var1 + param4.nextInt(16);
                int var7 = var2 + param4.nextInt(16);
                int var8 = var6;
                int var9 = var7;

                for(int var10 = 0; var10 < var4; ++var10) {
                    boolean var11 = false;

                    for(int var12 = 0; !var11 && var12 < 4; ++var12) {
                        BlockPos var13 = getTopNonCollidingPos(param0, var3.type, var6, var7);
                        if (var3.type.canSummon() && isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, param0, var13, var3.type)) {
                            float var14 = var3.type.getWidth();
                            double var15 = Mth.clamp((double)var6, (double)var1 + (double)var14, (double)var1 + 16.0 - (double)var14);
                            double var16 = Mth.clamp((double)var7, (double)var2 + (double)var14, (double)var2 + 16.0 - (double)var14);
                            if (!param0.noCollision(var3.type.getAABB(var15, (double)var13.getY(), var16))
                                || !SpawnPlacements.checkSpawnRules(
                                    var3.type, param0, MobSpawnType.CHUNK_GENERATION, new BlockPos(var15, (double)var13.getY(), var16), param0.getRandom()
                                )) {
                                continue;
                            }

                            Entity var17;
                            try {
                                var17 = var3.type.create(param0.getLevel());
                            } catch (Exception var26) {
                                LOGGER.warn("Failed to create mob", (Throwable)var26);
                                continue;
                            }

                            var17.moveTo(var15, (double)var13.getY(), var16, param4.nextFloat() * 360.0F, 0.0F);
                            if (var17 instanceof Mob) {
                                Mob var20 = (Mob)var17;
                                if (var20.checkSpawnRules(param0, MobSpawnType.CHUNK_GENERATION) && var20.checkSpawnObstruction(param0)) {
                                    var5 = var20.finalizeSpawn(
                                        param0, param0.getCurrentDifficultyAt(var20.blockPosition()), MobSpawnType.CHUNK_GENERATION, var5, null
                                    );
                                    param0.addFreshEntity(var20);
                                    var11 = true;
                                }
                            }
                        }

                        var6 += param4.nextInt(5) - param4.nextInt(5);

                        for(var7 += param4.nextInt(5) - param4.nextInt(5);
                            var6 < var1 || var6 >= var1 + 16 || var7 < var2 || var7 >= var2 + 16;
                            var7 = var9 + param4.nextInt(5) - param4.nextInt(5)
                        ) {
                            var6 = var8 + param4.nextInt(5) - param4.nextInt(5);
                        }
                    }
                }
            }

        }
    }

    private static BlockPos getTopNonCollidingPos(LevelReader param0, @Nullable EntityType<?> param1, int param2, int param3) {
        BlockPos var0 = new BlockPos(param2, param0.getHeight(SpawnPlacements.getHeightmapType(param1), param2, param3), param3);
        BlockPos var1 = var0.below();
        return param0.getBlockState(var1).isPathfindable(param0, var1, PathComputationType.LAND) ? var1 : var0;
    }
}
