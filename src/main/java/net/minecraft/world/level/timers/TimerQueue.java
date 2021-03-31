package net.minecraft.world.level.timers;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.primitives.UnsignedLong;
import com.mojang.serialization.Dynamic;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerQueue<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CALLBACK_DATA_TAG = "Callback";
    private static final String TIMER_NAME_TAG = "Name";
    private static final String TIMER_TRIGGER_TIME_TAG = "TriggerTime";
    private final TimerCallbacks<T> callbacksRegistry;
    private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue<>(createComparator());
    private UnsignedLong sequentialId = UnsignedLong.ZERO;
    private final Table<String, Long, TimerQueue.Event<T>> events = HashBasedTable.create();

    private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
        return Comparator.<TimerQueue.Event<T>>comparingLong(param0 -> param0.triggerTime).thenComparing(param0 -> param0.sequentialId);
    }

    public TimerQueue(TimerCallbacks<T> param0, Stream<Dynamic<Tag>> param1) {
        this(param0);
        this.queue.clear();
        this.events.clear();
        this.sequentialId = UnsignedLong.ZERO;
        param1.forEach(param0x -> {
            if (!(param0x.getValue() instanceof CompoundTag)) {
                LOGGER.warn("Invalid format of events: {}", param0x);
            } else {
                this.loadEvent((CompoundTag)param0x.getValue());
            }
        });
    }

    public TimerQueue(TimerCallbacks<T> param0) {
        this.callbacksRegistry = param0;
    }

    public void tick(T param0, long param1) {
        while(true) {
            TimerQueue.Event<T> var0 = this.queue.peek();
            if (var0 == null || var0.triggerTime > param1) {
                return;
            }

            this.queue.remove();
            this.events.remove(var0.id, param1);
            var0.callback.handle(param0, this, param1);
        }
    }

    public void schedule(String param0, long param1, TimerCallback<T> param2) {
        if (!this.events.contains(param0, param1)) {
            this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
            TimerQueue.Event<T> var0 = new TimerQueue.Event<>(param1, this.sequentialId, param0, param2);
            this.events.put(param0, param1, var0);
            this.queue.add(var0);
        }
    }

    public int remove(String param0) {
        Collection<TimerQueue.Event<T>> var0 = this.events.row(param0).values();
        var0.forEach(this.queue::remove);
        int var1 = var0.size();
        var0.clear();
        return var1;
    }

    public Set<String> getEventsIds() {
        return Collections.unmodifiableSet(this.events.rowKeySet());
    }

    private void loadEvent(CompoundTag param0) {
        CompoundTag var0 = param0.getCompound("Callback");
        TimerCallback<T> var1 = this.callbacksRegistry.deserialize(var0);
        if (var1 != null) {
            String var2 = param0.getString("Name");
            long var3 = param0.getLong("TriggerTime");
            this.schedule(var2, var3, var1);
        }

    }

    private CompoundTag storeEvent(TimerQueue.Event<T> param0) {
        CompoundTag var0 = new CompoundTag();
        var0.putString("Name", param0.id);
        var0.putLong("TriggerTime", param0.triggerTime);
        var0.put("Callback", this.callbacksRegistry.serialize(param0.callback));
        return var0;
    }

    public ListTag store() {
        ListTag var0 = new ListTag();
        this.queue.stream().sorted(createComparator()).map(this::storeEvent).forEach(var0::add);
        return var0;
    }

    public static class Event<T> {
        public final long triggerTime;
        public final UnsignedLong sequentialId;
        public final String id;
        public final TimerCallback<T> callback;

        private Event(long param0, UnsignedLong param1, String param2, TimerCallback<T> param3) {
            this.triggerTime = param0;
            this.sequentialId = param1;
            this.id = param2;
            this.callback = param3;
        }
    }
}
