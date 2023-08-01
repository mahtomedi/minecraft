package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.exception.RealmsServiceException;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface ErrorCallback {
    void error(Component var1);

    default void error(Exception param0) {
        if (param0 instanceof RealmsServiceException var0) {
            this.error(var0.realmsError.errorMessage());
        } else {
            this.error(Component.literal(param0.getMessage()));
        }

    }

    default void error(RealmsServiceException param0) {
        this.error(param0.realmsError.errorMessage());
    }
}
