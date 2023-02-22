package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.ImmutableList.Builder;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class Brain<E extends LivingEntity> {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Supplier<Codec<Brain<E>>> codec;
    private static final int SCHEDULE_UPDATE_DELAY = 20;
    private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public static <E extends LivingEntity> Brain.Provider<E> provider(
        Collection<? extends MemoryModuleType<?>> param0, Collection<? extends SensorType<? extends Sensor<? super E>>> param1
    ) {
        return new Brain.Provider<>(param0, param1);
    }

    public static <E extends LivingEntity> Codec<Brain<E>> codec(
        final Collection<? extends MemoryModuleType<?>> param0, final Collection<? extends SensorType<? extends Sensor<? super E>>> param1
    ) {
        final MutableObject<Codec<Brain<E>>> var0 = new MutableObject<>();
        var0.setValue(
            (new MapCodec<Brain<E>>() {
                    @Override
                    public <T> Stream<T> keys(DynamicOps<T> param0x) {
                        return param0.stream()
                            .flatMap(param0xx -> param0xx.getCodec().map(param1xxx -> BuiltInRegistries.MEMORY_MODULE_TYPE.getKey(param0xx)).stream())
                            .map(param1xx -> param0.createString(param1xx.toString()));
                    }
        
                    @Override
                    public <T> DataResult<Brain<E>> decode(DynamicOps<T> param0x, MapLike<T> param1x) {
                        MutableObject<DataResult<Builder<Brain.MemoryValue<?>>>> var0 = new MutableObject<>(DataResult.success(ImmutableList.builder()));
                        param1.entries().forEach(param2 -> {
                            DataResult<MemoryModuleType<?>> var0xxx = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().parse(param0, param2.getFirst());
                            DataResult<? extends Brain.MemoryValue<?>> var1x = var0xxx.flatMap(
                                param2x -> this.captureRead(param2x, param0, (T)param2.getSecond())
                            );
                            var0.setValue(var0.getValue().apply2(Builder::add, var1x));
                        });
                        ImmutableList<Brain.MemoryValue<?>> var1 = var0.getValue()
                            .resultOrPartial(Brain.LOGGER::error)
                            .map(Builder::build)
                            .orElseGet(ImmutableList::of);
                        return DataResult.success(new Brain<>(param0, param1, var1, var0::getValue));
                    }
        
                    private <T, U> DataResult<Brain.MemoryValue<U>> captureRead(MemoryModuleType<U> param0x, DynamicOps<T> param1x, T param2) {
                        return param0.getCodec()
                            .map(DataResult::success)
                            .orElseGet(() -> DataResult.error(() -> "No codec for memory: " + param0))
                            .<ExpirableValue<U>>flatMap(param2x -> param2x.parse(param1, param2))
                            .map(param1xxx -> new Brain.MemoryValue<>(param0, Optional.of(param1xxx)));
                    }
        
                    public <T> RecordBuilder<T> encode(Brain<E> param0x, DynamicOps<T> param1x, RecordBuilder<T> param2) {
                        param0.memories().forEach(param2x -> param2x.serialize(param1, param2));
                        return param2;
                    }
                })
                .fieldOf("memories")
                .codec()
        );
        return var0.getValue();
    }

    public Brain(
        Collection<? extends MemoryModuleType<?>> param0,
        Collection<? extends SensorType<? extends Sensor<? super E>>> param1,
        ImmutableList<Brain.MemoryValue<?>> param2,
        Supplier<Codec<Brain<E>>> param3
    ) {
        this.codec = param3;

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

        for(Brain.MemoryValue<?> var4 : param2) {
            var4.setMemoryInternal(this);
        }

    }

    public <T> DataResult<T> serializeStart(DynamicOps<T> param0) {
        return this.codec.get().encodeStart(param0, this);
    }

    Stream<Brain.MemoryValue<?>> memories() {
        return this.memories
            .entrySet()
            .stream()
            .map(param0 -> Brain.MemoryValue.createUnchecked((MemoryModuleType<? extends ExpirableValue<?>>)param0.getKey(), param0.getValue()));
    }

    public boolean hasMemoryValue(MemoryModuleType<?> param0) {
        return this.checkMemory(param0, MemoryStatus.VALUE_PRESENT);
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

    <U> void setMemoryInternal(MemoryModuleType<U> param0, Optional<? extends ExpirableValue<?>> param1) {
        if (this.memories.containsKey(param0)) {
            if (param1.isPresent() && this.isEmptyCollection(param1.get().getValue())) {
                this.eraseMemory(param0);
            } else {
                this.memories.put(param0, param1);
            }
        }

    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> param0) {
        Optional<? extends ExpirableValue<?>> var0 = this.memories.get(param0);
        if (var0 == null) {
            throw new IllegalStateException("Unregistered memory fetched: " + param0);
        } else {
            return var0.map(ExpirableValue::getValue);
        }
    }

    @Nullable
    public <U> Optional<U> getMemoryInternal(MemoryModuleType<U> param0) {
        Optional<? extends ExpirableValue<?>> var0 = this.memories.get(param0);
        return var0 == null ? null : var0.map(ExpirableValue::getValue);
    }

    public <U> long getTimeUntilExpiry(MemoryModuleType<U> param0) {
        Optional<? extends ExpirableValue<?>> var0 = this.memories.get(param0);
        return var0.map(ExpirableValue::getTimeToLive).orElse(0L);
    }

    @Deprecated
    @VisibleForDebug
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return this.memories;
    }

    public <U> boolean isMemoryValue(MemoryModuleType<U> param0, U param1) {
        return !this.hasMemoryValue(param0) ? false : this.getMemory(param0).filter(param1x -> param1x.equals(param1)).isPresent();
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
    @VisibleForDebug
    public Set<Activity> getActiveActivities() {
        return this.activeActivities;
    }

    @Deprecated
    @VisibleForDebug
    public List<BehaviorControl<? super E>> getRunningBehaviors() {
        List<BehaviorControl<? super E>> var0 = new ObjectArrayList<>();

        for(Map<Activity, Set<BehaviorControl<? super E>>> var1 : this.availableBehaviorsByPriority.values()) {
            for(Set<BehaviorControl<? super E>> var2 : var1.values()) {
                for(BehaviorControl<? super E> var3 : var2) {
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

    public void addActivity(Activity param0, int param1, ImmutableList<? extends BehaviorControl<? super E>> param2) {
        this.addActivity(param0, this.createPriorityPairs(param1, param2));
    }

    public void addActivityAndRemoveMemoryWhenStopped(
        Activity param0, int param1, ImmutableList<? extends BehaviorControl<? super E>> param2, MemoryModuleType<?> param3
    ) {
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> var0 = ImmutableSet.of(Pair.of(param3, MemoryStatus.VALUE_PRESENT));
        Set<MemoryModuleType<?>> var1 = ImmutableSet.of(param3);
        this.addActivityAndRemoveMemoriesWhenStopped(param0, this.createPriorityPairs(param1, param2), var0, var1);
    }

    public void addActivity(Activity param0, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> param1) {
        this.addActivityAndRemoveMemoriesWhenStopped(param0, param1, ImmutableSet.of(), Sets.newHashSet());
    }

    public void addActivityWithConditions(
        Activity param0,
        ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> param1,
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> param2
    ) {
        this.addActivityAndRemoveMemoriesWhenStopped(param0, param1, param2, Sets.newHashSet());
    }

    public void addActivityAndRemoveMemoriesWhenStopped(
        Activity param0,
        ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> param1,
        Set<Pair<MemoryModuleType<?>, MemoryStatus>> param2,
        Set<MemoryModuleType<?>> param3
    ) {
        this.activityRequirements.put(param0, param2);
        if (!param3.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(param0, param3);
        }

        for(Pair<Integer, ? extends BehaviorControl<? super E>> var0 : param1) {
            this.availableBehaviorsByPriority
                .computeIfAbsent(var0.getFirst(), param0x -> Maps.newHashMap())
                .computeIfAbsent(param0, param0x -> Sets.newLinkedHashSet())
                .add(var0.getSecond());
        }

    }

    @VisibleForTesting
    public void removeAllBehaviors() {
        this.availableBehaviorsByPriority.clear();
    }

    public boolean isActive(Activity param0) {
        return this.activeActivities.contains(param0);
    }

    public Brain<E> copyWithoutBehaviors() {
        Brain<E> var0 = new Brain<>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);

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
                if (var1.hasExpired()) {
                    this.eraseMemory(var0.getKey());
                }

                var1.tick();
            }
        }

    }

    public void stopAll(ServerLevel param0, E param1) {
        long var0 = param1.level.getGameTime();

        for(BehaviorControl<? super E> var1 : this.getRunningBehaviors()) {
            var1.doStop(param0, param1, var0);
        }

    }

    private void startEachNonRunningBehavior(ServerLevel param0, E param1) {
        long var0 = param0.getGameTime();

        for(Map<Activity, Set<BehaviorControl<? super E>>> var1 : this.availableBehaviorsByPriority.values()) {
            for(Entry<Activity, Set<BehaviorControl<? super E>>> var2 : var1.entrySet()) {
                Activity var3 = var2.getKey();
                if (this.activeActivities.contains(var3)) {
                    for(BehaviorControl<? super E> var5 : var2.getValue()) {
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

        for(BehaviorControl<? super E> var1 : this.getRunningBehaviors()) {
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

    ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(
        int param0, ImmutableList<? extends BehaviorControl<? super E>> param1
    ) {
        int var0 = param0;
        Builder<Pair<Integer, ? extends BehaviorControl<? super E>>> var1 = ImmutableList.builder();

        for(BehaviorControl<? super E> var2 : param1) {
            var1.add(Pair.of(var0++, var2));
        }

        return var1.build();
    }

    static final class MemoryValue<U> {
        private final MemoryModuleType<U> type;
        private final Optional<? extends ExpirableValue<U>> value;

        static <U> Brain.MemoryValue<U> createUnchecked(MemoryModuleType<U> param0, Optional<? extends ExpirableValue<?>> param1) {
            return new Brain.MemoryValue<>(param0, param1);
        }

        MemoryValue(MemoryModuleType<U> param0, Optional<? extends ExpirableValue<U>> param1) {
            this.type = param0;
            this.value = param1;
        }

        void setMemoryInternal(Brain<?> param0) {
            param0.setMemoryInternal(this.type, this.value);
        }

        public <T> void serialize(DynamicOps<T> param0, RecordBuilder<T> param1) {
            this.type
                .getCodec()
                .ifPresent(
                    param2 -> this.value
                            .ifPresent(
                                param3 -> param1.add(
                                        BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().encodeStart(param0, this.type), param2.encodeStart(param0, param3)
                                    )
                            )
                );
        }
    }

    public static final class Provider<E extends LivingEntity> {
        private final Collection<? extends MemoryModuleType<?>> memoryTypes;
        private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
        private final Codec<Brain<E>> codec;

        Provider(Collection<? extends MemoryModuleType<?>> param0, Collection<? extends SensorType<? extends Sensor<? super E>>> param1) {
            this.memoryTypes = param0;
            this.sensorTypes = param1;
            this.codec = Brain.codec(param0, param1);
        }

        public Brain<E> makeBrain(Dynamic<?> param0) {
            return this.codec
                .parse(param0)
                .resultOrPartial(Brain.LOGGER::error)
                .orElseGet(() -> new Brain<>(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> this.codec));
        }
    }
}
