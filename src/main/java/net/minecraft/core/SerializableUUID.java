package net.minecraft.core;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class SerializableUUID {
    public static final Codec<SerializableUUID> CODEC = Codec.INT_STREAM
        .comapFlatMap(
            param0 -> Util.fixedSize(param0, 4).map(param0x -> new SerializableUUID(uuidFromIntArray(param0x))),
            param0 -> Arrays.stream(uuidToIntArray(param0.value))
        );
    private final UUID value;

    public SerializableUUID(UUID param0) {
        this.value = param0;
    }

    public UUID value() {
        return this.value;
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
}
