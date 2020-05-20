package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.WorldData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final String levelName;
    private final ImmutableMap<ResourceKey<DimensionType>, DimensionType> dimensionTypes;
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
    private final Object2FloatMap<DimensionType> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
    private volatile Component status = new TranslatableComponent("optimizeWorld.stage.counting");
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    public WorldUpgrader(LevelStorageSource.LevelStorageAccess param0, DataFixer param1, WorldData param2, boolean param3) {
        this.levelName = param2.getLevelName();
        this.dimensionTypes = param2.worldGenSettings()
            .dimensions()
            .entrySet()
            .stream()
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, param0x -> param0x.getValue().getFirst()));
        this.eraseCache = param3;
        this.dataFixer = param1;
        this.levelStorage = param0;
        param0.saveDataTag(param2);
        this.overworldDataStorage = new DimensionDataStorage(new File(this.levelStorage.getDimensionPath(DimensionType.OVERWORLD_LOCATION), "data"), param1);
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((param0x, param1x) -> {
            LOGGER.error("Error upgrading world", param1x);
            this.status = new TranslatableComponent("optimizeWorld.stage.failed");
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
        Builder<DimensionType, ListIterator<ChunkPos>> var0 = ImmutableMap.builder();

        for(Entry<ResourceKey<DimensionType>, DimensionType> var1 : this.dimensionTypes.entrySet()) {
            List<ChunkPos> var2 = this.getAllChunkPos(var1.getKey());
            var0.put(var1.getValue(), var2.listIterator());
            this.totalChunks += var2.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float var3 = (float)this.totalChunks;
            ImmutableMap<DimensionType, ListIterator<ChunkPos>> var4 = var0.build();
            Builder<DimensionType, ChunkStorage> var5 = ImmutableMap.builder();

            for(Entry<ResourceKey<DimensionType>, DimensionType> var6 : this.dimensionTypes.entrySet()) {
                File var7 = this.levelStorage.getDimensionPath(var6.getKey());
                var5.put(var6.getValue(), new ChunkStorage(new File(var7, "region"), this.dataFixer, true));
            }

            ImmutableMap<DimensionType, ChunkStorage> var8 = var5.build();
            long var9 = Util.getMillis();
            this.status = new TranslatableComponent("optimizeWorld.stage.upgrading");

            while(this.running) {
                boolean var10 = false;
                float var11 = 0.0F;

                for(DimensionType var12 : this.dimensionTypes.values()) {
                    ListIterator<ChunkPos> var13 = var4.get(var12);
                    ChunkStorage var14 = var8.get(var12);
                    if (var13.hasNext()) {
                        ChunkPos var15 = var13.next();
                        boolean var16 = false;

                        try {
                            CompoundTag var17 = var14.read(var15);
                            if (var17 != null) {
                                int var18 = ChunkStorage.getVersion(var17);
                                CompoundTag var19 = var14.upgradeChunkTag(var12, () -> this.overworldDataStorage, var17);
                                CompoundTag var20 = var19.getCompound("Level");
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
                                }

                                if (var22) {
                                    var14.write(var15, var19);
                                    var16 = true;
                                }
                            }
                        } catch (ReportedException var23) {
                            Throwable var24 = var23.getCause();
                            if (!(var24 instanceof IOException)) {
                                throw var23;
                            }

                            LOGGER.error("Error upgrading chunk {}", var15, var24);
                        } catch (IOException var241) {
                            LOGGER.error("Error upgrading chunk {}", var15, var241);
                        }

                        if (var16) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        var10 = true;
                    }

                    float var26 = (float)var13.nextIndex() / var3;
                    this.progressMap.put(var12, var26);
                    var11 += var26;
                }

                this.progress = var11;
                if (!var10) {
                    this.running = false;
                }
            }

            this.status = new TranslatableComponent("optimizeWorld.stage.finished");

            for(ChunkStorage var27 : var8.values()) {
                try {
                    var27.close();
                } catch (IOException var221) {
                    LOGGER.error("Error upgrading chunk", (Throwable)var221);
                }
            }

            this.overworldDataStorage.save();
            var9 = Util.getMillis() - var9;
            LOGGER.info("World optimizaton finished after {} ms", var9);
            this.finished = true;
        }
    }

    private List<ChunkPos> getAllChunkPos(ResourceKey<DimensionType> param0) {
        File var0 = this.levelStorage.getDimensionPath(param0);
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

                    try (RegionFile var8 = new RegionFile(var4, var1, true)) {
                        for(int var9 = 0; var9 < 32; ++var9) {
                            for(int var10 = 0; var10 < 32; ++var10) {
                                ChunkPos var11 = new ChunkPos(var9 + var6, var10 + var7);
                                if (var8.doesChunkExist(var11)) {
                                    var3.add(var11);
                                }
                            }
                        }
                    } catch (Throwable var28) {
                    }
                }
            }

            return var3;
        }
    }

    public boolean isFinished() {
        return this.finished;
    }

    @OnlyIn(Dist.CLIENT)
    public ImmutableMap<ResourceKey<DimensionType>, DimensionType> dimensionTypes() {
        return this.dimensionTypes;
    }

    @OnlyIn(Dist.CLIENT)
    public float dimensionProgress(DimensionType param0) {
        return this.progressMap.getFloat(param0);
    }

    @OnlyIn(Dist.CLIENT)
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
