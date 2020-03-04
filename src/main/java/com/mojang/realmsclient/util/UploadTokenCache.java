package com.mojang.realmsclient.util;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UploadTokenCache {
    private static final Long2ObjectMap<String> TOKEN_CACHE = new Long2ObjectOpenHashMap<>();

    public static String get(long param0) {
        return TOKEN_CACHE.get(param0);
    }

    public static void invalidate(long param0) {
        TOKEN_CACHE.remove(param0);
    }

    public static void put(long param0, String param1) {
        TOKEN_CACHE.put(param0, param1);
    }
}
