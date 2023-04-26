package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.CarvingMask;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.slf4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codecRW(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
    );
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";
    public static final String X_POS_TAG = "xPos";
    public static final String Z_POS_TAG = "zPos";
    public static final String HEIGHTMAPS_TAG = "Heightmaps";
    public static final String IS_LIGHT_ON_TAG = "isLightOn";
    public static final String SECTIONS_TAG = "sections";
    public static final String BLOCK_LIGHT_TAG = "BlockLight";
    public static final String SKY_LIGHT_TAG = "SkyLight";

    public static ProtoChunk read(ServerLevel param0, PoiManager param1, ChunkPos param2, CompoundTag param3) {
        ChunkPos var0 = new ChunkPos(param3.getInt("xPos"), param3.getInt("zPos"));
        if (!Objects.equals(param2, var0)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", param2, param2, var0);
        }

        UpgradeData var1 = param3.contains("UpgradeData", 10) ? new UpgradeData(param3.getCompound("UpgradeData"), param0) : UpgradeData.EMPTY;
        boolean var2 = param3.getBoolean("isLightOn");
        ListTag var3 = param3.getList("sections", 10);
        int var4 = param0.getSectionsCount();
        LevelChunkSection[] var5 = new LevelChunkSection[var4];
        boolean var6 = param0.dimensionType().hasSkyLight();
        ChunkSource var7 = param0.getChunkSource();
        LevelLightEngine var8 = var7.getLightEngine();
        Registry<Biome> var9 = param0.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> var10 = makeBiomeCodec(var9);
        boolean var11 = false;

        for(int var12 = 0; var12 < var3.size(); ++var12) {
            CompoundTag var13 = var3.getCompound(var12);
            int var14 = var13.getByte("Y");
            int var15 = param0.getSectionIndexFromSectionY(var14);
            if (var15 >= 0 && var15 < var5.length) {
                PalettedContainer<BlockState> var16;
                if (var13.contains("block_states", 10)) {
                    var16 = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, var13.getCompound("block_states"))
                        .promotePartial(param2x -> logErrors(param2, var14, param2x))
                        .getOrThrow(false, LOGGER::error);
                } else {
                    var16 = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                }

                PalettedContainerRO<Holder<Biome>> var18;
                if (var13.contains("biomes", 10)) {
                    var18 = var10.parse(NbtOps.INSTANCE, var13.getCompound("biomes"))
                        .promotePartial(param2x -> logErrors(param2, var14, param2x))
                        .getOrThrow(false, LOGGER::error);
                } else {
                    var18 = new PalettedContainer<>(var9.asHolderIdMap(), var9.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                }

                LevelChunkSection var20 = new LevelChunkSection(var16, var18);
                var5[var15] = var20;
                SectionPos var21 = SectionPos.of(param2, var14);
                param1.checkConsistencyWithBlocks(var21, var20);
            }

            boolean var22 = var13.contains("BlockLight", 7);
            boolean var23 = var6 && var13.contains("SkyLight", 7);
            if (var22 || var23) {
                if (!var11) {
                    var8.retainData(param2, true);
                    var11 = true;
                }

                if (var22) {
                    var8.queueSectionData(LightLayer.BLOCK, SectionPos.of(param2, var14), new DataLayer(var13.getByteArray("BlockLight")));
                }

                if (var23) {
                    var8.queueSectionData(LightLayer.SKY, SectionPos.of(param2, var14), new DataLayer(var13.getByteArray("SkyLight")));
                }
            }
        }

        long var24 = param3.getLong("InhabitedTime");
        ChunkStatus.ChunkType var25 = getChunkTypeFromTag(param3);
        BlendingData var26;
        if (param3.contains("blending_data", 10)) {
            var26 = BlendingData.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, param3.getCompound("blending_data"))).resultOrPartial(LOGGER::error).orElse(null);
        } else {
            var26 = null;
        }

        ChunkAccess var30;
        if (var25 == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> var28 = LevelChunkTicks.load(
                param3.getList("block_ticks", 10), param0x -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            LevelChunkTicks<Fluid> var29 = LevelChunkTicks.load(
                param3.getList("fluid_ticks", 10), param0x -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            var30 = new LevelChunk(param0.getLevel(), param2, var1, var28, var29, var24, var5, postLoadChunk(param0, param3), var26);
        } else {
            ProtoChunkTicks<Block> var31 = ProtoChunkTicks.load(
                param3.getList("block_ticks", 10), param0x -> BuiltInRegistries.BLOCK.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            ProtoChunkTicks<Fluid> var32 = ProtoChunkTicks.load(
                param3.getList("fluid_ticks", 10), param0x -> BuiltInRegistries.FLUID.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            ProtoChunk var33 = new ProtoChunk(param2, var1, var5, var31, var32, param0, var9, var26);
            var30 = var33;
            var33.setInhabitedTime(var24);
            if (param3.contains("below_zero_retrogen", 10)) {
                BelowZeroRetrogen.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, param3.getCompound("below_zero_retrogen")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(var33::setBelowZeroRetrogen);
            }

            ChunkStatus var35 = ChunkStatus.byName(param3.getString("Status"));
            var33.setStatus(var35);
            if (var35.isOrAfter(ChunkStatus.INITIALIZE_LIGHT)) {
                var33.setLightEngine(var8);
            }
        }

        var30.setLightCorrect(var2);
        CompoundTag var36 = param3.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> var37 = EnumSet.noneOf(Heightmap.Types.class);

        for(Heightmap.Types var38 : var30.getStatus().heightmapsAfter()) {
            String var39 = var38.getSerializationKey();
            if (var36.contains(var39, 12)) {
                var30.setHeightmap(var38, var36.getLongArray(var39));
            } else {
                var37.add(var38);
            }
        }

        Heightmap.primeHeightmaps(var30, var37);
        CompoundTag var40 = param3.getCompound("structures");
        var30.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(param0), var40, param0.getSeed()));
        var30.setAllReferences(unpackStructureReferences(param0.registryAccess(), param2, var40));
        if (param3.getBoolean("shouldSave")) {
            var30.setUnsaved(true);
        }

        ListTag var41 = param3.getList("PostProcessing", 9);

        for(int var42 = 0; var42 < var41.size(); ++var42) {
            ListTag var43 = var41.getList(var42);

            for(int var44 = 0; var44 < var43.size(); ++var44) {
                var30.addPackedPostProcess(var43.getShort(var44), var42);
            }
        }

        if (var25 == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var30, false);
        } else {
            ProtoChunk var45 = (ProtoChunk)var30;
            ListTag var46 = param3.getList("entities", 10);

            for(int var47 = 0; var47 < var46.size(); ++var47) {
                var45.addEntity(var46.getCompound(var47));
            }

            ListTag var48 = param3.getList("block_entities", 10);

            for(int var49 = 0; var49 < var48.size(); ++var49) {
                CompoundTag var50 = var48.getCompound(var49);
                var30.setBlockEntityNbt(var50);
            }

            CompoundTag var51 = param3.getCompound("CarvingMasks");

            for(String var52 : var51.getAllKeys()) {
                GenerationStep.Carving var53 = GenerationStep.Carving.valueOf(var52);
                var45.setCarvingMask(var53, new CarvingMask(var51.getLongArray(var52), var30.getMinBuildHeight()));
            }

            return var45;
        }
    }

    private static void logErrors(ChunkPos param0, int param1, String param2) {
        LOGGER.error("Recoverable errors when loading section [" + param0.x + ", " + param1 + ", " + param0.z + "]: " + param2);
    }

    private static Codec<PalettedContainerRO<Holder<Biome>>> makeBiomeCodec(Registry<Biome> param0) {
        return PalettedContainer.codecRO(
            param0.asHolderIdMap(), param0.holderByNameCodec(), PalettedContainer.Strategy.SECTION_BIOMES, param0.getHolderOrThrow(Biomes.PLAINS)
        );
    }

    public static CompoundTag write(ServerLevel param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        CompoundTag var1 = NbtUtils.addCurrentDataVersion(new CompoundTag());
        var1.putInt("xPos", var0.x);
        var1.putInt("yPos", param1.getMinSection());
        var1.putInt("zPos", var0.z);
        var1.putLong("LastUpdate", param0.getGameTime());
        var1.putLong("InhabitedTime", param1.getInhabitedTime());
        var1.putString("Status", param1.getStatus().getName());
        BlendingData var2 = param1.getBlendingData();
        if (var2 != null) {
            BlendingData.CODEC.encodeStart(NbtOps.INSTANCE, var2).resultOrPartial(LOGGER::error).ifPresent(param1x -> var1.put("blending_data", param1x));
        }

        BelowZeroRetrogen var3 = param1.getBelowZeroRetrogen();
        if (var3 != null) {
            BelowZeroRetrogen.CODEC
                .encodeStart(NbtOps.INSTANCE, var3)
                .resultOrPartial(LOGGER::error)
                .ifPresent(param1x -> var1.put("below_zero_retrogen", param1x));
        }

        UpgradeData var4 = param1.getUpgradeData();
        if (!var4.isEmpty()) {
            var1.put("UpgradeData", var4.write());
        }

        LevelChunkSection[] var5 = param1.getSections();
        ListTag var6 = new ListTag();
        LevelLightEngine var7 = param0.getChunkSource().getLightEngine();
        Registry<Biome> var8 = param0.registryAccess().registryOrThrow(Registries.BIOME);
        Codec<PalettedContainerRO<Holder<Biome>>> var9 = makeBiomeCodec(var8);
        boolean var10 = param1.isLightCorrect();

        for(int var11 = var7.getMinLightSection(); var11 < var7.getMaxLightSection(); ++var11) {
            int var12 = param1.getSectionIndexFromSectionY(var11);
            boolean var13 = var12 >= 0 && var12 < var5.length;
            DataLayer var14 = var7.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(var0, var11));
            DataLayer var15 = var7.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(var0, var11));
            if (var13 || var14 != null || var15 != null) {
                CompoundTag var16 = new CompoundTag();
                if (var13) {
                    LevelChunkSection var17 = var5[var12];
                    var16.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, var17.getStates()).getOrThrow(false, LOGGER::error));
                    var16.put("biomes", var9.encodeStart(NbtOps.INSTANCE, var17.getBiomes()).getOrThrow(false, LOGGER::error));
                }

                if (var14 != null && !var14.isEmpty()) {
                    var16.putByteArray("BlockLight", var14.getData());
                }

                if (var15 != null && !var15.isEmpty()) {
                    var16.putByteArray("SkyLight", var15.getData());
                }

                if (!var16.isEmpty()) {
                    var16.putByte("Y", (byte)var11);
                    var6.add(var16);
                }
            }
        }

        var1.put("sections", var6);
        if (var10) {
            var1.putBoolean("isLightOn", true);
        }

        ListTag var18 = new ListTag();

        for(BlockPos var19 : param1.getBlockEntitiesPos()) {
            CompoundTag var20 = param1.getBlockEntityNbtForSaving(var19);
            if (var20 != null) {
                var18.add(var20);
            }
        }

        var1.put("block_entities", var18);
        if (param1.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk var21 = (ProtoChunk)param1;
            ListTag var22 = new ListTag();
            var22.addAll(var21.getEntities());
            var1.put("entities", var22);
            CompoundTag var23 = new CompoundTag();

            for(GenerationStep.Carving var24 : GenerationStep.Carving.values()) {
                CarvingMask var25 = var21.getCarvingMask(var24);
                if (var25 != null) {
                    var23.putLongArray(var24.toString(), var25.toArray());
                }
            }

            var1.put("CarvingMasks", var23);
        }

        saveTicks(param0, var1, param1.getTicksForSerialization());
        var1.put("PostProcessing", packOffsets(param1.getPostProcessing()));
        CompoundTag var26 = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var27 : param1.getHeightmaps()) {
            if (param1.getStatus().heightmapsAfter().contains(var27.getKey())) {
                var26.put(var27.getKey().getSerializationKey(), new LongArrayTag(var27.getValue().getRawData()));
            }
        }

        var1.put("Heightmaps", var26);
        var1.put("structures", packStructureData(StructurePieceSerializationContext.fromLevel(param0), var0, param1.getAllStarts(), param1.getAllReferences()));
        return var1;
    }

    private static void saveTicks(ServerLevel param0, CompoundTag param1, ChunkAccess.TicksToSave param2) {
        long var0 = param0.getLevelData().getGameTime();
        param1.put("block_ticks", param2.blocks().save(var0, param0x -> BuiltInRegistries.BLOCK.getKey(param0x).toString()));
        param1.put("fluid_ticks", param2.fluids().save(var0, param0x -> BuiltInRegistries.FLUID.getKey(param0x).toString()));
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag param0) {
        return param0 != null ? ChunkStatus.byName(param0.getString("Status")).getChunkType() : ChunkStatus.ChunkType.PROTOCHUNK;
    }

    @Nullable
    private static LevelChunk.PostLoadProcessor postLoadChunk(ServerLevel param0, CompoundTag param1) {
        ListTag var0 = getListOfCompoundsOrNull(param1, "entities");
        ListTag var1 = getListOfCompoundsOrNull(param1, "block_entities");
        return var0 == null && var1 == null ? null : param3 -> {
            if (var0 != null) {
                param0.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(var0, param0));
            }

            if (var1 != null) {
                for(int var0x = 0; var0x < var1.size(); ++var0x) {
                    CompoundTag var1x = var1.getCompound(var0x);
                    boolean var2x = var1x.getBoolean("keepPacked");
                    if (var2x) {
                        param3.setBlockEntityNbt(var1x);
                    } else {
                        BlockPos var3x = BlockEntity.getPosFromTag(var1x);
                        BlockEntity var4 = BlockEntity.loadStatic(var3x, param3.getBlockState(var3x), var1x);
                        if (var4 != null) {
                            param3.setBlockEntity(var4);
                        }
                    }
                }
            }

        };
    }

    @Nullable
    private static ListTag getListOfCompoundsOrNull(CompoundTag param0, String param1) {
        ListTag var0 = param0.getList(param1, 10);
        return var0.isEmpty() ? null : var0;
    }

    private static CompoundTag packStructureData(
        StructurePieceSerializationContext param0, ChunkPos param1, Map<Structure, StructureStart> param2, Map<Structure, LongSet> param3
    ) {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();
        Registry<Structure> var2 = param0.registryAccess().registryOrThrow(Registries.STRUCTURE);

        for(Entry<Structure, StructureStart> var3 : param2.entrySet()) {
            ResourceLocation var4 = var2.getKey(var3.getKey());
            var1.put(var4.toString(), var3.getValue().createTag(param0, param1));
        }

        var0.put("starts", var1);
        CompoundTag var5 = new CompoundTag();

        for(Entry<Structure, LongSet> var6 : param3.entrySet()) {
            if (!var6.getValue().isEmpty()) {
                ResourceLocation var7 = var2.getKey(var6.getKey());
                var5.put(var7.toString(), new LongArrayTag(var6.getValue()));
            }
        }

        var0.put("References", var5);
        return var0;
    }

    private static Map<Structure, StructureStart> unpackStructureStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        Map<Structure, StructureStart> var0 = Maps.newHashMap();
        Registry<Structure> var1 = param0.registryAccess().registryOrThrow(Registries.STRUCTURE);
        CompoundTag var2 = param1.getCompound("starts");

        for(String var3 : var2.getAllKeys()) {
            ResourceLocation var4 = ResourceLocation.tryParse(var3);
            Structure var5 = var1.get(var4);
            if (var5 == null) {
                LOGGER.error("Unknown structure start: {}", var4);
            } else {
                StructureStart var6 = StructureStart.loadStaticStart(param0, var2.getCompound(var3), param2);
                if (var6 != null) {
                    var0.put(var5, var6);
                }
            }
        }

        return var0;
    }

    private static Map<Structure, LongSet> unpackStructureReferences(RegistryAccess param0, ChunkPos param1, CompoundTag param2) {
        Map<Structure, LongSet> var0 = Maps.newHashMap();
        Registry<Structure> var1 = param0.registryOrThrow(Registries.STRUCTURE);
        CompoundTag var2 = param2.getCompound("References");

        for(String var3 : var2.getAllKeys()) {
            ResourceLocation var4 = ResourceLocation.tryParse(var3);
            Structure var5 = var1.get(var4);
            if (var5 == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", var4, param1);
            } else {
                long[] var6 = var2.getLongArray(var3);
                if (var6.length != 0) {
                    var0.put(var5, new LongOpenHashSet(Arrays.stream(var6).filter(param2x -> {
                        ChunkPos var0x = new ChunkPos(param2x);
                        if (var0x.getChessboardDistance(param1) > 8) {
                            LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", var4, var0x, param1);
                            return false;
                        } else {
                            return true;
                        }
                    }).toArray()));
                }
            }
        }

        return var0;
    }

    public static ListTag packOffsets(ShortList[] param0) {
        ListTag var0 = new ListTag();

        for(ShortList var1 : param0) {
            ListTag var2 = new ListTag();
            if (var1 != null) {
                for(Short var3 : var1) {
                    var2.add(ShortTag.valueOf(var3));
                }
            }

            var0.add(var2);
        }

        return var0;
    }
}
