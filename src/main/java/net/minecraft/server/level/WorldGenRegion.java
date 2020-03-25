package net.minecraft.server.level;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.TickList;
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
import net.minecraft.world.level.dimension.Dimension;
import net.minecraft.world.level.levelgen.ChunkGeneratorSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldGenRegion implements LevelAccessor {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<ChunkAccess> cache;
    private final int x;
    private final int z;
    private final int size;
    private final ServerLevel level;
    private final long seed;
    private final int seaLevel;
    private final LevelData levelData;
    private final Random random;
    private final Dimension dimension;
    private final ChunkGeneratorSettings settings;
    private final TickList<Block> blockTicks = new WorldGenTickList<>(param0x -> this.getChunk(param0x).getBlockTicks());
    private final TickList<Fluid> liquidTicks = new WorldGenTickList<>(param0x -> this.getChunk(param0x).getLiquidTicks());
    private final BiomeManager biomeManager;

    public WorldGenRegion(ServerLevel param0, List<ChunkAccess> param1) {
        int var0 = Mth.floor(Math.sqrt((double)param1.size()));
        if (var0 * var0 != param1.size()) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("Cache size is not a square."));
        } else {
            ChunkPos var1 = param1.get(param1.size() / 2).getPos();
            this.cache = param1;
            this.x = var1.x;
            this.z = var1.z;
            this.size = var0;
            this.level = param0;
            this.seed = param0.getSeed();
            this.settings = param0.getChunkSource().getGenerator().getSettings();
            this.seaLevel = param0.getSeaLevel();
            this.levelData = param0.getLevelData();
            this.random = param0.getRandom();
            this.dimension = param0.getDimension();
            this.biomeManager = new BiomeManager(this, LevelData.obfuscateSeed(this.seed), this.dimension.getType().getBiomeZoomer());
        }
    }

    public int getCenterX() {
        return this.x;
    }

    public int getCenterZ() {
        return this.z;
    }

    @Override
    public ChunkAccess getChunk(int param0, int param1) {
        return this.getChunk(param0, param1, ChunkStatus.EMPTY);
    }

    @Nullable
    @Override
    public ChunkAccess getChunk(int param0, int param1, ChunkStatus param2, boolean param3) {
        ChunkAccess var3;
        if (this.hasChunk(param0, param1)) {
            ChunkPos var0 = this.cache.get(0).getPos();
            int var1 = param0 - var0.x;
            int var2 = param1 - var0.z;
            var3 = this.cache.get(var1 + var2 * this.size);
            if (var3.getStatus().isOrAfter(param2)) {
                return var3;
            }
        } else {
            var3 = null;
        }

        if (!param3) {
            return null;
        } else {
            ChunkAccess var5 = this.cache.get(0);
            ChunkAccess var6 = this.cache.get(this.cache.size() - 1);
            LOGGER.error("Requested chunk : {} {}", param0, param1);
            LOGGER.error("Region bounds : {} {} | {} {}", var5.getPos().x, var5.getPos().z, var6.getPos().x, var6.getPos().z);
            if (var3 != null) {
                throw (RuntimeException)Util.pauseInIde(
                    new RuntimeException(
                        String.format("Chunk is not of correct status. Expecting %s, got %s | %s %s", param2, var3.getStatus(), param0, param1)
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
        ChunkAccess var0 = this.cache.get(0);
        ChunkAccess var1 = this.cache.get(this.cache.size() - 1);
        return param0 >= var0.getPos().x && param0 <= var1.getPos().x && param1 >= var0.getPos().z && param1 <= var1.getPos().z;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        return this.getChunk(param0.getX() >> 4, param0.getZ() >> 4).getBlockState(param0);
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
    public Biome getUncachedNoiseBiome(int param0, int param1, int param2) {
        return this.level.getUncachedNoiseBiome(param0, param1, param2);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShade(Direction param0, boolean param1) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return this.level.getLightEngine();
    }

    @Override
    public boolean destroyBlock(BlockPos param0, boolean param1, @Nullable Entity param2) {
        BlockState var0 = this.getBlockState(param0);
        if (var0.isAir()) {
            return false;
        } else {
            if (param1) {
                BlockEntity var1 = var0.getBlock().isEntityBlock() ? this.getBlockEntity(param0) : null;
                Block.dropResources(var0, this.level, param0, var1, param2, ItemStack.EMPTY);
            }

            return this.setBlock(param0, Blocks.AIR.defaultBlockState(), 3);
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
                    Block var4 = var3.getBlock();
                    if (!(var4 instanceof EntityBlock)) {
                        return null;
                    }

                    var1 = ((EntityBlock)var4).newBlockEntity(this.level);
                } else {
                    var1 = BlockEntity.loadStatic(var3, var2);
                }

                if (var1 != null) {
                    var0.setBlockEntity(param0, var1);
                    return var1;
                }
            }

            if (var3.getBlock() instanceof EntityBlock) {
                LOGGER.warn("Tried to access a block entity before it was created. {}", param0);
            }

            return null;
        }
    }

    @Override
    public boolean setBlock(BlockPos param0, BlockState param1, int param2) {
        ChunkAccess var0 = this.getChunk(param0);
        BlockState var1 = var0.setBlockState(param0, param1, false);
        if (var1 != null) {
            this.level.onBlockStateChange(param0, var1, param1);
        }

        Block var2 = param1.getBlock();
        if (var2.isEntityBlock()) {
            if (var0.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
                var0.setBlockEntity(param0, ((EntityBlock)var2).newBlockEntity(this));
            } else {
                CompoundTag var3 = new CompoundTag();
                var3.putInt("x", param0.getX());
                var3.putInt("y", param0.getY());
                var3.putInt("z", param0.getZ());
                var3.putString("id", "DUMMY");
                var0.setBlockEntityNbt(var3);
            }
        } else if (var1 != null && var1.getBlock().isEntityBlock()) {
            var0.removeBlockEntity(param0);
        }

        if (param1.hasPostProcess(this, param0)) {
            this.markPosForPostprocessing(param0);
        }

        return true;
    }

    private void markPosForPostprocessing(BlockPos param0) {
        this.getChunk(param0).markPosForPostprocessing(param0);
    }

    @Override
    public boolean addFreshEntity(Entity param0) {
        int var0 = Mth.floor(param0.getX() / 16.0);
        int var1 = Mth.floor(param0.getZ() / 16.0);
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
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public LevelData getLevelData() {
        return this.levelData;
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos param0) {
        if (!this.hasChunk(param0.getX() >> 4, param0.getZ() >> 4)) {
            throw new RuntimeException("We are asking a region for a chunk out of bound");
        } else {
            return new DifficultyInstance(this.level.getDifficulty(), this.level.getDayTime(), 0L, this.level.getMoonBrightness());
        }
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
    public TickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    @Override
    public TickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Override
    public int getSeaLevel() {
        return this.seaLevel;
    }

    @Override
    public Random getRandom() {
        return this.random;
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.getChunk(param1 >> 4, param2 >> 4).getHeight(param0, param1 & 15, param2 & 15) + 1;
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
    public Dimension getDimension() {
        return this.dimension;
    }

    @Override
    public boolean isStateAtPosition(BlockPos param0, Predicate<BlockState> param1) {
        return param1.test(this.getBlockState(param0));
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> param0, AABB param1, @Nullable Predicate<? super T> param2) {
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
}
