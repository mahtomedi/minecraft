package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;

public class GossipContainer {
    public static final int DISCARD_THRESHOLD = 2;
    private final Map<UUID, GossipContainer.EntityGossips> gossips = Maps.newHashMap();

    @VisibleForDebug
    public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
        Map<UUID, Object2IntMap<GossipType>> var0 = Maps.newHashMap();
        this.gossips.keySet().forEach(param1 -> {
            GossipContainer.EntityGossips var0x = this.gossips.get(param1);
            var0.put(param1, var0x.entries);
        });
        return var0;
    }

    public void decay() {
        Iterator<GossipContainer.EntityGossips> var0 = this.gossips.values().iterator();

        while(var0.hasNext()) {
            GossipContainer.EntityGossips var1 = var0.next();
            var1.decay();
            if (var1.isEmpty()) {
                var0.remove();
            }
        }

    }

    private Stream<GossipContainer.GossipEntry> unpack() {
        return this.gossips.entrySet().stream().flatMap(param0 -> param0.getValue().unpack(param0.getKey()));
    }

    private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(RandomSource param0, int param1) {
        List<GossipContainer.GossipEntry> var0 = this.unpack().collect(Collectors.toList());
        if (var0.isEmpty()) {
            return Collections.emptyList();
        } else {
            int[] var1 = new int[var0.size()];
            int var2 = 0;

            for(int var3 = 0; var3 < var0.size(); ++var3) {
                GossipContainer.GossipEntry var4 = var0.get(var3);
                var2 += Math.abs(var4.weightedValue());
                var1[var3] = var2 - 1;
            }

            Set<GossipContainer.GossipEntry> var5 = Sets.newIdentityHashSet();

            for(int var6 = 0; var6 < param1; ++var6) {
                int var7 = param0.nextInt(var2);
                int var8 = Arrays.binarySearch(var1, var7);
                var5.add(var0.get(var8 < 0 ? -var8 - 1 : var8));
            }

            return var5;
        }
    }

    private GossipContainer.EntityGossips getOrCreate(UUID param0) {
        return this.gossips.computeIfAbsent(param0, param0x -> new GossipContainer.EntityGossips());
    }

    public void transferFrom(GossipContainer param0, RandomSource param1, int param2) {
        Collection<GossipContainer.GossipEntry> var0 = param0.selectGossipsForTransfer(param1, param2);
        var0.forEach(param0x -> {
            int var0x = param0x.value - param0x.type.decayPerTransfer;
            if (var0x >= 2) {
                this.getOrCreate(param0x.target).entries.mergeInt(param0x.type, var0x, GossipContainer::mergeValuesForTransfer);
            }

        });
    }

    public int getReputation(UUID param0, Predicate<GossipType> param1) {
        GossipContainer.EntityGossips var0 = this.gossips.get(param0);
        return var0 != null ? var0.weightedValue(param1) : 0;
    }

    public long getCountForType(GossipType param0, DoublePredicate param1) {
        return this.gossips.values().stream().filter(param2 -> param1.test((double)(param2.entries.getOrDefault(param0, 0) * param0.weight))).count();
    }

    public void add(UUID param0, GossipType param1, int param2) {
        GossipContainer.EntityGossips var0 = this.getOrCreate(param0);
        var0.entries.mergeInt(param1, param2, (param1x, param2x) -> this.mergeValuesForAddition(param1, param1x, param2x));
        var0.makeSureValueIsntTooLowOrTooHigh(param1);
        if (var0.isEmpty()) {
            this.gossips.remove(param0);
        }

    }

    public void remove(UUID param0, GossipType param1, int param2) {
        this.add(param0, param1, -param2);
    }

    public void remove(UUID param0, GossipType param1) {
        GossipContainer.EntityGossips var0 = this.gossips.get(param0);
        if (var0 != null) {
            var0.remove(param1);
            if (var0.isEmpty()) {
                this.gossips.remove(param0);
            }
        }

    }

    public void remove(GossipType param0) {
        Iterator<GossipContainer.EntityGossips> var0 = this.gossips.values().iterator();

        while(var0.hasNext()) {
            GossipContainer.EntityGossips var1 = var0.next();
            var1.remove(param0);
            if (var1.isEmpty()) {
                var0.remove();
            }
        }

    }

    public <T> Dynamic<T> store(DynamicOps<T> param0) {
        return new Dynamic<>(param0, param0.createList(this.unpack().map(param1 -> param1.store(param0)).map(Dynamic::getValue)));
    }

    public void update(Dynamic<?> param0) {
        param0.asStream()
            .map(GossipContainer.GossipEntry::load)
            .flatMap(param0x -> param0x.result().stream())
            .forEach(param0x -> this.getOrCreate(param0x.target).entries.put(param0x.type, param0x.value));
    }

    private static int mergeValuesForTransfer(int param0, int param1) {
        return Math.max(param0, param1);
    }

    private int mergeValuesForAddition(GossipType param0, int param1, int param2) {
        int var0 = param1 + param2;
        return var0 > param0.max ? Math.max(param0.max, param1) : var0;
    }

    static class EntityGossips {
        final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

        public int weightedValue(Predicate<GossipType> param0) {
            return this.entries
                .object2IntEntrySet()
                .stream()
                .filter(param1 -> param0.test(param1.getKey()))
                .mapToInt(param0x -> param0x.getIntValue() * param0x.getKey().weight)
                .sum();
        }

        public Stream<GossipContainer.GossipEntry> unpack(UUID param0) {
            return this.entries.object2IntEntrySet().stream().map(param1 -> new GossipContainer.GossipEntry(param0, param1.getKey(), param1.getIntValue()));
        }

        public void decay() {
            ObjectIterator<Entry<GossipType>> var0 = this.entries.object2IntEntrySet().iterator();

            while(var0.hasNext()) {
                Entry<GossipType> var1 = var0.next();
                int var2 = var1.getIntValue() - var1.getKey().decayPerDay;
                if (var2 < 2) {
                    var0.remove();
                } else {
                    var1.setValue(var2);
                }
            }

        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(GossipType param0) {
            int var0 = this.entries.getInt(param0);
            if (var0 > param0.max) {
                this.entries.put(param0, param0.max);
            }

            if (var0 < 2) {
                this.remove(param0);
            }

        }

        public void remove(GossipType param0) {
            this.entries.removeInt(param0);
        }
    }

    static class GossipEntry {
        public static final String TAG_TARGET = "Target";
        public static final String TAG_TYPE = "Type";
        public static final String TAG_VALUE = "Value";
        public final UUID target;
        public final GossipType type;
        public final int value;

        public GossipEntry(UUID param0, GossipType param1, int param2) {
            this.target = param0;
            this.type = param1;
            this.value = param2;
        }

        public int weightedValue() {
            return this.value * this.type.weight;
        }

        @Override
        public String toString() {
            return "GossipEntry{target=" + this.target + ", type=" + this.type + ", value=" + this.value + "}";
        }

        public <T> Dynamic<T> store(DynamicOps<T> param0) {
            return new Dynamic<>(
                param0,
                param0.createMap(
                    ImmutableMap.of(
                        param0.createString("Target"),
                        UUIDUtil.CODEC.encodeStart(param0, this.target).result().orElseThrow(RuntimeException::new),
                        param0.createString("Type"),
                        param0.createString(this.type.id),
                        param0.createString("Value"),
                        param0.createInt(this.value)
                    )
                )
            );
        }

        public static DataResult<GossipContainer.GossipEntry> load(Dynamic<?> param0) {
            return DataResult.unbox(
                DataResult.instance()
                    .group(
                        param0.get("Target").read(UUIDUtil.CODEC),
                        param0.get("Type").asString().map(GossipType::byId),
                        param0.get("Value").asNumber().map(Number::intValue)
                    )
                    .apply(DataResult.instance(), GossipContainer.GossipEntry::new)
            );
        }
    }
}
