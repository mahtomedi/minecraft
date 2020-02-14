package net.minecraft.world.level;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagManager;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockMaterialPredicate;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Level implements AutoCloseable, LevelAccessor {
    protected static final Logger LOGGER = LogManager.getLogger();
    private static final Direction[] DIRECTIONS = Direction.values();
    public final List<BlockEntity> blockEntityList = Lists.newArrayList();
    public final List<BlockEntity> tickableBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> pendingBlockEntities = Lists.newArrayList();
    protected final List<BlockEntity> blockEntitiesToUnload = Lists.newArrayList();
    private final Thread thread;
    private int skyDarken;
    protected int randValue = new Random().nextInt();
    protected final int addend = 1013904223;
    protected float oRainLevel;
    protected float rainLevel;
    protected float oThunderLevel;
    protected float thunderLevel;
    public final Random random = new Random();
    public final Dimension dimension;
    protected final ChunkSource chunkSource;
    protected final LevelData levelData;
    private final Supplier<ProfilerFiller> profiler;
    public final boolean isClientSide;
    protected boolean updatingBlockEntities;
    private final WorldBorder worldBorder;
    private final BiomeManager biomeManager;

    protected Level(LevelData param0, DimensionType param1, BiFunction<Level, Dimension, ChunkSource> param2, Supplier<ProfilerFiller> param3, boolean param4) {
        this.profiler = param3;
        this.levelData = param0;
        this.dimension = param1.create(this);
        this.chunkSource = param2.apply(this, this.dimension);
        this.isClientSide = param4;
        this.worldBorder = this.dimension.createWorldBorder();
        this.thread = Thread.currentThread();
        this.biomeManager = new BiomeManager(this, param4 ? param0.getSeed() : LevelData.obfuscateSeed(param0.getSeed()), param1.getBiomeZoomer());
    }

    @Override
    public boolean isClientSide() {
        return this.isClientSide;
    }

    @Nullable
    public MinecraftServer getServer() {
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public void validateSpawn() {
        this.setSpawnPos(new BlockPos(8, 64, 8));
    }

    public BlockState getTopBlockState(BlockPos param0) {
        BlockPos var0 = new BlockPos(param0.getX(), this.getSeaLevel(), param0.getZ());

        while(!this.isEmptyBlock(var0.above())) {
            var0 = var0.above();
        }

        return this.getBlockState(var0);
    }

    public static boolean isInWorldBounds(BlockPos param0) {
        return !isOutsideBuildHeight(param0) && isInWorldBoundsHorizontal(param0);
    }

    public static boolean isInWorldBoundsHorizontal(BlockPos param0) {
        return param0.getX() >= -30000000 && param0.getZ() >= -30000000 && param0.getX() < 30000000 && param0.getZ() < 30000000;
    }

    public static boolean isOutsideBuildHeight(BlockPos param0) {
        return isOutsideBuildHeight(param0.getY());
    }

    public static boolean isOutsideBuildHeight(int param0) {
        return param0 < 0 || param0 >= 256;
    }

    public LevelChunk getChunkAt(BlockPos param0) {
        return this.getChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    public LevelChunk getChunk(int param0, int param1) {
        return (LevelChunk)this.getChunk(param0, param1, ChunkStatus.FULL);
    }

    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        ChunkAccess var0 = this.chunkSource.getChunk(param0, param1, param2, param3);
        if (var0 == null && param3) {
            throw new IllegalStateException("Should always be able to create a chunk!");
        } else {
            return var0;
        }
    }

    @Override
    public boolean setBlock(BlockPos param0, BlockState param1, int param2) {
        if (isOutsideBuildHeight(param0)) {
            return false;
        } else if (!this.isClientSide && this.levelData.getGeneratorType() == LevelType.DEBUG_ALL_BLOCK_STATES) {
            return false;
        } else {
            LevelChunk var0 = this.getChunkAt(param0);
            Block var1 = param1.getBlock();
            BlockState var2 = var0.setBlockState(param0, param1, (param2 & 64) != 0);
            if (var2 == null) {
                return false;
            } else {
                BlockState var3 = this.getBlockState(param0);
                if (var3 != var2
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

                    if ((param2 & 16) == 0) {
                        int var4 = param2 & -2;
                        var2.updateIndirectNeighbourShapes(this, param0, var4);
                        param1.updateNeighbourShapes(this, param0, var4);
                        param1.updateIndirectNeighbourShapes(this, param0, var4);
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
    public boolean destroyBlock(BlockPos param0, boolean param1, @Nullable Entity param2) {
        BlockState var0 = this.getBlockState(param0);
        if (var0.isAir()) {
            return false;
        } else {
            FluidState var1 = this.getFluidState(param0);
            this.levelEvent(2001, param0, Block.getId(var0));
            if (param1) {
                BlockEntity var2 = var0.getBlock().isEntityBlock() ? this.getBlockEntity(param0) : null;
                Block.dropResources(var0, this, param0, var2, param2, ItemStack.EMPTY);
            }

            return this.setBlock(param0, var1.createLegacyBlock(), 3);
        }
    }

    public boolean setBlockAndUpdate(BlockPos param0, BlockState param1) {
        return this.setBlock(param0, param1, 3);
    }

    public abstract void sendBlockUpdated(BlockPos var1, BlockState var2, BlockState var3, int var4);

    public void setBlocksDirty(BlockPos param0, BlockState param1, BlockState param2) {
    }

    public void updateNeighborsAt(BlockPos param0, Block param1) {
        this.neighborChanged(param0.west(), param1, param0);
        this.neighborChanged(param0.east(), param1, param0);
        this.neighborChanged(param0.below(), param1, param0);
        this.neighborChanged(param0.above(), param1, param0);
        this.neighborChanged(param0.north(), param1, param0);
        this.neighborChanged(param0.south(), param1, param0);
    }

    public void updateNeighborsAtExceptFromFacing(BlockPos param0, Block param1, Direction param2) {
        if (param2 != Direction.WEST) {
            this.neighborChanged(param0.west(), param1, param0);
        }

        if (param2 != Direction.EAST) {
            this.neighborChanged(param0.east(), param1, param0);
        }

        if (param2 != Direction.DOWN) {
            this.neighborChanged(param0.below(), param1, param0);
        }

        if (param2 != Direction.UP) {
            this.neighborChanged(param0.above(), param1, param0);
        }

        if (param2 != Direction.NORTH) {
            this.neighborChanged(param0.north(), param1, param0);
        }

        if (param2 != Direction.SOUTH) {
            this.neighborChanged(param0.south(), param1, param0);
        }

    }

    public void neighborChanged(BlockPos param0, Block param1, BlockPos param2) {
        if (!this.isClientSide) {
            BlockState var0 = this.getBlockState(param0);

            try {
                var0.neighborChanged(this, param0, param1, param2, false);
            } catch (Throwable var8) {
                CrashReport var2 = CrashReport.forThrowable(var8, "Exception while updating neighbours");
                CrashReportCategory var3 = var2.addCategory("Block being updated");
                var3.setDetail(
                    "Source block type",
                    () -> {
                        try {
                            return String.format(
                                "ID #%s (%s // %s)", Registry.BLOCK.getKey(param1), param1.getDescriptionId(), param1.getClass().getCanonicalName()
                            );
                        } catch (Throwable var2x) {
                            return "ID #" + Registry.BLOCK.getKey(param1);
                        }
                    }
                );
                CrashReportCategory.populateBlockDetails(var3, param0, var0);
                throw new ReportedException(var2);
            }
        }
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        int var1;
        if (param1 >= -30000000 && param2 >= -30000000 && param1 < 30000000 && param2 < 30000000) {
            if (this.hasChunk(param1 >> 4, param2 >> 4)) {
                var1 = this.getChunk(param1 >> 4, param2 >> 4).getHeight(param0, param1 & 15, param2 & 15) + 1;
            } else {
                var1 = 0;
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
        if (isOutsideBuildHeight(param0)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunk var0 = this.getChunk(param0.getX() >> 4, param0.getZ() >> 4);
            return var0.getBlockState(param0);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        if (isOutsideBuildHeight(param0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunk var0 = this.getChunkAt(param0);
            return var0.getFluidState(param0);
        }
    }

    public boolean isDay() {
        return this.dimension.getType() == DimensionType.OVERWORLD && this.skyDarken < 4;
    }

    public boolean isNight() {
        return this.dimension.getType() == DimensionType.OVERWORLD && !this.isDay();
    }

    @Override
    public void playSound(@Nullable Player param0, BlockPos param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
        this.playSound(param0, (double)param1.getX() + 0.5, (double)param1.getY() + 0.5, (double)param1.getZ() + 0.5, param2, param3, param4, param5);
    }

    public abstract void playSound(@Nullable Player var1, double var2, double var4, double var6, SoundEvent var8, SoundSource var9, float var10, float var11);

    public abstract void playSound(@Nullable Player var1, Entity var2, SoundEvent var3, SoundSource var4, float var5, float var6);

    public void playLocalSound(double param0, double param1, double param2, SoundEvent param3, SoundSource param4, float param5, float param6, boolean param7) {
    }

    @Override
    public void addParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
    }

    @OnlyIn(Dist.CLIENT)
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

    public boolean addBlockEntity(BlockEntity param0) {
        if (this.updatingBlockEntities) {
            LOGGER.error("Adding block entity while ticking: {} @ {}", () -> Registry.BLOCK_ENTITY_TYPE.getKey(param0.getType()), param0::getBlockPos);
        }

        boolean var0 = this.blockEntityList.add(param0);
        if (var0 && param0 instanceof TickableBlockEntity) {
            this.tickableBlockEntities.add(param0);
        }

        if (this.isClientSide) {
            BlockPos var1 = param0.getBlockPos();
            BlockState var2 = this.getBlockState(var1);
            this.sendBlockUpdated(var1, var2, var2, 2);
        }

        return var0;
    }

    public void addAllPendingBlockEntities(Collection<BlockEntity> param0) {
        if (this.updatingBlockEntities) {
            this.pendingBlockEntities.addAll(param0);
        } else {
            for(BlockEntity var0 : param0) {
                this.addBlockEntity(var0);
            }
        }

    }

    public void tickBlockEntities() {
        ProfilerFiller var0 = this.getProfiler();
        var0.push("blockEntities");
        if (!this.blockEntitiesToUnload.isEmpty()) {
            this.tickableBlockEntities.removeAll(this.blockEntitiesToUnload);
            this.blockEntityList.removeAll(this.blockEntitiesToUnload);
            this.blockEntitiesToUnload.clear();
        }

        this.updatingBlockEntities = true;
        Iterator<BlockEntity> var1 = this.tickableBlockEntities.iterator();

        while(var1.hasNext()) {
            BlockEntity var2 = var1.next();
            if (!var2.isRemoved() && var2.hasLevel()) {
                BlockPos var3 = var2.getBlockPos();
                if (this.chunkSource.isTickingChunk(var3) && this.getWorldBorder().isWithinBounds(var3)) {
                    try {
                        var0.push(() -> String.valueOf(BlockEntityType.getKey(var2.getType())));
                        if (var2.getType().isValid(this.getBlockState(var3).getBlock())) {
                            ((TickableBlockEntity)var2).tick();
                        } else {
                            var2.logInvalidState();
                        }

                        var0.pop();
                    } catch (Throwable var81) {
                        CrashReport var5 = CrashReport.forThrowable(var81, "Ticking block entity");
                        CrashReportCategory var6 = var5.addCategory("Block entity being ticked");
                        var2.fillCrashReportCategory(var6);
                        throw new ReportedException(var5);
                    }
                }
            }

            if (var2.isRemoved()) {
                var1.remove();
                this.blockEntityList.remove(var2);
                if (this.hasChunkAt(var2.getBlockPos())) {
                    this.getChunkAt(var2.getBlockPos()).removeBlockEntity(var2.getBlockPos());
                }
            }
        }

        this.updatingBlockEntities = false;
        var0.popPush("pendingBlockEntities");
        if (!this.pendingBlockEntities.isEmpty()) {
            for(int var7 = 0; var7 < this.pendingBlockEntities.size(); ++var7) {
                BlockEntity var8 = this.pendingBlockEntities.get(var7);
                if (!var8.isRemoved()) {
                    if (!this.blockEntityList.contains(var8)) {
                        this.addBlockEntity(var8);
                    }

                    if (this.hasChunkAt(var8.getBlockPos())) {
                        LevelChunk var9 = this.getChunkAt(var8.getBlockPos());
                        BlockState var10 = var9.getBlockState(var8.getBlockPos());
                        var9.setBlockEntity(var8.getBlockPos(), var8);
                        this.sendBlockUpdated(var8.getBlockPos(), var10, var10, 3);
                    }
                }
            }

            this.pendingBlockEntities.clear();
        }

        var0.pop();
    }

    public void guardEntityTick(Consumer<Entity> param0, Entity param1) {
        try {
            param0.accept(param1);
        } catch (Throwable var6) {
            CrashReport var1 = CrashReport.forThrowable(var6, "Ticking entity");
            CrashReportCategory var2 = var1.addCategory("Entity being ticked");
            param1.fillCrashReportCategory(var2);
            throw new ReportedException(var1);
        }
    }

    public boolean containsAnyBlocks(AABB param0) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);

        try (BlockPos.PooledMutableBlockPos var6 = BlockPos.PooledMutableBlockPos.acquire()) {
            for(int var7 = var0; var7 < var1; ++var7) {
                for(int var8 = var2; var8 < var3; ++var8) {
                    for(int var9 = var4; var9 < var5; ++var9) {
                        BlockState var10 = this.getBlockState(var6.set(var7, var8, var9));
                        if (!var10.isAir()) {
                            return true;
                        }
                    }
                }
            }

            return false;
        }
    }

    public boolean containsFireBlock(AABB param0) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);
        if (this.hasChunksAt(var0, var2, var4, var1, var3, var5)) {
            try (BlockPos.PooledMutableBlockPos var6 = BlockPos.PooledMutableBlockPos.acquire()) {
                for(int var7 = var0; var7 < var1; ++var7) {
                    for(int var8 = var2; var8 < var3; ++var8) {
                        for(int var9 = var4; var9 < var5; ++var9) {
                            BlockState var10 = this.getBlockState(var6.set(var7, var8, var9));
                            if (var10.is(BlockTags.FIRE) || var10.getBlock() == Blocks.LAVA) {
                                return true;
                            }
                        }
                    }
                }

                return false;
            }
        } else {
            return false;
        }
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    public BlockState containsBlock(AABB param0, Block param1) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);
        if (this.hasChunksAt(var0, var2, var4, var1, var3, var5)) {
            try (BlockPos.PooledMutableBlockPos var6 = BlockPos.PooledMutableBlockPos.acquire()) {
                for(int var7 = var0; var7 < var1; ++var7) {
                    for(int var8 = var2; var8 < var3; ++var8) {
                        for(int var9 = var4; var9 < var5; ++var9) {
                            BlockState var10 = this.getBlockState(var6.set(var7, var8, var9));
                            if (var10.getBlock() == param1) {
                                return var10;
                            }
                        }
                    }
                }

                return null;
            }
        } else {
            return null;
        }
    }

    public boolean containsMaterial(AABB param0, Material param1) {
        int var0 = Mth.floor(param0.minX);
        int var1 = Mth.ceil(param0.maxX);
        int var2 = Mth.floor(param0.minY);
        int var3 = Mth.ceil(param0.maxY);
        int var4 = Mth.floor(param0.minZ);
        int var5 = Mth.ceil(param0.maxZ);
        BlockMaterialPredicate var6 = BlockMaterialPredicate.forMaterial(param1);
        return BlockPos.betweenClosedStream(var0, var2, var4, var1 - 1, var3 - 1, var5 - 1).anyMatch(param1x -> var6.test(this.getBlockState(param1x)));
    }

    public Explosion explode(@Nullable Entity param0, double param1, double param2, double param3, float param4, Explosion.BlockInteraction param5) {
        return this.explode(param0, null, param1, param2, param3, param4, false, param5);
    }

    public Explosion explode(
        @Nullable Entity param0, double param1, double param2, double param3, float param4, boolean param5, Explosion.BlockInteraction param6
    ) {
        return this.explode(param0, null, param1, param2, param3, param4, param5, param6);
    }

    public Explosion explode(
        @Nullable Entity param0,
        @Nullable DamageSource param1,
        double param2,
        double param3,
        double param4,
        float param5,
        boolean param6,
        Explosion.BlockInteraction param7
    ) {
        Explosion var0 = new Explosion(this, param0, param2, param3, param4, param5, param6, param7);
        if (param1 != null) {
            var0.setDamageSource(param1);
        }

        var0.explode();
        var0.finalizeExplosion(true);
        return var0;
    }

    public boolean extinguishFire(@Nullable Player param0, BlockPos param1, Direction param2) {
        param1 = param1.relative(param2);
        if (this.getBlockState(param1).is(BlockTags.FIRE)) {
            this.levelEvent(param0, 1009, param1, 0);
            this.removeBlock(param1, false);
            return true;
        } else {
            return false;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public String gatherChunkSourceStats() {
        return this.chunkSource.gatherStats();
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        if (isOutsideBuildHeight(param0)) {
            return null;
        } else if (!this.isClientSide && Thread.currentThread() != this.thread) {
            return null;
        } else {
            BlockEntity var0 = null;
            if (this.updatingBlockEntities) {
                var0 = this.getPendingBlockEntityAt(param0);
            }

            if (var0 == null) {
                var0 = this.getChunkAt(param0).getBlockEntity(param0, LevelChunk.EntityCreationType.IMMEDIATE);
            }

            if (var0 == null) {
                var0 = this.getPendingBlockEntityAt(param0);
            }

            return var0;
        }
    }

    @Nullable
    private BlockEntity getPendingBlockEntityAt(BlockPos param0) {
        for(int var0 = 0; var0 < this.pendingBlockEntities.size(); ++var0) {
            BlockEntity var1 = this.pendingBlockEntities.get(var0);
            if (!var1.isRemoved() && var1.getBlockPos().equals(param0)) {
                return var1;
            }
        }

        return null;
    }

    public void setBlockEntity(BlockPos param0, @Nullable BlockEntity param1) {
        if (!isOutsideBuildHeight(param0)) {
            if (param1 != null && !param1.isRemoved()) {
                if (this.updatingBlockEntities) {
                    param1.setLevelAndPosition(this, param0);
                    Iterator<BlockEntity> var0 = this.pendingBlockEntities.iterator();

                    while(var0.hasNext()) {
                        BlockEntity var1 = var0.next();
                        if (var1.getBlockPos().equals(param0)) {
                            var1.setRemoved();
                            var0.remove();
                        }
                    }

                    this.pendingBlockEntities.add(param1);
                } else {
                    this.getChunkAt(param0).setBlockEntity(param0, param1);
                    this.addBlockEntity(param1);
                }
            }

        }
    }

    public void removeBlockEntity(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        if (var0 != null && this.updatingBlockEntities) {
            var0.setRemoved();
            this.pendingBlockEntities.remove(var0);
        } else {
            if (var0 != null) {
                this.pendingBlockEntities.remove(var0);
                this.blockEntityList.remove(var0);
                this.tickableBlockEntities.remove(var0);
            }

            this.getChunkAt(param0).removeBlockEntity(param0);
        }

    }

    public boolean isLoaded(BlockPos param0) {
        return isOutsideBuildHeight(param0) ? false : this.chunkSource.hasChunk(param0.getX() >> 4, param0.getZ() >> 4);
    }

    public boolean loadedAndEntityCanStandOnFace(BlockPos param0, Entity param1, Direction param2) {
        if (isOutsideBuildHeight(param0)) {
            return false;
        } else {
            ChunkAccess var0 = this.getChunk(param0.getX() >> 4, param0.getZ() >> 4, ChunkStatus.FULL, false);
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
        this.chunkSource.close();
    }

    @Nullable
    @Override
    public BlockGetter getChunkForCollisions(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.FULL, false);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity param0, AABB param1, @Nullable Predicate<? super Entity> param2) {
        this.getProfiler().incrementCounter("getEntities");
        List<Entity> var0 = Lists.newArrayList();
        int var1 = Mth.floor((param1.minX - 2.0) / 16.0);
        int var2 = Mth.floor((param1.maxX + 2.0) / 16.0);
        int var3 = Mth.floor((param1.minZ - 2.0) / 16.0);
        int var4 = Mth.floor((param1.maxZ + 2.0) / 16.0);

        for(int var5 = var1; var5 <= var2; ++var5) {
            for(int var6 = var3; var6 <= var4; ++var6) {
                LevelChunk var7 = this.getChunkSource().getChunk(var5, var6, false);
                if (var7 != null) {
                    var7.getEntities(param0, param1, var0, param2);
                }
            }
        }

        return var0;
    }

    public <T extends Entity> List<T> getEntities(@Nullable EntityType<T> param0, AABB param1, Predicate<? super T> param2) {
        this.getProfiler().incrementCounter("getEntities");
        int var0 = Mth.floor((param1.minX - 2.0) / 16.0);
        int var1 = Mth.ceil((param1.maxX + 2.0) / 16.0);
        int var2 = Mth.floor((param1.minZ - 2.0) / 16.0);
        int var3 = Mth.ceil((param1.maxZ + 2.0) / 16.0);
        List<T> var4 = Lists.newArrayList();

        for(int var5 = var0; var5 < var1; ++var5) {
            for(int var6 = var2; var6 < var3; ++var6) {
                LevelChunk var7 = this.getChunkSource().getChunk(var5, var6, false);
                if (var7 != null) {
                    var7.getEntities(param0, param1, var4, param2);
                }
            }
        }

        return var4;
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> param0, AABB param1, @Nullable Predicate<? super T> param2) {
        this.getProfiler().incrementCounter("getEntities");
        int var0 = Mth.floor((param1.minX - 2.0) / 16.0);
        int var1 = Mth.ceil((param1.maxX + 2.0) / 16.0);
        int var2 = Mth.floor((param1.minZ - 2.0) / 16.0);
        int var3 = Mth.ceil((param1.maxZ + 2.0) / 16.0);
        List<T> var4 = Lists.newArrayList();
        ChunkSource var5 = this.getChunkSource();

        for(int var6 = var0; var6 < var1; ++var6) {
            for(int var7 = var2; var7 < var3; ++var7) {
                LevelChunk var8 = var5.getChunk(var6, var7, false);
                if (var8 != null) {
                    var8.getEntitiesOfClass(param0, param1, var4, param2);
                }
            }
        }

        return var4;
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> param0, AABB param1, @Nullable Predicate<? super T> param2) {
        this.getProfiler().incrementCounter("getLoadedEntities");
        int var0 = Mth.floor((param1.minX - 2.0) / 16.0);
        int var1 = Mth.ceil((param1.maxX + 2.0) / 16.0);
        int var2 = Mth.floor((param1.minZ - 2.0) / 16.0);
        int var3 = Mth.ceil((param1.maxZ + 2.0) / 16.0);
        List<T> var4 = Lists.newArrayList();
        ChunkSource var5 = this.getChunkSource();

        for(int var6 = var0; var6 < var1; ++var6) {
            for(int var7 = var2; var7 < var3; ++var7) {
                LevelChunk var8 = var5.getChunkNow(var6, var7);
                if (var8 != null) {
                    var8.getEntitiesOfClass(param0, param1, var4, param2);
                }
            }
        }

        return var4;
    }

    @Nullable
    public abstract Entity getEntity(int var1);

    public void blockEntityChanged(BlockPos param0, BlockEntity param1) {
        if (this.hasChunkAt(param0)) {
            this.getChunkAt(param0).markUnsaved();
        }

    }

    @Override
    public int getSeaLevel() {
        return 63;
    }

    @Override
    public Level getLevel() {
        return this;
    }

    public LevelType getGeneratorType() {
        return this.levelData.getGeneratorType();
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
        return var0.isRedstoneConductor(this, param0) ? this.getDirectSignalTo(param0) : var0.getSignal(this, param0, param1);
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

    @OnlyIn(Dist.CLIENT)
    public void disconnect() {
    }

    public void setGameTime(long param0) {
        this.levelData.setGameTime(param0);
    }

    @Override
    public long getSeed() {
        return this.levelData.getSeed();
    }

    public long getGameTime() {
        return this.levelData.getGameTime();
    }

    public long getDayTime() {
        return this.levelData.getDayTime();
    }

    public void setDayTime(long param0) {
        this.levelData.setDayTime(param0);
    }

    protected void tickTime() {
        this.setGameTime(this.levelData.getGameTime() + 1L);
        if (this.levelData.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT)) {
            this.setDayTime(this.levelData.getDayTime() + 1L);
        }

    }

    @Override
    public BlockPos getSharedSpawnPos() {
        BlockPos var0 = new BlockPos(this.levelData.getXSpawn(), this.levelData.getYSpawn(), this.levelData.getZSpawn());
        if (!this.getWorldBorder().isWithinBounds(var0)) {
            var0 = this.getHeightmapPos(
                Heightmap.Types.MOTION_BLOCKING, new BlockPos(this.getWorldBorder().getCenterX(), 0.0, this.getWorldBorder().getCenterZ())
            );
        }

        return var0;
    }

    public void setSpawnPos(BlockPos param0) {
        this.levelData.setSpawn(param0);
    }

    public boolean mayInteract(Player param0, BlockPos param1) {
        return true;
    }

    public void broadcastEntityEvent(Entity param0, byte param1) {
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.chunkSource;
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

    @OnlyIn(Dist.CLIENT)
    public void setThunderLevel(float param0) {
        this.oThunderLevel = param0;
        this.thunderLevel = param0;
    }

    public float getRainLevel(float param0) {
        return Mth.lerp(param0, this.oRainLevel, this.rainLevel);
    }

    @OnlyIn(Dist.CLIENT)
    public void setRainLevel(float param0) {
        this.oRainLevel = param0;
        this.rainLevel = param0;
    }

    public boolean isThundering() {
        if (this.dimension.isHasSkyLight() && !this.dimension.isHasCeiling()) {
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
            Biome var0 = this.getBiome(param0);
            return var0.getPrecipitation() == Biome.Precipitation.RAIN && var0.getTemperature(param0) >= 0.15F;
        }
    }

    public boolean isHumidAt(BlockPos param0) {
        Biome var0 = this.getBiome(param0);
        return var0.isHumid();
    }

    @Nullable
    public abstract MapItemSavedData getMapData(String var1);

    public abstract void setMapData(MapItemSavedData var1);

    public abstract int getFreeMapId();

    public void globalLevelEvent(int param0, BlockPos param1, int param2) {
    }

    public int getHeight() {
        return this.dimension.isHasCeiling() ? 128 : 256;
    }

    public CrashReportCategory fillReportDetails(CrashReport param0) {
        CrashReportCategory var0 = param0.addCategory("Affected level", 1);
        var0.setDetail("All players", () -> this.players().size() + " total; " + this.players());
        var0.setDetail("Chunk stats", this.chunkSource::gatherStats);
        var0.setDetail("Level dimension", () -> this.dimension.getType().toString());

        try {
            this.levelData.fillCrashReportCategory(var0);
        } catch (Throwable var4) {
            var0.setDetailError("Level Data Unobtainable", var4);
        }

        return var0;
    }

    public abstract void destroyBlockProgress(int var1, BlockPos var2, int var3);

    @OnlyIn(Dist.CLIENT)
    public void createFireworks(double param0, double param1, double param2, double param3, double param4, double param5, @Nullable CompoundTag param6) {
    }

    public abstract Scoreboard getScoreboard();

    public void updateNeighbourForOutputSignal(BlockPos param0, Block param1) {
        for(Direction var0 : Direction.Plane.HORIZONTAL) {
            BlockPos var1 = param0.relative(var0);
            if (this.hasChunkAt(var1)) {
                BlockState var2 = this.getBlockState(var1);
                if (var2.getBlock() == Blocks.COMPARATOR) {
                    var2.neighborChanged(this, var1, param1, param0, false);
                } else if (var2.isRedstoneConductor(this, var1)) {
                    var1 = var1.relative(var0);
                    var2 = this.getBlockState(var1);
                    if (var2.getBlock() == Blocks.COMPARATOR) {
                        var2.neighborChanged(this, var1, param1, param0, false);
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
    public Dimension getDimension() {
        return this.dimension;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public boolean isStateAtPosition(BlockPos param0, Predicate<BlockState> param1) {
        return param1.test(this.getBlockState(param0));
    }

    public abstract RecipeManager getRecipeManager();

    public abstract TagManager getTagManager();

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
}
