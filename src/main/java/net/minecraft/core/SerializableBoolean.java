package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.util.Serializable;

public final class SerializableBoolean implements Serializable {
    private final boolean value;

    private SerializableBoolean(boolean param0) {
        this.value = param0;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return param0.createBoolean(this.value);
    }

    public static SerializableBoolean of(Dynamic<?> param0) {
        return new SerializableBoolean(param0.asBoolean(false));
    }

    public static SerializableBoolean of(boolean param0) {
        return new SerializableBoolean(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            SerializableBoolean var0 = (SerializableBoolean)param0;
            return this.value == var0.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(this.value);
    }

    @Override
    public String toString() {
        return Boolean.toString(this.value);
    }
}
