package net.minecraft.core;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.Util;

public final class UUIDUtil {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM
        .comapFlatMap(param0 -> Util.fixedSize(param0, 4).map(UUIDUtil::uuidFromIntArray), param0 -> Arrays.stream(uuidToIntArray(param0)));
    private static final String UUID_PREFIX_OFFLINE_PLAYER = "OfflinePlayer:";

    private UUIDUtil() {
    }

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

    public static UUID readUUID(Dynamic<?> param0) {
        int[] var0 = param0.asIntStream().toArray();
        if (var0.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + var0.length + ".");
        } else {
            return uuidFromIntArray(var0);
        }
    }

    public static UUID getOrCreatePlayerUUID(GameProfile param0) {
        UUID var0 = param0.getId();
        if (var0 == null) {
            var0 = createOfflinePlayerUUID(param0.getName());
        }

        return var0;
    }

    public static UUID createOfflinePlayerUUID(String param0) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + param0).getBytes(StandardCharsets.UTF_8));
    }
}
