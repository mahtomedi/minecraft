package net.minecraft.world.level.timers;

import com.google.common.collect.Maps;
import com.google.common.primitives.UnsignedLong;
import java.util.Comparator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TimerQueue<T> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final TimerCallbacks<T> callbacksRegistry;
    private final Queue<TimerQueue.Event<T>> queue = new PriorityQueue<>(createComparator());
    private UnsignedLong sequentialId = UnsignedLong.ZERO;
    private final Map<String, TimerQueue.Event<T>> events = Maps.newHashMap();

    private static <T> Comparator<TimerQueue.Event<T>> createComparator() {
        return (param0, param1) -> {
            int var0 = Long.compare(param0.triggerTime, param1.triggerTime);
            return var0 != 0 ? var0 : param0.sequentialId.compareTo(param1.sequentialId);
        };
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
            this.events.remove(var0.id);
            var0.callback.handle(param0, this, param1);
        }
    }

    private void addEvent(String param0, long param1, TimerCallback<T> param2) {
        this.sequentialId = this.sequentialId.plus(UnsignedLong.ONE);
        TimerQueue.Event<T> var0 = new TimerQueue.Event<>(param1, this.sequentialId, param0, param2);
        this.events.put(param0, var0);
        this.queue.add(var0);
    }

    public boolean schedule(String param0, long param1, TimerCallback<T> param2) {
        if (this.events.containsKey(param0)) {
            return false;
        } else {
            this.addEvent(param0, param1, param2);
            return true;
        }
    }

    public void reschedule(String param0, long param1, TimerCallback<T> param2) {
        TimerQueue.Event<T> var0 = this.events.remove(param0);
        if (var0 != null) {
            this.queue.remove(var0);
        }

        this.addEvent(param0, param1, param2);
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

    public void load(ListTag param0) {
        this.queue.clear();
        this.events.clear();
        this.sequentialId = UnsignedLong.ZERO;
        if (!param0.isEmpty()) {
            if (param0.getElementType() != 10) {
                LOGGER.warn("Invalid format of events: " + param0);
            } else {
                for(Tag var0 : param0) {
                    this.loadEvent((CompoundTag)var0);
                }

            }
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
