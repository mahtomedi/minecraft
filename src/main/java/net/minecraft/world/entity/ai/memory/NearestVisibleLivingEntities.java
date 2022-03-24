package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class NearestVisibleLivingEntities {
    private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
    private final List<LivingEntity> nearbyEntities;
    private final Predicate<LivingEntity> lineOfSightTest;

    private NearestVisibleLivingEntities() {
        this.nearbyEntities = List.of();
        this.lineOfSightTest = param0 -> false;
    }

    public NearestVisibleLivingEntities(LivingEntity param0, List<LivingEntity> param1) {
        this.nearbyEntities = param1;
        Object2BooleanOpenHashMap<LivingEntity> var0 = new Object2BooleanOpenHashMap<>(param1.size());
        Predicate<LivingEntity> var1 = param1x -> Sensor.isEntityTargetable(param0, param1x);
        this.lineOfSightTest = param2 -> var0.computeIfAbsent(param2, var1);
    }

    public static NearestVisibleLivingEntities empty() {
        return EMPTY;
    }

    public Optional<LivingEntity> findClosest(Predicate<LivingEntity> param0) {
        for(LivingEntity var0 : this.nearbyEntities) {
            if (param0.test(var0) && this.lineOfSightTest.test(var0)) {
                return Optional.of(var0);
            }
        }

        return Optional.empty();
    }

    @SafeVarargs
    public final Optional<LivingEntity> findClosest(Predicate<LivingEntity>... param0) {
        return Stream.of(param0).map(this::findClosest).filter(Optional::isPresent).findAny().orElse(Optional.empty());
    }

    public Iterable<LivingEntity> findAll(Predicate<LivingEntity> param0) {
        return Iterables.filter(this.nearbyEntities, param1 -> param0.test(param1) && this.lineOfSightTest.test(param1));
    }

    public Stream<LivingEntity> find(Predicate<LivingEntity> param0) {
        return this.nearbyEntities.stream().filter(param1 -> param0.test(param1) && this.lineOfSightTest.test(param1));
    }

    public boolean contains(LivingEntity param0) {
        return this.nearbyEntities.contains(param0) && this.lineOfSightTest.test(param0);
    }

    public boolean contains(Predicate<LivingEntity> param0) {
        for(LivingEntity var0 : this.nearbyEntities) {
            if (param0.test(var0) && this.lineOfSightTest.test(var0)) {
                return true;
            }
        }

        return false;
    }
}
