package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ComponentRenderUtils {
    public static String stripColor(String param0, boolean param1) {
        return !param1 && !Minecraft.getInstance().options.chatColors ? ChatFormatting.stripFormatting(param0) : param0;
    }

    public static List<Component> wrapComponents(Component param0, int param1, Font param2, boolean param3, boolean param4) {
        int var0 = 0;
        Component var1 = new TextComponent("");
        List<Component> var2 = Lists.newArrayList();
        List<Component> var3 = Lists.newArrayList(param0);

        for(int var4 = 0; var4 < var3.size(); ++var4) {
            Component var5 = var3.get(var4);
            String var6 = var5.getContents();
            boolean var7 = false;
            if (var6.contains("\n")) {
                int var8 = var6.indexOf(10);
                String var9 = var6.substring(var8 + 1);
                var6 = var6.substring(0, var8 + 1);
                Component var10 = new TextComponent(var9).setStyle(var5.getStyle().copy());
                var3.add(var4 + 1, var10);
                var7 = true;
            }

            String var11 = stripColor(var5.getStyle().getLegacyFormatCodes() + var6, param4);
            String var12 = var11.endsWith("\n") ? var11.substring(0, var11.length() - 1) : var11;
            int var13 = param2.width(var12);
            Component var14 = new TextComponent(var12).setStyle(var5.getStyle().copy());
            if (var0 + var13 > param1) {
                String var15 = param2.substrByWidth(var11, param1 - var0, false);
                String var16 = var15.length() < var11.length() ? var11.substring(var15.length()) : null;
                if (var16 != null && !var16.isEmpty()) {
                    int var17 = var16.charAt(0) != ' ' ? var15.lastIndexOf(32) : var15.length();
                    if (var17 >= 0 && param2.width(var11.substring(0, var17)) > 0) {
                        var15 = var11.substring(0, var17);
                        if (param3) {
                            ++var17;
                        }

                        var16 = var11.substring(var17);
                    } else if (var0 > 0 && !var11.contains(" ")) {
                        var15 = "";
                        var16 = var11;
                    }

                    Component var18 = new TextComponent(var16).setStyle(var5.getStyle().copy());
                    var3.add(var4 + 1, var18);
                }

                var13 = param2.width(var15);
                var14 = new TextComponent(var15);
                var14.setStyle(var5.getStyle().copy());
                var7 = true;
            }

            if (var0 + var13 <= param1) {
                var0 += var13;
                var1.append(var14);
            } else {
                var7 = true;
            }

            if (var7) {
                var2.add(var1);
                var0 = 0;
                var1 = new TextComponent("");
            }
        }

        var2.add(var1);
        return var2;
    }
}
