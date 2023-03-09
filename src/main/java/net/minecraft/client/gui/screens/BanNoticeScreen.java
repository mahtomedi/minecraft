package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.chat.report.BanReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

@OnlyIn(Dist.CLIENT)
public class BanNoticeScreen {
    private static final Component TEMPORARY_BAN_TITLE = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
    private static final Component PERMANENT_BAN_TITLE = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);

    public static ConfirmLinkScreen create(BooleanConsumer param0, BanDetails param1) {
        return new ConfirmLinkScreen(
            param0, getBannedTitle(param1), getBannedScreenText(param1), "https://aka.ms/mcjavamoderation", CommonComponents.GUI_ACKNOWLEDGE, true
        );
    }

    private static Component getBannedTitle(BanDetails param0) {
        return isTemporaryBan(param0) ? TEMPORARY_BAN_TITLE : PERMANENT_BAN_TITLE;
    }

    private static Component getBannedScreenText(BanDetails param0) {
        return Component.translatable(
            "gui.banned.description", getBanReasonText(param0), getBanStatusText(param0), Component.literal("https://aka.ms/mcjavamoderation")
        );
    }

    private static Component getBanReasonText(BanDetails param0) {
        String var0 = param0.reason();
        String var1 = param0.reasonMessage();
        if (StringUtils.isNumeric(var0)) {
            int var2 = Integer.parseInt(var0);
            BanReason var3 = BanReason.byId(var2);
            Component var4;
            if (var3 != null) {
                var4 = ComponentUtils.mergeStyles(var3.title().copy(), Style.EMPTY.withBold(true));
            } else if (var1 != null) {
                var4 = Component.translatable("gui.banned.description.reason_id_message", var2, var1).withStyle(ChatFormatting.BOLD);
            } else {
                var4 = Component.translatable("gui.banned.description.reason_id", var2).withStyle(ChatFormatting.BOLD);
            }

            return Component.translatable("gui.banned.description.reason", var4);
        } else {
            return Component.translatable("gui.banned.description.unknownreason");
        }
    }

    private static Component getBanStatusText(BanDetails param0) {
        if (isTemporaryBan(param0)) {
            Component var0 = getBanDurationText(param0);
            return Component.translatable(
                "gui.banned.description.temporary", Component.translatable("gui.banned.description.temporary.duration", var0).withStyle(ChatFormatting.BOLD)
            );
        } else {
            return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
        }
    }

    private static Component getBanDurationText(BanDetails param0) {
        Duration var0 = Duration.between(Instant.now(), param0.expires());
        long var1 = var0.toHours();
        if (var1 > 72L) {
            return CommonComponents.days(var0.toDays());
        } else {
            return var1 < 1L ? CommonComponents.minutes(var0.toMinutes()) : CommonComponents.hours(var0.toHours());
        }
    }

    private static boolean isTemporaryBan(BanDetails param0) {
        return param0.expires() != null;
    }
}
