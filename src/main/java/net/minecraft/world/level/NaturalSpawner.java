package net.minecraft.world.level;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;
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
import net.minecraft.world.level.biome.NearestNeighborBiomeZoomer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final MobCategory[] SPAWNING_CATEGORIES = Stream.of(MobCategory.values())
        .filter(param0 -> param0 != MobCategory.MISC)
        .toArray(param0 -> new MobCategory[param0]);

    public static NaturalSpawner.SpawnState createState(int param0, Iterable<Entity> param1, NaturalSpawner.ChunkGetter param2) {
        PotentialCalculator var0 = new PotentialCalculator();
        Object2IntOpenHashMap<MobCategory> var1 = new Object2IntOpenHashMap<>();
        Iterator var5 = param1.iterator();

        while(true) {
            Entity var2;
            Mob var3;
            do {
                if (!var5.hasNext()) {
                    return new NaturalSpawner.SpawnState(param0, var1, var0);
                }

                var2 = (Entity)var5.next();
                if (!(var2 instanceof Mob)) {
                    break;
                }

                var3 = (Mob)var2;
            } while(var3.isPersistenceRequired() || var3.requiresCustomPersistence());

            MobCategory var4 = var2.getType().getCategory();
            if (var4 != MobCategory.MISC) {
                BlockPos var5x = var2.blockPosition();
                long var6 = ChunkPos.asLong(var5x.getX() >> 4, var5x.getZ() >> 4);
                param2.query(var6, param5 -> {
                    Biome var0x = getRoughBiome(var5, param5);
                    Biome.MobSpawnCost var1x = var0x.getMobSpawnCost(var2.getType());
                    if (var1x != null) {
                        var0.addCharge(var2.blockPosition(), var1x.getCharge());
                    }

                    var1.addTo(var4, 1);
                });
            }
        }
    }

    private static Biome getRoughBiome(BlockPos param0, ChunkAccess param1) {
        return NearestNeighborBiomeZoomer.INSTANCE.getBiome(0L, param0.getX(), param0.getY(), param0.getZ(), param1.getBiomes());
    }

    public static void spawnForChunk(ServerLevel param0, LevelChunk param1, NaturalSpawner.SpawnState param2, boolean param3, boolean param4, boolean param5) {
        param0.getProfiler().push("spawner");

        for(MobCategory var0 : SPAWNING_CATEGORIES) {
            if ((param3 || !var0.isFriendly()) && (param4 || var0.isFriendly()) && (param5 || !var0.isPersistent()) && param2.canSpawnForCategory(var0)) {
                spawnCategoryForChunk(
                    var0,
                    param0,
                    param1,
                    (param1x, param2x, param3x) -> param2.canSpawn(param1x, param2x, param3x),
                    (param1x, param2x) -> param2.afterSpawn(param1x, param2x)
                );
            }
        }

        param0.getProfiler().pop();
    }

    public static void spawnCategoryForChunk(
        MobCategory param0, ServerLevel param1, LevelChunk param2, NaturalSpawner.SpawnPredicate param3, NaturalSpawner.AfterSpawnCallback param4
    ) {
        BlockPos var0 = getRandomPosWithin(param1, param2);
        if (var0.getY() >= 1) {
            spawnCategoryForPosition(param0, param1, param2, var0, param3, param4);
        }
    }

    public static void spawnCategoryForPosition(
        MobCategory param0,
        ServerLevel param1,
        ChunkAccess param2,
        BlockPos param3,
        NaturalSpawner.SpawnPredicate param4,
        NaturalSpawner.AfterSpawnCallback param5
    ) {
        StructureFeatureManager var0 = param1.structureFeatureManager();
        ChunkGenerator var1 = param1.getChunkSource().getGenerator();
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
                int var12 = Mth.ceil(param1.random.nextFloat() * 4.0F);
                int var13 = 0;

                for(int var14 = 0; var14 < var12; ++var14) {
                    var7 += param1.random.nextInt(6) - param1.random.nextInt(6);
                    var8 += param1.random.nextInt(6) - param1.random.nextInt(6);
                    var4.set(var7, var2, var8);
                    double var15 = (double)var7 + 0.5;
                    double var16 = (double)var8 + 0.5;
                    Player var17 = param1.getNearestPlayer(var15, (double)var2, var16, -1.0, false);
                    if (var17 != null) {
                        double var18 = var17.distanceToSqr(var15, (double)var2, var16);
                        if (isRightDistanceToPlayerAndSpawnPoint(param1, param2, var4, var18)) {
                            if (var10 == null) {
                                var10 = getRandomSpawnMobAt(param1, var0, var1, param0, param1.random, var4);
                                if (var10 == null) {
                                    break;
                                }

                                var12 = var10.minCount + param1.random.nextInt(1 + var10.maxCount - var10.minCount);
                            }

                            if (isValidSpawnPostitionForType(param1, param0, var0, var1, var10, var4, var18) && param4.test(var10.type, var4, param2)) {
                                Mob var19 = getMobForSpawn(param1, var10.type);
                                if (var19 == null) {
                                    return;
                                }

                                var19.moveTo(var15, (double)var2, var16, param1.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidPositionForMob(param1, var19, var18)) {
                                    var11 = var19.finalizeSpawn(param1, param1.getCurrentDifficultyAt(var19.blockPosition()), MobSpawnType.NATURAL, var11, null);
                                    ++var5;
                                    ++var13;
                                    param1.addFreshEntity(var19);
                                    param5.run(var19, param2);
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
        } else if (param0.getSharedSpawnPos().closerThan(new Vec3((double)param2.getX() + 0.5, (double)param2.getY(), (double)param2.getZ() + 0.5), 24.0)) {
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
        ChunkGenerator param3,
        Biome.SpawnerData param4,
        BlockPos.MutableBlockPos param5,
        double param6
    ) {
        EntityType<?> var0 = param4.type;
        if (var0.getCategory() == MobCategory.MISC) {
            return false;
        } else if (!var0.canSpawnFarFromPlayer() && param6 > (double)(var0.getCategory().getDespawnDistance() * var0.getCategory().getDespawnDistance())) {
            return false;
        } else if (var0.canSummon() && canSpawnMobAt(param0, param2, param3, param1, param4, param5)) {
            SpawnPlacements.Type var1 = SpawnPlacements.getPlacementType(var0);
            if (!isSpawnPositionOk(var1, param0, param5, var0)) {
                return false;
            } else if (!SpawnPlacements.checkSpawnRules(var0, param0, MobSpawnType.NATURAL, param5, param0.random)) {
                return false;
            } else {
                return param0.noCollision(var0.getAABB((double)param5.getX() + 0.5, (double)param5.getY(), (double)param5.getZ() + 0.5));
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
        if (param2 > (double)(param1.getType().getCategory().getDespawnDistance() * param1.getType().getCategory().getDespawnDistance())
            && param1.removeWhenFarAway(param2)) {
            return false;
        } else {
            return param1.checkSpawnRules(param0, MobSpawnType.NATURAL) && param1.checkSpawnObstruction(param0);
        }
    }

    @Nullable
    private static Biome.SpawnerData getRandomSpawnMobAt(
        ServerLevel param0, StructureFeatureManager param1, ChunkGenerator param2, MobCategory param3, Random param4, BlockPos param5
    ) {
        List<Biome.SpawnerData> var0 = param2.getMobsAt(param0.getBiome(param5), param1, param3, param5);
        return var0.isEmpty() ? null : WeighedRandom.getRandomItem(param4, var0);
    }

    private static boolean canSpawnMobAt(
        ServerLevel param0, StructureFeatureManager param1, ChunkGenerator param2, MobCategory param3, Biome.SpawnerData param4, BlockPos param5
    ) {
        return param2.getMobsAt(param0.getBiome(param5), param1, param3, param5).contains(param4);
    }

    private static BlockPos getRandomPosWithin(Level param0, LevelChunk param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.getMinBlockX() + param0.random.nextInt(16);
        int var2 = var0.getMinBlockZ() + param0.random.nextInt(16);
        int var3 = param1.getHeight(Heightmap.Types.WORLD_SURFACE, var1, var2) + 1;
        int var4 = param0.random.nextInt(var3 + 1);
        return new BlockPos(var1, var4, var2);
    }

    public static boolean isValidEmptySpawnBlock(BlockGetter param0, BlockPos param1, BlockState param2, FluidState param3, EntityType param4) {
        if (param2.isCollisionShapeFullBlock(param0, param1)) {
            return false;
        } else if (param2.isSignalSource()) {
            return false;
        } else if (!param3.isEmpty()) {
            return false;
        } else if (param2.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
            return false;
        } else {
            return !param4.isBlockDangerous(param2);
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
                        return isValidEmptySpawnBlock(param1, param2, var0, var1, param3)
                            && isValidEmptySpawnBlock(param1, var2, param1.getBlockState(var2), param1.getFluidState(var2), param3);
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

    @FunctionalInterface
    public interface AfterSpawnCallback {
        void run(Mob var1, ChunkAccess var2);
    }

    @FunctionalInterface
    public interface ChunkGetter {
        void query(long var1, Consumer<LevelChunk> var3);
    }

    @FunctionalInterface
    public interface SpawnPredicate {
        boolean test(EntityType<?> var1, BlockPos var2, ChunkAccess var3);
    }

    public static class SpawnState {
        private final int spawnableChunkCount;
        private final Object2IntOpenHashMap<MobCategory> mobCategoryCounts;
        private final PotentialCalculator spawnPotential;
        private final Object2IntMap<MobCategory> unmodifiableMobCategoryCounts;
        @Nullable
        private BlockPos lastCheckedPos;
        @Nullable
        private EntityType<?> lastCheckedType;
        private double lastCharge;

        private SpawnState(int param0, Object2IntOpenHashMap<MobCategory> param1, PotentialCalculator param2) {
            this.spawnableChunkCount = param0;
            this.mobCategoryCounts = param1;
            this.spawnPotential = param2;
            this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(param1);
        }

        private boolean canSpawn(EntityType<?> param0, BlockPos param1, ChunkAccess param2) {
            this.lastCheckedPos = param1;
            this.lastCheckedType = param0;
            Biome var0 = NaturalSpawner.getRoughBiome(param1, param2);
            Biome.MobSpawnCost var1 = var0.getMobSpawnCost(param0);
            if (var1 == null) {
                this.lastCharge = 0.0;
                return true;
            } else {
                double var2 = var1.getCharge();
                this.lastCharge = var2;
                double var3 = this.spawnPotential.getPotentialEnergyChange(param1, var2);
                return var3 <= var1.getEnergyBudget();
            }
        }

        private void afterSpawn(Mob param0, ChunkAccess param1) {
            EntityType<?> var0 = param0.getType();
            BlockPos var1 = param0.blockPosition();
            double var2;
            if (var1.equals(this.lastCheckedPos) && var0 == this.lastCheckedType) {
                var2 = this.lastCharge;
            } else {
                Biome var3 = NaturalSpawner.getRoughBiome(var1, param1);
                Biome.MobSpawnCost var4 = var3.getMobSpawnCost(var0);
                if (var4 != null) {
                    var2 = var4.getCharge();
                } else {
                    var2 = 0.0;
                }
            }

            this.spawnPotential.addCharge(var1, var2);
            this.mobCategoryCounts.addTo(var0.getCategory(), 1);
        }

        @OnlyIn(Dist.CLIENT)
        public int getSpawnableChunkCount() {
            return this.spawnableChunkCount;
        }

        public Object2IntMap<MobCategory> getMobCategoryCounts() {
            return this.unmodifiableMobCategoryCounts;
        }

        private boolean canSpawnForCategory(MobCategory param0) {
            int var0 = param0.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
            return this.mobCategoryCounts.getInt(param0) < var0;
        }
    }
}
