package net.minecraft.network.syncher;

import com.google.common.collect.Lists;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SynchedEntityData {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Object2IntMap<Class<? extends Entity>> ENTITY_ID_POOL = new Object2IntOpenHashMap<>();
    private final Entity entity;
    private final Int2ObjectMap<SynchedEntityData.DataItem<?>> itemsById = new Int2ObjectOpenHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private boolean isEmpty = true;
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
            throw new IllegalArgumentException("Data value id is too big with " + var1 + "! (Max is " + 254 + ")");
        } else {
            ENTITY_ID_POOL.put(param0, var1);
            return param1.createAccessor(var1);
        }
    }

    public <T> void define(EntityDataAccessor<T> param0, T param1) {
        int var0 = param0.getId();
        if (var0 > 254) {
            throw new IllegalArgumentException("Data value id is too big with " + var0 + "! (Max is " + 254 + ")");
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
        this.isEmpty = false;
        this.lock.writeLock().unlock();
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
        SynchedEntityData.DataItem<T> var0 = this.getItem(param0);
        if (ObjectUtils.notEqual(param1, var0.getValue())) {
            var0.setValue(param1);
            this.entity.onSyncedDataUpdated(param0);
            var0.setDirty(true);
            this.isDirty = true;
        }

    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public static void pack(@Nullable List<SynchedEntityData.DataItem<?>> param0, FriendlyByteBuf param1) {
        if (param0 != null) {
            for(SynchedEntityData.DataItem<?> var0 : param0) {
                writeDataItem(param1, var0);
            }
        }

        param1.writeByte(255);
    }

    @Nullable
    public List<SynchedEntityData.DataItem<?>> packDirty() {
        List<SynchedEntityData.DataItem<?>> var0 = null;
        if (this.isDirty) {
            this.lock.readLock().lock();

            for(SynchedEntityData.DataItem<?> var1 : this.itemsById.values()) {
                if (var1.isDirty()) {
                    var1.setDirty(false);
                    if (var0 == null) {
                        var0 = Lists.newArrayList();
                    }

                    var0.add(var1.copy());
                }
            }

            this.lock.readLock().unlock();
        }

        this.isDirty = false;
        return var0;
    }

    @Nullable
    public List<SynchedEntityData.DataItem<?>> getAll() {
        List<SynchedEntityData.DataItem<?>> var0 = null;
        this.lock.readLock().lock();

        for(SynchedEntityData.DataItem<?> var1 : this.itemsById.values()) {
            if (var0 == null) {
                var0 = Lists.newArrayList();
            }

            var0.add(var1.copy());
        }

        this.lock.readLock().unlock();
        return var0;
    }

    private static <T> void writeDataItem(FriendlyByteBuf param0, SynchedEntityData.DataItem<T> param1) {
        EntityDataAccessor<T> var0 = param1.getAccessor();
        int var1 = EntityDataSerializers.getSerializedId(var0.getSerializer());
        if (var1 < 0) {
            throw new EncoderException("Unknown serializer type " + var0.getSerializer());
        } else {
            param0.writeByte(var0.getId());
            param0.writeVarInt(var1);
            var0.getSerializer().write(param0, param1.getValue());
        }
    }

    @Nullable
    public static List<SynchedEntityData.DataItem<?>> unpack(FriendlyByteBuf param0) {
        List<SynchedEntityData.DataItem<?>> var0 = null;

        int var1;
        while((var1 = param0.readUnsignedByte()) != 255) {
            if (var0 == null) {
                var0 = Lists.newArrayList();
            }

            int var2 = param0.readVarInt();
            EntityDataSerializer<?> var3 = EntityDataSerializers.getSerializer(var2);
            if (var3 == null) {
                throw new DecoderException("Unknown serializer type " + var2);
            }

            var0.add(genericHelper(param0, var1, var3));
        }

        return var0;
    }

    private static <T> SynchedEntityData.DataItem<T> genericHelper(FriendlyByteBuf param0, int param1, EntityDataSerializer<T> param2) {
        return new SynchedEntityData.DataItem<>(param2.createAccessor(param1), param2.read(param0));
    }

    @OnlyIn(Dist.CLIENT)
    public void assignValues(List<SynchedEntityData.DataItem<?>> param0) {
        this.lock.writeLock().lock();

        for(SynchedEntityData.DataItem<?> var0 : param0) {
            SynchedEntityData.DataItem<?> var1 = this.itemsById.get(var0.getAccessor().getId());
            if (var1 != null) {
                this.assignValue(var1, var0);
                this.entity.onSyncedDataUpdated(var0.getAccessor());
            }
        }

        this.lock.writeLock().unlock();
        this.isDirty = true;
    }

    @OnlyIn(Dist.CLIENT)
    private <T> void assignValue(SynchedEntityData.DataItem<T> param0, SynchedEntityData.DataItem<?> param1) {
        if (!Objects.equals(param1.accessor.getSerializer(), param0.accessor.getSerializer())) {
            throw new IllegalStateException(
                String.format(
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
            param0.setValue((T)param1.getValue());
        }
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    public void clearDirty() {
        this.isDirty = false;
        this.lock.readLock().lock();

        for(SynchedEntityData.DataItem<?> var0 : this.itemsById.values()) {
            var0.setDirty(false);
        }

        this.lock.readLock().unlock();
    }

    public static class DataItem<T> {
        private final EntityDataAccessor<T> accessor;
        private T value;
        private boolean dirty;

        public DataItem(EntityDataAccessor<T> param0, T param1) {
            this.accessor = param0;
            this.value = param1;
            this.dirty = true;
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

        public SynchedEntityData.DataItem<T> copy() {
            return new SynchedEntityData.DataItem<>(this.accessor, this.accessor.getSerializer().copy(this.value));
        }
    }
}
