package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.NbtOps;
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
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.BelowZeroRetrogen;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.blending.GenerationUpgradeData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.ticks.LevelChunkTicks;
import net.minecraft.world.ticks.ProtoChunkTicks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codec(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES, Blocks.AIR.defaultBlockState()
    );
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String TAG_UPGRADE_DATA = "UpgradeData";
    private static final String BLOCK_TICKS_TAG = "block_ticks";
    private static final String FLUID_TICKS_TAG = "fluid_ticks";

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
        if (var2) {
            var8.retainData(param2, true);
        }

        Registry<Biome> var9 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Biome>> var10 = makeBiomeCodec(var9);

        for(int var11 = 0; var11 < var3.size(); ++var11) {
            CompoundTag var12 = var3.getCompound(var11);
            int var13 = var12.getByte("Y");
            int var14 = param0.getSectionIndexFromSectionY(var13);
            if (var14 >= 0 && var14 < var5.length) {
                PalettedContainer<BlockState> var15;
                if (var12.contains("block_states", 10)) {
                    var15 = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, var12.getCompound("block_states"))
                        .promotePartial(param2x -> logErrors(param2, var13, param2x))
                        .getOrThrow(false, LOGGER::error);
                } else {
                    var15 = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                }

                PalettedContainer<Biome> var17;
                if (var12.contains("biomes", 10)) {
                    var17 = var10.parse(NbtOps.INSTANCE, var12.getCompound("biomes"))
                        .promotePartial(param2x -> logErrors(param2, var13, param2x))
                        .getOrThrow(false, LOGGER::error);
                } else {
                    var17 = new PalettedContainer<>(var9, var9.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                }

                LevelChunkSection var19 = new LevelChunkSection(var13, var15, var17);
                var5[var14] = var19;
                param1.checkConsistencyWithBlocks(param2, var19);
            }

            if (var2) {
                if (var12.contains("BlockLight", 7)) {
                    var8.queueSectionData(LightLayer.BLOCK, SectionPos.of(param2, var13), new DataLayer(var12.getByteArray("BlockLight")), true);
                }

                if (var6 && var12.contains("SkyLight", 7)) {
                    var8.queueSectionData(LightLayer.SKY, SectionPos.of(param2, var13), new DataLayer(var12.getByteArray("SkyLight")), true);
                }
            }
        }

        long var20 = param3.getLong("InhabitedTime");
        ChunkStatus.ChunkType var21 = getChunkTypeFromTag(param3);
        GenerationUpgradeData var22 = GenerationUpgradeData.read(param3.getCompound("blending_data"));
        ChunkAccess var25;
        if (var21 == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunkTicks<Block> var23 = LevelChunkTicks.load(
                param3.getList("block_ticks", 10), param0x -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            LevelChunkTicks<Fluid> var24 = LevelChunkTicks.load(
                param3.getList("fluid_ticks", 10), param0x -> Registry.FLUID.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            var25 = new LevelChunk(param0.getLevel(), param2, var1, var23, var24, var20, var5, param2x -> postLoadChunk(param0, param3, param2x), var22);
        } else {
            ProtoChunkTicks<Block> var26 = ProtoChunkTicks.load(
                param3.getList("block_ticks", 9), param0x -> Registry.BLOCK.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            ProtoChunkTicks<Fluid> var27 = ProtoChunkTicks.load(
                param3.getList("fluid_ticks", 9), param0x -> Registry.FLUID.getOptional(ResourceLocation.tryParse(param0x)), param2
            );
            ProtoChunk var28 = new ProtoChunk(param2, var1, var5, var26, var27, param0, var9, var22);
            var25 = var28;
            var28.setInhabitedTime(var20);
            if (param3.contains("below_zero_retrogen", 10)) {
                BelowZeroRetrogen.CODEC
                    .parse(new Dynamic<>(NbtOps.INSTANCE, param3.getCompound("below_zero_retrogen")))
                    .resultOrPartial(LOGGER::error)
                    .ifPresent(var28::setBelowZeroRetrogen);
            }

            ChunkStatus var30 = ChunkStatus.byName(param3.getString("Status"));
            var28.setStatus(var30);
            if (var30.isOrAfter(ChunkStatus.FEATURES)) {
                var28.setLightEngine(var8);
            }

            BelowZeroRetrogen var31 = var28.getBelowZeroRetrogen();
            boolean var32 = var30.isOrAfter(ChunkStatus.LIGHT) || var31 != null && var31.targetStatus().isOrAfter(ChunkStatus.LIGHT);
            if (!var2 && var32) {
                for(BlockPos var33 : BlockPos.betweenClosed(
                    param2.getMinBlockX(),
                    param0.getMinBuildHeight(),
                    param2.getMinBlockZ(),
                    param2.getMaxBlockX(),
                    param0.getMaxBuildHeight() - 1,
                    param2.getMaxBlockZ()
                )) {
                    if (var25.getBlockState(var33).getLightEmission() != 0) {
                        var28.addLight(var33);
                    }
                }
            }
        }

        var25.setLightCorrect(var2);
        CompoundTag var34 = param3.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> var35 = EnumSet.noneOf(Heightmap.Types.class);

        for(Heightmap.Types var36 : var25.getStatus().heightmapsAfter()) {
            String var37 = var36.getSerializationKey();
            if (var34.contains(var37, 12)) {
                var25.setHeightmap(var36, var34.getLongArray(var37));
            } else {
                var35.add(var36);
            }
        }

        Heightmap.primeHeightmaps(var25, var35);
        CompoundTag var38 = param3.getCompound("structures");
        var25.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(param0), var38, param0.getSeed()));
        var25.setAllReferences(unpackStructureReferences(param2, var38));
        if (param3.getBoolean("shouldSave")) {
            var25.setUnsaved(true);
        }

        ListTag var39 = param3.getList("PostProcessing", 9);

        for(int var40 = 0; var40 < var39.size(); ++var40) {
            ListTag var41 = var39.getList(var40);

            for(int var42 = 0; var42 < var41.size(); ++var42) {
                var25.addPackedPostProcess(var41.getShort(var42), var40);
            }
        }

        if (var21 == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var25, false);
        } else {
            ProtoChunk var43 = (ProtoChunk)var25;
            ListTag var44 = param3.getList("entities", 10);

            for(int var45 = 0; var45 < var44.size(); ++var45) {
                var43.addEntity(var44.getCompound(var45));
            }

            ListTag var46 = param3.getList("block_entities", 10);

            for(int var47 = 0; var47 < var46.size(); ++var47) {
                CompoundTag var48 = var46.getCompound(var47);
                var25.setBlockEntityNbt(var48);
            }

            ListTag var49 = param3.getList("Lights", 9);

            for(int var50 = 0; var50 < var49.size(); ++var50) {
                ListTag var51 = var49.getList(var50);

                for(int var52 = 0; var52 < var51.size(); ++var52) {
                    var43.addLight(var51.getShort(var52), var50);
                }
            }

            CompoundTag var53 = param3.getCompound("CarvingMasks");

            for(String var54 : var53.getAllKeys()) {
                GenerationStep.Carving var55 = GenerationStep.Carving.valueOf(var54);
                var43.setCarvingMask(var55, new CarvingMask(var53.getLongArray(var54), var25.getMinBuildHeight()));
            }

            return var43;
        }
    }

    private static void logErrors(ChunkPos param0, int param1, String param2) {
        LOGGER.error("Recoverable errors when loading section [" + param0.x + ", " + param1 + ", " + param0.z + "]: " + param2);
    }

    private static Codec<PalettedContainer<Biome>> makeBiomeCodec(Registry<Biome> param0) {
        return PalettedContainer.codec(param0, param0, PalettedContainer.Strategy.SECTION_BIOMES, param0.getOrThrow(Biomes.PLAINS));
    }

    public static CompoundTag write(ServerLevel param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        CompoundTag var1 = new CompoundTag();
        var1.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        var1.putInt("xPos", var0.x);
        var1.putInt("yPos", param1.getMinSection());
        var1.putInt("zPos", var0.z);
        var1.putLong("LastUpdate", param0.getGameTime());
        var1.putLong("InhabitedTime", param1.getInhabitedTime());
        var1.putString("Status", param1.getStatus().getName());
        GenerationUpgradeData var2 = param1.getGenerationUpgradeData();
        if (var2 != null) {
            var1.put("blending_data", var2.write());
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
        Registry<Biome> var8 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Biome>> var9 = makeBiomeCodec(var8);
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
            var1.put("Lights", packOffsets(var21.getPackedLights()));
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
        param1.put("block_ticks", param2.blocks().save(var0, param0x -> Registry.BLOCK.getKey(param0x).toString()));
        param1.put("fluid_ticks", param2.fluids().save(var0, param0x -> Registry.FLUID.getKey(param0x).toString()));
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag param0) {
        return param0 != null ? ChunkStatus.byName(param0.getString("Status")).getChunkType() : ChunkStatus.ChunkType.PROTOCHUNK;
    }

    private static void postLoadChunk(ServerLevel param0, CompoundTag param1, LevelChunk param2) {
        if (param1.contains("entities", 9)) {
            ListTag var0 = param1.getList("entities", 10);
            if (!var0.isEmpty()) {
                param0.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(var0, param0));
            }
        }

        ListTag var1 = param1.getList("block_entities", 10);

        for(int var2 = 0; var2 < var1.size(); ++var2) {
            CompoundTag var3 = var1.getCompound(var2);
            boolean var4 = var3.getBoolean("keepPacked");
            if (var4) {
                param2.setBlockEntityNbt(var3);
            } else {
                BlockPos var5 = BlockEntity.getPosFromTag(var3);
                BlockEntity var6 = BlockEntity.loadStatic(var5, param2.getBlockState(var5), var3);
                if (var6 != null) {
                    param2.setBlockEntity(var6);
                }
            }
        }

    }

    private static CompoundTag packStructureData(
        StructurePieceSerializationContext param0,
        ChunkPos param1,
        Map<StructureFeature<?>, StructureStart<?>> param2,
        Map<StructureFeature<?>, LongSet> param3
    ) {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();

        for(Entry<StructureFeature<?>, StructureStart<?>> var2 : param2.entrySet()) {
            var1.put(var2.getKey().getFeatureName(), var2.getValue().createTag(param0, param1));
        }

        var0.put("starts", var1);
        CompoundTag var3 = new CompoundTag();

        for(Entry<StructureFeature<?>, LongSet> var4 : param3.entrySet()) {
            var3.put(var4.getKey().getFeatureName(), new LongArrayTag(var4.getValue()));
        }

        var0.put("References", var3);
        return var0;
    }

    private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        Map<StructureFeature<?>, StructureStart<?>> var0 = Maps.newHashMap();
        CompoundTag var1 = param1.getCompound("starts");

        for(String var2 : var1.getAllKeys()) {
            String var3 = var2.toLowerCase(Locale.ROOT);
            StructureFeature<?> var4 = StructureFeature.STRUCTURES_REGISTRY.get(var3);
            if (var4 == null) {
                LOGGER.error("Unknown structure start: {}", var3);
            } else {
                StructureStart<?> var5 = StructureFeature.loadStaticStart(param0, var1.getCompound(var2), param2);
                if (var5 != null) {
                    var0.put(var4, var5);
                }
            }
        }

        return var0;
    }

    private static Map<StructureFeature<?>, LongSet> unpackStructureReferences(ChunkPos param0, CompoundTag param1) {
        Map<StructureFeature<?>, LongSet> var0 = Maps.newHashMap();
        CompoundTag var1 = param1.getCompound("References");

        for(String var2 : var1.getAllKeys()) {
            String var3 = var2.toLowerCase(Locale.ROOT);
            StructureFeature<?> var4 = StructureFeature.STRUCTURES_REGISTRY.get(var3);
            if (var4 == null) {
                LOGGER.warn("Found reference to unknown structure '{}' in chunk {}, discarding", var3, param0);
            } else {
                var0.put(var4, new LongOpenHashSet(Arrays.stream(var1.getLongArray(var2)).filter(param2 -> {
                    ChunkPos var0x = new ChunkPos(param2);
                    if (var0x.getChessboardDistance(param0) > 8) {
                        LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", var3, var0x, param0);
                        return false;
                    } else {
                        return true;
                    }
                }).toArray()));
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
