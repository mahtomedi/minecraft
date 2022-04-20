package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource {
    PackSource DEFAULT = passThrough();
    PackSource BUILT_IN = decorating("pack.source.builtin");
    PackSource WORLD = decorating("pack.source.world");
    PackSource SERVER = decorating("pack.source.server");

    Component decorate(Component var1);

    static PackSource passThrough() {
        return param0 -> param0;
    }

    static PackSource decorating(String param0) {
        Component var0 = Component.translatable(param0);
        return param1 -> Component.translatable("pack.nameAndSource", param1, var0).withStyle(ChatFormatting.GRAY);
    }
}
