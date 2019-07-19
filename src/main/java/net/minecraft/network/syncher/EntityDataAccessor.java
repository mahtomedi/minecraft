package net.minecraft.network.syncher;

public class EntityDataAccessor<T> {
    private final int id;
    private final EntityDataSerializer<T> serializer;

    public EntityDataAccessor(int param0, EntityDataSerializer<T> param1) {
        this.id = param0;
        this.serializer = param1;
    }

    public int getId() {
        return this.id;
    }

    public EntityDataSerializer<T> getSerializer() {
        return this.serializer;
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            EntityDataAccessor<?> var0 = (EntityDataAccessor)param0;
            return this.id == var0.id;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public String toString() {
        return "<entity data: " + this.id + ">";
    }
}
