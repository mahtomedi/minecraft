package com.mojang.realmsclient.exception;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RealmsHttpException extends RuntimeException {
    public RealmsHttpException(String param0, Exception param1) {
        super(param0, param1);
    }
}
