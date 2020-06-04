package net.minecraft.core;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class SerializableUUID {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM
        .comapFlatMap(param0 -> Util.fixedSize(param0, 4).map(SerializableUUID::uuidFromIntArray), param0 -> Arrays.stream(uuidToIntArray(param0)));

    public static UUID uuidFromIntArray(int[] param0) {
        return new UUID((long)param0[0] << 32 | (long)param0[1] & 4294967295L, (long)param0[2] << 32 | (long)param0[3] & 4294967295L);
    }

    public static int[] uuidToIntArray(UUID param0) {
        long var0 = param0.getMostSignificantBits();
        long var1 = param0.getLeastSignificantBits();
        return leastMostToIntArray(var0, var1);
    }

    private static int[] leastMostToIntArray(long param0, long param1) {
        return new int[]{(int)(param0 >> 32), (int)param0, (int)(param1 >> 32), (int)param1};
    }
}
