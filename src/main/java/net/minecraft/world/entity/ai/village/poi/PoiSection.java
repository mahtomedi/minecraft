package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoiSection implements Serializable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
    private final Map<PoiType, Set<PoiRecord>> byType = Maps.newHashMap();
    private final Runnable setDirty;
    private boolean isValid;

    public PoiSection(Runnable param0) {
        this.setDirty = param0;
        this.isValid = true;
    }

    public <T> PoiSection(Runnable param0, Dynamic<T> param1) {
        this.setDirty = param0;

        try {
            this.isValid = param1.get("Valid").asBoolean(false);
            param1.get("Records").asStream().forEach(param1x -> this.add(new PoiRecord(param1x, param0)));
        } catch (Exception var4) {
            LOGGER.error("Failed to load POI chunk", (Throwable)var4);
            this.clear();
            this.isValid = false;
        }

    }

    public Stream<PoiRecord> getRecords(Predicate<PoiType> param0, PoiManager.Occupancy param1) {
        return this.byType
            .entrySet()
            .stream()
            .filter(param1x -> param0.test(param1x.getKey()))
            .flatMap(param0x -> param0x.getValue().stream())
            .filter(param1.getTest());
    }

    public void add(BlockPos param0, PoiType param1) {
        if (this.add(new PoiRecord(param0, param1, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ {}", () -> param1, () -> param0);
            this.setDirty.run();
        }

    }

    private boolean add(PoiRecord param0) {
        BlockPos var0 = param0.getPos();
        PoiType var1 = param0.getPoiType();
        short var2 = SectionPos.sectionRelativePos(var0);
        PoiRecord var3 = this.records.get(var2);
        if (var3 != null) {
            if (var1.equals(var3.getPoiType())) {
                return false;
            } else {
                throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI data mismatch: already registered at " + var0));
            }
        } else {
            this.records.put(var2, param0);
            this.byType.computeIfAbsent(var1, param0x -> Sets.newHashSet()).add(param0);
            return true;
        }
    }

    public void remove(BlockPos param0) {
        PoiRecord var0 = this.records.remove(SectionPos.sectionRelativePos(param0));
        if (var0 == null) {
            LOGGER.error("POI data mismatch: never registered at " + param0);
        } else {
            this.byType.get(var0.getPoiType()).remove(var0);
            LOGGER.debug("Removed POI of type {} @ {}", var0::getPoiType, var0::getPos);
            this.setDirty.run();
        }
    }

    public boolean release(BlockPos param0) {
        PoiRecord var0 = this.records.get(SectionPos.sectionRelativePos(param0));
        if (var0 == null) {
            throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + param0));
        } else {
            boolean var1 = var0.releaseTicket();
            this.setDirty.run();
            return var1;
        }
    }

    public boolean exists(BlockPos param0, Predicate<PoiType> param1) {
        short var0 = SectionPos.sectionRelativePos(param0);
        PoiRecord var1 = this.records.get(var0);
        return var1 != null && param1.test(var1.getPoiType());
    }

    public Optional<PoiType> getType(BlockPos param0) {
        short var0 = SectionPos.sectionRelativePos(param0);
        PoiRecord var1 = this.records.get(var0);
        return var1 != null ? Optional.of(var1.getPoiType()) : Optional.empty();
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        T var0 = param0.createList(this.records.values().stream().map(param1 -> param1.serialize(param0)));
        return param0.createMap(ImmutableMap.of(param0.createString("Records"), var0, param0.createString("Valid"), param0.createBoolean(this.isValid)));
    }

    public void refresh(Consumer<BiConsumer<BlockPos, PoiType>> param0) {
        if (!this.isValid) {
            Short2ObjectMap<PoiRecord> var0 = new Short2ObjectOpenHashMap<>(this.records);
            this.clear();
            param0.accept((param1, param2) -> {
                short var0x = SectionPos.sectionRelativePos(param1);
                PoiRecord var1x = var0.computeIfAbsent(var0x, param2x -> new PoiRecord(param1, param2, this.setDirty));
                this.add(var1x);
            });
            this.isValid = true;
            this.setDirty.run();
        }

    }

    private void clear() {
        this.records.clear();
        this.byType.clear();
    }

    boolean isValid() {
        return this.isValid;
    }
}
