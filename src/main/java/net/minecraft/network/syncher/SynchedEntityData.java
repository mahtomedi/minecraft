package net.minecraft.network.syncher;

import com.mojang.logging.LogUtils;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<>();
    private static final int MAX_ID_VALUE = 254;
    private final Entity entity;
    private final Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById = new Int2ObjectOpenHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean isDirty;

    public SynchedEntityData(Entity param0) {
        this.entity = param0;
    }

    public static <T> EntityDataAccessor<T> defineId(Class<? extends Entity> param0, EntityDataSerializer<T> param1) {
        if (LOGGER.isDebugEnabled()) {
            try {
                Class<?> var0 = Class.forName(Thread.currentThread().getStackTrace()[2].getClassName());
                if (!var0.equals(param0)) {
                    LOGGER.debug("defineId called for: {} from {}", param0, var0, new RuntimeException());
                }
            } catch (ClassNotFoundException var5) {
            }
        }

        int var1;
        if (ENTITY_ID_POOL.containsKey(param0)) {
            var1 = ENTITY_ID_POOL.getInt(param0) + 1;
        } else {
            int var2 = 0;
            Class<?> var3 = param0;

            while(var3 != Entity.class) {
                var3 = var3.getSuperclass();
                if (ENTITY_ID_POOL.containsKey(var3)) {
                    var2 = ENTITY_ID_POOL.getInt(var3) + 1;
                    break;
                }
            }

            var1 = var2;
        }

        if (var1 > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + var1 + "! (Max is 254)");
        } else {
            ENTITY_ID_POOL.put(param0, var1);
            return param1.createAccessor(var1);
        }
    }

    public <T> void define(EntityDataAccessor<T> param0, T param1) {
        int var0 = param0.getId();
        if (var0 > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + var0 + "! (Max is 254)");
        } else if (this.itemsById.containsKey(var0)) {
            throw new IllegalArgumentException("Duplicate id value for " + var0 + "!");
        } else if (EntityDataSerializers.getSerializedId(param0.getSerializer()) < 0) {
            throw new IllegalArgumentException("Unregistered serializer " + param0.getSerializer() + " for " + var0 + "!");
        } else {
            this.createDataItem(param0, param1);
        }
    }

    private <T> void createDataItem(EntityDataAccessor<T> param0, T param1) {
        SynchedEntityData.DataItem<T> var0 = new SynchedEntityData.DataItem<>(param0, param1);
        this.lock.writeLock().lock();
        this.itemsById.put(param0.getId(), var0);
        this.lock.writeLock().unlock();
    }

    public <T> boolean hasItem(EntityDataAccessor<T> param0) {
        return this.itemsById.containsKey(param0.getId());
    }

    private <T> SynchedEntityData.DataItem<T> getItem(EntityDataAccessor<T> param0) {
        this.lock.readLock().lock();

        SynchedEntityData.DataItem<T> var0;
        try {
            var0 = (SynchedEntityData.DataItem)this.itemsById.get(param0.getId());
        } catch (Throwable var9) {
            CrashReport var2 = CrashReport.forThrowable(var9, "Getting synched entity data");
            CrashReportCategory var3 = var2.addCategory("Synched entity data");
            var3.setDetail("Data ID", param0);
            throw new ReportedException(var2);
        } finally {
            this.lock.readLock().unlock();
        }

        return var0;
    }

    public <T> T get(EntityDataAccessor<T> param0) {
        return this.getItem(param0).getValue();
    }

    public <T> void set(EntityDataAccessor<T> param0, T param1) {
        this.set(param0, param1, false);
    }

    public <T> void set(EntityDataAccessor<T> param0, T param1, boolean param2) {
        SynchedEntityData.DataItem<T> var0 = this.getItem(param0);
        if (param2 || ObjectUtils.notEqual(param1, var0.getValue())) {
            var0.setValue(param1);
            this.entity.onSyncedDataUpdated(param0);
            var0.setDirty(true);
            this.isDirty = true;
        }

    }

    public boolean isDirty() {
        return this.isDirty;
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> packDirty() {
        List<SynchedEntityData.DataValue<?>> var0 = null;
        if (this.isDirty) {
            this.lock.readLock().lock();

            for(SynchedEntityData.DataItem<?> var1 : this.itemsById.values()) {
                if (var1.isDirty()) {
                    var1.setDirty(false);
                    if (var0 == null) {
                        var0 = new ArrayList<>();
                    }

                    var0.add(var1.value());
                }
            }

            this.lock.readLock().unlock();
        }

        this.isDirty = false;
        return var0;
    }

    @Nullable
    public List<SynchedEntityData.DataValue<?>> getNonDefaultValues() {
        List<SynchedEntityData.DataValue<?>> var0 = null;
        this.lock.readLock().lock();

        for(SynchedEntityData.DataItem<?> var1 : this.itemsById.values()) {
            if (!var1.isSetToDefault()) {
                if (var0 == null) {
                    var0 = new ArrayList<>();
                }

                var0.add(var1.value());
            }
        }

        this.lock.readLock().unlock();
        return var0;
    }

    public void assignValues(List<SynchedEntityData.DataValue<?>> param0) {
        this.lock.writeLock().lock();

        try {
            for(SynchedEntityData.DataValue<?> var0 : param0) {
                SynchedEntityData.DataItem<?> var1 = this.itemsById.get(var0.id);
                if (var1 != null) {
                    this.assignValue(var1, var0);
                    this.entity.onSyncedDataUpdated(var1.getAccessor());
                }
            }
        } finally {
            this.lock.writeLock().unlock();
        }

        this.entity.onSyncedDataUpdated(param0);
    }

    private <T> void assignValue(SynchedEntityData.DataItem<T> param0, SynchedEntityData.DataValue<?> param1) {
        if (!Objects.equals(param1.serializer(), param0.accessor.getSerializer())) {
            throw new IllegalStateException(
                String.format(
                    Locale.ROOT,
                    "Invalid entity data item type for field %d on entity %s: old=%s(%s), new=%s(%s)",
                    param0.accessor.getId(),
                    this.entity,
                    param0.value,
                    param0.value.getClass(),
                    param1.value,
                    param1.value.getClass()
                )
            );
        } else {
            param0.setValue(param1.value);
        }
    }

    public boolean isEmpty() {
        return this.itemsById.isEmpty();
    }

    public static class DataItem<T> {
        final EntityDataAccessor<T> accessor;
        T value;
        private final T initialValue;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> param0, T param1) {
            this.accessor = param0;
            this.initialValue = param1;
            this.value = param1;
        }

        public EntityDataAccessor<T> getAccessor() {
            return this.accessor;
        }

        public void setValue(T param0) {
            this.value = param0;
        }

        public T getValue() {
            return this.value;
        }

        public boolean isDirty() {
            return this.dirty;
        }

        public void setDirty(boolean param0) {
            this.dirty = param0;
        }

        public boolean isSetToDefault() {
            return this.initialValue.equals(this.value);
        }

        public SynchedEntityData.DataValue<T> value() {
            return SynchedEntityData.DataValue.create(this.accessor, this.value);
        }
    }

    public static record DataValue<T>(int id, EntityDataSerializer<T> serializer, T value) {
        public static <T> SynchedEntityData.DataValue<T> create(EntityDataAccessor<T> param0, T param1) {
            EntityDataSerializer<T> var0 = param0.getSerializer();
            return new SynchedEntityData.DataValue<>(param0.getId(), var0, var0.copy(param1));
        }

        public void write(FriendlyByteBuf param0) {
            int var0 = EntityDataSerializers.getSerializedId(this.serializer);
            if (var0 < 0) {
                throw new EncoderException("Unknown serializer type " + this.serializer);
            } else {
                param0.writeByte(this.id);
                param0.writeVarInt(var0);
                this.serializer.write(param0, this.value);
            }
        }

        public static SynchedEntityData.DataValue<?> read(FriendlyByteBuf param0, int param1) {
            int var0 = param0.readVarInt();
            EntityDataSerializer<?> var1 = EntityDataSerializers.getSerializer(var0);
            if (var1 == null) {
                throw new DecoderException("Unknown serializer type " + var0);
            } else {
                return read(param0, param1, var1);
            }
        }

        private static <T> SynchedEntityData.DataValue<T> read(FriendlyByteBuf param0, int param1, EntityDataSerializer<T> param2) {
            return new SynchedEntityData.DataValue<>(param1, param2, param2.read(param0));
        }
    }
}
