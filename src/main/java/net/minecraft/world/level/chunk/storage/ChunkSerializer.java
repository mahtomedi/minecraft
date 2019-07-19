package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumSet;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ChunkTickList;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.TickList;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
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
import net.minecraft.world.level.levelgen.structure.StructureFeatureIO;
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
        ChunkGenerator<?> var0 = param0.getChunkSource().getGenerator();
        BiomeSource var1 = var0.getBiomeSource();
        CompoundTag var2 = param4.getCompound("Level");
        ChunkPos var3 = new ChunkPos(var2.getInt("xPos"), var2.getInt("zPos"));
        if (!Objects.equals(param3, var3)) {
            LOGGER.error("Chunk file at {} is in the wrong location; relocating. (Expected {}, got {})", param3, param3, var3);
        }

        Biome[] var4 = new Biome[256];
        BlockPos.MutableBlockPos var5 = new BlockPos.MutableBlockPos();
        if (var2.contains("Biomes", 11)) {
            int[] var6 = var2.getIntArray("Biomes");

            for(int var7 = 0; var7 < var6.length; ++var7) {
                var4[var7] = Registry.BIOME.byId(var6[var7]);
                if (var4[var7] == null) {
                    var4[var7] = var1.getBiome(var5.set((var7 & 15) + param3.getMinBlockX(), 0, (var7 >> 4 & 15) + param3.getMinBlockZ()));
                }
            }
        } else {
            for(int var8 = 0; var8 < var4.length; ++var8) {
                var4[var8] = var1.getBiome(var5.set((var8 & 15) + param3.getMinBlockX(), 0, (var8 >> 4 & 15) + param3.getMinBlockZ()));
            }
        }

        UpgradeData var9 = var2.contains("UpgradeData", 10) ? new UpgradeData(var2.getCompound("UpgradeData")) : UpgradeData.EMPTY;
        ProtoTickList<Block> var10 = new ProtoTickList<>(
            param0x -> param0x == null || param0x.defaultBlockState().isAir(), param3, var2.getList("ToBeTicked", 9)
        );
        ProtoTickList<Fluid> var11 = new ProtoTickList<>(param0x -> param0x == null || param0x == Fluids.EMPTY, param3, var2.getList("LiquidsToBeTicked", 9));
        boolean var12 = var2.getBoolean("isLightOn");
        ListTag var13 = var2.getList("Sections", 10);
        int var14 = 16;
        LevelChunkSection[] var15 = new LevelChunkSection[16];
        boolean var16 = param0.getDimension().isHasSkyLight();
        ChunkSource var17 = param0.getChunkSource();
        LevelLightEngine var18 = var17.getLightEngine();
        if (var12) {
            var18.retainData(param3, true);
        }

        for(int var19 = 0; var19 < var13.size(); ++var19) {
            CompoundTag var20 = var13.getCompound(var19);
            int var21 = var20.getByte("Y");
            if (var20.contains("Palette", 9) && var20.contains("BlockStates", 12)) {
                LevelChunkSection var22 = new LevelChunkSection(var21 << 4);
                var22.getStates().read(var20.getList("Palette", 10), var20.getLongArray("BlockStates"));
                var22.recalcBlockCounts();
                if (!var22.isEmpty()) {
                    var15[var21] = var22;
                }

                param2.checkConsistencyWithBlocks(param3, var22);
            }

            if (var12) {
                if (var20.contains("BlockLight", 7)) {
                    var18.queueSectionData(LightLayer.BLOCK, SectionPos.of(param3, var21), new DataLayer(var20.getByteArray("BlockLight")));
                }

                if (var16 && var20.contains("SkyLight", 7)) {
                    var18.queueSectionData(LightLayer.SKY, SectionPos.of(param3, var21), new DataLayer(var20.getByteArray("SkyLight")));
                }
            }
        }

        long var23 = var2.getLong("InhabitedTime");
        ChunkStatus.ChunkType var24 = getChunkTypeFromTag(param4);
        ChunkAccess var29;
        if (var24 == ChunkStatus.ChunkType.LEVELCHUNK) {
            TickList<Block> var25;
            if (var2.contains("TileTicks", 9)) {
                var25 = ChunkTickList.create(var2.getList("TileTicks", 10), Registry.BLOCK::getKey, Registry.BLOCK::get);
            } else {
                var25 = var10;
            }

            TickList<Fluid> var27;
            if (var2.contains("LiquidTicks", 9)) {
                var27 = ChunkTickList.create(var2.getList("LiquidTicks", 10), Registry.FLUID::getKey, Registry.FLUID::get);
            } else {
                var27 = var11;
            }

            var29 = new LevelChunk(param0.getLevel(), param3, var4, var9, var25, var27, var23, var15, param1x -> postLoadChunk(var2, param1x));
        } else {
            ProtoChunk var30 = new ProtoChunk(param3, var9, var15, var10, var11);
            var29 = var30;
            var30.setBiomes(var4);
            var30.setInhabitedTime(var23);
            var30.setStatus(ChunkStatus.byName(var2.getString("Status")));
            if (var30.getStatus().isOrAfter(ChunkStatus.FEATURES)) {
                var30.setLightEngine(var18);
            }

            if (!var12 && var30.getStatus().isOrAfter(ChunkStatus.LIGHT)) {
                for(BlockPos var32 : BlockPos.betweenClosed(param3.getMinBlockX(), 0, param3.getMinBlockZ(), param3.getMaxBlockX(), 255, param3.getMaxBlockZ())) {
                    if (var29.getBlockState(var32).getLightEmission() != 0) {
                        var30.addLight(var32);
                    }
                }
            }
        }

        var29.setLightCorrect(var12);
        CompoundTag var33 = var2.getCompound("Heightmaps");
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
        CompoundTag var37 = var2.getCompound("Structures");
        var29.setAllStarts(unpackStructureStart(var0, param1, var1, var37));
        var29.setAllReferences(unpackStructureReferences(var37));
        if (var2.getBoolean("shouldSave")) {
            var29.setUnsaved(true);
        }

        ListTag var38 = var2.getList("PostProcessing", 9);

        for(int var39 = 0; var39 < var38.size(); ++var39) {
            ListTag var40 = var38.getList(var39);

            for(int var41 = 0; var41 < var40.size(); ++var41) {
                var29.addPackedPostProcess(var40.getShort(var41), var39);
            }
        }

        if (var24 == ChunkStatus.ChunkType.LEVELCHUNK) {
            return new ImposterProtoChunk((LevelChunk)var29);
        } else {
            ProtoChunk var42 = (ProtoChunk)var29;
            ListTag var43 = var2.getList("Entities", 10);

            for(int var44 = 0; var44 < var43.size(); ++var44) {
                var42.addEntity(var43.getCompound(var44));
            }

            ListTag var45 = var2.getList("TileEntities", 10);

            for(int var46 = 0; var46 < var45.size(); ++var46) {
                CompoundTag var47 = var45.getCompound(var46);
                var29.setBlockEntityNbt(var47);
            }

            ListTag var48 = var2.getList("Lights", 9);

            for(int var49 = 0; var49 < var48.size(); ++var49) {
                ListTag var50 = var48.getList(var49);

                for(int var51 = 0; var51 < var50.size(); ++var51) {
                    var42.addLight(var50.getShort(var51), var49);
                }
            }

            CompoundTag var52 = var2.getCompound("CarvingMasks");

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
        boolean var7 = param1.isLightCorrect();

        for(int var8 = -1; var8 < 17; ++var8) {
            int var9 = var8;
            LevelChunkSection var10 = Arrays.stream(var4)
                .filter(param1x -> param1x != null && param1x.bottomBlockY() >> 4 == var9)
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

        Biome[] var14 = param1.getBiomes();
        int[] var15 = var14 != null ? new int[var14.length] : new int[0];
        if (var14 != null) {
            for(int var16 = 0; var16 < var14.length; ++var16) {
                var15[var16] = Registry.BIOME.getId(var14[var16]);
            }
        }

        var2.putIntArray("Biomes", var15);
        ListTag var17 = new ListTag();

        for(BlockPos var18 : param1.getBlockEntitiesPos()) {
            CompoundTag var19 = param1.getBlockEntityNbtForSaving(var18);
            if (var19 != null) {
                var17.add(var19);
            }
        }

        var2.put("TileEntities", var17);
        ListTag var20 = new ListTag();
        if (param1.getStatus().getChunkType() == ChunkStatus.ChunkType.LEVELCHUNK) {
            LevelChunk var21 = (LevelChunk)param1;
            var21.setLastSaveHadEntities(false);

            for(int var22 = 0; var22 < var21.getEntitySections().length; ++var22) {
                for(Entity var23 : var21.getEntitySections()[var22]) {
                    CompoundTag var24 = new CompoundTag();
                    if (var23.save(var24)) {
                        var21.setLastSaveHadEntities(true);
                        var20.add(var24);
                    }
                }
            }
        } else {
            ProtoChunk var25 = (ProtoChunk)param1;
            var20.addAll(var25.getEntities());
            var2.put("Lights", packOffsets(var25.getPackedLights()));
            CompoundTag var26 = new CompoundTag();

            for(GenerationStep.Carving var27 : GenerationStep.Carving.values()) {
                var26.putByteArray(var27.toString(), param1.getCarvingMask(var27).toByteArray());
            }

            var2.put("CarvingMasks", var26);
        }

        var2.put("Entities", var20);
        TickList<Block> var28 = param1.getBlockTicks();
        if (var28 instanceof ProtoTickList) {
            var2.put("ToBeTicked", ((ProtoTickList)var28).save());
        } else if (var28 instanceof ChunkTickList) {
            var2.put("TileTicks", ((ChunkTickList)var28).save(param0.getGameTime()));
        } else {
            var2.put("TileTicks", param0.getBlockTicks().save(var0));
        }

        TickList<Fluid> var29 = param1.getLiquidTicks();
        if (var29 instanceof ProtoTickList) {
            var2.put("LiquidsToBeTicked", ((ProtoTickList)var29).save());
        } else if (var29 instanceof ChunkTickList) {
            var2.put("LiquidTicks", ((ChunkTickList)var29).save(param0.getGameTime()));
        } else {
            var2.put("LiquidTicks", param0.getLiquidTicks().save(var0));
        }

        var2.put("PostProcessing", packOffsets(param1.getPostProcessing()));
        CompoundTag var30 = new CompoundTag();

        for(Entry<Heightmap.Types, Heightmap> var31 : param1.getHeightmaps()) {
            if (param1.getStatus().heightmapsAfter().contains(var31.getKey())) {
                var30.put(var31.getKey().getSerializationKey(), new LongArrayTag(var31.getValue().getRawData()));
            }
        }

        var2.put("Heightmaps", var30);
        var2.put("Structures", packStructureData(var0, param1.getAllStarts(), param1.getAllReferences()));
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

    private static void postLoadChunk(CompoundTag param0, LevelChunk param1) {
        ListTag var0 = param0.getList("Entities", 10);
        Level var1 = param1.getLevel();

        for(int var2 = 0; var2 < var0.size(); ++var2) {
            CompoundTag var3 = var0.getCompound(var2);
            EntityType.loadEntityRecursive(var3, var1, param1x -> {
                param1.addEntity(param1x);
                return param1x;
            });
            param1.setLastSaveHadEntities(true);
        }

        ListTag var4 = param0.getList("TileEntities", 10);

        for(int var5 = 0; var5 < var4.size(); ++var5) {
            CompoundTag var6 = var4.getCompound(var5);
            boolean var7 = var6.getBoolean("keepPacked");
            if (var7) {
                param1.setBlockEntityNbt(var6);
            } else {
                BlockEntity var8 = BlockEntity.loadStatic(var6);
                if (var8 != null) {
                    param1.addBlockEntity(var8);
                }
            }
        }

    }

    private static CompoundTag packStructureData(ChunkPos param0, Map<String, StructureStart> param1, Map<String, LongSet> param2) {
        CompoundTag var0 = new CompoundTag();
        CompoundTag var1 = new CompoundTag();

        for(Entry<String, StructureStart> var2 : param1.entrySet()) {
            var1.put(var2.getKey(), var2.getValue().createTag(param0.x, param0.z));
        }

        var0.put("Starts", var1);
        CompoundTag var3 = new CompoundTag();

        for(Entry<String, LongSet> var4 : param2.entrySet()) {
            var3.put(var4.getKey(), new LongArrayTag(var4.getValue()));
        }

        var0.put("References", var3);
        return var0;
    }

    private static Map<String, StructureStart> unpackStructureStart(ChunkGenerator<?> param0, StructureManager param1, BiomeSource param2, CompoundTag param3) {
        Map<String, StructureStart> var0 = Maps.newHashMap();
        CompoundTag var1 = param3.getCompound("Starts");

        for(String var2 : var1.getAllKeys()) {
            var0.put(var2, StructureFeatureIO.loadStaticStart(param0, param1, param2, var1.getCompound(var2)));
        }

        return var0;
    }

    private static Map<String, LongSet> unpackStructureReferences(CompoundTag param0) {
        Map<String, LongSet> var0 = Maps.newHashMap();
        CompoundTag var1 = param0.getCompound("References");

        for(String var2 : var1.getAllKeys()) {
            var0.put(var2, new LongOpenHashSet(var1.getLongArray(var2)));
        }

        return var0;
    }

    public static ListTag packOffsets(ShortList[] param0) {
        ListTag var0 = new ListTag();

        for(ShortList var1 : param0) {
            ListTag var2 = new ListTag();
            if (var1 != null) {
                for(Short var3 : var1) {
                    var2.add(new ShortTag(var3));
                }
            }

            var0.add(var2);
        }

        return var0;
    }
}
