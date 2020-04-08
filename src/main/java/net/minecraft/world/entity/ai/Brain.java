package net.minecraft.world.entity.ai;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableMap.Builder;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Serializable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;

public class Brain<E extends LivingEntity> implements Serializable {
    private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<Behavior<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public <T> Brain(Collection<MemoryModuleType<?>> param0, Collection<SensorType<? extends Sensor<? super E>>> param1, Dynamic<T> param2) {
        for(MemoryModuleType<?> var0 : param0) {
            this.memories.put(var0, Optional.empty());
        }

        for(SensorType<? extends Sensor<? super E>> var1 : param1) {
            this.sensors.put(var1, var1.create());
        }

        for(Sensor<? super E> var2 : this.sensors.values()) {
            for(MemoryModuleType<?> var3 : var2.requires()) {
                this.memories.put(var3, Optional.empty());
            }
        }

        for(Entry<Dynamic<T>, Dynamic<T>> var4 : param2.get("memories").asMap(Function.identity(), Function.identity()).entrySet()) {
            this.readMemory(Registry.MEMORY_MODULE_TYPE.get(new ResourceLocation(var4.getKey().asString(""))), var4.getValue());
        }

    }

    public boolean hasMemoryValue(MemoryModuleType<?> param0) {
        return this.checkMemory(param0, MemoryStatus.VALUE_PRESENT);
    }

    private <T, U> void readMemory(MemoryModuleType<U> param0, Dynamic<T> param1) {
        ExpirableValue<U> var0 = new ExpirableValue<>(param0.getDeserializer().orElseThrow(RuntimeException::new), param1);
        this.setMemoryInternal(param0, Optional.of(var0));
    }

