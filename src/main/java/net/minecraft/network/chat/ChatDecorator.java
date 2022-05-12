package net.minecraft.network.chat;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;

@FunctionalInterface
public interface ChatDecorator {
    ChatDecorator PLAIN = (param0, param1) -> param1;

    @Deprecated
    static ChatDecorator testRainbowChat() {
        return (param0, param1) -> {
            String var0 = param1.getString().trim();
            int var1 = var0.length();
            float var2 = Math.nextDown(1.0F) * (float)var1;
            MutableComponent var3 = Component.literal(String.valueOf(var0.charAt(0)))
                .withStyle(Style.EMPTY.withColor(Mth.hsvToRgb(Math.nextDown(1.0F), 1.0F, 1.0F)));

            for(int var4 = 1; var4 < var1; ++var4) {
                var3.append(Component.literal(String.valueOf(var0.charAt(var4))).withStyle(Style.EMPTY.withColor(Mth.hsvToRgb((float)var4 / var2, 1.0F, 1.0F))));
            }

            return var3;
        };
    }

    Component decorate(@Nullable ServerPlayer var1, Component var2);

    default PlayerChatMessage decorate(@Nullable ServerPlayer param0, Component param1, MessageSignature param2, boolean param3) {
        Component var0 = this.decorate(param0, param1);
        if (param1.equals(var0)) {
            return PlayerChatMessage.signed(param1, param2);
        } else {
            return !param3 ? PlayerChatMessage.signed(param1, param2).withUnsignedContent(var0) : PlayerChatMessage.signed(var0, param2);
        }
    }

    default PlayerChatMessage decorate(@Nullable ServerPlayer param0, PlayerChatMessage param1) {
        return this.decorate(param0, param1.signedContent(), param1.signature(), false);
    }
}
