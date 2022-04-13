package net.minecraft.server.level;

import com.mojang.logging.LogUtils;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.WorldGenTickAccess;
import org.slf4j.Logger;

public class WorldGenRegion implements WorldGenLevel {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<ChunkAccess> cache;
    private final ChunkAccess center;
    private final int size;
    private final ServerLevel level;
    private final long seed;
    private final LevelData levelData;
    private final RandomSource random;
    private final DimensionType dimensionType;
    private final WorldGenTickAccess<Block> blockTicks = new WorldGenTickAccess<>(param0x -> this.getChunk(param0x).getBlockTicks());
    private final WorldGenTickAccess<Fluid> fluidTicks = new WorldGenTickAccess<>(param0x -> this.getChunk(param0x).getFluidTicks());
    private final BiomeManager biomeManager;
    private final ChunkPos firstPos;
    private final ChunkPos lastPos;
    private final StructureManager structureManager;
    private final ChunkStatus generatingStatus;
    private final int writeRadiusCutoff;
    @Nullable
    private Supplier<String> currentlyGenerating;
    private final AtomicLong subTickCount = new AtomicLong();
    private static final ResourceLocation WORLDGEN_REGION_RANDOM = new ResourceLocation("worldgen_region_random");

    public WorldGenRegion(ServerLevel param0, List<ChunkAccess> param1, ChunkStatus param2, int param3) {
        this.generatingStatus = param2;
        this.writeRadiusCutoff = param3;
        int var0 = Mth.floor(Math.sqrt((double)param1.size()));
        if (var0 * var0 != param1.size()) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
        } else {
            this.cache = param1;
            this.center = param1.get(param1.size() / 2);
            this.size = var0;
            this.level = param0;
            this.seed = param0.getSeed();
            this.levelData = param0.getLevelData();
            this.random = param0.getChunkSource().randomState().getOrCreateRandomFactory(WORLDGEN_REGION_RANDOM).at(this.center.getPos().getWorldPosition());
            this.dimensionType = param0.dimensionType();
            this.biomeManager = new BiomeManager(this, BiomeManager.obfuscateSeed(this.seed));
            this.firstPos = param1.get(0).getPos();
            this.lastPos = param1.get(param1.size() - 1).getPos();
            this.structureManager = param0.structureManager().forWorldGenRegion(this);
        }
    }

    public boolean isOldChunkAround(ChunkPos param0, int param1) {
        return this.level.getChunkSource().chunkMap.isOldChunkAround(param0, param1);
    }

    public ChunkPos getCenter() {
        return this.center.getPos();
    }

    @Override
    public void setCurrentlyGenerating(@Nullable Supplier<String> param0) {
        this.currentlyGenerating = param0;
    }

    @Override
    public ChunkAccess getChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.EMPTY);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        ChunkAccess var2;
        if (this.hasChunk(param0, param1)) {
            int var0 = param0 - this.firstPos.x;
            int var1 = param1 - this.firstPos.z;
            var2 = this.cache.get(var0 + var1 * this.size);
            if (var2.getStatus().isOrAfter(param2)) {
                return var2;
            }
        } else {
            var2 = null;
        }

        if (!param3) {
            return null;
        } else {
            LOGGER.error("Requested chunk : {} {}", param0, param1);
            LOGGER.error("Region bounds : {} {} | {} {}", this.firstPos.x, this.firstPos.z, this.lastPos.x, this.lastPos.z);
            if (var2 != null) {
                throw (RuntimeException)Util.pauseInIde(
                    new RuntimeException(
                        String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", param2, var2.getStatus(), param0, param1)
                    )
                );
            } else {
                throw (RuntimeException)Util.pauseInIde(
                    new RuntimeException(String.format("We are asking a region for a chunk out of bound | %s %s", param0, param1))
                );
            }
        }
    }

    @Override
    public boolean hasChunk(int param0, int param1) {
        return param0 >= this.firstPos.x && param0 <= this.lastPos.x && param1 >= this.firstPos.z && param1 <= this.lastPos.z;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return this.getChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ())).getBlockState(param0);
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.getChunk(param0).getFluidState(param0);
    }

    @Nullable
    @Override
    public Player getNearestPlayer(double param0, double param1, double param2, double param3, Predicate<Entity> param4) {
        return null;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return this.biomeManager;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int param0, int param1, int param2) {
        return this.level.getUncachedNoiseBiome(param0, param1, param2);
    }

    @Override
    public float getShade(Direction param0, boolean param1) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public boolean destroyBlock(BlockPos param0, boolean param1, @Nullable Entity param2, int param3) {
        BlockState var0 = this.getBlockState(param0);
        if (var0.isAir()) {
            return false;
        } else {
            if (param1) {
                BlockEntity var1 = var0.hasBlockEntity() ? this.getBlockEntity(param0) : null;
                Block.dropResources(var0, this.level, param0, var1, param2, ItemStack.EMPTY);
            }

            return this.setBlock(param0, Blocks.AIR.defaultBlockState(), 3, param3);
        }
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        ChunkAccess var0 = this.getChunk(param0);
        BlockEntity var1 = var0.getBlockEntity(param0);
        if (var1 != null) {
            return var1;
        } else {
            CompoundTag var2 = var0.getBlockEntityNbt(param0);
            BlockState var3 = var0.getBlockState(param0);
            if (var2 != null) {
                if ("DUMMY".equals(var2.getString("id"))) {
                    if (!var3.hasBlockEntity()) {
                        return null;
                    }

                    var1 = ((EntityBlock)var3.getBlock()).newBlockEntity(param0, var3);
                } else {
                    var1 = BlockEntity.loadStatic(param0, var3, var2);
                }

                if (var1 != null) {
                    var0.setBlockEntity(var1);
                    return var1;
                }
            }

            if (var3.hasBlockEntity()) {
                LOGGER.warn("Tried to access a block entity before it was created. {}", param0);
            }

            return null;
        }
    }

    @Override
    public boolean ensureCanWrite(BlockPos param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getX());
        int var1 = SectionPos.blockToSectionCoord(param0.getZ());
        ChunkPos var2 = this.getCenter();
        int var3 = Math.abs(var2.x - var0);
        int var4 = Math.abs(var2.z - var1);
        if (var3 <= this.writeRadiusCutoff && var4 <= this.writeRadiusCutoff) {
            if (this.center.isUpgrading()) {
                LevelHeightAccessor var5 = this.center.getHeightAccessorForGeneration();
                if (param0.getY() < var5.getMinBuildHeight() || param0.getY() >= var5.getMaxBuildHeight()) {
                    return false;
                }
            }

            return true;
        } else {
            Util.logAndPauseIfInIde(
                "Detected setBlock in a far chunk ["
                    + var0
                    + ", "
                    + var1
                    + "], pos: "
                    + param0
                    + ", status: "
                    + this.generatingStatus
                    + (this.currentlyGenerating == null ? "" : ", currently generating: " + (String)this.currentlyGenerating.get())
            );
            return false;
        }
    }

    @Override
    public boolean setBlock(BlockPos param0, BlockState param1, int param2, int param3) {
        if (!this.ensureCanWrite(param0)) {
            return false;
        } else {
            ChunkAccess var0 = this.getChunk(param0);
            BlockState var1 = var0.setBlockState(param0, param1, false);
            if (var1 != null) {
                this.level.onBlockStateChange(param0, var1, param1);
            }

            if (param1.hasBlockEntity()) {
                if (var0.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
                    BlockEntity var2 = ((EntityBlock)param1.getBlock()).newBlockEntity(param0, param1);
                    if (var2 != null) {
                        var0.setBlockEntity(var2);
                    } else {
                        var0.removeBlockEntity(param0);
                    }
                } else {
                    CompoundTag var3 = new CompoundTag();
                    var3.putInt("x", param0.getX());
                    var3.putInt("y", param0.getY());
                    var3.putInt("z", param0.getZ());
                    var3.putString("id", "DUMMY");
                    var0.setBlockEntityNbt(var3);
                }
            } else if (var1 != null && var1.hasBlockEntity()) {
                var0.removeBlockEntity(param0);
            }

            if (param1.hasPostProcess(this, param0)) {
                this.markPosForPostprocessing(param0);
            }

            return true;
        }
    }

    private void markPosForPostprocessing(BlockPos param0) {
        this.getChunk(param0).markPosForPostprocessing(param0);
    }

    @Override
    public boolean addFreshEntity(Entity param0) {
        int var0 = SectionPos.blockToSectionCoord(param0.getBlockX());
        int var1 = SectionPos.blockToSectionCoord(param0.getBlockZ());
        this.getChunk(var0, var1).addEntity(param0);
        return true;
    }

    @Override
    public boolean removeBlock(BlockPos param0, boolean param1) {
        return this.setBlock(param0, Blocks.AIR.defaultBlockState(), 3);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return this.level.getWorldBorder();
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Deprecated
    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.level.registryAccess();
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos param0) {
        if (!this.hasChunk(SectionPos.blockToSectionCoord(param0.getX()), SectionPos.blockToSectionCoord(param0.getZ()))) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        } else {
            return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
        }
    }

    @Nullable
    @Override
    public MinecraftServer getServer() {
        return this.level.getServer();
    }

    @Override
    public ChunkSource getChunkSource() {
        return this.level.getChunkSource();
    }

    @Override
    public long getSeed() {
        return this.seed;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return this.fluidTicks;
    }

    @Override
    public int getSeaLevel() {
        return this.level.getSeaLevel();
    }

    @Override
    public RandomSource getRandom() {
        return this.random;
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.getChunk(SectionPos.blockToSectionCoord(param1), SectionPos.blockToSectionCoord(param2)).getHeight(param0, param1 & 15, param2 & 15) + 1;
    }

    @Override
    public void playSound(@Nullable Player param0, BlockPos param1, SoundEvent param2, SoundSource param3, float param4, float param5) {
    }

    @Override
    public void addParticle(ParticleOptions param0, double param1, double param2, double param3, double param4, double param5, double param6) {
    }

    @Override
    public void levelEvent(@Nullable Player param0, int param1, BlockPos param2, int param3) {
    }

    @Override
    public void gameEvent(GameEvent param0, Vec3 param1, @Nullable GameEvent.Context param2) {
    }

    @Override
    public DimensionType dimensionType() {
        return this.dimensionType;
    }

    @Override
    public boolean isStateAtPosition(BlockPos param0, Predicate<BlockState> param1) {
        return param1.test(this.getBlockState(param0));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos param0, Predicate<FluidState> param1) {
        return param1.test(this.getFluidState(param0));
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> param0, AABB param1, Predicate<? super T> param2) {
        return Collections.emptyList();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity param0, AABB param1, @Nullable Predicate<? super Entity> param2) {
        return Collections.emptyList();
    }

    @Override
    public List<Player> players() {
        return Collections.emptyList();
    }

    @Override
    public int getMinBuildHeight() {
        return this.level.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.level.getHeight();
    }

    @Override
    public long nextSubTickCount() {
        return this.subTickCount.getAndIncrement();
    }
}
