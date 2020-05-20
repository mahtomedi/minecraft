package net.minecraft.core;

import com.mojang.serialization.Codec;

public final class SerializableLong {
    public static final Codec<SerializableLong> CODEC = Codec.LONG.xmap(SerializableLong::new, param0 -> param0.value);
    private final long value;

    private SerializableLong(long param0) {
        this.value = param0;
    }

    public long value() {
        return this.value;
    }

    public static SerializableLong of(long param0) {
        return new SerializableLong(param0);
    }

    @Override
    public boolean equals(Object param0) {
        if (this == param0) {
            return true;
        } else if (param0 != null && this.getClass() == param0.getClass()) {
            SerializableLong var0 = (SerializableLong)param0;
            return this.value == var0.value;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(this.value);
    }

    @Override
    public String toString() {
        return Long.toString(this.value);
    }
}