    public <U> void eraseMemory(MemoryModuleType<U> param0) {
        this.setMemory(param0, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> param0, @Nullable U param1) {
        this.setMemory(param0, Optional.ofNullable(param1));
    }

    public <U> void setMemoryWithExpiry(MemoryModuleType<U> param0, U param1, long param2) {
        this.setMemoryInternal(param0, Optional.of(ExpirableValue.of(param1, param2)));
    }

    public <U> void setMemory(MemoryModuleType<U> param0, Optional<? extends U> param1) {
        this.setMemoryInternal(param0, param1.map(ExpirableValue::of));
    }

    private <U> void setMemoryInternal(MemoryModuleType<U> param0, Optional<? extends ExpirableValue<?>> param1) {
        if (this.memories.containsKey(param0)) {
            if (param1.isPresent() && this.isEmptyCollection(param1.get().getValue())) {
                this.eraseMemory(param0);
            } else {
                this.memories.put(param0, param1);
            }
        }

    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> param0) {
        return this.memories.get(param0).map(ExpirableValue::getValue);
    }

    public boolean checkMemory(MemoryModuleType<?> param0, MemoryStatus param1) {
        Optional<? extends ExpirableValue<?>> var0 = this.memories.get(param0);
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
    public List<Behavior<? super E>> getRunningBehaviors() {
        List<Behavior<? super E>> var0 = new ObjectArrayList<>();

        for(Map<Activity, Set<Behavior<? super E>>> var1 : this.availableBehaviorsByPriority.values()) {
            for(Set<Behavior<? super E>> var2 : var1.values()) {
                for(Behavior<? super E> var3 : var2) {
                    if (var3.getStatus() == Behavior.Status.RUNNING) {
                        var0.add(var3);
                    }
                }
            }
        }

        return var0;
    }

    public void useDefaultActivity() {
        this.setActiveActivity(this.defaultActivity);
    }

    public Optional<Activity> getActiveNonCoreActivity() {
        for(Activity var0 : this.activeActivities) {
            if (!this.coreActivities.contains(var0)) {
                return Optional.of(var0);
            }
        }

        return Optional.empty();
    }

    public void setActiveActivityIfPossible(Activity param0) {
        if (this.activityRequirementsAreMet(param0)) {
            this.setActiveActivity(param0);
        } else {
            this.useDefaultActivity();
        }

    }

    private void setActiveActivity(Activity param0) {
        if (!this.isActive(param0)) {
            this.eraseMemoriesForOtherActivitesThan(param0);
            this.activeActivities.clear();
            this.activeActivities.addAll(this.coreActivities);
            this.activeActivities.add(param0);
        }
    }

    private void eraseMemoriesForOtherActivitesThan(Activity param0) {
        for(Activity var0 : this.activeActivities) {
            if (var0 != param0) {
                Set<MemoryModuleType<?>> var1 = this.activityMemoriesToEraseWhenStopped.get(var0);
                if (var1 != null) {
                    for(MemoryModuleType<?> var2 : var1) {
                        this.eraseMemory(var2);
                    }
                }
            }
        }

    }

    public void updateActivityFromSchedule(long param0, long param1) {
        if (param1 - this.lastScheduleUpdate > 20L) {
            this.lastScheduleUpdate = param1;
            Activity var0 = this.getSchedule().getActivityAt((int)(param0 % 24000L));
            if (!this.activeActivities.contains(var0)) {
                this.setActiveActivityIfPossible(var0);
            }
        }

    }

    public void setActiveActivityToFirstValid(List<Activity> param0) {
        for(Activity var0 : param0) {
            if (this.activityRequirementsAreMet(var0)) {
                this.setActiveActivity(var0);
                break;
            }
        }

    }

    public void setDefaultActivity(Activity param0) {
        this.defaultActivity = param0;
    }

    public void addActivity(Activity param0, int param1, ImmutableList<? extends Behavior<? super E>> param2) {
        this.addActivity(param0, this.createPriorityPairs(param1, param2));
    }

    public void addActivityAndRemoveMemoryWhenStopped(
        Activity param0, int param1, ImmutableList<? extends Behavior<? super E>> param2, MemoryModuleType<?> param3
    ) {
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> var0 = ImmutableSet.of(Pair.of(param3, MemoryStatus.VALUE_PRESENT));
        Set<MemoryModuleType<?>> var1 = ImmutableSet.of(param3);
        this.addActivityAndRemoveMemoriesWhenStopped(param0, this.createPriorityPairs(param1, param2), var0, var1);
    }

    public void addActivity(Activity param0, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> param1) {
        this.addActivityAndRemoveMemoriesWhenStopped(param0, param1, ImmutableSet.of(), Sets.newHashSet());
    }

    public void addActivityWithConditions(
        Activity param0, ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> param1, Set<Pair<MemoryModuleType<?>, MemoryStatus>> param2
    ) {
        this.addActivityAndRemoveMemoriesWhenStopped(param0, param1, param2, Sets.newHashSet());
    }

    private void addActivityAndRemoveMemoriesWhenStopped(
        Activity param0,
        ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> param1,
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> param2,
        Set<MemoryModuleType<?>> param3
    ) {
        this.activityRequirements.put(param0, param2);
        if (!param3.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(param0, param3);
        }

        for(Pair<Integer, ? extends Behavior<? super E>> var0 : param1) {
            this.availableBehaviorsByPriority
                .computeIfAbsent(var0.getFirst(), param0x -> Maps.newHashMap())
                .computeIfAbsent(param0, param0x -> Sets.newLinkedHashSet())
                .add(var0.getSecond());
        }

    }

    public boolean isActive(Activity param0) {
        return this.activeActivities.contains(param0);
    }

    public Brain<E> copyWithoutBehaviors() {
        Brain<E> var0 = new Brain<>(this.memories.keySet(), this.sensors.keySet(), new Dynamic<>(NbtOps.INSTANCE, new CompoundTag()));

        for(Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> var1 : this.memories.entrySet()) {
            MemoryModuleType<?> var2 = var1.getKey();
            if (var1.getValue().isPresent()) {
                var0.memories.put(var2, var1.getValue());
            }
        }

        return var0;
    }

    public void tick(ServerLevel param0, E param1) {
        this.forgetOutdatedMemories();
        this.tickSensors(param0, param1);
        this.startEachNonRunningBehavior(param0, param1);
        this.tickEachRunningBehavior(param0, param1);
    }

    private void tickSensors(ServerLevel param0, E param1) {
        for(Sensor<? super E> var0 : this.sensors.values()) {
            var0.tick(param0, param1);
        }

    }

    private void forgetOutdatedMemories() {
        for(Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> var0 : this.memories.entrySet()) {
            if (var0.getValue().isPresent()) {
                ExpirableValue<?> var1 = var0.getValue().get();
                var1.tick();
                if (var1.hasExpired()) {
                    this.eraseMemory(var0.getKey());
                }
            }
        }

    }

    public void stopAll(ServerLevel param0, E param1) {
        long var0 = param1.level.getGameTime();

        for(Behavior<? super E> var1 : this.getRunningBehaviors()) {
            var1.doStop(param0, param1, var0);
        }

    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        Builder<T, T> var0 = ImmutableMap.builder();

        for(Entry<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> var1 : this.memories.entrySet()) {
            MemoryModuleType<?> var2 = var1.getKey();
            if (var1.getValue().isPresent() && var2.getDeserializer().isPresent()) {
                ExpirableValue<?> var3 = var1.getValue().get();
                T var4 = param0.createString(Registry.MEMORY_MODULE_TYPE.getKey(var2).toString());
                T var5 = var3.serialize(param0);
                var0.put(var4, var5);
            }
        }

        return param0.createMap(ImmutableMap.of(param0.createString("memories"), param0.createMap(var0.build())));
    }

    private void startEachNonRunningBehavior(ServerLevel param0, E param1) {
        long var0 = param0.getGameTime();

        for(Map<Activity, Set<Behavior<? super E>>> var1 : this.availableBehaviorsByPriority.values()) {
            for(Entry<Activity, Set<Behavior<? super E>>> var2 : var1.entrySet()) {
                Activity var3 = var2.getKey();
                if (this.activeActivities.contains(var3)) {
                    for(Behavior<? super E> var5 : var2.getValue()) {
                        if (var5.getStatus() == Behavior.Status.STOPPED) {
                            var5.tryStart(param0, param1, var0);
                        }
                    }
                }
            }
        }

    }

    private void tickEachRunningBehavior(ServerLevel param0, E param1) {
        long var0 = param0.getGameTime();

        for(Behavior<? super E> var1 : this.getRunningBehaviors()) {
            var1.tickOrStop(param0, param1, var0);
        }

    }

    private boolean activityRequirementsAreMet(Activity param0) {
        if (!this.activityRequirements.containsKey(param0)) {
            return false;
        } else {
            for(Pair<MemoryModuleType<?>, MemoryStatus> var0 : this.activityRequirements.get(param0)) {
                MemoryModuleType<?> var1 = var0.getFirst();
                MemoryStatus var2 = var0.getSecond();
                if (!this.checkMemory(var1, var2)) {
                    return false;
                }
            }

            return true;
        }
    }

    private boolean isEmptyCollection(Object param0) {
        return param0 instanceof Collection && ((Collection)param0).isEmpty();
    }

    ImmutableList<? extends Pair<Integer, ? extends Behavior<? super E>>> createPriorityPairs(int param0, ImmutableList<? extends Behavior<? super E>> param1) {
        int var0 = param0;
        com.google.common.collect.ImmutableList.Builder<Pair<Integer, ? extends Behavior<? super E>>> var1 = ImmutableList.builder();

        for(Behavior<? super E> var2 : param1) {
            var1.add(Pair.of(var0++, var2));
        }

        return var1.build();
    }
}
