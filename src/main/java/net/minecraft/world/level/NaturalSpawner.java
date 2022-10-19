package net.minecraft.world.level;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.structures.NetherFortressStructure;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public final class NaturalSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MIN_SPAWN_DISTANCE = 24;
    public static final int SPAWN_DISTANCE_CHUNK = 8;
    public static final int SPAWN_DISTANCE_BLOCK = 128;
    static final int MAGIC_NUMBER = (int)Math.pow(17.0, 2.0);
    private static final MobCategory[] SPAWNING_CATEGORIES = Stream.of(MobCategory.values())
        .filter(param0 -> param0 != MobCategory.MISC)
        .toArray(param0 -> new MobCategory[param0]);

    private NaturalSpawner() {
    }

    public static NaturalSpawner.SpawnState createState(int param0, Iterable<Entity> param1, NaturalSpawner.ChunkGetter param2, LocalMobCapCalculator param3) {
        PotentialCalculator var0 = new PotentialCalculator();
        Object2IntOpenHashMap<MobCategory> var1 = new Object2IntOpenHashMap<>();
        Iterator var6 = param1.iterator();

        while(true) {
            Entity var2;
            Mob var3;
            do {
                if (!var6.hasNext()) {
                    return new NaturalSpawner.SpawnState(param0, var1, var0, param3);
                }

                var2 = (Entity)var6.next();
                if (!(var2 instanceof Mob)) {
                    break;
                }

                var3 = (Mob)var2;
            } while(var3.isPersistenceRequired() || var3.requiresCustomPersistence());

            MobCategory var4 = var2.getType().getCategory();
            if (var4 != MobCategory.MISC) {
                BlockPos var5 = var2.blockPosition();
                param2.query(ChunkPos.asLong(var5), param6 -> {
                    MobSpawnSettings.MobSpawnCost var0x = getRoughBiome(var5, param6).getMobSettings().getMobSpawnCost(var2.getType());
                    if (var0x != null) {
                        var0.addCharge(var2.blockPosition(), var0x.getCharge());
                    }

                    if (var2 instanceof Mob) {
                        param3.addMob(param6.getPos(), var4);
                    }

                    var1.addTo(var4, 1);
                });
            }
        }
    }

    static Biome getRoughBiome(BlockPos param0, ChunkAccess param1) {
        return param1.getNoiseBiome(QuartPos.fromBlock(param0.getX()), QuartPos.fromBlock(param0.getY()), QuartPos.fromBlock(param0.getZ())).value();
    }

    public static void spawnForChunk(ServerLevel param0, LevelChunk param1, NaturalSpawner.SpawnState param2, boolean param3, boolean param4, boolean param5) {
        param0.getProfiler().push("spawner");

        for(MobCategory var0 : SPAWNING_CATEGORIES) {
            if ((param3 || !var0.isFriendly())
                && (param4 || var0.isFriendly())
                && (param5 || !var0.isPersistent())
                && param2.canSpawnForCategory(var0, param1.getPos())) {
                spawnCategoryForChunk(var0, param0, param1, param2::canSpawn, param2::afterSpawn);
            }
        }

        param0.getProfiler().pop();
    }

    public static void spawnCategoryForChunk(
        MobCategory param0, ServerLevel param1, LevelChunk param2, NaturalSpawner.SpawnPredicate param3, NaturalSpawner.AfterSpawnCallback param4
    ) {
        BlockPos var0 = getRandomPosWithin(param1, param2);
        if (var0.getY() >= param1.getMinBuildHeight() + 1) {
            spawnCategoryForPosition(param0, param1, param2, var0, param3, param4);
        }
    }

    @VisibleForDebug
    public static void spawnCategoryForPosition(MobCategory param0, ServerLevel param1, BlockPos param2) {
        spawnCategoryForPosition(param0, param1, param1.getChunk(param2), param2, (param0x, param1x, param2x) -> true, (param0x, param1x) -> {
        });
    }

    public static void spawnCategoryForPosition(
        MobCategory param0,
        ServerLevel param1,
        ChunkAccess param2,
        BlockPos param3,
        NaturalSpawner.SpawnPredicate param4,
        NaturalSpawner.AfterSpawnCallback param5
    ) {
        StructureManager var0 = param1.structureManager();
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
                MobSpawnSettings.SpawnerData var10 = null;
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
                                Optional<MobSpawnSettings.SpawnerData> var19 = getRandomSpawnMobAt(param1, var0, var1, param0, param1.random, var4);
                                if (var19.isEmpty()) {
                                    break;
                                }

                                var10 = var19.get();
                                var12 = var10.minCount + param1.random.nextInt(1 + var10.maxCount - var10.minCount);
                            }

                            if (isValidSpawnPostitionForType(param1, param0, var0, var1, var10, var4, var18) && param4.test(var10.type, var4, param2)) {
                                Mob var20 = getMobForSpawn(param1, var10.type);
                                if (var20 == null) {
                                    return;
                                }

                                var20.moveTo(var15, (double)var2, var16, param1.random.nextFloat() * 360.0F, 0.0F);
                                if (isValidPositionForMob(param1, var20, var18)) {
                                    var11 = var20.finalizeSpawn(param1, param1.getCurrentDifficultyAt(var20.blockPosition()), MobSpawnType.NATURAL, var11, null);
                                    ++var5;
                                    ++var13;
                                    param1.addFreshEntityWithPassengers(var20);
                                    param5.run(var20, param2);
                                    if (var5 >= var20.getMaxSpawnClusterSize()) {
                                        return;
                                    }

                                    if (var20.isMaxGroupSizeReached(var13)) {
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
            .closerToCenterThan(new Vec3((double)param2.getX() + 0.5, (double)param2.getY(), (double)param2.getZ() + 0.5), 24.0)) {
            return false;
        } else {
            return Objects.equals(new ChunkPos(param2), param1.getPos()) || param0.isNaturalSpawningAllowed(param2);
        }
    }

    private static boolean isValidSpawnPostitionForType(
        ServerLevel param0,
        MobCategory param1,
        StructureManager param2,
        ChunkGenerator param3,
        MobSpawnSettings.SpawnerData param4,
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
            Entity var3 = param1.create(param0);
            if (var3 instanceof Mob) {
                return (Mob)var3;
            }

            LOGGER.warn("Can't spawn entity of type: {}", Registry.ENTITY_TYPE.getKey(param1));
        } catch (Exception var4) {
            LOGGER.warn("Failed to create mob", (Throwable)var4);
        }

        return null;
    }

    private static boolean isValidPositionForMob(ServerLevel param0, Mob param1, double param2) {
        if (param2 > (double)(param1.getType().getCategory().getDespawnDistance() * param1.getType().getCategory().getDespawnDistance())
            && param1.removeWhenFarAway(param2)) {
            return false;
        } else {
            return param1.checkSpawnRules(param0, MobSpawnType.NATURAL) && param1.checkSpawnObstruction(param0);
        }
    }

    private static Optional<MobSpawnSettings.SpawnerData> getRandomSpawnMobAt(
        ServerLevel param0, StructureManager param1, ChunkGenerator param2, MobCategory param3, RandomSource param4, BlockPos param5
    ) {
        Holder<Biome> var0 = param0.getBiome(param5);
        return param3 == MobCategory.WATER_AMBIENT && var0.is(BiomeTags.REDUCED_WATER_AMBIENT_SPAWNS) && param4.nextFloat() < 0.98F
            ? Optional.empty()
            : mobsAt(param0, param1, param2, param3, param5, var0).getRandom(param4);
    }

    private static boolean canSpawnMobAt(
        ServerLevel param0, StructureManager param1, ChunkGenerator param2, MobCategory param3, MobSpawnSettings.SpawnerData param4, BlockPos param5
    ) {
        return mobsAt(param0, param1, param2, param3, param5, null).unwrap().contains(param4);
    }

    private static WeightedRandomList<MobSpawnSettings.SpawnerData> mobsAt(
        ServerLevel param0, StructureManager param1, ChunkGenerator param2, MobCategory param3, BlockPos param4, @Nullable Holder<Biome> param5
    ) {
        return isInNetherFortressBounds(param4, param0, param3, param1)
            ? NetherFortressStructure.FORTRESS_ENEMIES
            : param2.getMobsAt(param5 != null ? param5 : param0.getBiome(param4), param1, param3, param4);
    }

    public static boolean isInNetherFortressBounds(BlockPos param0, ServerLevel param1, MobCategory param2, StructureManager param3) {
        if (param2 == MobCategory.MONSTER && param1.getBlockState(param0.below()).is(Blocks.NETHER_BRICKS)) {
            Structure var0 = param3.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(BuiltinStructures.FORTRESS);
            return var0 == null ? false : param3.getStructureAt(param0, var0).isValid();
        } else {
            return false;
        }
    }

    private static BlockPos getRandomPosWithin(Level param0, LevelChunk param1) {
        ChunkPos var0 = param1.getPos();
        int var1 = var0.getMinBlockX() + param0.random.nextInt(16);
        int var2 = var0.getMinBlockZ() + param0.random.nextInt(16);
        int var3 = param1.getHeight(Heightmap.Types.WORLD_SURFACE, var1, var2) + 1;
        int var4 = Mth.randomBetweenInclusive(param0.random, param0.getMinBuildHeight(), var3);
        return new BlockPos(var1, var4, var2);
    }

    public static boolean isValidEmptySpawnBlock(BlockGetter param0, BlockPos param1, BlockState param2, FluidState param3, EntityType<?> param4) {
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
                    return var1.is(FluidTags.WATER) && !param1.getBlockState(var2).isRedstoneConductor(param1, var2);
                case IN_LAVA:
                    return var1.is(FluidTags.LAVA);
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

    public static void spawnMobsForChunkGeneration(ServerLevelAccessor param0, Holder<Biome> param1, ChunkPos param2, RandomSource param3) {
        MobSpawnSettings var0 = param1.value().getMobSettings();
        WeightedRandomList<MobSpawnSettings.SpawnerData> var1 = var0.getMobs(MobCategory.CREATURE);
        if (!var1.isEmpty()) {
            int var2 = param2.getMinBlockX();
            int var3 = param2.getMinBlockZ();

            while(param3.nextFloat() < var0.getCreatureProbability()) {
                Optional<MobSpawnSettings.SpawnerData> var4 = var1.getRandom(param3);
                if (var4.isPresent()) {
                    MobSpawnSettings.SpawnerData var5 = var4.get();
                    int var6 = var5.minCount + param3.nextInt(1 + var5.maxCount - var5.minCount);
                    SpawnGroupData var7 = null;
                    int var8 = var2 + param3.nextInt(16);
                    int var9 = var3 + param3.nextInt(16);
                    int var10 = var8;
                    int var11 = var9;

                    for(int var12 = 0; var12 < var6; ++var12) {
                        boolean var13 = false;

                        for(int var14 = 0; !var13 && var14 < 4; ++var14) {
                            BlockPos var15 = getTopNonCollidingPos(param0, var5.type, var8, var9);
                            if (var5.type.canSummon() && isSpawnPositionOk(SpawnPlacements.getPlacementType(var5.type), param0, var15, var5.type)) {
                                float var16 = var5.type.getWidth();
                                double var17 = Mth.clamp((double)var8, (double)var2 + (double)var16, (double)var2 + 16.0 - (double)var16);
                                double var18 = Mth.clamp((double)var9, (double)var3 + (double)var16, (double)var3 + 16.0 - (double)var16);
                                if (!param0.noCollision(var5.type.getAABB(var17, (double)var15.getY(), var18))
                                    || !SpawnPlacements.checkSpawnRules(
                                        var5.type, param0, MobSpawnType.CHUNK_GENERATION, new BlockPos(var17, (double)var15.getY(), var18), param0.getRandom()
                                    )) {
                                    continue;
                                }

                                Entity var19;
                                try {
                                    var19 = var5.type.create(param0.getLevel());
                                } catch (Exception var27) {
                                    LOGGER.warn("Failed to create mob", (Throwable)var27);
                                    continue;
                                }

                                if (var19 == null) {
                                    continue;
                                }

                                var19.moveTo(var17, (double)var15.getY(), var18, param3.nextFloat() * 360.0F, 0.0F);
                                if (var19 instanceof Mob var22
                                    && var22.checkSpawnRules(param0, MobSpawnType.CHUNK_GENERATION)
                                    && var22.checkSpawnObstruction(param0)) {
                                    var7 = var22.finalizeSpawn(
                                        param0, param0.getCurrentDifficultyAt(var22.blockPosition()), MobSpawnType.CHUNK_GENERATION, var7, null
                                    );
                                    param0.addFreshEntityWithPassengers(var22);
                                    var13 = true;
                                }
                            }

                            var8 += param3.nextInt(5) - param3.nextInt(5);

                            for(var9 += param3.nextInt(5) - param3.nextInt(5);
                                var8 < var2 || var8 >= var2 + 16 || var9 < var3 || var9 >= var3 + 16;
                                var9 = var11 + param3.nextInt(5) - param3.nextInt(5)
                            ) {
                                var8 = var10 + param3.nextInt(5) - param3.nextInt(5);
                            }
                        }
                    }
                }
            }

        }
    }

    private static BlockPos getTopNonCollidingPos(LevelReader param0, EntityType<?> param1, int param2, int param3) {
        int var0 = param0.getHeight(SpawnPlacements.getHeightmapType(param1), param2, param3);
        BlockPos.MutableBlockPos var1 = new BlockPos.MutableBlockPos(param2, var0, param3);
        if (param0.dimensionType().hasCeiling()) {
            do {
                var1.move(Direction.DOWN);
            } while(!param0.getBlockState(var1).isAir());

            do {
                var1.move(Direction.DOWN);
            } while(param0.getBlockState(var1).isAir() && var1.getY() > param0.getMinBuildHeight());
        }

        if (SpawnPlacements.getPlacementType(param1) == SpawnPlacements.Type.ON_GROUND) {
            BlockPos var2 = var1.below();
            if (param0.getBlockState(var2).isPathfindable(param0, var2, PathComputationType.LAND)) {
                return var2;
            }
        }

        return var1.immutable();
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
        private final LocalMobCapCalculator localMobCapCalculator;
        @Nullable
        private BlockPos lastCheckedPos;
        @Nullable
        private EntityType<?> lastCheckedType;
        private double lastCharge;

        SpawnState(int param0, Object2IntOpenHashMap<MobCategory> param1, PotentialCalculator param2, LocalMobCapCalculator param3) {
            this.spawnableChunkCount = param0;
            this.mobCategoryCounts = param1;
            this.spawnPotential = param2;
            this.localMobCapCalculator = param3;
            this.unmodifiableMobCategoryCounts = Object2IntMaps.unmodifiable(param1);
        }

        private boolean canSpawn(EntityType<?> param0, BlockPos param1, ChunkAccess param2) {
            this.lastCheckedPos = param1;
            this.lastCheckedType = param0;
            MobSpawnSettings.MobSpawnCost var0 = NaturalSpawner.getRoughBiome(param1, param2).getMobSettings().getMobSpawnCost(param0);
            if (var0 == null) {
                this.lastCharge = 0.0;
                return true;
            } else {
                double var1 = var0.getCharge();
                this.lastCharge = var1;
                double var2 = this.spawnPotential.getPotentialEnergyChange(param1, var1);
                return var2 <= var0.getEnergyBudget();
            }
        }

        private void afterSpawn(Mob param0, ChunkAccess param1) {
            EntityType<?> var0 = param0.getType();
            BlockPos var1 = param0.blockPosition();
            double var2;
            if (var1.equals(this.lastCheckedPos) && var0 == this.lastCheckedType) {
                var2 = this.lastCharge;
            } else {
                MobSpawnSettings.MobSpawnCost var3 = NaturalSpawner.getRoughBiome(var1, param1).getMobSettings().getMobSpawnCost(var0);
                if (var3 != null) {
                    var2 = var3.getCharge();
                } else {
                    var2 = 0.0;
                }
            }

            this.spawnPotential.addCharge(var1, var2);
            MobCategory var6 = var0.getCategory();
            this.mobCategoryCounts.addTo(var6, 1);
            this.localMobCapCalculator.addMob(new ChunkPos(var1), var6);
        }

        public int getSpawnableChunkCount() {
            return this.spawnableChunkCount;
        }

        public Object2IntMap<MobCategory> getMobCategoryCounts() {
            return this.unmodifiableMobCategoryCounts;
        }

        boolean canSpawnForCategory(MobCategory param0, ChunkPos param1) {
            int var0 = param0.getMaxInstancesPerChunk() * this.spawnableChunkCount / NaturalSpawner.MAGIC_NUMBER;
            if (this.mobCategoryCounts.getInt(param0) >= var0) {
                return false;
            } else {
                return this.localMobCapCalculator.canSpawn(param0, param1);
            }
        }
    }
}
