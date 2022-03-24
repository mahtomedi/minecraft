package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AngerManagement {
    private static final int MAX_ANGER = 150;
    private static final int DEFAULT_ANGER_DECREASE = 1;
    public static final Codec<AngerManagement> CODEC = RecordCodecBuilder.create(
        param0 -> param0.group(
                    Codec.unboundedMap(ExtraCodecs.UUID, ExtraCodecs.NON_NEGATIVE_INT).fieldOf("suspects").forGetter(param0x -> param0x.angerBySuspect)
                )
                .apply(param0, AngerManagement::new)
    );
    private Object2IntMap<UUID> angerBySuspect;

    public AngerManagement(Map<UUID, Integer> param0) {
        this.angerBySuspect = new Object2IntOpenHashMap<>(param0);
    }

    public void tick() {
        this.angerBySuspect
            .keySet()
            .forEach(param0 -> this.angerBySuspect.computeInt(param0, (param0x, param1) -> param1 <= 1 ? null : Math.max(0, param1 - 1)));
    }

    public int addAnger(Entity param0, int param1) {
        return this.angerBySuspect.computeInt(param0.getUUID(), (param1x, param2) -> Math.min(150, (param2 == null ? 0 : param2) + param1));
    }

    public void clearAnger(Entity param0) {
        this.angerBySuspect.removeInt(param0.getUUID());
    }

    private Optional<Entry<UUID>> getTopEntry() {
        return this.angerBySuspect.object2IntEntrySet().stream().max(java.util.Map.Entry.comparingByValue());
    }

    public int getActiveAnger() {
        return this.getTopEntry().map(java.util.Map.Entry::getValue).orElse(0);
    }

    public Optional<LivingEntity> getActiveEntity(Level param0) {
        return param0 instanceof ServerLevel var0
            ? this.getTopEntry()
                .map(java.util.Map.Entry::getKey)
                .map(var0::getEntity)
                .filter(param0x -> param0x instanceof LivingEntity)
                .map(param0x -> (LivingEntity)param0x)
            : Optional.empty();
    }
}
