package net.minecraft.world.level;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
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

    public static void spawnCategoryForChunk(MobCategory param0, Level param1, LevelChunk param2, BlockPos param3) {
        ChunkGenerator<?> var0 = param1.getChunkSource().getGenerator();
        int var1 = 0;
        BlockPos var2 = getRandomPosWithin(param1, param2);
        int var3 = var2.getX();
        int var4 = var2.getY();
        int var5 = var2.getZ();
        if (var4 >= 1) {
            BlockState var6 = param2.getBlockState(var2);
            if (!var6.isRedstoneConductor(param2, var2)) {
                BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();
                int var8 = 0;

                while(var8 < 3) {
                    int var9 = var3;
                    int var10 = var5;
                    int var11 = 6;
                    Biome.SpawnerData var12 = null;
                    SpawnGroupData var13 = null;
                    int var14 = Mth.ceil(Math.random() * 4.0);
                    int var15 = 0;
                    int var16 = 0;

                    while(true) {
                        label115: {
                            label114:
                            if (var16 < var14) {
                                var9 += param1.random.nextInt(6) - param1.random.nextInt(6);
                                var10 += param1.random.nextInt(6) - param1.random.nextInt(6);
                                var7.set(var9, var4, var10);
                                float var17 = (float)var9 + 0.5F;
                                float var18 = (float)var10 + 0.5F;
                                Player var19 = param1.getNearestPlayerIgnoreY((double)var17, (double)var18, -1.0);
                                if (var19 == null) {
                                    break label115;
                                }

                                double var20 = var19.distanceToSqr((double)var17, (double)var4, (double)var18);
                                if (var20 <= 576.0 || param3.closerThan(new Vec3((double)var17, (double)var4, (double)var18), 24.0)) {
                                    break label115;
                                }

                                ChunkPos var21 = new ChunkPos(var7);
                                if (!Objects.equals(var21, param2.getPos()) && !param1.getChunkSource().isEntityTickingChunk(var21)) {
                                    break label115;
                                }

                                if (var12 == null) {
                                    var12 = getRandomSpawnMobAt(var0, param0, param1.random, var7);
                                    if (var12 == null) {
                                        break label114;
                                    }

                                    var14 = var12.minCount + param1.random.nextInt(1 + var12.maxCount - var12.minCount);
                                }

                                if (var12.type.getCategory() == MobCategory.MISC || !var12.type.canSpawnFarFromPlayer() && var20 > 16384.0) {
                                    break label115;
                                }

                                EntityType<?> var22 = var12.type;
                                if (!var22.canSummon() || !canSpawnMobAt(var0, param0, var12, var7)) {
                                    break label115;
                                }

                                SpawnPlacements.Type var23 = SpawnPlacements.getPlacementType(var22);
                                if (!isSpawnPositionOk(var23, param1, var7, var22)
                                    || !SpawnPlacements.checkSpawnRules(var22, param1, MobSpawnType.NATURAL, var7, param1.random)
                                    || !param1.noCollision(var22.getAABB((double)var17, (double)var4, (double)var18))) {
                                    break label115;
                                }

                                Mob var25;
                                try {
                                    Entity var24 = var22.create(param1);
                                    if (!(var24 instanceof Mob)) {
                                        throw new IllegalStateException("Trying to spawn a non-mob: " + Registry.ENTITY_TYPE.getKey(var22));
                                    }

                                    var25 = (Mob)var24;
                                } catch (Exception var31) {
                                    LOGGER.warn("Failed to create mob", (Throwable)var31);
                                    return;
                                }

                                var25.moveTo((double)var17, (double)var4, (double)var18, param1.random.nextFloat() * 360.0F, 0.0F);
                                if (var20 > 16384.0 && var25.removeWhenFarAway(var20)
                                    || !var25.checkSpawnRules(param1, MobSpawnType.NATURAL)
                                    || !var25.checkSpawnObstruction(param1)) {
                                    break label115;
                                }

                                var13 = var25.finalizeSpawn(param1, param1.getCurrentDifficultyAt(new BlockPos(var25)), MobSpawnType.NATURAL, var13, null);
                                ++var1;
                                ++var15;
                                param1.addFreshEntity(var25);
                                if (var1 >= var25.getMaxSpawnClusterSize()) {
                                    return;
                                }

                                if (!var25.isMaxGroupSizeReached(var15)) {
                                    break label115;
                                }
                            }

                            ++var8;
                            break;
                        }

                        ++var16;
                    }
                }

            }
        }
    }

    @Nullable
    private static Biome.SpawnerData getRandomSpawnMobAt(ChunkGenerator<?> param0, MobCategory param1, Random param2, BlockPos param3) {
        List<Biome.SpawnerData> var0 = param0.getMobsAt(param1, param3);
        return var0.isEmpty() ? null : WeighedRandom.getRandomItem(param2, var0);
    }

    private static boolean canSpawnMobAt(ChunkGenerator<?> param0, MobCategory param1, Biome.SpawnerData param2, BlockPos param3) {
        List<Biome.SpawnerData> var0 = param0.getMobsAt(param1, param3);
        return var0.isEmpty() ? false : var0.contains(param2);
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
                                        param0, param0.getCurrentDifficultyAt(new BlockPos(var20)), MobSpawnType.CHUNK_GENERATION, var5, null
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
