package net.minecraft.world.level.chunk.storage;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkBiomeContainer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;

public class OldChunkStorage {
    private static final int DATALAYER_BITS = 7;
    private static final LevelHeightAccessor OLD_LEVEL_HEIGHT = new LevelHeightAccessor() {
        @Override
        public int getMinBuildHeight() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 128;
        }
    };

    public static OldChunkStorage.OldLevelChunk load(CompoundTag param0) {
        int var0 = param0.getInt("xPos");
        int var1 = param0.getInt("zPos");
        OldChunkStorage.OldLevelChunk var2 = new OldChunkStorage.OldLevelChunk(var0, var1);
        var2.blocks = param0.getByteArray("Blocks");
        var2.data = new OldDataLayer(param0.getByteArray("Data"), 7);
        var2.skyLight = new OldDataLayer(param0.getByteArray("SkyLight"), 7);
        var2.blockLight = new OldDataLayer(param0.getByteArray("BlockLight"), 7);
        var2.heightmap = param0.getByteArray("HeightMap");
        var2.terrainPopulated = param0.getBoolean("TerrainPopulated");
        var2.entities = param0.getList("Entities", 10);
        var2.blockEntities = param0.getList("TileEntities", 10);
        var2.blockTicks = param0.getList("TileTicks", 10);

        try {
            var2.lastUpdated = param0.getLong("LastUpdate");
        } catch (ClassCastException var5) {
            var2.lastUpdated = (long)param0.getInt("LastUpdate");
        }

        return var2;
    }

    public static void convertToAnvilFormat(RegistryAccess.RegistryHolder param0, OldChunkStorage.OldLevelChunk param1, CompoundTag param2, BiomeSource param3) {
        param2.putInt("xPos", param1.x);
        param2.putInt("zPos", param1.z);
        param2.putLong("LastUpdate", param1.lastUpdated);
        int[] var0 = new int[param1.heightmap.length];

        for(int var1 = 0; var1 < param1.heightmap.length; ++var1) {
            var0[var1] = param1.heightmap[var1];
        }

        param2.putIntArray("HeightMap", var0);
        param2.putBoolean("TerrainPopulated", param1.terrainPopulated);
        ListTag var2 = new ListTag();

        for(int var3 = 0; var3 < 8; ++var3) {
            boolean var4 = true;

            for(int var5 = 0; var5 < 16 && var4; ++var5) {
                for(int var6 = 0; var6 < 16 && var4; ++var6) {
                    for(int var7 = 0; var7 < 16; ++var7) {
                        int var8 = var5 << 11 | var7 << 7 | var6 + (var3 << 4);
                        int var9 = param1.blocks[var8];
                        if (var9 != 0) {
                            var4 = false;
                            break;
                        }
                    }
                }
            }

            if (!var4) {
                byte[] var10 = new byte[4096];
                DataLayer var11 = new DataLayer();
                DataLayer var12 = new DataLayer();
                DataLayer var13 = new DataLayer();

                for(int var14 = 0; var14 < 16; ++var14) {
                    for(int var15 = 0; var15 < 16; ++var15) {
                        for(int var16 = 0; var16 < 16; ++var16) {
                            int var17 = var14 << 11 | var16 << 7 | var15 + (var3 << 4);
                            int var18 = param1.blocks[var17];
                            var10[var15 << 8 | var16 << 4 | var14] = (byte)(var18 & 0xFF);
                            var11.set(var14, var15, var16, param1.data.get(var14, var15 + (var3 << 4), var16));
                            var12.set(var14, var15, var16, param1.skyLight.get(var14, var15 + (var3 << 4), var16));
                            var13.set(var14, var15, var16, param1.blockLight.get(var14, var15 + (var3 << 4), var16));
                        }
                    }
                }

                CompoundTag var19 = new CompoundTag();
                var19.putByte("Y", (byte)(var3 & 0xFF));
                var19.putByteArray("Blocks", var10);
                var19.putByteArray("Data", var11.getData());
                var19.putByteArray("SkyLight", var12.getData());
                var19.putByteArray("BlockLight", var13.getData());
                var2.add(var19);
            }
        }

        param2.put("Sections", var2);
        param2.putIntArray(
            "Biomes",
            new ChunkBiomeContainer(param0.registryOrThrow(Registry.BIOME_REGISTRY), OLD_LEVEL_HEIGHT, new ChunkPos(param1.x, param1.z), param3).writeBiomes()
        );
        param2.put("Entities", param1.entities);
        param2.put("TileEntities", param1.blockEntities);
        if (param1.blockTicks != null) {
            param2.put("TileTicks", param1.blockTicks);
        }

        param2.putBoolean("convertedFromAlphaFormat", true);
    }

    public static class OldLevelChunk {
        public long lastUpdated;
        public boolean terrainPopulated;
        public byte[] heightmap;
        public OldDataLayer blockLight;
        public OldDataLayer skyLight;
        public OldDataLayer data;
        public byte[] blocks;
        public ListTag entities;
        public ListTag blockEntities;
        public ListTag blockTicks;
        public final int x;
        public final int z;

        public OldLevelChunk(int param0, int param1) {
            this.x = param0;
            this.z = param1;
        }
    }
}
