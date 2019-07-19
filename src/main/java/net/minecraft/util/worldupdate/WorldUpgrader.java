package net.minecraft.util.worldupdate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WorldUpgrader {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().setDaemon(true).build();
    private final String levelName;
    private final boolean eraseCache;
    private final LevelStorage levelStorage;
    private final Thread thread;
    private final File pathToWorld;
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

    public WorldUpgrader(String param0, LevelStorageSource param1, LevelData param2, boolean param3) {
        this.levelName = param2.getLevelName();
        this.eraseCache = param3;
        this.levelStorage = param1.selectLevel(param0, null);
        this.levelStorage.saveLevelData(param2);
        this.overworldDataStorage = new DimensionDataStorage(
            new File(DimensionType.OVERWORLD.getStorageFolder(this.levelStorage.getFolder()), "data"), this.levelStorage.getFixerUpper()
        );
        this.pathToWorld = this.levelStorage.getFolder();
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((param0x, param1x) -> {
            LOGGER.error("Error upgrading world", param1x);
            this.status = new TranslatableComponent("optimizeWorld.stage.failed");
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
        File var0 = this.levelStorage.getFolder();
        this.totalChunks = 0;
        Builder<DimensionType, ListIterator<ChunkPos>> var1 = ImmutableMap.builder();

        for(DimensionType var2 : DimensionType.getAllTypes()) {
            List<ChunkPos> var3 = this.getAllChunkPos(var2);
            var1.put(var2, var3.listIterator());
            this.totalChunks += var3.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float var4 = (float)this.totalChunks;
            ImmutableMap<DimensionType, ListIterator<ChunkPos>> var5 = var1.build();
            Builder<DimensionType, ChunkStorage> var6 = ImmutableMap.builder();

            for(DimensionType var7 : DimensionType.getAllTypes()) {
                File var8 = var7.getStorageFolder(var0);
                var6.put(var7, new ChunkStorage(new File(var8, "region"), this.levelStorage.getFixerUpper()));
            }

            ImmutableMap<DimensionType, ChunkStorage> var9 = var6.build();
            long var10 = Util.getMillis();
            this.status = new TranslatableComponent("optimizeWorld.stage.upgrading");

            while(this.running) {
                boolean var11 = false;
                float var12 = 0.0F;

                for(DimensionType var13 : DimensionType.getAllTypes()) {
                    ListIterator<ChunkPos> var14 = var5.get(var13);
                    ChunkStorage var15 = var9.get(var13);
                    if (var14.hasNext()) {
                        ChunkPos var16 = var14.next();
                        boolean var17 = false;

                        try {
                            CompoundTag var18 = var15.read(var16);
                            if (var18 != null) {
                                int var19 = ChunkStorage.getVersion(var18);
                                CompoundTag var20 = var15.upgradeChunkTag(var13, () -> this.overworldDataStorage, var18);
                                boolean var21 = var19 < SharedConstants.getCurrentVersion().getWorldVersion();
                                if (this.eraseCache) {
                                    CompoundTag var22 = var20.getCompound("Level");
                                    var21 = var21 || var22.contains("Heightmaps");
                                    var22.remove("Heightmaps");
                                    var21 = var21 || var22.contains("isLightOn");
                                    var22.remove("isLightOn");
                                }

                                if (var21) {
                                    var15.write(var16, var20);
                                    var17 = true;
                                }
                            }
                        } catch (ReportedException var23) {
                            Throwable var24 = var23.getCause();
                            if (!(var24 instanceof IOException)) {
                                throw var23;
                            }

                            LOGGER.error("Error upgrading chunk {}", var16, var24);
                        } catch (IOException var241) {
                            LOGGER.error("Error upgrading chunk {}", var16, var241);
                        }

                        if (var17) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        var11 = true;
                    }

                    float var26 = (float)var14.nextIndex() / var4;
                    this.progressMap.put(var13, var26);
                    var12 += var26;
                }

                this.progress = var12;
                if (!var11) {
                    this.running = false;
                }
            }

            this.status = new TranslatableComponent("optimizeWorld.stage.finished");

            for(ChunkStorage var27 : var9.values()) {
                try {
                    var27.close();
                } catch (IOException var221) {
                    LOGGER.error("Error upgrading chunk", (Throwable)var221);
                }
            }

            this.overworldDataStorage.save();
            var10 = Util.getMillis() - var10;
            LOGGER.info("World optimizaton finished after {} ms", var10);
            this.finished = true;
        }
    }

    private List<ChunkPos> getAllChunkPos(DimensionType param0) {
        File var0 = param0.getStorageFolder(this.pathToWorld);
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

                    try (RegionFile var8 = new RegionFile(var4)) {
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
