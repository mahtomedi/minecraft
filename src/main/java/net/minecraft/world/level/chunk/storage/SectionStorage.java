package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SectionStorage<R> implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IOWorker worker;
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final Function<Runnable, Codec<R>> codec;
    private final Function<Runnable, R> factory;
    private final DataFixer fixerUpper;
    private final DataFixTypes type;
    protected final LevelHeightAccessor levelHeightAccessor;

    public SectionStorage(
        File param0,
        Function<Runnable, Codec<R>> param1,
        Function<Runnable, R> param2,
        DataFixer param3,
        DataFixTypes param4,
        boolean param5,
        LevelHeightAccessor param6
    ) {
        this.codec = param1;
        this.factory = param2;
        this.fixerUpper = param3;
        this.type = param4;
        this.levelHeightAccessor = param6;
        this.worker = new IOWorker(param0, param5, param0.getName());
    }

    protected void tick(BooleanSupplier param0) {
        while(!this.dirty.isEmpty() && param0.getAsBoolean()) {
            ChunkPos var0 = SectionPos.of(this.dirty.firstLong()).chunk();
            this.writeColumn(var0);
        }

    }

    @Nullable
    protected Optional<R> get(long param0) {
        return this.storage.get(param0);
    }

    protected Optional<R> getOrLoad(long param0) {
        SectionPos var0 = SectionPos.of(param0);
        if (this.outsideStoredRange(var0)) {
            return Optional.empty();
        } else {
            Optional<R> var1 = this.get(param0);
            if (var1 != null) {
                return var1;
            } else {
                this.readColumn(var0.chunk());
                var1 = this.get(param0);
                if (var1 == null) {
                    throw (IllegalStateException)Util.pauseInIde(new IllegalStateException());
                } else {
                    return var1;
                }
            }
        }
    }

    protected boolean outsideStoredRange(SectionPos param0) {
        int var0 = SectionPos.sectionToBlockCoord(param0.y());
        return this.levelHeightAccessor.isOutsideBuildHeight(var0);
    }

    protected R getOrCreate(long param0) {
        Optional<R> var0 = this.getOrLoad(param0);
        if (var0.isPresent()) {
            return var0.get();
        } else {
            R var1 = this.factory.apply(() -> this.setDirty(param0));
            this.storage.put(param0, Optional.of(var1));
            return var1;
        }
    }

    private void readColumn(ChunkPos param0) {
        this.readColumn(param0, NbtOps.INSTANCE, this.tryRead(param0));
    }

    @Nullable
    private CompoundTag tryRead(ChunkPos param0) {
        try {
            return this.worker.load(param0);
        } catch (IOException var3) {
            LOGGER.error("Error reading chunk {} data from disk", param0, var3);
            return null;
        }
    }

    private <T> void readColumn(ChunkPos param0, DynamicOps<T> param1, @Nullable T param2) {
        if (param2 == null) {
            for(int var0 = this.levelHeightAccessor.getMinSection(); var0 < this.levelHeightAccessor.getMaxSection(); ++var0) {
                this.storage.put(SectionPos.of(param0, var0).asLong(), Optional.empty());
            }
        } else {
            Dynamic<T> var1 = new Dynamic<>(param1, param2);
            int var2 = getVersion(var1);
            int var3 = SharedConstants.getCurrentVersion().getWorldVersion();
            boolean var4 = var2 != var3;
            Dynamic<T> var5 = this.fixerUpper.update(this.type.getType(), var1, var2, var3);
            OptionalDynamic<T> var6 = var5.get("Sections");

            for(int var7 = this.levelHeightAccessor.getMinSection(); var7 < this.levelHeightAccessor.getMaxSection(); ++var7) {
                long var8 = SectionPos.of(param0, var7).asLong();
                Optional<R> var9 = var6.get(Integer.toString(var7))
                    .result()
                    .flatMap(param1x -> this.codec.apply(() -> this.setDirty(var8)).parse(param1x).resultOrPartial(LOGGER::error));
                this.storage.put(var8, var9);
                var9.ifPresent(param2x -> {
                    this.onSectionLoad(var8);
                    if (var4) {
                        this.setDirty(var8);
                    }

                });
            }
        }

    }

    private void writeColumn(ChunkPos param0) {
        Dynamic<Tag> var0 = this.writeColumn(param0, NbtOps.INSTANCE);
        Tag var1 = var0.getValue();
        if (var1 instanceof CompoundTag) {
            this.worker.store(param0, (CompoundTag)var1);
        } else {
            LOGGER.error("Expected compound tag, got {}", var1);
        }

    }

    private <T> Dynamic<T> writeColumn(ChunkPos param0, DynamicOps<T> param1) {
        Map<T, T> var0 = Maps.newHashMap();

        for(int var1 = this.levelHeightAccessor.getMinSection(); var1 < this.levelHeightAccessor.getMaxSection(); ++var1) {
            long var2 = SectionPos.of(param0, var1).asLong();
            this.dirty.remove(var2);
            Optional<R> var3 = this.storage.get(var2);
            if (var3 != null && var3.isPresent()) {
                DataResult<T> var4 = this.codec.apply(() -> this.setDirty(var2)).encodeStart(param1, var3.get());
                String var5 = Integer.toString(var1);
                var4.resultOrPartial(LOGGER::error).ifPresent(param3 -> var0.put(param1.createString(var5), param3));
            }
        }

        return new Dynamic<>(
            param1,
            param1.createMap(
                ImmutableMap.of(
                    param1.createString("Sections"),
                    param1.createMap(var0),
                    param1.createString("DataVersion"),
                    param1.createInt(SharedConstants.getCurrentVersion().getWorldVersion())
                )
            )
        );
    }

    protected void onSectionLoad(long param0) {
    }

    protected void setDirty(long param0) {
        Optional<R> var0 = this.storage.get(param0);
        if (var0 != null && var0.isPresent()) {
            this.dirty.add(param0);
        } else {
            LOGGER.warn("No data for position: {}", SectionPos.of(param0));
        }
    }

    private static int getVersion(Dynamic<?> param0) {
        return param0.get("DataVersion").asInt(1945);
    }

    public void flush(ChunkPos param0) {
        if (!this.dirty.isEmpty()) {
            for(int var0 = this.levelHeightAccessor.getMinSection(); var0 < this.levelHeightAccessor.getMaxSection(); ++var0) {
                long var1 = SectionPos.of(param0, var0).asLong();
                if (this.dirty.contains(var1)) {
                    this.writeColumn(param0);
                    return;
                }
            }
        }

    }

    @Override
    public void close() throws IOException {
        this.worker.close();
    }
}
