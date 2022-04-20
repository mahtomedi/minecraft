package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class WorldUpgrader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final WorldGenSettings worldGenSettings;
    private final boolean eraseCache;
    private final LevelStorageSource.LevelStorageAccess levelStorage;
    private final Thread thread;
    private final DataFixer dataFixer;
    private volatile boolean running = true;
    private volatile boolean finished;
    private volatile float progress;
    private volatile int totalChunks;
    private volatile int converted;
    private volatile int skipped;
    private final Object2FloatMap<ResourceKey<Level>> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
    private volatile Component status = Component.translatable("optimizeWorld.stage.counting");
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess param0, DataFixer param1, WorldGenSettings param2, boolean param3) {
        this.worldGenSettings = param2;
        this.eraseCache = param3;
        this.dataFixer = param1;
        this.levelStorage = param0;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), param1);
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((param0x, param1x) -> {
            LOGGER.error("Error upgrading world", param1x);
            this.status = Component.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;

        try {
            this.thread.join();
        } catch (InterruptedException var2) {
        }

    }

    private void work() {
        this.totalChunks = 0;
        Builder<ResourceKey<Level>, ListIterator<ChunkPos>> var0 = ImmutableMap.builder();
        ImmutableSet<ResourceKey<Level>> var1 = this.worldGenSettings.levels();

        for(ResourceKey<Level> var2 : var1) {
            List<ChunkPos> var3 = this.getAllChunkPos(var2);
            var0.put(var2, var3.listIterator());
            this.totalChunks += var3.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float var4 = (float)this.totalChunks;
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> var5 = var0.build();
            Builder<ResourceKey<Level>, ChunkStorage> var6 = ImmutableMap.builder();

            for(ResourceKey<Level> var7 : var1) {
                Path var8 = this.levelStorage.getDimensionPath(var7);
                var6.put(var7, new ChunkStorage(var8.resolve("region"), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> var9 = var6.build();
            long var10 = Util.getMillis();
            this.status = Component.translatable("optimizeWorld.stage.upgrading");

            while(this.running) {
                boolean var11 = false;
                float var12 = 0.0F;

                for(ResourceKey<Level> var13 : var1) {
                    ListIterator<ChunkPos> var14 = var5.get(var13);
                    ChunkStorage var15 = var9.get(var13);
                    if (var14.hasNext()) {
                        ChunkPos var16 = var14.next();
                        boolean var17 = false;

                        try {
                            CompoundTag var18 = var15.read(var16).join().orElse(null);
                            if (var18 != null) {
                                int var19 = ChunkStorage.getVersion(var18);
                                ChunkGenerator var20 = this.worldGenSettings.dimensions().get(WorldGenSettings.levelToLevelStem(var13)).generator();
                                CompoundTag var21 = var15.upgradeChunkTag(var13, () -> this.overworldDataStorage, var18, var20.getTypeNameForDataFixer());
                                ChunkPos var22 = new ChunkPos(var21.getInt("xPos"), var21.getInt("zPos"));
                                if (!var22.equals(var16)) {
                                    LOGGER.warn("Chunk {} has invalid position {}", var16, var22);
                                }

                                boolean var23 = var19 < SharedConstants.getCurrentVersion().getWorldVersion();
                                if (this.eraseCache) {
                                    var23 = var23 || var21.contains("Heightmaps");
                                    var21.remove("Heightmaps");
                                    var23 = var23 || var21.contains("isLightOn");
                                    var21.remove("isLightOn");
                                    ListTag var24 = var21.getList("sections", 10);

                                    for(int var25 = 0; var25 < var24.size(); ++var25) {
                                        CompoundTag var26 = var24.getCompound(var25);
                                        var23 = var23 || var26.contains("BlockLight");
                                        var26.remove("BlockLight");
                                        var23 = var23 || var26.contains("SkyLight");
                                        var26.remove("SkyLight");
                                    }
                                }

                                if (var23) {
                                    var15.write(var16, var21);
                                    var17 = true;
                                }
                            }
                        } catch (CompletionException | ReportedException var271) {
                            Throwable var28 = var271.getCause();
                            if (!(var28 instanceof IOException)) {
                                throw var271;
                            }

                            LOGGER.error("Error upgrading chunk {}", var16, var28);
                        }

                        if (var17) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        var11 = true;
                    }

                    float var29 = (float)var14.nextIndex() / var4;
                    this.progressMap.put(var13, var29);
                    var12 += var29;
                }

                this.progress = var12;
                if (!var11) {
                    this.running = false;
                }
            }

            this.status = Component.translatable("optimizeWorld.stage.finished");

            for(ChunkStorage var30 : var9.values()) {
                try {
                    var30.close();
                } catch (IOException var261) {
                    LOGGER.error("Error upgrading chunk", (Throwable)var261);
                }
            }

            this.overworldDataStorage.save();
            var10 = Util.getMillis() - var10;
            LOGGER.info("World optimizaton finished after {} ms", var10);
            this.finished = true;
        }
    }

    private List<ChunkPos> getAllChunkPos(ResourceKey<Level> param0) {
        File var0 = this.levelStorage.getDimensionPath(param0).toFile();
        File var1 = new File(var0, "region");
        File[] var2 = var1.listFiles((param0x, param1) -> param1.endsWith(".mca"));
        if (var2 == null) {
            return ImmutableList.of();
        } else {
            List<ChunkPos> var3 = Lists.newArrayList();

            for(File var4 : var2) {
                Matcher var5 = REGEX.matcher(var4.getName());
                if (var5.matches()) {
                    int var6 = Integer.parseInt(var5.group(1)) << 5;
                    int var7 = Integer.parseInt(var5.group(2)) << 5;

                    try (RegionFile var8 = new RegionFile(var4.toPath(), var1.toPath(), true)) {
                        for(int var9 = 0; var9 < 32; ++var9) {
                            for(int var10 = 0; var10 < 32; ++var10) {
                                ChunkPos var11 = new ChunkPos(var9 + var6, var10 + var7);
                                if (var8.doesChunkExist(var11)) {
                                    var3.add(var11);
                                }
                            }
                        }
                    } catch (Throwable var19) {
                    }
                }
            }

            return var3;
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.worldGenSettings.levels();
    }

    public float dimensionProgress(ResourceKey<Level> param0) {
        return this.progressMap.getFloat(param0);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public Component getStatus() {
        return this.status;
    }
}
