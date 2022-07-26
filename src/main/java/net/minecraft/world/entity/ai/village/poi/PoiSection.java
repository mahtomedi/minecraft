package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class PoiSection {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
    private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.newHashMap();
    private final Runnable setDirty;
    private boolean isValid;

    public static Codec<PoiSection> codec(Runnable param0) {
        return RecordCodecBuilder.<PoiSection>create(
                param1 -> param1.group(
                            RecordCodecBuilder.point(param0),
                            Codec.BOOL.optionalFieldOf("Valid", Boolean.valueOf(false)).forGetter(param0x -> param0x.isValid),
                            PoiRecord.codec(param0).listOf().fieldOf("Records").forGetter(param0x -> ImmutableList.copyOf(param0x.records.values()))
                        )
                        .apply(param1, PoiSection::new)
            )
            .orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> new PoiSection(param0, false, ImmutableList.of()));
    }

    public PoiSection(Runnable param0) {
        this(param0, true, ImmutableList.of());
    }

    private PoiSection(Runnable param0, boolean param1, List<PoiRecord> param2) {
        this.setDirty = param0;
        this.isValid = param1;
        param2.forEach(this::add);
    }

    public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> param0, PoiManager.Occupancy param1) {
        return this.byType
            .entrySet()
            .stream()
            .filter(param1x -> param0.test(param1x.getKey()))
            .flatMap(param0x -> param0x.getValue().stream())
            .filter(param1.getTest());
    }

    public void add(BlockPos param0, Holder<PoiType> param1) {
        if (this.add(new PoiRecord(param0, param1, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ {}", param1.unwrapKey().map(param0x -> param0x.location().toString()).orElse("[unregistered]"), param0);
            this.setDirty.run();
        }

    }

    private boolean add(PoiRecord param0x) {
        BlockPos var0 = param0x.getPos();
        Holder<PoiType> var1 = param0x.getPoiType();
        short var2 = SectionPos.sectionRelativePos(var0);
        PoiRecord var3 = this.records.get(var2);
        if (var3 != null) {
            if (var1.equals(var3.getPoiType())) {
                return false;
            }

            Util.logAndPauseIfInIde("POI data mismatch: already registered at " + var0);
        }

        this.records.put(var2, param0x);
        this.byType.computeIfAbsent(var1, param0xx -> Sets.newHashSet()).add(param0x);
        return true;
    }

    public void remove(BlockPos param0) {
        PoiRecord var0 = this.records.remove(SectionPos.sectionRelativePos(param0));
        if (var0 == null) {
            LOGGER.error("POI data mismatch: never registered at {}", param0);
        } else {
            this.byType.get(var0.getPoiType()).remove(var0);
            LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(var0::getPoiType), LogUtils.defer(var0::getPos));
            this.setDirty.run();
        }
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets(BlockPos param0) {
        return this.getPoiRecord(param0).map(PoiRecord::getFreeTickets).orElse(0);
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

    public boolean exists(BlockPos param0, Predicate<Holder<PoiType>> param1) {
        return this.getType(param0).filter(param1).isPresent();
    }

    public Optional<Holder<PoiType>> getType(BlockPos param0) {
        return this.getPoiRecord(param0).map(PoiRecord::getPoiType);
    }

    private Optional<PoiRecord> getPoiRecord(BlockPos param0) {
        return Optional.ofNullable(this.records.get(SectionPos.sectionRelativePos(param0)));
    }

    public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> param0) {
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
