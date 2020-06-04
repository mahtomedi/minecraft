package net.minecraft.world.level.storage;

import com.google.common.collect.Lists;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.OverworldBiomeSource;
import net.minecraft.world.level.chunk.storage.OldChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class McRegionUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();

    static boolean convertLevel(LevelStorageSource.LevelStorageAccess param0, ProgressListener param1) {
        param1.progressStagePercentage(0);
        List<File> var0 = Lists.newArrayList();
        List<File> var1 = Lists.newArrayList();
        List<File> var2 = Lists.newArrayList();
        File var3 = param0.getDimensionPath(Level.OVERWORLD);
        File var4 = param0.getDimensionPath(Level.NETHER);
        File var5 = param0.getDimensionPath(Level.END);
        LOGGER.info("Scanning folders...");
        addRegionFiles(var3, var0);
        if (var4.exists()) {
            addRegionFiles(var4, var1);
        }

        if (var5.exists()) {
            addRegionFiles(var5, var2);
        }

        int var6 = var0.size() + var1.size() + var2.size();
        LOGGER.info("Total conversion count is {}", var6);
        RegistryAccess.RegistryHolder var7 = RegistryAccess.builtin();
        RegistryReadOps<Tag> var8 = RegistryReadOps.create(NbtOps.INSTANCE, ResourceManager.Empty.INSTANCE, var7);
        WorldData var9 = param0.getDataTag(var8, DataPackConfig.DEFAULT);
        long var10 = var9 != null ? var9.worldGenSettings().seed() : 0L;
        BiomeSource var11;
        if (var9 != null && var9.worldGenSettings().isFlatWorld()) {
            var11 = new FixedBiomeSource(Biomes.PLAINS);
        } else {
            var11 = new OverworldBiomeSource(var10, false, false);
        }

        convertRegions(new File(var3, "region"), var0, var11, 0, var6, param1);
        convertRegions(new File(var4, "region"), var1, new FixedBiomeSource(Biomes.NETHER_WASTES), var0.size(), var6, param1);
        convertRegions(new File(var5, "region"), var2, new FixedBiomeSource(Biomes.THE_END), var0.size() + var1.size(), var6, param1);
        makeMcrLevelDatBackup(param0);
        param0.saveDataTag(var7, var9);
        return true;
    }

    private static void makeMcrLevelDatBackup(LevelStorageSource.LevelStorageAccess param0) {
        File var0 = param0.getLevelPath(LevelResource.LEVEL_DATA_FILE).toFile();
        if (!var0.exists()) {
            LOGGER.warn("Unable to create level.dat_mcr backup");
        } else {
            File var1 = new File(var0.getParent(), "level.dat_mcr");
            if (!var0.renameTo(var1)) {
                LOGGER.warn("Unable to create level.dat_mcr backup");
            }

        }
    }

    private static void convertRegions(File param0, Iterable<File> param1, BiomeSource param2, int param3, int param4, ProgressListener param5) {
        for(File var0 : param1) {
            convertRegion(param0, var0, param2, param3, param4, param5);
            ++param3;
            int var1 = (int)Math.round(100.0 * (double)param3 / (double)param4);
            param5.progressStagePercentage(var1);
        }

    }

    private static void convertRegion(File param0, File param1, BiomeSource param2, int param3, int param4, ProgressListener param5) {
        String var0 = param1.getName();

        try (
            RegionFile var1 = new RegionFile(param1, param0, true);
            RegionFile var2 = new RegionFile(new File(param0, var0.substring(0, var0.length() - ".mcr".length()) + ".mca"), param0, true);
        ) {
            for(int var3 = 0; var3 < 32; ++var3) {
                for(int var4 = 0; var4 < 32; ++var4) {
                    ChunkPos var5 = new ChunkPos(var3, var4);
                    if (var1.hasChunk(var5) && !var2.hasChunk(var5)) {
                        CompoundTag var7;
                        try (DataInputStream var6 = var1.getChunkDataInputStream(var5)) {
                            if (var6 == null) {
                                LOGGER.warn("Failed to fetch input stream for chunk {}", var5);
                                continue;
                            }

                            var7 = NbtIo.read(var6);
                        } catch (IOException var106) {
                            LOGGER.warn("Failed to read data for chunk {}", var5, var106);
                            continue;
                        }

                        CompoundTag var11 = var7.getCompound("Level");
                        OldChunkStorage.OldLevelChunk var12 = OldChunkStorage.load(var11);
                        CompoundTag var13 = new CompoundTag();
                        CompoundTag var14 = new CompoundTag();
                        var13.put("Level", var14);
                        OldChunkStorage.convertToAnvilFormat(var12, var14, param2);

                        try (DataOutputStream var15 = var2.getChunkDataOutputStream(var5)) {
                            NbtIo.write(var13, var15);
                        }
                    }
                }

                int var16 = (int)Math.round(100.0 * (double)(param3 * 1024) / (double)(param4 * 1024));
                int var17 = (int)Math.round(100.0 * (double)((var3 + 1) * 32 + param3 * 1024) / (double)(param4 * 1024));
                if (var17 > var16) {
                    param5.progressStagePercentage(var17);
                }
            }
        } catch (IOException var111) {
            LOGGER.error("Failed to upgrade region file {}", param1, var111);
        }

    }

    private static void addRegionFiles(File param0, Collection<File> param1) {
        File var0 = new File(param0, "region");
        File[] var1 = var0.listFiles((param0x, param1x) -> param1x.endsWith(".mcr"));
        if (var1 != null) {
            Collections.addAll(param1, var1);
        }

    }
}
