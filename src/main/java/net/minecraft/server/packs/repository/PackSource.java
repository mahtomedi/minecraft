package net.minecraft.server.packs.repository;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

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
        Component var0 = new TranslatableComponent(param0);
        return param1 -> new TranslatableComponent("pack.nameAndSource", param1, var0);
    }
}
