package net.minecraft.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import com.mojang.util.UndashedUuid;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import net.minecraft.Util;

public final class UUIDUtil {
    public static final Codec<UUID> CODEC = Codec.INT_STREAM
        .comapFlatMap(param0 -> Util.fixedSize(param0, 4).map(UUIDUtil::uuidFromIntArray), param0 -> Arrays.stream(uuidToIntArray(param0)));
    public static final Codec<Set<UUID>> CODEC_SET = Codec.list(CODEC).xmap(Sets::newHashSet, Lists::newArrayList);
    public static final Codec<UUID> STRING_CODEC = Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(UUID.fromString(param0), Lifecycle.stable());
        } catch (IllegalArgumentException var2) {
            return DataResult.error(() -> "Invalid UUID " + param0 + ": " + var2.getMessage());
        }
    }, UUID::toString);
    public static Codec<UUID> AUTHLIB_CODEC = Codec.either(CODEC, Codec.STRING.comapFlatMap(param0 -> {
        try {
            return DataResult.success(UndashedUuid.fromStringLenient(param0), Lifecycle.stable());
        } catch (IllegalArgumentException var2) {
            return DataResult.error(() -> "Invalid UUID " + param0 + ": " + var2.getMessage());
        }
    }, UndashedUuid::toString)).xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x), Either::right);
    public static Codec<UUID> LENIENT_CODEC = Codec.either(CODEC, STRING_CODEC)
        .xmap(param0 -> param0.map(param0x -> param0x, param0x -> param0x), Either::left);
    public static final int UUID_BYTES = 16;
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

    public static byte[] uuidToByteArray(UUID param0) {
        byte[] var0 = new byte[16];
        ByteBuffer.wrap(var0).order(ByteOrder.BIG_ENDIAN).putLong(param0.getMostSignificantBits()).putLong(param0.getLeastSignificantBits());
        return var0;
    }

    public static UUID readUUID(Dynamic<?> param0) {
        int[] var0 = param0.asIntStream().toArray();
        if (var0.length != 4) {
            throw new IllegalArgumentException("Could not read UUID. Expected int-array of length 4, got " + var0.length + ".");
        } else {
            return uuidFromIntArray(var0);
        }
    }

    public static UUID createOfflinePlayerUUID(String param0) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + param0).getBytes(StandardCharsets.UTF_8));
    }

    public static GameProfile createOfflineProfile(String param0) {
        UUID var0 = createOfflinePlayerUUID(param0);
        return new GameProfile(var0, param0);
    }
}
