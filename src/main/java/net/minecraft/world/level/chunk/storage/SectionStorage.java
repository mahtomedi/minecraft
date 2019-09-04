package net.minecraft.world.level.chunk.storage;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.OptionalDynamic;
import com.mojang.datafixers.types.DynamicOps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Serializable;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SectionStorage<R extends Serializable> extends RegionFileStorage {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Long2ObjectMap<Optional<R>> storage = new Long2ObjectOpenHashMap<>();
    private final LongLinkedOpenHashSet dirty = new LongLinkedOpenHashSet();
    private final BiFunction<Runnable, Dynamic<?>, R> deserializer;
    private final Function<Runnable, R> factory;
    private final DataFixer fixerUpper;
    private final DataFixTypes type;

    public SectionStorage(File param0, BiFunction<Runnable, Dynamic<?>, R> param1, Function<Runnable, R> param2, DataFixer param3, DataFixTypes param4) {
        super(param0);
        this.deserializer = param1;
        this.factory = param2;
        this.fixerUpper = param3;
        this.type = param4;
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
        return Level.isOutsideBuildHeight(SectionPos.sectionToBlockCoord(param0.y()));
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
            return this.read(param0);
        } catch (IOException var3) {
            LOGGER.error("Error reading chunk {} data from disk", param0, var3);
            return null;
        }
    }

    private <T> void readColumn(ChunkPos param0, DynamicOps<T> param1, @Nullable T param2) {
        if (param2 == null) {
            for(int var0 = 0; var0 < 16; ++var0) {
                this.storage.put(SectionPos.of(param0, var0).asLong(), Optional.empty());
            }
        } else {
            Dynamic<T> var1 = new Dynamic<>(param1, param2);
            int var2 = getVersion(var1);
            int var3 = SharedConstants.getCurrentVersion().getWorldVersion();
            boolean var4 = var2 != var3;
            Dynamic<T> var5 = this.fixerUpper.update(this.type.getType(), var1, var2, var3);
            OptionalDynamic<T> var6 = var5.get("Sections");

            for(int var7 = 0; var7 < 16; ++var7) {
                long var8 = SectionPos.of(param0, var7).asLong();
                Optional<R> var9 = var6.get(Integer.toString(var7)).get().map(param1x -> this.deserializer.apply(() -> this.setDirty(var8), param1x));
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
            try {
                this.write(param0, (CompoundTag)var1);
            } catch (IOException var5) {
                LOGGER.error("Error writing data to disk", (Throwable)var5);
            }
        } else {
            LOGGER.error("Expected compound tag, got {}", var1);
        }

    }

    private <T> Dynamic<T> writeColumn(ChunkPos param0, DynamicOps<T> param1) {
        Map<T, T> var0 = Maps.newHashMap();

        for(int var1 = 0; var1 < 16; ++var1) {
            long var2 = SectionPos.of(param0, var1).asLong();
            this.dirty.remove(var2);
            Optional<R> var3 = this.storage.get(var2);
            if (var3 != null && var3.isPresent()) {
                var0.put(param1.createString(Integer.toString(var1)), var3.get().serialize(param1));
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
        return param0.get("DataVersion").asNumber().orElse(1945).intValue();
    }

    public void flush(ChunkPos param0) {
        if (!this.dirty.isEmpty()) {
            for(int var0 = 0; var0 < 16; ++var0) {
                long var1 = SectionPos.of(param0, var0).asLong();
                if (this.dirty.contains(var1)) {
                    this.writeColumn(param0);
                    return;
                }
            }
        }

    }
}
