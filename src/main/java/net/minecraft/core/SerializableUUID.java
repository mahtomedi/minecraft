package net.minecraft.core;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.util.Serializable;

public final class SerializableUUID implements Serializable {
    private final UUID value;

    public SerializableUUID(UUID param0) {
        this.value = param0;
    }

    public UUID value() {
        return this.value;
    }

    @Override
    public <T> T serialize(DynamicOps<T> param0) {
        return serialize(param0, this.value);
    }

    public static SerializableUUID of(Dynamic<?> param0) {
        return new SerializableUUID(readUUID(param0));
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    public static UUID uuidFromIntArray(int[] param0) {
        return new UUID((long)param0[0] << 32 | (long)param0[1] & 4294967295L, (long)param0[2] << 32 | (long)param0[3] & 4294967295L);
    }

    public static int[] uuidToIntArray(UUID param0) {
        long var0 = param0.getMostSignificantBits();
        long var1 = param0.getLeastSignificantBits();
        return leastMostToIntArray(var0, var1);
    }

    public static int[] leastMostToIntArray(long param0, long param1) {
        return new int[]{(int)(param0 >> 32), (int)param0, (int)(param1 >> 32), (int)param1};
    }

    public static UUID readUUID(Dynamic<?> param0) {
        int[] var0 = param0.asIntStream().toArray();
        if (var0.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + var0.length + ".");
        } else {
            return uuidFromIntArray(var0);
        }
    }

    public static <T> T serialize(DynamicOps<T> param0, UUID param1) {
        return param0.createIntList(Arrays.stream(uuidToIntArray(param1)));
    }
}
