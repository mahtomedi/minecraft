package com.mojang.realmsclient.gui;

import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ErrorCallback {
    void error(Component var1);

    default void error(String param0) {
        this.error(Component.literal(param0));
    }
}
