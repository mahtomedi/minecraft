package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.SerializableTickContainer;
import net.minecraft.world.ticks.TickContainerAccess;
import org.slf4j.Logger;

public abstract class ChunkAccess implements BlockGetter, BiomeManager.NoiseBiomeSource, LightChunk, StructureAccess {
    public static final int NO_FILLED_SECTION = -1;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final LongSet EMPTY_REFERENCE_SET = new LongOpenHashSet();
    protected final ShortList[] postProcessing;
    protected volatile boolean unsaved;
    private volatile boolean isLightCorrect;
    protected final ChunkPos chunkPos;
    private long inhabitedTime;
    @Nullable
    @Deprecated
    private BiomeGenerationSettings carverBiomeSettings;
    @Nullable
    protected NoiseChunk noiseChunk;
    protected final UpgradeData upgradeData;
    @Nullable
    protected BlendingData blendingData;
    protected final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    protected ChunkSkyLightSources skyLightSources;
    private final Map<Structure, StructureStart> structureStarts = Maps.newHashMap();
    private final Map<Structure, LongSet> structuresRefences = Maps.newHashMap();
    protected final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    protected final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    protected final LevelHeightAccessor levelHeightAccessor;
    protected final LevelChunkSection[] sections;

    public ChunkAccess(
        ChunkPos param0,
        UpgradeData param1,
        LevelHeightAccessor param2,
        Registry<Biome> param3,
        long param4,
        @Nullable LevelChunkSection[] param5,
        @Nullable BlendingData param6
    ) {
        this.chunkPos = param0;
        this.upgradeData = param1;
        this.levelHeightAccessor = param2;
        this.sections = new LevelChunkSection[param2.getSectionsCount()];
        this.inhabitedTime = param4;
        this.postProcessing = new ShortList[param2.getSectionsCount()];
        this.blendingData = param6;
        this.skyLightSources = new ChunkSkyLightSources(param2);
        if (param5 != null) {
            if (this.sections.length == param5.length) {
                System.arraycopy(param5, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", param5.length, this.sections.length);
            }
        }

        replaceMissingSections(param3, this.sections);
    }

    private static void replaceMissingSections(Registry<Biome> param0, LevelChunkSection[] param1) {
        for(int var0 = 0; var0 < param1.length; ++var0) {
            if (param1[var0] == null) {
                param1[var0] = new LevelChunkSection(param0);
            }
        }

    }

    public GameEventListenerRegistry getListenerRegistry(int param0) {
        return GameEventListenerRegistry.NOOP;
    }

    @Nullable
    public abstract BlockState setBlockState(BlockPos var1, BlockState var2, boolean var3);

    public abstract void setBlockEntity(BlockEntity var1);

    public abstract void addEntity(Entity var1);

    public int getHighestFilledSectionIndex() {
        LevelChunkSection[] var0 = this.getSections();

        for(int var1 = var0.length - 1; var1 >= 0; --var1) {
            LevelChunkSection var2 = var0[var1];
            if (!var2.hasOnlyAir()) {
                return var1;
            }
        }

        return -1;
    }

    @Deprecated(
        forRemoval = true
    )
    public int getHighestSectionPosition() {
        int var0 = this.getHighestFilledSectionIndex();
        return var0 == -1 ? this.getMinBuildHeight() : SectionPos.sectionToBlockCoord(this.getSectionYFromSectionIndex(var0));
    }

    public Set<BlockPos> getBlockEntitiesPos() {
        Set<BlockPos> var0 = Sets.newHashSet(this.pendingBlockEntities.keySet());
        var0.addAll(this.blockEntities.keySet());
        return var0;
    }

    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    public LevelChunkSection getSection(int param0) {
        return this.getSections()[param0];
    }

    public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public void setHeightmap(Heightmap.Types param0, long[] param1) {
        this.getOrCreateHeightmapUnprimed(param0).setRawData(this, param0, param1);
    }

    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types param0) {
        return this.heightmaps.computeIfAbsent(param0, param0x -> new Heightmap(this, param0x));
    }

