package net.minecraft.world.level.chunk.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.OldDataLayer;

public class OldChunkStorage {
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

    public static void convertToAnvilFormat(OldChunkStorage.OldLevelChunk param0, CompoundTag param1, BiomeSource param2) {
        param1.putInt("xPos", param0.x);
        param1.putInt("zPos", param0.z);
        param1.putLong("LastUpdate", param0.lastUpdated);
        int[] var0 = new int[param0.heightmap.length];

        for(int var1 = 0; var1 < param0.heightmap.length; ++var1) {
            var0[var1] = param0.heightmap[var1];
        }

        param1.putIntArray("HeightMap", var0);
        param1.putBoolean("TerrainPopulated", param0.terrainPopulated);
        ListTag var2 = new ListTag();

        for(int var3 = 0; var3 < 8; ++var3) {
            boolean var4 = true;

            for(int var5 = 0; var5 < 16 && var4; ++var5) {
                for(int var6 = 0; var6 < 16 && var4; ++var6) {
                    for(int var7 = 0; var7 < 16; ++var7) {
                        int var8 = var5 << 11 | var7 << 7 | var6 + (var3 << 4);
                        int var9 = param0.blocks[var8];
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
                            int var18 = param0.blocks[var17];
                            var10[var15 << 8 | var16 << 4 | var14] = (byte)(var18 & 0xFF);
                            var11.set(var14, var15, var16, param0.data.get(var14, var15 + (var3 << 4), var16));
                            var12.set(var14, var15, var16, param0.skyLight.get(var14, var15 + (var3 << 4), var16));
                            var13.set(var14, var15, var16, param0.blockLight.get(var14, var15 + (var3 << 4), var16));
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

        param1.put("Sections", var2);
        byte[] var20 = new byte[256];
        BlockPos.MutableBlockPos var21 = new BlockPos.MutableBlockPos();

        for(int var22 = 0; var22 < 16; ++var22) {
            for(int var23 = 0; var23 < 16; ++var23) {
                var21.set(param0.x << 4 | var22, 0, param0.z << 4 | var23);
                var20[var23 << 4 | var22] = (byte)(Registry.BIOME.getId(param2.getBiome(var21)) & 0xFF);
            }
        }

        param1.putByteArray("Biomes", var20);
        param1.put("Entities", param0.entities);
        param1.put("TileEntities", param0.blockEntities);
        if (param0.blockTicks != null) {
            param1.put("TileTicks", param0.blockTicks);
        }

        param1.putBoolean("convertedFromAlphaFormat", true);
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
