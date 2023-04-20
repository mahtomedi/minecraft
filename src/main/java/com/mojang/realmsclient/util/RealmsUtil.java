package com.mojang.realmsclient.util;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsUtil {
    static final MinecraftSessionService SESSION_SERVICE = Minecraft.getInstance().getMinecraftSessionService();
    private static final LoadingCache<String, GameProfile> GAME_PROFILE_CACHE = CacheBuilder.newBuilder()
        .expireAfterWrite(60L, TimeUnit.MINUTES)
        .build(new CacheLoader<String, GameProfile>() {
            public GameProfile load(String param0) {
                return RealmsUtil.SESSION_SERVICE.fillProfileProperties(new GameProfile(UUIDTypeAdapter.fromString(param0), null), false);
            }
        });
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static String uuidToName(String param0) {
        return GAME_PROFILE_CACHE.getUnchecked(param0).getName();
    }

    public static GameProfile getGameProfile(String param0) {
        return GAME_PROFILE_CACHE.getUnchecked(param0);
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

    public static void renderPlayerFace(GuiGraphics param0, int param1, int param2, int param3, String param4) {
        GameProfile var0 = getGameProfile(param4);
        ResourceLocation var1 = Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(var0);
        PlayerFaceRenderer.draw(param0, var1, param1, param2, param3);
    }
}
