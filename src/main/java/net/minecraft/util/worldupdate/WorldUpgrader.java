package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class WorldUpgrader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final Registry<LevelStem> dimensions;
    private final Set<ResourceKey<Level>> levels;
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

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess param0, DataFixer param1, Registry<LevelStem> param2, boolean param3) {
        this.dimensions = param2;
        this.levels = param2.registryKeySet().stream().map(Registries::levelStemToLevel).collect(Collectors.toUnmodifiableSet());
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

        for(ResourceKey<Level> var1 : this.levels) {
            List<ChunkPos> var2 = this.getAllChunkPos(var1);
            var0.put(var1, var2.listIterator());
            this.totalChunks += var2.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float var3 = (float)this.totalChunks;
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> var4 = var0.build();
            Builder<ResourceKey<Level>, ChunkStorage> var5 = ImmutableMap.builder();

            for(ResourceKey<Level> var6 : this.levels) {
                Path var7 = this.levelStorage.getDimensionPath(var6);
                var5.put(var6, new ChunkStorage(var7.resolve("region"), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> var8 = var5.build();
            long var9 = Util.getMillis();
            this.status = Component.translatable("optimizeWorld.stage.upgrading");

            while(this.running) {
                boolean var10 = false;
                float var11 = 0.0F;

                for(ResourceKey<Level> var12 : this.levels) {
                    ListIterator<ChunkPos> var13 = var4.get(var12);
                    ChunkStorage var14 = var8.get(var12);
                    if (var13.hasNext()) {
                        ChunkPos var15 = var13.next();
                        boolean var16 = false;

                        try {
                            CompoundTag var17 = var14.read(var15).join().orElse(null);
                            if (var17 != null) {
                                int var18 = ChunkStorage.getVersion(var17);
                                ChunkGenerator var19 = this.dimensions.getOrThrow(Registries.levelToLevelStem(var12)).generator();
                                CompoundTag var20 = var14.upgradeChunkTag(var12, () -> this.overworldDataStorage, var17, var19.getTypeNameForDataFixer());
                                ChunkPos var21 = new ChunkPos(var20.getInt("xPos"), var20.getInt("zPos"));
                                if (!var21.equals(var15)) {
                                    LOGGER.warn("Chunk {} has invalid position {}", var15, var21);
                                }

                                boolean var22 = var18 < SharedConstants.getCurrentVersion().getWorldVersion();
                                if (this.eraseCache) {
                                    var22 = var22 || var20.contains("Heightmaps");
                                    var20.remove("Heightmaps");
                                    var22 = var22 || var20.contains("isLightOn");
                                    var20.remove("isLightOn");
                                    ListTag var23 = var20.getList("sections", 10);

                                    for(int var24 = 0; var24 < var23.size(); ++var24) {
                                        CompoundTag var25 = var23.getCompound(var24);
                                        var22 = var22 || var25.contains("BlockLight");
                                        var25.remove("BlockLight");
                                        var22 = var22 || var25.contains("SkyLight");
                                        var25.remove("SkyLight");
                                    }
                                }

                                if (var22) {
                                    var14.write(var15, var20);
                                    var16 = true;
                                }
                            }
                        } catch (CompletionException | ReportedException var261) {
                            Throwable var27 = var261.getCause();
                            if (!(var27 instanceof IOException)) {
                                throw var261;
                            }

                            LOGGER.error("Error upgrading chunk {}", var15, var27);
                        }

                        if (var16) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        var10 = true;
                    }

                    float var28 = (float)var13.nextIndex() / var3;
                    this.progressMap.put(var12, var28);
                    var11 += var28;
                }

                this.progress = var11;
                if (!var10) {
                    this.running = false;
                }
            }

            this.status = Component.translatable("optimizeWorld.stage.finished");

            for(ChunkStorage var29 : var8.values()) {
                try {
                    var29.close();
                } catch (IOException var251) {
                    LOGGER.error("Error upgrading chunk", (Throwable)var251);
                }
            }

            this.overworldDataStorage.save();
            var9 = Util.getMillis() - var9;
            LOGGER.info("World optimizaton finished after {} ms", var9);
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

    public Set<ResourceKey<Level>> levels() {
        return this.levels;
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
