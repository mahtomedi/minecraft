package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsUtil {
    private static final YggdrasilAuthenticationService AUTHENTICATION_SERVICE = new YggdrasilAuthenticationService(Minecraft.getInstance().getProxy());
    static final MinecraftSessionService SESSION_SERVICE = AUTHENTICATION_SERVICE.createMinecraftSessionService();
    public static LoadingCache<String, GameProfile> gameProfileCache = CacheBuilder.newBuilder()
        .expireAfterWrite(60L, TimeUnit.MINUTES)
        .build(new CacheLoader<String, GameProfile>() {
            public GameProfile load(String param0) throws Exception {
                GameProfile var0 = RealmsUtil.SESSION_SERVICE.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(param0), null), false);
                if (var0 == null) {
                    throw new Exception("Couldn't get profile");
                } else {
                    return var0;
                }
            }
        });
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static String uuidToName(String param0) throws Exception {
        GameProfile var0 = gameProfileCache.get(param0);
        return var0.getName();
    }

    public static Map<Type, MinecraftProfileTexture> getTextures(String param0) {
        try {
            GameProfile var0 = gameProfileCache.get(param0);
            return SESSION_SERVICE.getTextures(var0, false);
        } catch (Exception var2) {
            return Maps.newHashMap();
        }
    }

    public static String convertToAgePresentation(long param0) {
        if (param0 < 0L) {
            return "right now";
        } else {
            long var0 = param0 / 1000L;
            if (var0 < 60L) {
                return (var0 == 1L ? "1 second" : var0 + " seconds") + " ago";
            } else if (var0 < 3600L) {
                long var1 = var0 / 60L;
                return (var1 == 1L ? "1 minute" : var1 + " minutes") + " ago";
            } else if (var0 < 86400L) {
                long var2 = var0 / 3600L;
                return (var2 == 1L ? "1 hour" : var2 + " hours") + " ago";
            } else {
                long var3 = var0 / 86400L;
                return (var3 == 1L ? "1 day" : var3 + " days") + " ago";
            }
        }
    }

    public static String convertToAgePresentationFromInstant(Date param0) {
        return convertToAgePresentation(System.currentTimeMillis() - param0.getTime());
    }
}
