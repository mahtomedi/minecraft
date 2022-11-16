package net.minecraft.world.level;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.AbortableIterationConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.redstone.CollectingNeighborUpdater;
import net.minecraft.world.level.redstone.NeighborUpdater;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;

public abstract class Level implements AutoCloseable, LevelAccessor {
    public static final Codec<ResourceKey<Level>> RESOURCE_KEY_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final ResourceKey<Level> OVERWORLD = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("overworld"));
    public static final ResourceKey<Level> NETHER = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("the_nether"));
    public static final ResourceKey<Level> END = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("the_end"));
    public static final int MAX_LEVEL_SIZE = 30000000;
    public static final int LONG_PARTICLE_CLIP_RANGE = 512;
    public static final int SHORT_PARTICLE_CLIP_RANGE = 32;
    private static final Direction[] DIRECTIONS = Direction.values();
    public static final int MAX_BRIGHTNESS = 15;
    public static final int TICKS_PER_DAY = 24000;
    public static final int MAX_ENTITY_SPAWN_Y = 20000000;
    public static final int MIN_ENTITY_SPAWN_Y = -20000000;
    protected final List<TickingBlockEntity> blockEntityTickers = Lists.newArrayList();
    protected final NeighborUpdater neighborUpdater;
    private final List<TickingBlockEntity> pendingBlockEntityTickers = Lists.newArrayList();
    private boolean tickingBlockEntities;
    private final Thread thread;
    private final boolean isDebug;
    private int skyDarken;
    protected int randValue = RandomSource.create().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    public final RandomSource random = RandomSource.create();
    @Deprecated
    private final RandomSource threadSafeRandom = RandomSource.createThreadSafe();
    private final ResourceKey<DimensionType> dimensionTypeId;
    private final Holder<DimensionType> dimensionTypeRegistration;
    protected final WritableLevelData levelData;
    private final Supplier<ProfilerFiller> profiler;
    public final boolean isClientSide;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;
    private final ResourceKey<Level> dimension;
    private long subTickCount;

    protected Level(
        WritableLevelData param0,
        ResourceKey<Level> param1,
        Holder<DimensionType> param2,
        Supplier<ProfilerFiller> param3,
        boolean param4,
        boolean param5,
        long param6,
        int param7
    ) {
        this.profiler = param3;
        this.levelData = param0;
        this.dimensionTypeRegistration = param2;
        this.dimensionTypeId = param2.unwrapKey().orElseThrow(() -> new IllegalArgumentException("Dimension must be registered, got " + param2));
        final DimensionType var0 = param2.value();
        this.dimension = param1;
        this.isClientSide = param4;
        if (var0.coordinateScale() != 1.0) {
            this.worldBorder = new WorldBorder() {
                @Override
                public double getCenterX() {
                    return super.getCenterX() / var0.coordinateScale();
                }

                @Override
                public double getCenterZ() {
                    return super.getCenterZ() / var0.coordinateScale();
                }
            };
        } else {
            this.worldBorder = new WorldBorder();
        }

        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, param6);
        this.isDebug = param5;
        this.neighborUpdater = new CollectingNeighborUpdater(this, param7);
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }

    public boolean isInWorldBounds(BlockPos param0) {
        return !this.isOutsideBuildHeight(param0) && isInWorldBoundsHorizontal(param0);
    }

    public static boolean isInSpawnableBounds(BlockPos param0) {
        return !isOutsideSpawnableHeight(param0.getY()) && isInWorldBoundsHorizontal(param0);
    }

    private static boolean isInWorldBoundsHorizontal(BlockPos param0) {
        return param0.getX() >= -30000000 && param0.getZ() >= -30000000 && param0.getX() < 30000000 && param0.getZ() < 30000000;
    }

    private static boolean isOutsideSpawnableHeight(int param0) {
        return param0 < -20000000 || param0 >= 20000000;
    }

    public LevelChunk getChunkAt(BlockPos param0) {
        return this.getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()));
    }

    public LevelChunk getChunk(int param0, int param1) {
        return (LevelChunk)this.getChunk(param0, param1, ChunkStatus.FULL);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        ChunkAccess var0 = this.getChunkSource().getChunk(param0, param1, param2, param3);
        if (var0 == null && param3) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return var0;
        }
    }

    @Override
    public boolean setBlock(BlockPos param0, BlockState param1, int param2) {
        return this.setBlock(param0, param1, param2, 512);
    }

    @Override
    public boolean setBlock(BlockPos param0, BlockState param1, int param2, int param3) {
        if (this.isOutsideBuildHeight(param0)) {
            return false;
        } else if (!this.isClientSide && this.isDebug()) {
            return false;
        } else {
            LevelChunk var0 = this.getChunkAt(param0);
            Block var1 = param1.getBlock();
            BlockState var2 = var0.setBlockState(param0, param1, (param2 & 64) != 0);
            if (var2 == null) {
                return false;
            } else {
                BlockState var3 = this.getBlockState(param0);
                if ((param2 & 128) == 0
                    && var3 != var2
                    && (
                        var3.getLightBlock(this, param0) != var2.getLightBlock(this, param0)
                            || var3.getLightEmission() != var2.getLightEmission()
                            || var3.useShapeForLightOcclusion()
                            || var2.useShapeForLightOcclusion()
                    )) {
                    this.getProfiler().push("queueCheckLight");
                    this.getChunkSource().getLightEngine().checkBlock(param0);
                    this.getProfiler().pop();
                }

                if (var3 == param1) {
                    if (var2 != var3) {
                        this.setBlocksDirty(param0, var2, var3);
                    }

                    if ((param2 & 2) != 0
                        && (!this.isClientSide || (param2 & 4) == 0)
                        && (this.isClientSide || var0.getFullStatus() != null && var0.getFullStatus().isOrAfter(ChunkHolder.FullChunkStatus.TICKING))) {
                        this.sendBlockUpdated(param0, var2, param1, param2);
                    }

                    if ((param2 & 1) != 0) {
                        this.blockUpdated(param0, var2.getBlock());
                        if (!this.isClientSide && param1.hasAnalogOutputSignal()) {
                            this.updateNeighbourForOutputSignal(param0, var1);
                        }
                    }

                    if ((param2 & 16) == 0 && param3 > 0) {
                        int var4 = param2 & -34;
                        var2.updateIndirectNeighbourShapes(this, param0, var4, param3 - 1);
                        param1.updateNeighbourShapes(this, param0, var4, param3 - 1);
                        param1.updateIndirectNeighbourShapes(this, param0, var4, param3 - 1);
                    }

                    this.onBlockStateChange(param0, var2, var3);
                }

                return true;
            }
        }
    }

    public void onBlockStateChange(BlockPos param0, BlockState param1, BlockState param2) {
    }

    @Override
    public boolean removeBlock(BlockPos param0, boolean param1) {
        FluidState var0 = this.getFluidState(param0);
        return this.setBlock(param0, var0.createLegacyBlock(), 3 | (param1 ? 64 : 0));
    }

    @Override
    public boolean destroyBlock(BlockPos param0, boolean param1, @Nullable Entity param2, int param3) {
        BlockState var0 = this.getBlockState(param0);
        if (var0.isAir()) {
            return false;
        } else {
            FluidState var1 = this.getFluidState(param0);
            if (!(var0.getBlock() instanceof BaseFireBlock)) {
                this.levelEvent(2001, param0, Block.getId(var0));
            }

            if (param1) {
                BlockEntity var2 = var0.hasBlockEntity() ? this.getBlockEntity(param0) : null;
                Block.dropResources(var0, this, param0, var2, param2, ItemStack.EMPTY);
            }

            boolean var3 = this.setBlock(param0, var1.createLegacyBlock(), 3, param3);
            if (var3) {
                this.gameEvent(GameEvent.BLOCK_DESTROY, param0, GameEvent.Context.of(param2, var0));
            }

            return var3;
        }
    }

    public void addDestroyBlockEffect(BlockPos param0, BlockState param1) {
    }

    public boolean setBlockAndUpdate(BlockPos param0, BlockState param1) {
        return this.setBlock(param0, param1, 3);
    }

    public abstract void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4);

    public void setBlocksDirty(BlockPos param0, BlockState param1, BlockState param2) {
    }

    public void updateNeighborsAt(BlockPos param0, Block param1) {
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos param0, Block param1, Direction param2) {
    }

    public void neighborChanged(BlockPos param0, Block param1, BlockPos param2) {
    }

    public void neighborChanged(BlockState param0, BlockPos param1, Block param2, BlockPos param3, boolean param4) {
    }

    @Override
    public void neighborShapeChanged(Direction param0, BlockState param1, BlockPos param2, BlockPos param3, int param4, int param5) {
        this.neighborUpdater.shapeUpdate(param0, param1, param2, param3, param4, param5);
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        int var1;
        if (param1 >= -30000000 && param2 >= -30000000 && param1 < 30000000 && param2 < 30000000) {
            if (this.hasChunk(SectionPos.blockToSectionCoord(param1), SectionPos.blockToSectionCoord(param2))) {
                var1 = this.getChunk(SectionPos.blockToSectionCoord(param1), SectionPos.blockToSectionCoord(param2))
                        .getHeight(param0, param1 & 15, param2 & 15)
                    + 1;
            } else {
                var1 = this.getMinBuildHeight();
            }
        } else {
            var1 = this.getSeaLevel() + 1;
        }

        return var1;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.getChunkSource().getLightEngine();
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        if (this.isOutsideBuildHeight(param0)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunk var0 = this.getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()));
            return var0.getBlockState(param0);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        if (this.isOutsideBuildHeight(param0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunk var0 = this.getChunkAt(param0);
            return var0.getFluidState(param0);
        }
    }

    public boolean isDay() {
        return !this.dimensionType().hasFixedTime() && this.skyDarken < 4;
    }

    public boolean isNight() {
        return !this.dimensionType().hasFixedTime() && !this.isDay();
    }

    public void playSound(@Nullable Entity param0, BlockPos param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        this.playSound(param0 instanceof Player var0 ? var0 : null, param1, param2, param3, param4, param5);
    }

    @Override
    public void playSound(@Nullable Player param0, BlockPos param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        this.playSound(param0, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, param2, param3, param4, param5);
    }

    public abstract void playSeededSound(
        @Nullable Player var1, double var2, double var4, double var6, SoundEvent var8, SoundSource var9, float var10, float var11, long var12
    );

    public abstract void playSeededSound(@Nullable Player var1, Entity var2, SoundEvent var3, SoundSource var4, float var5, float var6, long var7);

    public void playSound(
        @Nullable Player param0, double param1, double param2, double param3, SoundEvent param4, SoundSource param5, float param6, float param7
    ) {
        this.playSeededSound(param0, param1, param2, param3, param4, param5, param6, param7, this.threadSafeRandom.nextLong());
    }

    public void playSound(@Nullable Player param0, Entity param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        this.playSeededSound(param0, param1, param2, param3, param4, param5, this.threadSafeRandom.nextLong());
    }

    public void playLocalSound(BlockPos param0, SoundEvent param1, SoundSource param2, float param3, float param4, boolean param5) {
        this.playLocalSound((double)param0.getX() + 0.5, (double)param0.getY() + 0.5, (double)param0.getZ() + 0.5, param1, param2, param3, param4, param5);
    }

    public void playLocalSound(double param0, double param1, double param2, SoundEvent param3, SoundSource param4, float param5, float param6, boolean param7) {
    }

    @Override
    public void addParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
    }

    public void addParticle(ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7) {
    }

    public void addAlwaysVisibleParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
    }

    public void addAlwaysVisibleParticle(
        ParticleOptions param0, boolean param1, double param2, double param3, double param4, double param5, double param6, double param7
    ) {
    }

    public float getSunAngle(float param0) {
        float var0 = this.getTimeOfDay(param0);
        return var0 * (float) (Math.PI * 2);
    }

    public void addBlockEntityTicker(TickingBlockEntity param0) {
        (this.tickingBlockEntities ? this.pendingBlockEntityTickers : this.blockEntityTickers).add(param0);
    }

    protected void tickBlockEntities() {
        ProfilerFiller var0 = this.getProfiler();
        var0.push("blockEntities");
        this.tickingBlockEntities = true;
        if (!this.pendingBlockEntityTickers.isEmpty()) {
            this.blockEntityTickers.addAll(this.pendingBlockEntityTickers);
            this.pendingBlockEntityTickers.clear();
        }

        Iterator<TickingBlockEntity> var1 = this.blockEntityTickers.iterator();

        while(var1.hasNext()) {
            TickingBlockEntity var2 = var1.next();
            if (var2.isRemoved()) {
                var1.remove();
            } else if (this.shouldTickBlocksAt(var2.getPos())) {
                var2.tick();
            }
        }

        this.tickingBlockEntities = false;
        var0.pop();
    }

    public <T extends Entity> void guardEntityTick(Consumer<T> param0, T param1) {
        try {
            param0.accept(param1);
        } catch (Throwable var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, "Ticking entity");
            CrashReportCategory var2 = var1.addCategory("Entity being ticked");
            param1.fillCrashReportCategory(var2);
            throw new ReportedException(var1);
        }
    }

    public boolean shouldTickDeath(Entity param0) {
        return true;
    }

    public boolean shouldTickBlocksAt(long param0) {
        return true;
    }

    public boolean shouldTickBlocksAt(BlockPos param0) {
        return this.shouldTickBlocksAt(ChunkPos.asLong(param0));
    }

    public Explosion explode(@Nullable Entity param0, double param1, double param2, double param3, float param4, Level.ExplosionInteraction param5) {
        return this.explode(param0, null, null, param1, param2, param3, param4, false, param5);
    }

    public Explosion explode(
        @Nullable Entity param0, double param1, double param2, double param3, float param4, boolean param5, Level.ExplosionInteraction param6
    ) {
        return this.explode(param0, null, null, param1, param2, param3, param4, param5, param6);
    }

    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        @Nullable ExplosionDamageCalculator param2,
        Vec3 param3,
        float param4,
        boolean param5,
        Level.ExplosionInteraction param6
    ) {
        return this.explode(param0, param1, param2, param3.x(), param3.y(), param3.z(), param4, param5, param6);
    }

    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        @Nullable ExplosionDamageCalculator param2,
        double param3,
        double param4,
        double param5,
        float param6,
        boolean param7,
        Level.ExplosionInteraction param8
    ) {
        return this.explode(param0, param1, param2, param3, param4, param5, param6, param7, param8, true);
    }

    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        @Nullable ExplosionDamageCalculator param2,
        double param3,
        double param4,
        double param5,
        float param6,
        boolean param7,
        Level.ExplosionInteraction param8,
        boolean param9
    ) {
        Explosion.BlockInteraction var0 = switch(param8) {
            case NONE -> Explosion.BlockInteraction.KEEP;
            case BLOCK -> this.getDestroyType(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY);
            case MOB -> this.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
            ? this.getDestroyType(GameRules.RULE_MOB_EXPLOSION_DROP_DECAY)
            : Explosion.BlockInteraction.KEEP;
            case TNT -> this.getDestroyType(GameRules.RULE_TNT_EXPLOSION_DROP_DECAY);
        };
        Explosion var1 = new Explosion(this, param0, param1, param2, param3, param4, param5, param6, param7, var0);
        var1.explode();
        var1.finalizeExplosion(param9);
        return var1;
    }

    private Explosion.BlockInteraction getDestroyType(GameRules.Key<GameRules.BooleanValue> param0) {
        return this.getGameRules().getBoolean(param0) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
    }

    public abstract String gatherChunkSourceStats();

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        if (this.isOutsideBuildHeight(param0)) {
            return null;
        } else {
            return !this.isClientSide && Thread.currentThread() != this.thread
                ? null
                : this.getChunkAt(param0).getBlockEntity(param0, LevelChunk.EntityCreationType.IMMEDIATE);
        }
    }

    public void setBlockEntity(BlockEntity param0) {
        BlockPos var0 = param0.getBlockPos();
        if (!this.isOutsideBuildHeight(var0)) {
            this.getChunkAt(var0).addAndRegisterBlockEntity(param0);
        }
    }

    public void removeBlockEntity(BlockPos param0) {
        if (!this.isOutsideBuildHeight(param0)) {
            this.getChunkAt(param0).removeBlockEntity(param0);
        }
    }

    public boolean isLoaded(BlockPos param0) {
        return this.isOutsideBuildHeight(param0)
            ? false
            : this.getChunkSource().hasChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()));
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos param0, Entity param1, Direction param2) {
        if (this.isOutsideBuildHeight(param0)) {
            return false;
        } else {
            ChunkAccess var0 = this.getChunk(
                SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()), ChunkStatus.FULL, false
            );
            return var0 == null ? false : var0.getBlockState(param0).entityCanStandOnFace(this, param0, param1, param2);
        }
    }

    public boolean loadedAndEntityCanStandOn(BlockPos param0, Entity param1) {
        return this.loadedAndEntityCanStandOnFace(param0, param1, Direction.UP);
    }

    public void updateSkyBrightness() {
        double var0 = 1.0 - (double)(this.getRainLevel(1.0F) * 5.0F) / 16.0;
        double var1 = 1.0 - (double)(this.getThunderLevel(1.0F) * 5.0F) / 16.0;
        double var2 = 0.5 + 2.0 * Mth.clamp((double)Mth.cos(this.getTimeOfDay(1.0F) * (float) (Math.PI * 2)), -0.25, 0.25);
        this.skyDarken = (int)((1.0 - var2 * var0 * var1) * 11.0);
    }

    public void setSpawnSettings(boolean param0, boolean param1) {
        this.getChunkSource().setSpawnSettings(param0, param1);
    }

    public BlockPos getSharedSpawnPos() {
        BlockPos var0 = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(var0)) {
            var0 = this.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ())
            );
        }

        return var0;
    }

    public float getSharedSpawnAngle() {
        return this.levelData.getSpawnAngle();
    }

    protected void prepareWeather() {
        if (this.levelData.isRaining()) {
            this.rainLevel = 1.0F;
            if (this.levelData.isThundering()) {
                this.thunderLevel = 1.0F;
            }
        }

    }

    @Override
    public void close() throws IOException {
        this.getChunkSource().close();
    }

    @Nullable
    @Override
    public BlockGetter getChunkForCollisions(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity param0, AABB param1, Predicate<? super Entity> param2) {
        this.getProfiler().incrementCounter("getEntities");
        List<Entity> var0 = Lists.newArrayList();
        this.getEntities().get(param1, param3 -> {
            if (param3 != param0 && param2.test(param3)) {
                var0.add(param3);
            }

            if (param3 instanceof EnderDragon) {
                for(EnderDragonPart var0x : ((EnderDragon)param3).getSubEntities()) {
                    if (param3 != param0 && param2.test(var0x)) {
                        var0.add(var0x);
                    }
                }
            }

        });
        return var0;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> param0, AABB param1, Predicate<? super T> param2) {
        List<T> var0 = Lists.newArrayList();
        this.getEntities(param0, param1, param2, var0);
        return var0;
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> param0, AABB param1, Predicate<? super T> param2, List<? super T> param3) {
        this.getEntities(param0, param1, param2, param3, Integer.MAX_VALUE);
    }

    public <T extends Entity> void getEntities(EntityTypeTest<Entity, T> param0, AABB param1, Predicate<? super T> param2, List<? super T> param3, int param4) {
        this.getProfiler().incrementCounter("getEntities");
        this.getEntities().get(param0, param1, param4x -> {
            if (param2.test(param4x)) {
                param3.add(param4x);
                if (param3.size() >= param4) {
                    return AbortableIterationConsumer.Continuation.ABORT;
                }
            }

            if (param4x instanceof EnderDragon var0) {
                for(EnderDragonPart var1x : var0.getSubEntities()) {
                    T var2x = param0.tryCast(var1x);
                    if (var2x != null && param2.test((T)var2x)) {
                        param3.add((T)var2x);
                        if (param3.size() >= param4) {
                            return AbortableIterationConsumer.Continuation.ABORT;
                        }
                    }
                }
            }

            return AbortableIterationConsumer.Continuation.CONTINUE;
        });
    }

    @Nullable
    public abstract Entity getEntity(int var1);

    public void blockEntityChanged(BlockPos param0) {
        if (this.hasChunkAt(param0)) {
            this.getChunkAt(param0).setUnsaved(true);
        }

    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    public int getDirectSignalTo(BlockPos param0) {
        int var0 = 0;
        var0 = Math.max(var0, this.getDirectSignal(param0.below(), Direction.DOWN));
        if (var0 >= 15) {
            return var0;
        } else {
            var0 = Math.max(var0, this.getDirectSignal(param0.above(), Direction.UP));
            if (var0 >= 15) {
                return var0;
            } else {
                var0 = Math.max(var0, this.getDirectSignal(param0.north(), Direction.NORTH));
                if (var0 >= 15) {
                    return var0;
                } else {
                    var0 = Math.max(var0, this.getDirectSignal(param0.south(), Direction.SOUTH));
                    if (var0 >= 15) {
                        return var0;
                    } else {
                        var0 = Math.max(var0, this.getDirectSignal(param0.west(), Direction.WEST));
                        if (var0 >= 15) {
                            return var0;
                        } else {
                            var0 = Math.max(var0, this.getDirectSignal(param0.east(), Direction.EAST));
                            return var0 >= 15 ? var0 : var0;
                        }
                    }
                }
            }
        }
    }

    public boolean hasSignal(BlockPos param0, Direction param1) {
        return this.getSignal(param0, param1) > 0;
    }

    public int getSignal(BlockPos param0, Direction param1) {
        BlockState var0 = this.getBlockState(param0);
        int var1 = var0.getSignal(this, param0, param1);
        return var0.isRedstoneConductor(this, param0) ? Math.max(var1, this.getDirectSignalTo(param0)) : var1;
    }

    public boolean hasNeighborSignal(BlockPos param0) {
        if (this.getSignal(param0.below(), Direction.DOWN) > 0) {
            return true;
        } else if (this.getSignal(param0.above(), Direction.UP) > 0) {
            return true;
        } else if (this.getSignal(param0.north(), Direction.NORTH) > 0) {
            return true;
        } else if (this.getSignal(param0.south(), Direction.SOUTH) > 0) {
            return true;
        } else if (this.getSignal(param0.west(), Direction.WEST) > 0) {
            return true;
        } else {
            return this.getSignal(param0.east(), Direction.EAST) > 0;
        }
    }

    public int getBestNeighborSignal(BlockPos param0) {
        int var0 = 0;

        for(Direction var1 : DIRECTIONS) {
            int var2 = this.getSignal(param0.relative(var1), var1);
            if (var2 >= 15) {
                return 15;
            }

            if (var2 > var0) {
                var0 = var2;
            }
        }

        return var0;
    }

    public void disconnect() {
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public boolean mayInteract(Player param0, BlockPos param1) {
        return true;
    }

    public void broadcastEntityEvent(Entity param0, byte param1) {
    }

    public void blockEvent(BlockPos param0, Block param1, int param2, int param3) {
        this.getBlockState(param0).triggerEvent(this, param0, param2, param3);
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    public GameRules getGameRules() {
        return this.levelData.getGameRules();
    }

    public float getThunderLevel(float param0) {
        return Mth.lerp(param0, this.oThunderLevel, this.thunderLevel) * this.getRainLevel(param0);
    }

    public void setThunderLevel(float param0) {
        float var0 = Mth.clamp(param0, 0.0F, 1.0F);
        this.oThunderLevel = var0;
        this.thunderLevel = var0;
    }

    public float getRainLevel(float param0) {
        return Mth.lerp(param0, this.oRainLevel, this.rainLevel);
    }

    public void setRainLevel(float param0) {
        float var0 = Mth.clamp(param0, 0.0F, 1.0F);
        this.oRainLevel = var0;
        this.rainLevel = var0;
    }

    public boolean isThundering() {
        if (this.dimensionType().hasSkyLight() && !this.dimensionType().hasCeiling()) {
            return (double)this.getThunderLevel(1.0F) > 0.9;
        } else {
            return false;
        }
    }

    public boolean isRaining() {
        return (double)this.getRainLevel(1.0F) > 0.2;
    }

    public boolean isRainingAt(BlockPos param0) {
        if (!this.isRaining()) {
            return false;
        } else if (!this.canSeeSky(param0)) {
            return false;
        } else if (this.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, param0).getY() > param0.getY()) {
            return false;
        } else {
            Biome var0 = this.getBiome(param0).value();
            return var0.getPrecipitation() == Biome.Precipitation.RAIN && var0.warmEnoughToRain(param0);
        }
    }

    public boolean isHumidAt(BlockPos param0) {
        Biome var0 = this.getBiome(param0).value();
        return var0.isHumid();
    }

    @Nullable
    public abstract MapItemSavedData getMapData(String var1);

    public abstract void setMapData(String var1, MapItemSavedData var2);

    public abstract int getFreeMapId();

    public void globalLevelEvent(int param0, BlockPos param1, int param2) {
    }

    public CrashReportCategory fillReportDetails(CrashReport param0) {
        CrashReportCategory var0 = param0.addCategory("Affected level", 1);
        var0.setDetail("All players", () -> this.players().size() + " total; " + this.players());
        var0.setDetail("Chunk stats", this.getChunkSource()::gatherStats);
        var0.setDetail("Level dimension", () -> this.dimension().location().toString());

        try {
            this.levelData.fillCrashReportCategory(var0, this);
        } catch (Throwable var4) {
            var0.setDetailError("Level Data Unobtainable", var4);
        }

        return var0;
    }

    public abstract void destroyBlockProgress(int var1, BlockPos var2, int var3);

    public void createFireworks(double param0, double param1, double param2, double param3, double param4, double param5, @Nullable CompoundTag param6) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos param0, Block param1) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            BlockPos var1 = param0.relative(var0);
            if (this.hasChunkAt(var1)) {
                BlockState var2 = this.getBlockState(var1);
                if (var2.is(Blocks.COMPARATOR)) {
                    this.neighborChanged(var2, var1, param1, param0, false);
                } else if (var2.isRedstoneConductor(this, var1)) {
                    var1 = var1.relative(var0);
                    var2 = this.getBlockState(var1);
                    if (var2.is(Blocks.COMPARATOR)) {
                        this.neighborChanged(var2, var1, param1, param0, false);
                    }
                }
            }
        }

    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos param0) {
        long var0 = 0L;
        float var1 = 0.0F;
        if (this.hasChunkAt(param0)) {
            var1 = this.getMoonBrightness();
            var0 = this.getChunkAt(param0).getInhabitedTime();
        }

        return new DifficultyInstance(this.getDifficulty(), this.getDayTime(), var0, var1);
    }

    @Override
    public int getSkyDarken() {
        return this.skyDarken;
    }

    public void setSkyFlashTime(int param0) {
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.worldBorder;
    }

    public void sendPacketToServer(Packet<?> param0) {
        throw new UnsupportedOperationException("Can't send packets to server unless you're on the client.");
    }

    @Override
    public DimensionType dimensionType() {
        return this.dimensionTypeRegistration.value();
    }

    public ResourceKey<DimensionType> dimensionTypeId() {
        return this.dimensionTypeId;
    }

    public Holder<DimensionType> dimensionTypeRegistration() {
        return this.dimensionTypeRegistration;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPos param0, Predicate<BlockState> param1) {
        return param1.test(this.getBlockState(param0));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos param0, Predicate<FluidState> param1) {
        return param1.test(this.getFluidState(param0));
    }

    public abstract RecipeManager getRecipeManager();

    public BlockPos getBlockRandomPos(int param0, int param1, int param2, int param3) {
        this.randValue = this.randValue * 3 + 1013904223;
        int var0 = this.randValue >> 2;
        return new BlockPos(param0 + (var0 & 15), param1 + (var0 >> 16 & param3), param2 + (var0 >> 8 & 15));
    }

    public boolean noSave() {
        return false;
    }

    public ProfilerFiller getProfiler() {
        return this.profiler.get();
    }

    public Supplier<ProfilerFiller> getProfilerSupplier() {
        return this.profiler;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    public final boolean isDebug() {
        return this.isDebug;
    }

    protected abstract LevelEntityGetter<Entity> getEntities();

    @Override
    public long nextSubTickCount() {
        return (long)(this.subTickCount++);
    }

    public static enum ExplosionInteraction {
        NONE,
        BLOCK,
        MOB,
        TNT;
    }
}
