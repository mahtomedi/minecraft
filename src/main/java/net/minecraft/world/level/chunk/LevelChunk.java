package net.minecraft.world.level.chunk;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ClassInstanceMultiMap;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.EmptyTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LevelChunk implements ChunkAccess {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final LevelChunkSection EMPTY_SECTION = null;
    private final LevelChunkSection[] sections = new LevelChunkSection[16];
    private ChunkBiomeContainer biomes;
    private final Map<BlockPos, CompoundTag> pendingBlockEntities = Maps.newHashMap();
    private boolean loaded;
    private final Level level;
    private final Map<Heightmap.Types, Heightmap> heightmaps = Maps.newEnumMap(Heightmap.Types.class);
    private final UpgradeData upgradeData;
    private final Map<BlockPos, BlockEntity> blockEntities = Maps.newHashMap();
    private final ClassInstanceMultiMap<Entity>[] entitySections;
    private final Map<StructureFeature<?>, StructureStart<?>> structureStarts = Maps.newHashMap();
    private final Map<StructureFeature<?>, LongSet> structuresRefences = Maps.newHashMap();
    private final ShortList[] postProcessing = new ShortList[16];
    private TickList<Block> blockTicks;
    private TickList<Fluid> liquidTicks;
    private boolean lastSaveHadEntities;
    private long lastSaveTime;
    private volatile boolean unsaved;
    private long inhabitedTime;
    @Nullable
    private Supplier<ChunkHolder.FullChunkStatus> fullStatus;
    @Nullable
    private Consumer<LevelChunk> postLoad;
    private final ChunkPos chunkPos;
    private volatile boolean isLightCorrect;

    public LevelChunk(Level param0, ChunkPos param1, ChunkBiomeContainer param2) {
        this(param0, param1, param2, UpgradeData.EMPTY, EmptyTickList.empty(), EmptyTickList.empty(), 0L, null, null);
    }

    public LevelChunk(
        Level param0,
        ChunkPos param1,
        ChunkBiomeContainer param2,
        UpgradeData param3,
        TickList<Block> param4,
        TickList<Fluid> param5,
        long param6,
        @Nullable LevelChunkSection[] param7,
        @Nullable Consumer<LevelChunk> param8
    ) {
        this.entitySections = new ClassInstanceMultiMap[16];
        this.level = param0;
        this.chunkPos = param1;
        this.upgradeData = param3;

        for(Heightmap.Types var0 : Heightmap.Types.values()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(var0)) {
                this.heightmaps.put(var0, new Heightmap(this, var0));
            }
        }

        for(int var1 = 0; var1 < this.entitySections.length; ++var1) {
            this.entitySections[var1] = new ClassInstanceMultiMap<>(Entity.class);
        }

        this.biomes = param2;
        this.blockTicks = param4;
        this.liquidTicks = param5;
        this.inhabitedTime = param6;
        this.postLoad = param8;
        if (param7 != null) {
            if (this.sections.length == param7.length) {
                System.arraycopy(param7, 0, this.sections, 0, this.sections.length);
            } else {
                LOGGER.warn("Could not set level chunk sections, array length is {} instead of {}", param7.length, this.sections.length);
            }
        }

    }

    public LevelChunk(Level param0, ProtoChunk param1) {
        this(
            param0,
            param1.getPos(),
            param1.getBiomes(),
            param1.getUpgradeData(),
            param1.getBlockTicks(),
            param1.getLiquidTicks(),
            param1.getInhabitedTime(),
            param1.getSections(),
            null
        );

        for(CompoundTag var0 : param1.getEntities()) {
            EntityType.loadEntityRecursive(var0, param0, param0x -> {
                this.addEntity(param0x);
                return param0x;
            });
        }

        for(BlockEntity var1 : param1.getBlockEntities().values()) {
            this.addBlockEntity(var1);
        }

        this.pendingBlockEntities.putAll(param1.getBlockEntityNbts());

        for(int var2 = 0; var2 < param1.getPostProcessing().length; ++var2) {
            this.postProcessing[var2] = param1.getPostProcessing()[var2];
        }

        this.setAllStarts(param1.getAllStarts());
        this.setAllReferences(param1.getAllReferences());

        for(Entry<Heightmap.Types, Heightmap> var3 : param1.getHeightmaps()) {
            if (ChunkStatus.FULL.heightmapsAfter().contains(var3.getKey())) {
                this.getOrCreateHeightmapUnprimed(var3.getKey()).setRawData(var3.getValue().getRawData());
            }
        }

        this.setLightCorrect(param1.isLightCorrect());
        this.unsaved = true;
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types param0) {
        return this.heightmaps.computeIfAbsent(param0, param0x -> new Heightmap(this, param0x));
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        Set<BlockPos> var0 = Sets.newHashSet(this.pendingBlockEntities.keySet());
        var0.addAll(this.blockEntities.keySet());
        return var0;
    }

    @Override
    public LevelChunkSection[] getSections() {
        return this.sections;
    }

    @Override
    public BlockState getBlockState(BlockPos param0) {
        int var0 = param0.getX();
        int var1 = param0.getY();
        int var2 = param0.getZ();
        if (this.level.isDebug()) {
            BlockState var3 = null;
            if (var1 == 60) {
                var3 = Blocks.BARRIER.defaultBlockState();
            }

            if (var1 == 70) {
                var3 = DebugLevelSource.getBlockStateFor(var0, var2);
            }

            return var3 == null ? Blocks.AIR.defaultBlockState() : var3;
        } else {
            try {
                if (var1 >= 0 && var1 >> 4 < this.sections.length) {
                    LevelChunkSection var4 = this.sections[var1 >> 4];
                    if (!LevelChunkSection.isEmpty(var4)) {
                        return var4.getBlockState(var0 & 15, var1 & 15, var2 & 15);
                    }
                }

                return Blocks.AIR.defaultBlockState();
            } catch (Throwable var8) {
                CrashReport var6 = CrashReport.forThrowable(var8, "Getting block state");
                CrashReportCategory var7 = var6.addCategory("Block being got");
                var7.setDetail("Location", () -> CrashReportCategory.formatLocation(var0, var1, var2));
                throw new ReportedException(var6);
            }
        }
    }

    @Override
    public FluidState getFluidState(BlockPos param0) {
        return this.getFluidState(param0.getX(), param0.getY(), param0.getZ());
    }

    public FluidState getFluidState(int param0, int param1, int param2) {
        try {
            if (param1 >= 0 && param1 >> 4 < this.sections.length) {
                LevelChunkSection var0 = this.sections[param1 >> 4];
                if (!LevelChunkSection.isEmpty(var0)) {
                    return var0.getFluidState(param0 & 15, param1 & 15, param2 & 15);
                }
            }

            return Fluids.EMPTY.defaultFluidState();
        } catch (Throwable var7) {
            CrashReport var2 = CrashReport.forThrowable(var7, "Getting fluid state");
            CrashReportCategory var3 = var2.addCategory("Block being got");
            var3.setDetail("Location", () -> CrashReportCategory.formatLocation(param0, param1, param2));
            throw new ReportedException(var2);
        }
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos param0, BlockState param1, boolean param2) {
        int var0 = param0.getX() & 15;
        int var1 = param0.getY();
        int var2 = param0.getZ() & 15;
        LevelChunkSection var3 = this.sections[var1 >> 4];
        if (var3 == EMPTY_SECTION) {
            if (param1.isAir()) {
                return null;
            }

            var3 = new LevelChunkSection(var1 >> 4 << 4);
            this.sections[var1 >> 4] = var3;
        }

        boolean var4 = var3.isEmpty();
        BlockState var5 = var3.setBlockState(var0, var1 & 15, var2, param1);
        if (var5 == param1) {
            return null;
        } else {
            Block var6 = param1.getBlock();
            Block var7 = var5.getBlock();
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING).update(var0, var1, var2, param1);
            this.heightmaps.get(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES).update(var0, var1, var2, param1);
            this.heightmaps.get(Heightmap.Types.OCEAN_FLOOR).update(var0, var1, var2, param1);
            this.heightmaps.get(Heightmap.Types.WORLD_SURFACE).update(var0, var1, var2, param1);
            boolean var8 = var3.isEmpty();
            if (var4 != var8) {
                this.level.getChunkSource().getLightEngine().updateSectionStatus(param0, var8);
            }

            if (!this.level.isClientSide) {
                var5.onRemove(this.level, param0, param1, param2);
            } else if (var7 != var6 && var7 instanceof EntityBlock) {
                this.level.removeBlockEntity(param0);
            }

            if (!var3.getBlockState(var0, var1 & 15, var2).is(var6)) {
                return null;
            } else {
                if (var7 instanceof EntityBlock) {
                    BlockEntity var9 = this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
                    if (var9 != null) {
                        var9.clearCache();
                    }
                }

                if (!this.level.isClientSide) {
                    param1.onPlace(this.level, param0, var5, param2);
                }

                if (var6 instanceof EntityBlock) {
                    BlockEntity var10 = this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
                    if (var10 == null) {
                        var10 = ((EntityBlock)var6).newBlockEntity(this.level);
                        this.level.setBlockEntity(param0, var10);
                    } else {
                        var10.clearCache();
                    }
                }

                this.unsaved = true;
                return var5;
            }
        }
    }

    @Nullable
    public LevelLightEngine getLightEngine() {
        return this.level.getChunkSource().getLightEngine();
    }

    @Override
    public void addEntity(Entity param0) {
        this.lastSaveHadEntities = true;
        int var0 = Mth.floor(param0.getX() / 16.0);
        int var1 = Mth.floor(param0.getZ() / 16.0);
        if (var0 != this.chunkPos.x || var1 != this.chunkPos.z) {
            LOGGER.warn("Wrong location! ({}, {}) should be ({}, {}), {}", var0, var1, this.chunkPos.x, this.chunkPos.z, param0);
            param0.removed = true;
        }

        int var2 = Mth.floor(param0.getY() / 16.0);
        if (var2 < 0) {
            var2 = 0;
        }

        if (var2 >= this.entitySections.length) {
            var2 = this.entitySections.length - 1;
        }

        param0.inChunk = true;
        param0.xChunk = this.chunkPos.x;
        param0.yChunk = var2;
        param0.zChunk = this.chunkPos.z;
        this.entitySections[var2].add(param0);
    }

    @Override
    public void setHeightmap(Heightmap.Types param0, long[] param1) {
        this.heightmaps.get(param0).setRawData(param1);
    }

    public void removeEntity(Entity param0) {
        this.removeEntity(param0, param0.yChunk);
    }

    public void removeEntity(Entity param0, int param1) {
        if (param1 < 0) {
            param1 = 0;
        }

        if (param1 >= this.entitySections.length) {
            param1 = this.entitySections.length - 1;
        }

        this.entitySections[param1].remove(param0);
    }

    @Override
    public int getHeight(Heightmap.Types param0, int param1, int param2) {
        return this.heightmaps.get(param0).getFirstAvailable(param1 & 15, param2 & 15) - 1;
    }

    @Nullable
    private BlockEntity createBlockEntity(BlockPos param0) {
        BlockState var0 = this.getBlockState(param0);
        Block var1 = var0.getBlock();
        return !var1.isEntityBlock() ? null : ((EntityBlock)var1).newBlockEntity(this.level);
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos param0) {
        return this.getBlockEntity(param0, LevelChunk.EntityCreationType.CHECK);
    }

    @Nullable
    public BlockEntity getBlockEntity(BlockPos param0, LevelChunk.EntityCreationType param1) {
        BlockEntity var0 = this.blockEntities.get(param0);
        if (var0 == null) {
            CompoundTag var1 = this.pendingBlockEntities.remove(param0);
            if (var1 != null) {
                BlockEntity var2 = this.promotePendingBlockEntity(param0, var1);
                if (var2 != null) {
                    return var2;
                }
            }
        }

        if (var0 == null) {
            if (param1 == LevelChunk.EntityCreationType.IMMEDIATE) {
                var0 = this.createBlockEntity(param0);
                this.level.setBlockEntity(param0, var0);
            }
        } else if (var0.isRemoved()) {
            this.blockEntities.remove(param0);
            return null;
        }

        return var0;
    }

    public void addBlockEntity(BlockEntity param0) {
        this.setBlockEntity(param0.getBlockPos(), param0);
        if (this.loaded || this.level.isClientSide()) {
            this.level.setBlockEntity(param0.getBlockPos(), param0);
        }

    }

    @Override
    public void setBlockEntity(BlockPos param0, BlockEntity param1) {
        if (this.getBlockState(param0).getBlock() instanceof EntityBlock) {
            param1.setLevelAndPosition(this.level, param0);
            param1.clearRemoved();
            BlockEntity var0 = this.blockEntities.put(param0.immutable(), param1);
            if (var0 != null && var0 != param1) {
                var0.setRemoved();
            }

        }
    }

    @Override
    public void setBlockEntityNbt(CompoundTag param0) {
        this.pendingBlockEntities.put(new BlockPos(param0.getInt("x"), param0.getInt("y"), param0.getInt("z")), param0);
    }

    @Nullable
    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos param0) {
        BlockEntity var0 = this.getBlockEntity(param0);
        if (var0 != null && !var0.isRemoved()) {
            CompoundTag var1 = var0.save(new CompoundTag());
            var1.putBoolean("keepPacked", false);
            return var1;
        } else {
            CompoundTag var2 = this.pendingBlockEntities.get(param0);
            if (var2 != null) {
                var2 = var2.copy();
                var2.putBoolean("keepPacked", true);
            }

            return var2;
        }
    }

    @Override
    public void removeBlockEntity(BlockPos param0) {
        if (this.loaded || this.level.isClientSide()) {
            BlockEntity var0 = this.blockEntities.remove(param0);
            if (var0 != null) {
                var0.setRemoved();
            }
        }

    }

    public void runPostLoad() {
        if (this.postLoad != null) {
            this.postLoad.accept(this);
            this.postLoad = null;
        }

    }

    public void markUnsaved() {
        this.unsaved = true;
    }

    public void getEntities(@Nullable Entity param0, AABB param1, List<Entity> param2, @Nullable Predicate<? super Entity> param3) {
        int var0 = Mth.floor((param1.minY - 2.0) / 16.0);
        int var1 = Mth.floor((param1.maxY + 2.0) / 16.0);
        var0 = Mth.clamp(var0, 0, this.entitySections.length - 1);
        var1 = Mth.clamp(var1, 0, this.entitySections.length - 1);

        for(int var2 = var0; var2 <= var1; ++var2) {
            if (!this.entitySections[var2].isEmpty()) {
                for(Entity var3 : this.entitySections[var2]) {
                    if (var3.getBoundingBox().intersects(param1) && var3 != param0) {
                        if (param3 == null || param3.test(var3)) {
                            param2.add(var3);
                        }

                        if (var3 instanceof EnderDragon) {
                            for(EnderDragonPart var4 : ((EnderDragon)var3).getSubEntities()) {
                                if (var4 != param0 && var4.getBoundingBox().intersects(param1) && (param3 == null || param3.test(var4))) {
                                    param2.add(var4);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public <T extends Entity> void getEntities(@Nullable EntityType<?> param0, AABB param1, List<? super T> param2, Predicate<? super T> param3) {
        int var0 = Mth.floor((param1.minY - 2.0) / 16.0);
        int var1 = Mth.floor((param1.maxY + 2.0) / 16.0);
        var0 = Mth.clamp(var0, 0, this.entitySections.length - 1);
        var1 = Mth.clamp(var1, 0, this.entitySections.length - 1);

        for(int var2 = var0; var2 <= var1; ++var2) {
            for(Entity var3 : this.entitySections[var2].find(Entity.class)) {
                if ((param0 == null || var3.getType() == param0) && var3.getBoundingBox().intersects(param1) && param3.test((T)var3)) {
                    param2.add((T)var3);
                }
            }
        }

    }

    public <T extends Entity> void getEntitiesOfClass(Class<? extends T> param0, AABB param1, List<T> param2, @Nullable Predicate<? super T> param3) {
        int var0 = Mth.floor((param1.minY - 2.0) / 16.0);
        int var1 = Mth.floor((param1.maxY + 2.0) / 16.0);
        var0 = Mth.clamp(var0, 0, this.entitySections.length - 1);
        var1 = Mth.clamp(var1, 0, this.entitySections.length - 1);

        for(int var2 = var0; var2 <= var1; ++var2) {
            for(T var3 : this.entitySections[var2].find(param0)) {
                if (var3.getBoundingBox().intersects(param1) && (param3 == null || param3.test(var3))) {
                    param2.add(var3);
                }
            }
        }

    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public ChunkPos getPos() {
        return this.chunkPos;
    }

    @OnlyIn(Dist.CLIENT)
    public void replaceWithPacketData(@Nullable ChunkBiomeContainer param0, FriendlyByteBuf param1, CompoundTag param2, int param3) {
        boolean var0 = param0 != null;
        Predicate<BlockPos> var1 = var0 ? param0x -> true : param1x -> (param3 & 1 << (param1x.getY() >> 4)) != 0;
        Sets.newHashSet(this.blockEntities.keySet()).stream().filter(var1).forEach(this.level::removeBlockEntity);

        for(int var2 = 0; var2 < this.sections.length; ++var2) {
            LevelChunkSection var3 = this.sections[var2];
            if ((param3 & 1 << var2) == 0) {
                if (var0 && var3 != EMPTY_SECTION) {
                    this.sections[var2] = EMPTY_SECTION;
                }
            } else {
                if (var3 == EMPTY_SECTION) {
                    var3 = new LevelChunkSection(var2 << 4);
                    this.sections[var2] = var3;
                }

                var3.read(param1);
            }
        }

        if (param0 != null) {
            this.biomes = param0;
        }

        for(Heightmap.Types var4 : Heightmap.Types.values()) {
            String var5 = var4.getSerializationKey();
            if (param2.contains(var5, 12)) {
                this.setHeightmap(var4, param2.getLongArray(var5));
            }
        }

        for(BlockEntity var6 : this.blockEntities.values()) {
            var6.clearCache();
        }

    }

    @Override
    public ChunkBiomeContainer getBiomes() {
        return this.biomes;
    }

    public void setLoaded(boolean param0) {
        this.loaded = param0;
    }

    public Level getLevel() {
        return this.level;
    }

    @Override
    public Collection<Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return Collections.unmodifiableSet(this.heightmaps.entrySet());
    }

    public Map<BlockPos, BlockEntity> getBlockEntities() {
        return this.blockEntities;
    }

    public ClassInstanceMultiMap<Entity>[] getEntitySections() {
        return this.entitySections;
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos param0) {
        return this.pendingBlockEntities.get(param0);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return StreamSupport.stream(
                BlockPos.betweenClosed(
                        this.chunkPos.getMinBlockX(), 0, this.chunkPos.getMinBlockZ(), this.chunkPos.getMaxBlockX(), 255, this.chunkPos.getMaxBlockZ()
                    )
                    .spliterator(),
                false
            )
            .filter(param0 -> this.getBlockState(param0).getLightEmission() != 0);
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
    public void setUnsaved(boolean param0) {
        this.unsaved = param0;
    }

    @Override
    public boolean isUnsaved() {
        return this.unsaved || this.lastSaveHadEntities && this.level.getGameTime() != this.lastSaveTime;
    }

    public void setLastSaveHadEntities(boolean param0) {
        this.lastSaveHadEntities = param0;
    }

    @Override
    public void setLastSaveTime(long param0) {
        this.lastSaveTime = param0;
    }

    @Nullable
    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> param0) {
        return this.structureStarts.get(param0);
    }

    @Override
    public void setStartForFeature(StructureFeature<?> param0, StructureStart<?> param1) {
        this.structureStarts.put(param0, param1);
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return this.structureStarts;
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> param0) {
        this.structureStarts.clear();
        this.structureStarts.putAll(param0);
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> param0) {
        return this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet());
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> param0, long param1) {
        this.structuresRefences.computeIfAbsent(param0, param0x -> new LongOpenHashSet()).add(param1);
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return this.structuresRefences;
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> param0) {
        this.structuresRefences.clear();
        this.structuresRefences.putAll(param0);
    }

    @Override
    public long getInhabitedTime() {
        return this.inhabitedTime;
    }

    @Override
    public void setInhabitedTime(long param0) {
        this.inhabitedTime = param0;
    }

    public void postProcessGeneration() {
        ChunkPos var0 = this.getPos();

        for(int var1 = 0; var1 < this.postProcessing.length; ++var1) {
            if (this.postProcessing[var1] != null) {
                for(Short var2 : this.postProcessing[var1]) {
                    BlockPos var3 = ProtoChunk.unpackOffsetCoordinates(var2, var1, var0);
                    BlockState var4 = this.getBlockState(var3);
                    BlockState var5 = Block.updateFromNeighbourShapes(var4, this.level, var3);
                    this.level.setBlock(var3, var5, 20);
                }

                this.postProcessing[var1].clear();
            }
        }

        this.unpackTicks();

        for(BlockPos var6 : Sets.newHashSet(this.pendingBlockEntities.keySet())) {
            this.getBlockEntity(var6);
        }

        this.pendingBlockEntities.clear();
        this.upgradeData.upgrade(this);
    }

    @Nullable
    private BlockEntity promotePendingBlockEntity(BlockPos param0, CompoundTag param1) {
        BlockState var0 = this.getBlockState(param0);
        BlockEntity var2;
        if ("DUMMY".equals(param1.getString("id"))) {
            Block var1 = var0.getBlock();
            if (var1 instanceof EntityBlock) {
                var2 = ((EntityBlock)var1).newBlockEntity(this.level);
            } else {
                var2 = null;
                LOGGER.warn("Tried to load a DUMMY block entity @ {} but found not block entity block {} at location", param0, var0);
            }
        } else {
            var2 = BlockEntity.loadStatic(var0, param1);
        }

        if (var2 != null) {
            var2.setLevelAndPosition(this.level, param0);
            this.addBlockEntity(var2);
        } else {
            LOGGER.warn("Tried to load a block entity for block {} but failed at location {}", var0, param0);
        }

        return var2;
    }

    @Override
    public UpgradeData getUpgradeData() {
        return this.upgradeData;
    }

    @Override
    public ShortList[] getPostProcessing() {
        return this.postProcessing;
    }

    public void unpackTicks() {
        if (this.blockTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.blockTicks).copyOut(this.level.getBlockTicks(), param0 -> this.getBlockState(param0).getBlock());
            this.blockTicks = EmptyTickList.empty();
        } else if (this.blockTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.blockTicks).copyOut(this.level.getBlockTicks());
            this.blockTicks = EmptyTickList.empty();
        }

        if (this.liquidTicks instanceof ProtoTickList) {
            ((ProtoTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks(), param0 -> this.getFluidState(param0).getType());
            this.liquidTicks = EmptyTickList.empty();
        } else if (this.liquidTicks instanceof ChunkTickList) {
            ((ChunkTickList)this.liquidTicks).copyOut(this.level.getLiquidTicks());
            this.liquidTicks = EmptyTickList.empty();
        }

    }

    public void packTicks(ServerLevel param0) {
        if (this.blockTicks == EmptyTickList.empty()) {
            this.blockTicks = new ChunkTickList<>(
                Registry.BLOCK::getKey, param0.getBlockTicks().fetchTicksInChunk(this.chunkPos, true, false), param0.getGameTime()
            );
            this.setUnsaved(true);
        }

        if (this.liquidTicks == EmptyTickList.empty()) {
            this.liquidTicks = new ChunkTickList<>(
                Registry.FLUID::getKey, param0.getLiquidTicks().fetchTicksInChunk(this.chunkPos, true, false), param0.getGameTime()
            );
            this.setUnsaved(true);
        }

    }

    @Override
    public ChunkStatus getStatus() {
        return ChunkStatus.FULL;
    }

    public ChunkHolder.FullChunkStatus getFullStatus() {
        return this.fullStatus == null ? ChunkHolder.FullChunkStatus.BORDER : this.fullStatus.get();
    }

    public void setFullStatus(Supplier<ChunkHolder.FullChunkStatus> param0) {
        this.fullStatus = param0;
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

    public static enum EntityCreationType {
        IMMEDIATE,
        QUEUED,
        CHECK;
    }
}
