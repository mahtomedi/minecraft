package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
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
import net.minecraft.nbt.ShortTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.ProtoTickList;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChunkSerializer {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ProtoChunk read(ServerLevel param0, StructureManager param1, PoiManager param2, ChunkPos param3, CompoundTag param4) {
        ChunkGenerator var0 = param0.getChunkSource().getGenerator();
        BiomeSource var1 = var0.getBiomeSource();
        CompoundTag var2 = param4.getCompound("Level");
        ChunkPos var3 = new ChunkPos(var2.getInt("xPos"), var2.getInt("zPos"));
        if (!Objects.equals(param3, var3)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", param3, param3, var3);
        }

        ChunkBiomeContainer var4 = new ChunkBiomeContainer(
            param0.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY),
            param0,
            param3,
            var1,
            var2.contains("Biomes", 11) ? var2.getIntArray("Biomes") : null
        );
        UpgradeData var5 = var2.contains("UpgradeData", 10) ? new UpgradeData(var2.getCompound("UpgradeData"), param0) : UpgradeData.EMPTY;
        ProtoTickList<Block> var6 = new ProtoTickList<>(
            param0x -> param0x == null || param0x.defaultBlockState().isAir(), param3, var2.getList("ToBeTicked", 9), param0
        );
        ProtoTickList<Fluid> var7 = new ProtoTickList<>(
            param0x -> param0x == null || param0x == Fluids.EMPTY, param3, var2.getList("LiquidsToBeTicked", 9), param0
        );
        boolean var8 = var2.getBoolean("isLightOn");
        ListTag var9 = var2.getList("Sections", 10);
        int var10 = param0.getSectionsCount();
        LevelChunkSection[] var11 = new LevelChunkSection[var10];
        boolean var12 = param0.dimensionType().hasSkyLight();
        ChunkSource var13 = param0.getChunkSource();
        LevelLightEngine var14 = var13.getLightEngine();
        if (var8) {
            var14.retainData(param3, true);
        }

        for(int var15 = 0; var15 < var9.size(); ++var15) {
            CompoundTag var16 = var9.getCompound(var15);
            int var17 = var16.getByte("Y");
            if (var16.contains("Palette", 9) && var16.contains("BlockStates", 12)) {
                LevelChunkSection var18 = new LevelChunkSection(var17);
                var18.getStates().read(var16.getList("Palette", 10), var16.getLongArray("BlockStates"));
                var18.recalcBlockCounts();
                if (!var18.isEmpty()) {
                    var11[param0.getSectionIndexFromSectionY(var17)] = var18;
                }

                param2.checkConsistencyWithBlocks(param3, var18);
            }

            if (var8) {
                if (var16.contains("BlockLight", 7)) {
                    var14.queueSectionData(LightLayer.BLOCK, SectionPos.of(param3, var17), new DataLayer(var16.getByteArray("BlockLight")), true);
                }

                if (var12 && var16.contains("SkyLight", 7)) {
                    var14.queueSectionData(LightLayer.SKY, SectionPos.of(param3, var17), new DataLayer(var16.getByteArray("SkyLight")), true);
                }
            }
        }

        long var19 = var2.getLong("InhabitedTime");
        ChunkStatus.ChunkType var20 = getChunkTypeFromTag(param4);
        ChunkAccess var25;
        if (var20 == ChunkStatus.ChunkType.LEVELCHUNK) {
            TickList<Block> var21;
            if (var2.contains("TileTicks", 9)) {
                var21 = ChunkTickList.create(var2.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
            } else {
                var21 = var6;
            }

            TickList<Fluid> var23;
            if (var2.contains("LiquidTicks", 9)) {
                var23 = ChunkTickList.create(var2.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
            } else {
                var23 = var7;
            }

            var25 = new LevelChunk(param0.getLevel(), param3, var4, var5, var21, var23, var19, var11, param2x -> postLoadChunk(param0, var2, param2x));
        } else {
            ProtoChunk var26 = new ProtoChunk(param3, var5, var11, var6, var7, param0);
            var26.setBiomes(var4);
            var25 = var26;
            var26.setInhabitedTime(var19);
            var26.setStatus(ChunkStatus.byName(var2.getString("Status")));
            if (var26.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                var26.setLightEngine(var14);
            }

            if (!var8 && var26.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                for(BlockPos var28 : BlockPos.betweenClosed(
                    param3.getMinBlockX(),
                    param0.getMinBuildHeight(),
                    param3.getMinBlockZ(),
                    param3.getMaxBlockX(),
                    param0.getMaxBuildHeight() - 1,
                    param3.getMaxBlockZ()
                )) {
                    if (var25.getBlockState(var28).getLightEmission() != 0) {
                        var26.addLight(var28);
                    }
                }
            }
        }

        var25.setLightCorrect(var8);
        CompoundTag var29 = var2.getCompound("Heightmaps");
        EnumSet<Heightmap.Types> var30 = EnumSet.noneOf(Heightmap.Types.class);

        for(Heightmap.Types var31 : var25.getStatus().heightmapsAfter()) {
            String var32 = var31.getSerializationKey();
            if (var29.contains(var32, 12)) {
                var25.setHeightmap(var31, var29.getLongArray(var32));
            } else {
                var30.add(var31);
            }
        }

        Heightmap.primeHeightmaps(var25, var30);
        CompoundTag var33 = var2.getCompound("Structures");
        var25.setAllStarts(unpackStructureStart(param0, var33, param0.getSeed()));
        var25.setAllReferences(unpackStructureReferences(param3, var33));
        if (var2.getBoolean("shouldSave")) {
            var25.setUnsaved(true);
        }

        ListTag var34 = var2.getList("PostProcessing", 9);

        for(int var35 = 0; var35 < var34.size(); ++var35) {
            ListTag var36 = var34.getList(var35);

            for(int var37 = 0; var37 < var36.size(); ++var37) {
                var25.addPackedPostProcess(var36.getShort(var37), var35);
            }
        }

        if (var20 == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var25);
        } else {
            ProtoChunk var38 = (ProtoChunk)var25;
            ListTag var39 = var2.getList("Entities", 10);

            for(int var40 = 0; var40 < var39.size(); ++var40) {
                var38.addEntity(var39.getCompound(var40));
            }

            ListTag var41 = var2.getList("TileEntities", 10);

            for(int var42 = 0; var42 < var41.size(); ++var42) {
                CompoundTag var43 = var41.getCompound(var42);
                var25.setBlockEntityNbt(var43);
            }

            ListTag var44 = var2.getList("Lights", 9);

            for(int var45 = 0; var45 < var44.size(); ++var45) {
                ListTag var46 = var44.getList(var45);

                for(int var47 = 0; var47 < var46.size(); ++var47) {
                    var38.addLight(var46.getShort(var47), var45);
                }
            }

            CompoundTag var48 = var2.getCompound("CarvingMasks");

            for(String var49 : var48.getAllKeys()) {
                GenerationStep.Carving var50 = GenerationStep.Carving.valueOf(var49);
                var38.setCarvingMask(var50, BitSet.valueOf(var48.getByteArray(var49)));
            }

            return var38;
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
        boolean var7 = param1.isLightCorrect();

        for(int var8 = var6.getMinLightSection(); var8 < var6.getMaxLightSection(); ++var8) {
            int var9 = var8;
            LevelChunkSection var10 = Arrays.stream(var4)
                .filter(param1x -> param1x != null && SectionPos.blockToSectionCoord(param1x.bottomBlockY()) == var9)
                .findFirst()
                .orElse(LevelChunk.EMPTY_SECTION);
            DataLayer var11 = var6.getLayerListener(LightLayer.BLOCK).getDataLayerData(SectionPos.of(var0, var9));
            DataLayer var12 = var6.getLayerListener(LightLayer.SKY).getDataLayerData(SectionPos.of(var0, var9));
            if (var10 != LevelChunk.EMPTY_SECTION || var11 != null || var12 != null) {
                CompoundTag var13 = new CompoundTag();
                var13.putByte("Y", (byte)(var9 & 0xFF));
                if (var10 != LevelChunk.EMPTY_SECTION) {
                    var10.getStates().write(var13, "Palette", "BlockStates");
                }

                if (var11 != null && !var11.isEmpty()) {
                    var13.putByteArray("BlockLight", var11.getData());
                }

                if (var12 != null && !var12.isEmpty()) {
                    var13.putByteArray("SkyLight", var12.getData());
                }

                var5.add(var13);
            }
        }

        var2.put("Sections", var5);
        if (var7) {
            var2.putBoolean("isLightOn", true);
        }

        ChunkBiomeContainer var14 = param1.getBiomes();
        if (var14 != null) {
            var2.putIntArray("Biomes", var14.writeBiomes());
        }

        ListTag var15 = new ListTag();

        for(BlockPos var16 : param1.getBlockEntitiesPos()) {
            CompoundTag var17 = param1.getBlockEntityNbtForSaving(var16);
            if (var17 != null) {
                var15.add(var17);
            }
        }

        var2.put("TileEntities", var15);
        if (param1.getStatus().getChunkType() == ChunkStatus.ChunkType.PROTOCHUNK) {
            ProtoChunk var18 = (ProtoChunk)param1;
            ListTag var19 = new ListTag();
            var19.addAll(var18.getEntities());
            var2.put("Entities", var19);
            var2.put("Lights", packOffsets(var18.getPackedLights()));
            CompoundTag var20 = new CompoundTag();

            for(GenerationStep.Carving var21 : GenerationStep.Carving.values()) {
                BitSet var22 = var18.getCarvingMask(var21);
                if (var22 != null) {
                    var20.putByteArray(var21.toString(), var22.toByteArray());
                }
            }

            var2.put("CarvingMasks", var20);
        }

        TickList<Block> var23 = param1.getBlockTicks();
        if (var23 instanceof ProtoTickList) {
            var2.put("ToBeTicked", ((ProtoTickList)var23).save());
        } else if (var23 instanceof ChunkTickList) {
            var2.put("TileTicks", ((ChunkTickList)var23).save());
        } else {
            var2.put("TileTicks", param0.getBlockTicks().save(var0));
        }

        TickList<Fluid> var24 = param1.getLiquidTicks();
        if (var24 instanceof ProtoTickList) {
            var2.put("LiquidsToBeTicked", ((ProtoTickList)var24).save());
        } else if (var24 instanceof ChunkTickList) {
            var2.put("LiquidTicks", ((ChunkTickList)var24).save());
        } else {
            var2.put("LiquidTicks", param0.getLiquidTicks().save(var0));
        }

        var2.put("PostProcessing", packOffsets(param1.getPostProcessing()));
        CompoundTag var25 = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var26 : param1.getHeightmaps()) {
            if (param1.getStatus().heightmapsAfter().contains(var26.getKey())) {
                var25.put(var26.getKey().getSerializationKey(), new LongArrayTag(var26.getValue().getRawData()));
            }
        }

        var2.put("Heightmaps", var25);
        var2.put("Structures", packStructureData(param0, var0, param1.getAllStarts(), param1.getAllReferences()));
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
                BlockPos var5 = new BlockPos(var3.getInt("x"), var3.getInt("y"), var3.getInt("z"));
                BlockEntity var6 = BlockEntity.loadStatic(var5, param2.getBlockState(var5), var3);
                if (var6 != null) {
                    param2.setBlockEntity(var6);
                }
            }
        }

    }

    private static CompoundTag packStructureData(
        ServerLevel param0, ChunkPos param1, Map<StructureFeature<?>, StructureStart<?>> param2, Map<StructureFeature<?>, LongSet> param3
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

    private static Map<StructureFeature<?>, StructureStart<?>> unpackStructureStart(ServerLevel param0, CompoundTag param1, long param2) {
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
            var0.put(
                StructureFeature.STRUCTURES_REGISTRY.get(var2.toLowerCase(Locale.ROOT)),
                new LongOpenHashSet(Arrays.stream(var1.getLongArray(var2)).filter(param2 -> {
                    ChunkPos var0x = new ChunkPos(param2);
                    if (var0x.getChessboardDistance(param0) > 8) {
                        LOGGER.warn("Found invalid structure reference [ {} @ {} ] for chunk {}.", var2, var0x, param0);
                        return false;
                    } else {
                        return true;
                    }
                }).toArray())
            );
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
