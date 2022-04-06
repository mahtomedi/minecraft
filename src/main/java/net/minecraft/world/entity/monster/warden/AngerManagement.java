package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectAVLTreeSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AngerManagement {
    @VisibleForTesting
    protected static final int CONVERSION_DELAY = 40;
    @VisibleForTesting
    protected static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 40);
    private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create(
        param0 -> param0.group(
                    ExtraCodecs.UUID.fieldOf("uuid").forGetter(Pair::getFirst), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)
                )
                .apply(param0, Pair::of)
    );
    public static final Codec<AngerManagement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs))
                .apply(param0, AngerManagement::new)
    );
    @VisibleForTesting
    protected final SortedSet<Entity> suspects = new ObjectAVLTreeSet<>(new AngerManagement.Sorter(this));
    @VisibleForTesting
    protected final Object2IntMap<Entity> angerBySuspect = new Object2IntOpenHashMap<>();
    @VisibleForTesting
    protected final Object2IntMap<UUID> angerByUuid;

    public AngerManagement(List<Pair<UUID, Integer>> param0) {
        this.angerByUuid = new Object2IntOpenHashMap<>(param0.size());
        param0.forEach(param0x -> this.angerByUuid.put(param0x.getFirst(), param0x.getSecond()));
    }

    private List<Pair<UUID, Integer>> createUuidAngerPairs() {
        return Streams.<Pair<UUID, Integer>>concat(
                this.suspects.stream().map(param0 -> Pair.of(param0.getUUID(), this.angerBySuspect.getInt(param0))),
                this.angerByUuid.object2IntEntrySet().stream().map(param0 -> Pair.of(param0.getKey(), param0.getIntValue()))
            )
            .collect(Collectors.toList());
    }

    public void tick(ServerLevel param0, Predicate<Entity> param1) {
        --this.conversionDelay;
        if (this.conversionDelay <= 0) {
            this.convertFromUuids(param0);
            this.conversionDelay = 40;
        }

        ObjectIterator<Entry<UUID>> var0 = this.angerByUuid.object2IntEntrySet().iterator();

        while(var0.hasNext()) {
            Entry<UUID> var1 = var0.next();
            int var2 = var1.getIntValue();
            if (var2 <= 1) {
                var0.remove();
            } else {
                var1.setValue(var2 - 1);
            }
        }

        ObjectIterator<Entry<Entity>> var3 = this.angerBySuspect.object2IntEntrySet().iterator();

        while(var3.hasNext()) {
            Entry<Entity> var4 = var3.next();
            int var5 = var4.getIntValue();
            Entity var6 = var4.getKey();
            if (var5 > 1 && param1.test(var6)) {
                var4.setValue(var5 - 1);
            } else {
                this.suspects.remove(var6);
                var3.remove();
            }
        }

    }

    private void convertFromUuids(ServerLevel param0) {
        ObjectIterator<Entry<UUID>> var0 = this.angerByUuid.object2IntEntrySet().iterator();

        while(var0.hasNext()) {
            Entry<UUID> var1 = var0.next();
            int var2 = var1.getIntValue();
            Entity var3 = param0.getEntity(var1.getKey());
            if (var3 != null) {
                this.angerBySuspect.put(var3, var2);
                this.suspects.add(var3);
                var0.remove();
            }
        }

    }

    public int increaseAnger(Entity param0, int param1) {
        boolean var0 = !this.suspects.remove(param0);
        int var1 = this.angerBySuspect.computeInt(param0, (param1x, param2) -> Math.min(150, (param2 == null ? 0 : param2) + param1));
        if (var0) {
            int var2 = this.angerByUuid.removeInt(param0.getUUID());
            var1 += var2;
            this.angerBySuspect.put(param0, var1);
        }

        this.suspects.add(param0);
        return var1;
    }

    public void clearAnger(Entity param0) {
        this.angerBySuspect.removeInt(param0);
        this.suspects.remove(param0);
    }

    @Nullable
    private Entity getTopSuspect() {
        return this.suspects.isEmpty() ? null : this.suspects.first();
    }

    public int getActiveAnger() {
        return this.angerBySuspect.getInt(this.getTopSuspect());
    }

    public Optional<LivingEntity> getActiveEntity() {
        return Optional.ofNullable(this.getTopSuspect()).filter(param0 -> param0 instanceof LivingEntity).map(param0 -> (LivingEntity)param0);
    }

    @VisibleForTesting
    protected static record Sorter(AngerManagement angerManagement) implements Comparator<Entity> {
        public int compare(Entity param0, Entity param1) {
            if (param0.equals(param1)) {
                return 0;
            } else {
                int var0 = this.angerManagement.angerBySuspect.getOrDefault(param0, 0);
                int var1 = this.angerManagement.angerBySuspect.getOrDefault(param1, 0);
                boolean var2 = var0 >= AngerLevel.ANGRY.getMinimumAnger();
                boolean var3 = var1 >= AngerLevel.ANGRY.getMinimumAnger();
                if (var2 != var3) {
                    return var2 ? -1 : 1;
                } else {
                    if (var2) {
                        boolean var4 = param0 instanceof Player;
                        boolean var5 = param1 instanceof Player;
                        if (var4 != var5) {
                            return var4 ? -1 : 1;
                        }
                    }

                    return var0 > var1 ? -1 : 1;
                }
            }
        }
    }
}
