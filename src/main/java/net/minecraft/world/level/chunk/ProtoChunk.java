package net.minecraft.world.level.chunk;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProtoChunk implements ChunkAccess {
    private static final Logger LOGGER = LogManager.getLogger();
    private final ChunkPos chunkPos;
    private volatile boolean isDirty;
    @Nullable
    private ChunkBiomeContainer biomes;
    @Nullable
    private volatile LevelLightEngine lightEngine;
    private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private volatile ChunkStatus status = ChunkStatus.EMPTY;
    private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    private final Map<BlockPos, CompoundTag> blockEntityNbts = Maps.newHashMap();
    private final LevelChunkSection[] sections;
    private final List<CompoundTag> entities = Lists.newArrayList();
    private final List<BlockPos> lights = Lists.newArrayList();
    private final ShortList[] postProcessing;
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
    private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
    private final UpgradeData upgradeData;
    private final ProtoTickList<Block> blockTicks;
    private final ProtoTickList<Fluid> liquidTicks;
    private final LevelHeightAccessor levelHeightAccessor;
    private long inhabitedTime;
    private final Map<GenerationStep.Carving, BitSet> carvingMasks = new Object2ObjectArrayMap<>();
    private volatile boolean isLightCorrect;

    public ProtoChunk(ChunkPos param0, UpgradeData param1, LevelHeightAccessor param2) {
        this(
            param0,
            param1,
            null,
            new ProtoTickList<>(param0x -> param0x == null || param0x.defaultBlockState().isAir(), param0, param2),
            new ProtoTickList<>(param0x -> param0x == null || param0x == Fluids.EMPTY, param0, param2),
            param2
        );
    }

    public ProtoChunk(
        ChunkPos param0,
        UpgradeData param1,
        @Nullable LevelChunkSection[] param2,
        ProtoTickList<Block> param3,
        ProtoTickList<Fluid> param4,
        LevelHeightAccessor param5
    ) {
        this.chunkPos = param0;
        this.upgradeData = param1;
        this.blockTicks = param3;
        this.liquidTicks = param4;
        this.levelHeightAccessor = param5;
        this.sections = new LevelChunkSection[param5.getSectionsCount()];
        if (param2 != null) {
            if (this.sections.length == param2.length) {
                System.arraycopy(param2, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", param2.length, this.sections.length);
            }
        }

        this.postProcessing = new ShortList[param5.getSectionsCount()];
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getY();
        if (this.isOutsideBuildHeight(var0)) {
            return Blocks.VOID_AIR.defaultBlockState();
        } else {
            LevelChunkSection var1 = this.getSections()[this.getSectionIndex(var0)];
            return LevelChunkSection.isEmpty(var1) ? Blocks.AIR.defaultBlockState() : var1.getBlockState(param0.getX() & 15, var0 & 15, param0.getZ() & 15);
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        int var0 = param0.getY();
        if (this.isOutsideBuildHeight(var0)) {
            return Fluids.EMPTY.defaultFluidState();
        } else {
            LevelChunkSection var1 = this.getSections()[this.getSectionIndex(var0)];
            return LevelChunkSection.isEmpty(var1) ? Fluids.EMPTY.defaultFluidState() : var1.getFluidState(param0.getX() & 15, var0 & 15, param0.getZ() & 15);
        }
    }

    @Override
    public Stream<BlockPos> getLights() {
        return this.lights.stream();
    }

    public ShortList[] getPackedLights() {
        ShortList[] var0 = new ShortList[this.getSectionsCount()];

        for(BlockPos var1 : this.lights) {
            ChunkAccess.getOrCreateOffsetList(var0, this.getSectionIndex(var1.getY())).add(packOffsetCoordinates(var1));
        }

        return var0;
    }

    public void addLight(short param0, int param1) {
        this.addLight(unpackOffsetCoordinates(param0, this.getSectionYFromSectionIndex(param1), this.chunkPos));
    }

    public void addLight(BlockPos param0) {
        this.lights.add(param0.immutable());
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        if (var1 >= this.getMinBuildHeight() && var1 < this.getMaxBuildHeight()) {
            int var3 = this.getSectionIndex(var1);
            if (this.sections[var3] == LevelChunk.EMPTY_SECTION && param1.is(Blocks.AIR)) {
                return param1;
            } else {
                if (param1.getLightEmission() > 0) {
                    this.lights.add(new BlockPos((var0 & 15) + this.getPos().getMinBlockX(), var1, (var2 & 15) + this.getPos().getMinBlockZ()));
                }

                LevelChunkSection var4 = this.getOrCreateSection(var3);
                BlockState var5 = var4.setBlockState(var0 & 15, var1 & 15, var2 & 15, param1);
                if (this.status.isOrAfter(ChunkStatus.FEATURES)
                    && param1 != var5
                    && (
                        param1.getLightBlock(this, param0) != var5.getLightBlock(this, param0)
                            || param1.getLightEmission() != var5.getLightEmission()
                            || param1.useShapeForLightOcclusion()
                            || var5.useShapeForLightOcclusion()
                    )) {
                    LevelLightEngine var6 = this.getLightEngine();
                    var6.checkBlock(param0);
                }

                EnumSet<Heightmap.Types> var7 = this.getStatus().heightmapsAfter();
                EnumSet<Heightmap.Types> var8 = null;

                for(Heightmap.Types var9 : var7) {
                    Heightmap var10 = this.heightmaps.get(var9);
                    if (var10 == null) {
                        if (var8 == null) {
                            var8 = EnumSet.noneOf(Heightmap.Types.class);
                        }

                        var8.add(var9);
                    }
                }

                if (var8 != null) {
                    Heightmap.primeHeightmaps(this, var8);
                }

                for(Heightmap.Types var11 : var7) {
                    this.heightmaps.get(var11).update(var0 & 15, var1, var2 & 15, param1);
                }

                return var5;
            }
        } else {
            return Blocks.VOID_AIR.defaultBlockState();
        }
    }

    public LevelChunkSection getOrCreateSection(int param0) {
        if (this.sections[param0] == LevelChunk.EMPTY_SECTION) {
            this.sections[param0] = new LevelChunkSection(this.getSectionYFromSectionIndex(param0));
        }

        return this.sections[param0];
    }

    @Override
    public void setBlockEntity(BlockEntity param0) {
        this.blockEntities.put(param0.getBlockPos(), param0);
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        Set<BlockPos> var0 = Sets.newHashSet(this.blockEntityNbts.keySet());
        var0.addAll(this.blockEntities.keySet());
        return var0;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.blockEntities.get(param0);
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public void addEntity(CompoundTag param0) {
        this.entities.add(param0);
    }

    @Override
    public void addEntity(Entity param0) {
        if (!param0.isPassenger()) {
            CompoundTag var0 = new CompoundTag();
            param0.save(var0);
            this.addEntity(var0);
        }
    }

    public List<CompoundTag> getEntities() {
        return this.entities;
    }

    public void setBiomes(ChunkBiomeContainer param0) {
        this.biomes = param0;
    }

    @Nullable
    @Override
    public ChunkBiomeContainer getBiomes() {
        return this.biomes;
    }

    @Override
    public void setUnsaved(boolean param0) {
        this.isDirty = param0;
    }

    @Override
    public boolean isUnsaved() {
        return this.isDirty;
    }

    @Override
    public ChunkStatus getStatus() {
        return this.status;
    }

    public void setStatus(ChunkStatus param0) {
        this.status = param0;
        this.setUnsaved(true);
    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    @Nullable
    public LevelLightEngine getLightEngine() {
        return this.lightEngine;
    }

    @Override
    public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    @Override
    public void setHeightmap(Heightmap.Types param0, long[] param1) {
        this.getOrCreateHeightmapUnprimed(param0).setRawData(param1);
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types param0) {
        return this.heightmaps.computeIfAbsent(param0, param0x -> new Heightmap(this, param0x));
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        Heightmap var0 = this.heightmaps.get(param0);
        if (var0 == null) {
            Heightmap.primeHeightmaps(this, EnumSet.of(param0));
            var0 = this.heightmaps.get(param0);
        }

        return var0.getFirstAvailable(param1 & 15, param2 & 15) - 1;
    }

    @Override
    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @Nullable
    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> param0) {
        return this.structureStarts.get(param0);
    }

    @Override
    public void setStartForFeature(StructureFeature<?> param0, StructureStart<?> param1) {
        this.structureStarts.put(param0, param1);
        this.isDirty = true;
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return Collections.unmodifiableMap(this.structureStarts);
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> param0) {
        this.structureStarts.clear();
        this.structureStarts.putAll(param0);
        this.isDirty = true;
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> param0) {
        return this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet());
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> param0, long param1) {
        this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet()).add(param1);
        this.isDirty = true;
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return Collections.unmodifiableMap(this.structuresRefences);
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> param0) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(param0);
        this.isDirty = true;
    }

    public static short packOffsetCoordinates(BlockPos param0) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        int var3 = var0 & 15;
        int var4 = var1 & 15;
        int var5 = var2 & 15;
        return (short)(var3 | var4 << 4 | var5 << 8);
    }

    public static BlockPos unpackOffsetCoordinates(short param0, int param1, ChunkPos param2) {
        int var0 = SectionPos.sectionToBlockCoord(param2.x, param0 & 15);
        int var1 = SectionPos.sectionToBlockCoord(param1, param0 >>> 4 & 15);
        int var2 = SectionPos.sectionToBlockCoord(param2.z, param0 >>> 8 & 15);
        return new BlockPos(var0, var1, var2);
    }

    @Override
    public void markPosForPostprocessing(BlockPos param0) {
        if (!this.isOutsideBuildHeight(param0)) {
            ChunkAccess.getOrCreateOffsetList(this.postProcessing, this.getSectionIndex(param0.getY())).add(packOffsetCoordinates(param0));
        }

    }

    @Override
    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    @Override
    public void addPackedPostProcess(short param0, int param1) {
        ChunkAccess.getOrCreateOffsetList(this.postProcessing, param1).add(param0);
    }

    public ProtoTickList<Block> getBlockTicks() {
        return this.blockTicks;
    }

    public ProtoTickList<Fluid> getLiquidTicks() {
        return this.liquidTicks;
    }

    @Override
    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    @Override
    public void setInhabitedTime(long param0) {
        this.inhabitedTime = param0;
    }

    @Override
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    @Override
    public void setBlockEntityNbt(CompoundTag param0) {
        this.blockEntityNbts.put(new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z")), param0);
    }

    public Map<BlockPos, CompoundTag> getBlockEntityNbts() {
        return Collections.unmodifiableMap(this.blockEntityNbts);
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos param0) {
        return this.blockEntityNbts.get(param0);
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        return var0 != null ? var0.save(new CompoundTag()) : this.blockEntityNbts.get(param0);
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
        this.blockEntities.remove(param0);
        this.blockEntityNbts.remove(param0);
    }

    @Nullable
    public BitSet getCarvingMask(GenerationStep.Carving param0) {
        return this.carvingMasks.get(param0);
    }

    public BitSet getOrCreateCarvingMask(GenerationStep.Carving param0) {
        return this.carvingMasks.computeIfAbsent(param0, param0x -> new BitSet(65536));
    }

    public void setCarvingMask(GenerationStep.Carving param0, BitSet param1) {
        this.carvingMasks.put(param0, param1);
    }

    public void setLightEngine(LevelLightEngine param0) {
        this.lightEngine = param0;
    }

    @Override
    public boolean isLightCorrect() {
        return this.isLightCorrect;
    }

    @Override
    public void setLightCorrect(boolean param0) {
        this.isLightCorrect = param0;
        this.setUnsaved(true);
    }

    @Override
    public int getSectionsCount() {
        return this.levelHeightAccessor.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return this.levelHeightAccessor.getMinSection();
    }
}
