package com.mojang.realmsclient.util;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UploadTokenCache {
    private static final Map<Long, String> tokenCache = Maps.newHashMap();

    public static String get(long param0) {
        return tokenCache.get(param0);
    }

    public static void invalidate(long param0) {
        tokenCache.remove(param0);
    }

    public static void put(long param0, String param1) {
        tokenCache.put(param0, param1);
    }
}
