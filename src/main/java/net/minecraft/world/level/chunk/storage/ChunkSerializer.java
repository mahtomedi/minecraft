package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.BitSet;
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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
    private static final Codec<PalettedContainer<BlockState>> BLOCK_STATE_CODEC = PalettedContainer.codec(
        Block.BLOCK_STATE_REGISTRY, BlockState.CODEC, PalettedContainer.Strategy.SECTION_STATES
    );
    private static final Logger LOGGER = LogManager.getLogger();
    public static final String TAG_UPGRADE_DATA = "UpgradeData";

    public static ProtoChunk read(ServerLevel param0, PoiManager param1, ChunkPos param2, CompoundTag param3) {
        CompoundTag var0 = param3.getCompound("Level");
        ChunkPos var1 = new ChunkPos(var0.getInt("xPos"), var0.getInt("zPos"));
        if (!Objects.equals(param2, var1)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", param2, param2, var1);
        }

        UpgradeData var2 = var0.contains("UpgradeData", 10) ? new UpgradeData(var0.getCompound("UpgradeData"), param0) : UpgradeData.EMPTY;
        ProtoTickList<Block> var3 = new ProtoTickList<>(
            param0x -> param0x == null || param0x.defaultBlockState().isAir(), param2, var0.getList("ToBeTicked", 9), param0
        );
        ProtoTickList<Fluid> var4 = new ProtoTickList<>(
            param0x -> param0x == null || param0x == Fluids.EMPTY, param2, var0.getList("LiquidsToBeTicked", 9), param0
        );
        boolean var5 = var0.getBoolean("isLightOn");
        ListTag var6 = var0.getList("Sections", 10);
        int var7 = param0.getSectionsCount();
        LevelChunkSection[] var8 = new LevelChunkSection[var7];
        boolean var9 = param0.dimensionType().hasSkyLight();
        ChunkSource var10 = param0.getChunkSource();
        LevelLightEngine var11 = var10.getLightEngine();
        if (var5) {
            var11.retainData(param2, true);
        }

        Registry<Biome> var12 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Biome>> var13 = PalettedContainer.codec(var12, var12, PalettedContainer.Strategy.SECTION_BIOMES);

        for(int var14 = 0; var14 < var6.size(); ++var14) {
            CompoundTag var15 = var6.getCompound(var14);
            int var16 = var15.getByte("Y");
            int var17 = param0.getSectionIndexFromSectionY(var16);
            if (var17 >= 0 && var17 < var8.length) {
                PalettedContainer<BlockState> var18;
                if (var15.contains("block_states", 10)) {
                    var18 = BLOCK_STATE_CODEC.parse(NbtOps.INSTANCE, var15.getCompound("block_states")).getOrThrow(false, LOGGER::error);
                } else {
                    var18 = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
                }

                PalettedContainer<Biome> var20;
                if (var15.contains("biomes", 10)) {
                    var20 = var13.parse(NbtOps.INSTANCE, var15.getCompound("biomes")).getOrThrow(false, LOGGER::error);
                } else {
                    var20 = new PalettedContainer<>(var12, var12.getOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
                }

                LevelChunkSection var22 = new LevelChunkSection(var16, var18, var20);
                var8[var17] = var22;
                param1.checkConsistencyWithBlocks(param2, var22);
            }

            if (var5) {
                if (var15.contains("BlockLight", 7)) {
                    var11.queueSectionData(LightLayer.BLOCK, SectionPos.of(param2, var16), new DataLayer(var15.getByteArray("BlockLight")), true);
                }

                if (var9 && var15.contains("SkyLight", 7)) {
                    var11.queueSectionData(LightLayer.SKY, SectionPos.of(param2, var16), new DataLayer(var15.getByteArray("SkyLight")), true);
                }
            }
        }

        long var23 = var0.getLong("InhabitedTime");
        ChunkStatus.ChunkType var24 = getChunkTypeFromTag(param3);
        ChunkAccess var29;
        if (var24 == ChunkStatus.ChunkType.LEVELCHUNK) {
            TickList<Block> var25;
            if (var0.contains("TileTicks", 9)) {
                var25 = ChunkTickList.create(var0.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
            } else {
                var25 = var3;
            }

            TickList<Fluid> var27;
            if (var0.contains("LiquidTicks", 9)) {
                var27 = ChunkTickList.create(var0.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
            } else {
                var27 = var4;
            }

            var29 = new LevelChunk(param0.getLevel(), param2, var2, var25, var27, var23, var8, param2x -> postLoadChunk(param0, var0, param2x));
        } else {
            ProtoChunk var30 = new ProtoChunk(param2, var2, var8, var3, var4, param0, var12);
            var29 = var30;
            var30.setInhabitedTime(var23);
            var30.setStatus(ChunkStatus.byName(var0.getString("Status")));
            if (var30.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                var30.setLightEngine(var11);
            }

            if (!var5 && var30.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                for(BlockPos var32 : BlockPos.betweenClosed(
                    param2.getMinBlockX(),
                    param0.getMinBuildHeight(),
                    param2.getMinBlockZ(),
                    param2.getMaxBlockX(),
                    param0.getMaxBuildHeight() - 1,
                    param2.getMaxBlockZ()
                )) {
                    if (var29.getBlockState(var32).getLightEmission() != 0) {
                        var30.addLight(var32);
                    }
                }
            }
        }

        var29.setLightCorrect(var5);
        CompoundTag var33 = var0.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> var34 = EnumSet.noneOf(Heightmap.Types.class);

        for(Heightmap.Types var35 : var29.getStatus().heightmapsAfter()) {
            String var36 = var35.getSerializationKey();
            if (var33.contains(var36, 12)) {
                var29.setHeightmap(var35, var33.getLongArray(var36));
            } else {
                var34.add(var35);
            }
        }

        Heightmap.primeHeightmaps(var29, var34);
        CompoundTag var37 = var0.getCompound("Structures");
        var29.setAllStarts(unpackStructureStart(StructurePieceSerializationContext.fromLevel(param0), var37, param0.getSeed()));
        var29.setAllReferences(unpackStructureReferences(param2, var37));
        if (var0.getBoolean("shouldSave")) {
            var29.setUnsaved(true);
        }

        ListTag var38 = var0.getList("PostProcessing", 9);

        for(int var39 = 0; var39 < var38.size(); ++var39) {
            ListTag var40 = var38.getList(var39);

            for(int var41 = 0; var41 < var40.size(); ++var41) {
                var29.addPackedPostProcess(var40.getShort(var41), var39);
            }
        }

        if (var24 == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var29, false);
        } else {
            ProtoChunk var42 = (ProtoChunk)var29;
            ListTag var43 = var0.getList("Entities", 10);

            for(int var44 = 0; var44 < var43.size(); ++var44) {
                var42.addEntity(var43.getCompound(var44));
            }

            ListTag var45 = var0.getList("TileEntities", 10);

            for(int var46 = 0; var46 < var45.size(); ++var46) {
                CompoundTag var47 = var45.getCompound(var46);
                var29.setBlockEntityNbt(var47);
            }

            ListTag var48 = var0.getList("Lights", 9);

            for(int var49 = 0; var49 < var48.size(); ++var49) {
                ListTag var50 = var48.getList(var49);

                for(int var51 = 0; var51 < var50.size(); ++var51) {
                    var42.addLight(var50.getShort(var51), var49);
                }
            }

            CompoundTag var52 = var0.getCompound("CarvingMasks");

            for(String var53 : var52.getAllKeys()) {
                GenerationStep.Carving var54 = GenerationStep.Carving.valueOf(var53);
                var42.setCarvingMask(var54, BitSet.valueOf(var52.getByteArray(var53)));
            }

            return var42;
        }
    }

    public static CompoundTag write(ServerLevel param0, ChunkAccess param1) {
        ChunkPos var0 = param1.getPos();
        CompoundTag var1 = new CompoundTag();
        CompoundTag var2 = new CompoundTag();
        var1.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
        var1.put("Level", var2);
        var2.putInt("xPos", var0.x);
        var2.putInt("zPos", var0.z);
        var2.putLong("LastUpdate", param0.getGameTime());
        var2.putLong("InhabitedTime", param1.getInhabitedTime());
        var2.putString("Status", param1.getStatus().getName());
        UpgradeData var3 = param1.getUpgradeData();
        if (!var3.isEmpty()) {
            var2.put("UpgradeData", var3.write());
        }

        LevelChunkSection[] var4 = param1.getSections();
        ListTag var5 = new ListTag();
        LevelLightEngine var6 = param0.getChunkSource().getLightEngine();
        Registry<Biome> var7 = param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        Codec<PalettedContainer<Biome>> var8 = PalettedContainer.codec(var7, var7, PalettedContainer.Strategy.SECTION_BIOMES);
        boolean var9 = param1.isLightCorrect();

        for(int var10 = var6.getMinLightSection(); var10 < var6.getMaxLightSection(); ++var10) {
            int var11 = param1.getSectionIndexFromSectionY(var10);
            boolean var12 = var11 >= 0 && var11 < var4.length;
            DataLayer var13 = var6.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(var0, var10));
            DataLayer var14 = var6.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(var0, var10));
            if (var12 || var13 != null || var14 != null) {
                CompoundTag var15 = new CompoundTag();
                if (var12) {
                    LevelChunkSection var16 = var4[var11];
                    var15.put("block_states", BLOCK_STATE_CODEC.encodeStart(NbtOps.INSTANCE, var16.getStates()).getOrThrow(false, LOGGER::error));
                    var15.put("biomes", var8.encodeStart(NbtOps.INSTANCE, var16.getBiomes()).getOrThrow(false, LOGGER::error));
                }

                if (var13 != null && !var13.isEmpty()) {
                    var15.putByteArray("BlockLight", var13.getData());
                }

                if (var14 != null && !var14.isEmpty()) {
                    var15.putByteArray("SkyLight", var14.getData());
                }

                if (!var15.isEmpty()) {
                    var15.putByte("Y", (byte)var10);
                    var5.add(var15);
                }
            }
        }

        var2.put("Sections", var5);
        if (var9) {
            var2.putBoolean("isLightOn", true);
        }

        ListTag var17 = new ListTag();

        for(BlockPos var18 : param1.getBlockEntitiesPos()) {
            CompoundTag var19 = param1.getBlockEntityNbtForSaving(var18);
            if (var19 != null) {
                var17.add(var19);
            }
        }

        var2.put("TileEntities", var17);
        if (param1.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk var20 = (ProtoChunk)param1;
            ListTag var21 = new ListTag();
            var21.addAll(var20.getEntities());
            var2.put("Entities", var21);
            var2.put("Lights", packOffsets(var20.getPackedLights()));
            CompoundTag var22 = new CompoundTag();

            for(GenerationStep.Carving var23 : GenerationStep.Carving.values()) {
                BitSet var24 = var20.getCarvingMask(var23);
                if (var24 != null) {
                    var22.putByteArray(var23.toString(), var24.toByteArray());
                }
            }

            var2.put("CarvingMasks", var22);
        }

        TickList<Block> var25 = param1.getBlockTicks();
        if (var25 instanceof ProtoTickList) {
            var2.put("ToBeTicked", ((ProtoTickList)var25).save());
        } else if (var25 instanceof ChunkTickList) {
            var2.put("TileTicks", ((ChunkTickList)var25).save());
        } else {
            var2.put("TileTicks", param0.getBlockTicks().save(var0));
        }

        TickList<Fluid> var26 = param1.getLiquidTicks();
        if (var26 instanceof ProtoTickList) {
            var2.put("LiquidsToBeTicked", ((ProtoTickList)var26).save());
        } else if (var26 instanceof ChunkTickList) {
            var2.put("LiquidTicks", ((ChunkTickList)var26).save());
        } else {
            var2.put("LiquidTicks", param0.getLiquidTicks().save(var0));
        }

        var2.put("PostProcessing", packOffsets(param1.getPostProcessing()));
        CompoundTag var27 = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var28 : param1.getHeightmaps()) {
            if (param1.getStatus().heightmapsAfter().contains(var28.getKey())) {
                var27.put(var28.getKey().getSerializationKey(), new LongArrayTag(var28.getValue().getRawData()));
            }
        }

        var2.put("Heightmaps", var27);
        var2.put("Structures", packStructureData(StructurePieceSerializationContext.fromLevel(param0), var0, param1.getAllStarts(), param1.getAllReferences()));
        return var1;
    }

    public static ChunkStatus.ChunkType getChunkTypeFromTag(@Nullable CompoundTag param0) {
        if (param0 != null) {
            ChunkStatus var0 = ChunkStatus.byName(param0.getCompound("Level").getString("Status"));
            if (var0 != null) {
                return var0.getChunkType();
            }
        }

        return ChunkStatus.ChunkType.PROTOCHUNK;
    }

    private static void postLoadChunk(ServerLevel param0, CompoundTag param1, LevelChunk param2) {
        if (param1.contains("Entities", 9)) {
            ListTag var0 = param1.getList("Entities", 10);
            if (!var0.isEmpty()) {
                param0.addLegacyChunkEntities(EntityType.loadEntitiesRecursive(var0, param0));
            }
        }

        ListTag var1 = param1.getList("TileEntities", 10);

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

        var0.put("Starts", var1);
        CompoundTag var3 = new CompoundTag();

        for(Entry<StructureFeature<?>, LongSet> var4 : param3.entrySet()) {
            var3.put(var4.getKey().getFeatureName(), new LongArrayTag(var4.getValue()));
        }

        var0.put("References", var3);
        return var0;
    }

    private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(StructurePieceSerializationContext param0, CompoundTag param1, long param2) {
        Map<StructureFeature<?>, StructureStart<?>> var0 = Maps.newHashMap();
        CompoundTag var1 = param1.getCompound("Starts");

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