    public boolean hasPrimedHeightmap(Heightmap.Types param0) {
        return this.heightmaps.get(param0) != null;
    }

    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        Heightmap var0 = this.heightmaps.get(param0);
        if (var0 == null) {
            if (SharedConstants.IS_RUNNING_IN_IDE && this instanceof LevelChunk) {
                LOGGER.error("Unprimed heightmap: " + param0 + " " + param1 + " " + param2);
            }

            Heightmap.primeHeightmaps(this, EnumSet.of(param0));
            var0 = this.heightmaps.get(param0);
        }

        return var0.getFirstAvailable(param1 & 15, param2 & 15) - 1;
    }

    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Nullable
    @Override
    public StructureStart getStartForStructure(Structure param0) {
        return this.structureStarts.get(param0);
    }

    @Override
    public void setStartForStructure(Structure param0, StructureStart param1) {
        this.structureStarts.put(param0, param1);
        this.unsaved = true;
    }

    public Map<Structure, StructureStart> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    public void setAllStarts(Map<Structure, StructureStart> param0) {
        this.structureStarts.clear();
        this.structureStarts.putAll(param0);
        this.unsaved = true;
    }

    @Override
    public LongSet getReferencesForStructure(Structure param0) {
        return this.structuresRefences.getOrDefault(param0, EMPTY_REFERENCE_SET);
    }

    @Override
    public void addReferenceForStructure(Structure param0, long param1) {
        this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet()).add(param1);
        this.unsaved = true;
    }

    @Override
    public Map<Structure, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefences);
    }

    @Override
    public void setAllReferences(Map<Structure, LongSet> param0) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(param0);
        this.unsaved = true;
    }

    public boolean isYSpaceEmpty(int param0, int param1) {
        if (param0 < this.getMinBuildHeight()) {
            param0 = this.getMinBuildHeight();
        }

        if (param1 >= this.getMaxBuildHeight()) {
            param1 = this.getMaxBuildHeight() - 1;
        }

        for(int var0 = param0; var0 <= param1; var0 += 16) {
            if (!this.getSection(this.getSectionIndex(var0)).hasOnlyAir()) {
                return false;
            }
        }

        return true;
    }

    public void setUnsaved(boolean param0) {
        this.unsaved = param0;
    }

    public boolean isUnsaved() {
        return this.unsaved;
    }

    public abstract ChunkStatus getStatus();

    public ChunkStatus getHighestGeneratedStatus() {
        ChunkStatus var0 = this.getStatus();
        BelowZeroRetrogen var1 = this.getBelowZeroRetrogen();
        if (var1 != null) {
            ChunkStatus var2 = var1.targetStatus();
            return var2.isOrAfter(var0) ? var2 : var0;
        } else {
            return var0;
        }
    }

    public abstract void removeBlockEntity(BlockPos var1);

    public void markPosForPostprocessing(BlockPos param0) {
        LOGGER.warn("Trying to mark a block for PostProcessing @ {}, but this operation is not supported.", param0);
    }

    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void addPackedPostProcess(short param0, int param1) {
        getOrCreateOffsetList(this.getPostProcessing(), param1).add(param0);
    }

    public void setBlockEntityNbt(CompoundTag param0) {
        this.pendingBlockEntities.put(BlockEntity.getPosFromTag(param0), param0);
    }

    @Nullable
    public CompoundTag getBlockEntityNbt(BlockPos param0) {
        return this.pendingBlockEntities.get(param0);
    }

    @Nullable
    public abstract CompoundTag getBlockEntityNbtForSaving(BlockPos var1);

    @Override
    public final void findBlockLightSources(BiConsumer<BlockPos, BlockState> param0) {
        this.findBlocks(param0x -> param0x.getLightEmission() != 0, param0);
    }

    public void findBlocks(Predicate<BlockState> param0, BiConsumer<BlockPos, BlockState> param1) {
        BlockPos.MutableBlockPos var0 = new BlockPos.MutableBlockPos();

        for(int var1 = this.getMinSection(); var1 < this.getMaxSection(); ++var1) {
            LevelChunkSection var2 = this.getSection(this.getSectionIndexFromSectionY(var1));
            if (var2.maybeHas(param0)) {
                BlockPos var3 = SectionPos.of(this.chunkPos, var1).origin();

                for(int var4 = 0; var4 < 16; ++var4) {
                    for(int var5 = 0; var5 < 16; ++var5) {
                        for(int var6 = 0; var6 < 16; ++var6) {
                            BlockState var7 = var2.getBlockState(var6, var4, var5);
                            if (param0.test(var7)) {
                                param1.accept(var0.setWithOffset(var3, var6, var4, var5), var7);
                            }
                        }
                    }
                }
            }
        }

    }

    public abstract TickContainerAccess<Block> getBlockTicks();

    public abstract TickContainerAccess<Fluid> getFluidTicks();

    public abstract ChunkAccess.TicksToSave getTicksForSerialization();

    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    public boolean isOldNoiseGeneration() {
        return this.blendingData != null;
    }

    @Nullable
    public BlendingData getBlendingData() {
        return this.blendingData;
    }

    public void setBlendingData(BlendingData param0) {
        this.blendingData = param0;
    }

    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    public void incrementInhabitedTime(long param0) {
        this.inhabitedTime += param0;
    }

    public void setInhabitedTime(long param0) {
        this.inhabitedTime = param0;
    }

    public static ShortList getOrCreateOffsetList(ShortList[] param0, int param1) {
        if (param0[param1] == null) {
            param0[param1] = new ShortArrayList();
        }

        return param0[param1];
    }

    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    public void setLightCorrect(boolean param0) {
        this.isLightCorrect = param0;
        this.setUnsaved(true);
    }

    @Override
    public int getMinBuildHeight() {
        return this.levelHeightAccessor.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return this.levelHeightAccessor.getHeight();
    }

    public NoiseChunk getOrCreateNoiseChunk(Function<ChunkAccess, NoiseChunk> param0) {
        if (this.noiseChunk == null) {
            this.noiseChunk = param0.apply(this);
        }

        return this.noiseChunk;
    }

    @Deprecated
    public BiomeGenerationSettings carverBiome(Supplier<BiomeGenerationSettings> param0) {
        if (this.carverBiomeSettings == null) {
            this.carverBiomeSettings = param0.get();
        }

        return this.carverBiomeSettings;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int param0, int param1, int param2) {
        try {
            int var0 = QuartPos.fromBlock(this.getMinBuildHeight());
            int var1 = var0 + QuartPos.fromBlock(this.getHeight()) - 1;
            int var2 = Mth.clamp(param1, var0, var1);
            int var3 = this.getSectionIndex(QuartPos.toBlock(var2));
            return this.sections[var3].getNoiseBiome(param0 & 3, var2 & 3, param2 & 3);
        } catch (Throwable var8) {
            CrashReport var5 = CrashReport.forThrowable(var8, "Getting biome");
            CrashReportCategory var6 = var5.addCategory("Biome being got");
            var6.setDetail("Location", () -> CrashReportCategory.formatLocation(this, param0, param1, param2));
            throw new ReportedException(var5);
        }
    }

    public void fillBiomesFromNoise(BiomeResolver param0, Climate.Sampler param1) {
        ChunkPos var0 = this.getPos();
        int var1 = QuartPos.fromBlock(var0.getMinBlockX());
        int var2 = QuartPos.fromBlock(var0.getMinBlockZ());
        LevelHeightAccessor var3 = this.getHeightAccessorForGeneration();

        for(int var4 = var3.getMinSection(); var4 < var3.getMaxSection(); ++var4) {
            LevelChunkSection var5 = this.getSection(this.getSectionIndexFromSectionY(var4));
            int var6 = QuartPos.fromSection(var4);
            var5.fillBiomesFromNoise(param0, param1, var1, var6, var2);
        }

    }

    public boolean hasAnyStructureReferences() {
        return !this.getAllReferences().isEmpty();
    }

    @Nullable
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return null;
    }

    public boolean isUpgrading() {
        return this.getBelowZeroRetrogen() != null;
    }

    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return this;
    }

    public void initializeLightSources() {
        this.skyLightSources.fillFrom(this);
    }

    @Override
    public ChunkSkyLightSources getSkyLightSources() {
        return this.skyLightSources;
    }

    public static record TicksToSave(SerializableTickContainer<Block> blocks, SerializableTickContainer<Fluid> fluids) {
    }
}
