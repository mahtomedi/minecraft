package com.mojang.realmsclient.util;

import com.mojang.authlib.GameProfile;
import java.util.Date;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsUtil {
    private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static Component convertToAgePresentation(long param0) {
        if (param0 < 0L) {
            return RIGHT_NOW;
        } else {
            long var0 = param0 / 1000L;
            if (var0 < 60L) {
                return Component.translatable("mco.time.secondsAgo", var0);
            } else if (var0 < 3600L) {
                long var1 = var0 / 60L;
                return Component.translatable("mco.time.minutesAgo", var1);
            } else if (var0 < 86400L) {
                long var2 = var0 / 3600L;
                return Component.translatable("mco.time.hoursAgo", var2);
            } else {
                long var3 = var0 / 86400L;
                return Component.translatable("mco.time.daysAgo", var3);
            }
        }
    }

    public static Component convertToAgePresentationFromInstant(Date param0) {
        return convertToAgePresentation(System.currentTimeMillis() - param0.getTime());
    }

    public static void renderPlayerFace(GuiGraphics param0, int param1, int param2, int param3, UUID param4) {
        Minecraft var0 = Minecraft.getInstance();
        GameProfile var1 = var0.getMinecraftSessionService().fetchProfile(param4, false);
        PlayerSkin var2 = var1 != null ? var0.getSkinManager().getInsecureSkin(var1) : DefaultPlayerSkin.get(param4);
        PlayerFaceRenderer.draw(param0, var2.texture(), param1, param2, param3);
    }
}
