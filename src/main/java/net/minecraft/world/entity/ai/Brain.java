package net.minecraft.world.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Serializable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

public class Brain<E extends LivingEntity> implements Serializable {
    private final Map<MemoryModuleType<?>, Optional<?>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> availableGoalsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public <T> Brain(Collection<MemoryModuleType<?>> param0, Collection<SensorType<? extends Sensor<? super E>>> param1, Dynamic<T> param2) {
        param0.forEach(param0x -> {
        });
        param1.forEach(param0x -> {
        });
        this.sensors.values().forEach(param0x -> {
            for(MemoryModuleType<?> var0x : param0x.requires()) {
                this.memories.put(var0x, Optional.empty());
            }

        });

        for(Entry<Dynamic<T>, Dynamic<T>> var0 : param2.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
            this.readMemory(Registry.MEMORY_MODULE_TYPE.get(new ResourceLocation(var0.getKey().asString(""))), var0.getValue());
        }

    }

    public boolean hasMemoryValue(MemoryModuleType<?> param0) {
        return this.checkMemory(param0, MemoryStatus.VALUE_PRESENT);
    }

    private <T, U> void readMemory(MemoryModuleType<U> param0, Dynamic<T> param1) {
        this.setMemory(param0, param0.getDeserializer().orElseThrow(RuntimeException::new).apply(param1));
    }

    public <U> void eraseMemory(MemoryModuleType<U> param0) {
        this.setMemory(param0, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> param0, @Nullable U param1) {
        this.setMemory(param0, Optional.ofNullable(param1));
    }

    public <U> void setMemory(MemoryModuleType<U> param0, Optional<U> param1) {
        if (this.memories.containsKey(param0)) {
            if (param1.isPresent() && this.isEmptyCollection(param1.get())) {
                this.eraseMemory(param0);
            } else {
                this.memories.put(param0, param1);
            }
        }

    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> param0) {
        return (Optional<U>)this.memories.get(param0);
    }

    public boolean checkMemory(MemoryModuleType<?> param0, MemoryStatus param1) {
        Optional<?> var0 = this.memories.get(param0);
        if (var0 == null) {
            return false;
        } else {
            return param1 == MemoryStatus.REGISTERED
                || param1 == MemoryStatus.VALUE_PRESENT && var0.isPresent()
                || param1 == MemoryStatus.VALUE_ABSENT && !var0.isPresent();
        }
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(Schedule param0) {
        this.schedule = param0;
    }

    public void setCoreActivities(Set<Activity> param0) {
        this.coreActivities = param0;
    }

    @Deprecated
    public Stream<Behavior<? super E>> getRunningBehaviorsStream() {
        return this.availableGoalsByPriority
            .values()
            .stream()
            .flatMap(param0 -> param0.values().stream())
            .flatMap(Collection::stream)
            .filter(param0 -> param0.getStatus() == Behavior.Status.RUNNING);
    }

    public void setActivity(Activity param0) {
        this.activeActivities.clear();
        this.activeActivities.addAll(this.coreActivities);
        boolean var0 = this.activityRequirements.keySet().contains(param0) && this.activityRequirementsAreMet(param0);
        this.activeActivities.add(var0 ? param0 : this.defaultActivity);
    }

    public void updateActivity(long param0, long param1) {
        if (param1 - this.lastScheduleUpdate > 20L) {
            this.lastScheduleUpdate = param1;
            Activity var0 = this.getSchedule().getActivityAt((int)(param0 % 24000L));
            if (!this.activeActivities.contains(var0)) {
                this.setActivity(var0);
            }
        }

    }

    public void setDefaultActivity(Activity param0) {
        this.defaultActivity = param0;
    }

    public void addActivity(Activity param0, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> param1) {
        this.addActivity(param0, param1, ImmutableSet.of());
    }

    public void addActivity(
        Activity param0, ImmutableList<Pair<Integer, ? extends Behavior<? super E>>> param1, Set<Pair<MemoryModuleType<?>, MemoryStatus>> param2
    ) {
        this.activityRequirements.put(param0, param2);
        param1.forEach(
            param1x -> this.availableGoalsByPriority
                    .computeIfAbsent(param1x.getFirst(), param0x -> Maps.newHashMap())
                    .computeIfAbsent(param0, param0x -> Sets.newLinkedHashSet())
                    .add(param1x.getSecond())
        );
    }

    public boolean isActive(Activity param0) {
        return this.activeActivities.contains(param0);
    }

    public Brain<E> copyWithoutGoals() {
        Brain<E> var0 = new Brain<>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<>(NbtOps.INSTANCE, new CompoundTag()));
        this.memories.forEach((param1, param2) -> param2.ifPresent(param2x -> {
            }));
        return var0;
    }

    public void tick(ServerLevel param0, E param1) {
        this.tickEachSensor(param0, param1);
        this.startEachNonRunningBehavior(param0, param1);
        this.tickEachRunningBehavior(param0, param1);
    }

    public void stopAll(ServerLevel param0, E param1) {
        long var0 = param1.level.getGameTime();
        this.getRunningBehaviorsStream().forEach(param3 -> param3.doStop(param0, param1, var0));
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        T var0 = param0.createMap(
            this.memories
                .entrySet()
                .stream()
                .filter(param0x -> param0x.getKey().getDeserializer().isPresent() && param0x.getValue().isPresent())
                .map(
                    param1 -> Pair.of(
                            param0.createString(Registry.MEMORY_MODULE_TYPE.getKey(param1.getKey()).toString()),
                            ((Serializable)param1.getValue().get()).serialize(param0)
                        )
                )
                .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))
        );
        return param0.createMap(ImmutableMap.of(param0.createString("memories"), var0));
    }

    private void tickEachSensor(ServerLevel param0, E param1) {
        this.sensors.values().forEach(param2 -> param2.tick(param0, param1));
    }

    private void startEachNonRunningBehavior(ServerLevel param0, E param1) {
        long var0 = param0.getGameTime();
        this.availableGoalsByPriority
            .values()
            .stream()
            .flatMap(param0x -> param0x.entrySet().stream())
            .filter(param0x -> this.activeActivities.contains(param0x.getKey()))
            .map(Entry::getValue)
            .flatMap(Collection::stream)
            .filter(param0x -> param0x.getStatus() == Behavior.Status.STOPPED)
            .forEach(param3 -> param3.tryStart(param0, param1, var0));
    }

    private void tickEachRunningBehavior(ServerLevel param0, E param1) {
        long var0 = param0.getGameTime();
        this.getRunningBehaviorsStream().forEach(param3 -> param3.tickOrStop(param0, param1, var0));
    }

    private boolean activityRequirementsAreMet(Activity param0) {
        return this.activityRequirements.get(param0).stream().allMatch(param0x -> {
            MemoryModuleType<?> var0 = param0x.getFirst();
            MemoryStatus var1x = param0x.getSecond();
            return this.checkMemory(var0, var1x);
        });
    }

    private boolean isEmptyCollection(Object param0) {
        return param0 instanceof Collection && ((Collection)param0).isEmpty();
    }
}
